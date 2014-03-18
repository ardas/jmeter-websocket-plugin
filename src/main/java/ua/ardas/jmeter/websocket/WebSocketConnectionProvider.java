package ua.ardas.jmeter.websocket;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import java.util.List;

public interface WebSocketConnectionProvider {
	Connection getConnection();
	List<String> getAndClearReceivedMessagesCache();
	String getContentEncoding();
}
