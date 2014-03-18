package ua.ardas.jmeter.websocket;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketWaiter extends AbstractSampler {
	private static final Logger LOG = LoggingManager.getLoggerForClass();

	public static final String THREADS =ResourcesHelper.getPropertyFullKey("threads");
	public static final String KEY =ResourcesHelper.getPropertyFullKey("key");
	public static final String TIMEOUT =ResourcesHelper.getPropertyFullKey("timeout");

	public volatile static Map<String, AtomicInteger> threadGroupKeys = new ConcurrentHashMap<String, AtomicInteger>();

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult res = new SampleResult();
		res.setSampleLabel(getName());
		waitForThreads(res);
		return res;
	}

	private void waitForThreads(SampleResult res) {
		int threads = getThreads();
		if (0 >= threads) {
			return;
		}

		AtomicInteger counter = threadGroupKeys.get(getKey());
		if (null == counter) {
			synchronized (threadGroupKeys.getClass().getName()) {
				counter = threadGroupKeys.get(getKey());
				if (null == counter) {
					counter = new AtomicInteger(0);
					threadGroupKeys.put(getKey(), counter);
				}
			}
		}

		int curVal = counter.incrementAndGet();
		try {
			long timeout = System.currentTimeMillis() + getTimeout();
			while (curVal != threads && timeout > System.currentTimeMillis()) {
				Thread.sleep(500);
				curVal = counter.get();
			}
		} catch (Exception e) {
			LOG.error("Error", e);
		}

		res.setSuccessful(curVal == threads);
		threadGroupKeys.remove(getKey());
	}

	public int getThreads() {
		return Integer.valueOf(getPropertyAsString(THREADS, "-1"));
	}

	public String getKey() {
		return getPropertyAsString(KEY);
	}

	public Long getTimeout() {
		return Long.valueOf(getPropertyAsString(TIMEOUT, "20000"));
	}
}
