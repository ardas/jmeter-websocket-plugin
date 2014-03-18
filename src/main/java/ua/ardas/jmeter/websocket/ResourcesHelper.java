package ua.ardas.jmeter.websocket;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourcesHelper {
	private static final Logger LOG = LoggingManager.getLoggerForClass();
	private static final ResourceBundle resources;

	static {
		Locale loc = JMeterUtils.getLocale();
		resources = ResourceBundle.getBundle(WebSocketConnectionController.class.getName() + "Resources", loc);
		LOG.info("Resource " + WebSocketConnectionController.class.getName() + " is loaded for locale " + loc);
	}


	public static String getResString(String key) {
		return getResStringDefault(key, JMeterUtils.RES_KEY_PFX + key + "]");
	}

	public static String getPropertyFullKey(String key) {
		return String.format("%s.%s", WebSocketConnectionController.class.getSimpleName(), key);
	}

	private static String getResStringDefault(String key, String defaultValue) {
		if (key == null) {
			return null;
		}
		key = key.replace(' ', '_');
		key = key.toLowerCase(java.util.Locale.ENGLISH);
		String resString = null;
		try {
			resString = resources.getString(key);
		} catch (MissingResourceException mre) {
			LOG.warn("ERROR! Resource string not found: [" + key + "]", mre);
			resString = defaultValue;
		}
		return resString;
	}

}
