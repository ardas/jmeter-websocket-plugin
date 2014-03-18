package ua.ardas.jmeter.websocket;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WebSocketMessageChecker extends AbstractSampler implements WebSocketConnectionHandler {
	private static final Logger LOG = LoggingManager.getLoggerForClass();
	public static final String RECV_MESSAGE = ResourcesHelper.getPropertyFullKey("recvMessage");
	public static final String RECV_MESSAGE_CHECK_TYPE = ResourcesHelper.getPropertyFullKey("recvMessageCheckType");
	public static final String RECV_MESSAGE_COUNT = ResourcesHelper.getPropertyFullKey("recvMessageCount");
	public static final String RECV_TIMEOUT = ResourcesHelper.getPropertyFullKey("recvTimeout");

	private WebSocketConnectionProvider provider;
	private List<String> messagesCache = new ArrayList<String>();

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult res = new SampleResult();
		res.setSampleLabel(getName());

		if (null == provider) {
			res.setResponseMessage(WebSocketMessageChecker.class.getName() + " should be a child of " + WebSocketConnectionController.class.getName());
			res.setSuccessful(false);
			return res;
		}

		res.sampleStart();
		boolean isMessageReceived = checkMessages();
		res.setResponseData(StringUtils.join(messagesCache), provider.getContentEncoding());
		res.setSuccessful(isMessageReceived);
		res.sampleEnd();
		return res;
	}

	private boolean checkMessages() {
		String recvMessageRegExp = getRecvMessage();
		if (StringUtils.isEmpty(recvMessageRegExp)) {
			return true;
		}

		final Pattern regex = Pattern.compile(recvMessageRegExp);
		messagesCache.clear();

		boolean isMessageReceived =	isMessageReceived(regex);

		try {
			long timeout = System.currentTimeMillis() + getRecvTimeout();
			while ((!isMessageReceived) && (timeout > System.currentTimeMillis())) {
				Thread.sleep(100);
				isMessageReceived = isMessageReceived(regex);
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}
		return isMessageReceived;
	}

	private boolean isMessageReceived(Pattern regex) {
		messagesCache.addAll(provider.getAndClearReceivedMessagesCache());
		switch (getMessageCheckType()) {
			case ALL:
				return regex.matcher(StringUtils.join(messagesCache)).find();
			case EACH:
				if (null != getMessageCount() && getMessageCount().intValue() != messagesCache.size()) {
					return false;
				}
				for (String message : messagesCache) {
					if (!regex.matcher(message).find()) {
						return false;
					}
				}
				return true;
		}
		return false;
	}

	public String getRecvMessage() {
		return getPropertyAsString(RECV_MESSAGE);
	}

	public long getRecvTimeout() {
		return Long.valueOf(getPropertyAsString(RECV_TIMEOUT));
	}

	public MessageCheckType getMessageCheckType() {
		return MessageCheckType.valueOf(getPropertyAsString(RECV_MESSAGE_CHECK_TYPE));
	}

	public Integer getMessageCount() {
		return Integer.valueOf(getPropertyAsString(RECV_MESSAGE_COUNT));
	}

	@Override
	public void setWebSocketConnectionProvider(WebSocketConnectionProvider provider) {
		this.provider = provider;
	}
}
