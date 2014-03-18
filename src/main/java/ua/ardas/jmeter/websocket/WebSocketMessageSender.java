package ua.ardas.jmeter.websocket;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;

public class WebSocketMessageSender extends AbstractSampler implements WebSocketConnectionHandler {
	private static final Logger LOG = LoggingManager.getLoggerForClass();
	public static final String SEND_MESSAGE = ResourcesHelper.getPropertyFullKey("sendMessage");

	private WebSocketConnectionProvider provider;

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult res = new SampleResult();
		res.setSampleLabel(getName());

		if (null == provider) {
			res.setResponseMessage(WebSocketMessageSender.class.getName() + " should be a child of " + WebSocketConnectionController.class.getName());
			res.setSuccessful(false);
			return res;
		}

		String message = getPropertyAsString(SEND_MESSAGE, "default message");
		res.setSamplerData(message);
		res.sampleStart();
		res.setDataEncoding(provider.getContentEncoding());
		boolean isOk = sendMessage(message);
		if (isOk) {
			res.setResponseCodeOK();
		}
		res.sampleEnd();
		res.setSuccessful(isOk);
		return res;
	}

	private boolean sendMessage(String message) {
		try {
			provider.getConnection().sendMessage(message);
			return true;
		} catch (IOException e) {
			LOG.error("Error: ", e);
			return false;
		}
	}

	@Override
	public void setWebSocketConnectionProvider(WebSocketConnectionProvider provider) {
		this.provider = provider;
	}
}
