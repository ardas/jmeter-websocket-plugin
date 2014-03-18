package ua.ardas.jmeter.websocket;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class WebSocketConnectionController extends GenericController implements OnTextMessage, WebSocketConnectionProvider, TestStateListener {
	private static final Logger LOG = LoggingManager.getLoggerForClass();

	public static final String DOMAIN = ResourcesHelper.getPropertyFullKey("domain");
	public static final String PORT = ResourcesHelper.getPropertyFullKey("port");
	public static final String PATH = ResourcesHelper.getPropertyFullKey("path");
	public static final String PROTOCOL = ResourcesHelper.getPropertyFullKey("protocol");
	public static final String CONTENT_ENCODING = ResourcesHelper.getPropertyFullKey("contentEncoding");
	public static final String ARGUMENTS = "WebSocketConnectionController.arguments";

	private static final int UNSPECIFIED_PORT = 0;
	private static final String UNSPECIFIED_PORT_AS_STRING = "0";
	private static final String WS_PREFIX = "ws://";
	private static final String WSS_PREFIX = "wss://";
	private static final String DEFAULT_PROTOCOL = "ws";
	private static final String QRY_SEP = "&";
	private static final String ARG_VAL_SEP = "=";

	private static WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();

	private Connection connection = null;
	private List<String> receivedMessages = Collections.synchronizedList(new ArrayList<String>());
	private Object messagesLock = new Object();

	@Override
	public Sampler next() {
		if (isFirst()) {
			openConnection();
		}

		Sampler sampler = super.next();
		if (null == sampler && null != connection) {
			closeConnection();
		}

		if (sampler instanceof WebSocketConnectionHandler) {
			((WebSocketConnectionHandler) sampler).setWebSocketConnectionProvider(this);
		}

		return sampler;
	}

	private void openConnection() {
		try {
			WebSocketClient webSocketClient = webSocketClientFactory.newWebSocketClient();
			Future<Connection> futureConnection = webSocketClient.open(getUri(), this);
			connection = futureConnection.get();
		} catch (Exception e) {
			LOG.error("Error: ", e);
			throw new RuntimeException(e);
		}
	}

	private void closeConnection() {
		try {
			connection.close();
		} catch (Exception e) {
			LOG.error("Error: ", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onMessage(String data) {
		synchronized (messagesLock) {
			receivedMessages.add(data);
		}
	}

	@Override
	public void onOpen(Connection connection) {
		LOG.info(String.format("WebSocket connection opened: %s", connection));
	}

	@Override
	public void onClose(int closeCode, String message) {
		LOG.info(String.format("WebSocket connection closed with code: %s and message: %s", closeCode, message));
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public synchronized List<String> getAndClearReceivedMessagesCache() {
		synchronized (messagesLock) {
			List<String> res = new ArrayList<String>(receivedMessages);
			receivedMessages.clear();
			return res;
		}
	}

	@Override
	public String getContentEncoding() {
		return getPropertyAsString(CONTENT_ENCODING);
	}

	private URI getUri() throws URISyntaxException {
		String path = this.getPath();
		if (path.startsWith(WS_PREFIX)
				|| path.startsWith(WSS_PREFIX)){
			return new URI(path);
		}
		String domain = getDomain();
		String protocol = getProtocol();
		if (!path.startsWith("/")){
			path = "/" + path;
		}

		String queryString = getQueryString(getContentEncoding());
		if(isProtocolDefaultPort()) {
			return new URI(protocol, null, domain, -1, path, queryString, null);
		}
		return new URI(protocol, null, domain, getPort(), path, queryString, null);
	}

	private String getPath() {
		String p = getPropertyAsString(PATH);
		return encodeSpaces(p);
	}

	private String encodeSpaces(String path) {
		return JOrphanUtils.replaceAllChars(path, ' ', "%20");
	}

	private String getDomain() {
		return getPropertyAsString(DOMAIN);
	}

	private String getProtocol() {
		String protocol = getPropertyAsString(PROTOCOL);
		if (protocol == null || protocol.length() == 0 ) {
			return DEFAULT_PROTOCOL;
		}
		return protocol;
	}

	private String getQueryString(String contentEncoding) {
		// Check if the sampler has a specified content encoding
		if(JOrphanUtils.isBlank(contentEncoding)) {
			// We use the encoding which should be used according to the HTTP spec, which is UTF-8
			contentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
		}
		StringBuilder buf = new StringBuilder();
		PropertyIterator iter = getArguments().iterator();
		boolean first = true;
		while (iter.hasNext()) {
			HTTPArgument item = null;
			Object objectValue = iter.next().getObjectValue();
			try {
				item = (HTTPArgument) objectValue;
			} catch (ClassCastException e) {
				item = new HTTPArgument((Argument) objectValue);
			}
			final String encodedName = item.getEncodedName();
			if (encodedName.length() == 0) {
				continue; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
			}
			if (!first) {
				buf.append(QRY_SEP);
			} else {
				first = false;
			}
			buf.append(encodedName);
			if (item.getMetaData() == null) {
				buf.append(ARG_VAL_SEP);
			} else {
				buf.append(item.getMetaData());
			}

			// Encode the parameter value in the specified content encoding
			try {
				buf.append(item.getEncodedValue(contentEncoding));
			}
			catch(UnsupportedEncodingException e) {
				LOG.warn("Unable to encode parameter in encoding " + contentEncoding + ", parameter value not included in query string");
			}
		}
		return buf.toString();
	}

	private Arguments getArguments() {
		return (Arguments) getProperty(ARGUMENTS).getObjectValue();
	}

	private int getPort() {
		final int port = getPortIfSpecified();
		if (port == UNSPECIFIED_PORT) {
			String prot = getProtocol();
			if ("wss".equalsIgnoreCase(prot)) {
				return HTTPConstants.DEFAULT_HTTPS_PORT;
			}
			if (!"ws".equalsIgnoreCase(prot)) {
				LOG.warn("Unexpected protocol: "+ prot);
			}
			return HTTPConstants.DEFAULT_HTTP_PORT;
		}
		return port;
	}

	private int getPortIfSpecified() {
		String port_s = getPropertyAsString(PORT, UNSPECIFIED_PORT_AS_STRING);
		try {
			return Integer.parseInt(port_s.trim());
		} catch (NumberFormatException e) {
			return UNSPECIFIED_PORT;
		}
	}

	private boolean isProtocolDefaultPort() {
		final int port = getPortIfSpecified();
		final String protocol = getProtocol();
		return port == UNSPECIFIED_PORT ||
				("ws".equalsIgnoreCase(protocol) && port == HTTPConstants.DEFAULT_HTTP_PORT) ||
				("wss".equalsIgnoreCase(protocol) && port == HTTPConstants.DEFAULT_HTTPS_PORT);
	}

	@Override
	public void testStarted() {
		testStarted("");
	}

	@Override
	public void testStarted(String host) {
		try {
			webSocketClientFactory.start();
		} catch(Exception e) {
			LOG.error("Can't start WebSocketClientFactory", e);
		}
	}

	@Override
	public void testEnded() {
		testEnded("");
	}

	@Override
	public void testEnded(String host) {
		try {
			webSocketClientFactory.stop();
		} catch (Exception e) {
			LOG.error("Can't stop WebSocketClientFactory", e);
		}
	}
}
