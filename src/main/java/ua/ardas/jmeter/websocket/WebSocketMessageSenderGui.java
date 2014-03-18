package ua.ardas.jmeter.websocket;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class WebSocketMessageSenderGui extends AbstractSamplerGui {

	private boolean displayName = true;
	private JTextArea  sendMessage;

	public WebSocketMessageSenderGui() {
		this(true);
	}

	public WebSocketMessageSenderGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public String getStaticLabel() {
		return ResourcesHelper.getResString("websocket_message_sender_title");
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		sendMessage.setText(element.getPropertyAsString(WebSocketMessageSender.SEND_MESSAGE));
	}

	@Override
	public TestElement createTestElement() {
		WebSocketMessageSender element = new WebSocketMessageSender();
		modifyTestElement(element);
		return element;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(WebSocketMessageSender.SEND_MESSAGE, sendMessage.getText());
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}
		VerticalPanel mainPanel = new VerticalPanel();

		mainPanel.add(getSendMessagePanel());
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel getSendMessagePanel() {
		JLabel sendMessageLabel = new JLabel(ResourcesHelper.getResString("websocket_send_message")); // $NON-NLS-1$
		sendMessage = new JTextArea(3, 0);
		sendMessage.setLineWrap(true);
		sendMessageLabel.setLabelFor(sendMessage);

		JPanel sendMessagePanel = new JPanel(new BorderLayout(5, 0));
		sendMessagePanel.add(sendMessageLabel, BorderLayout.WEST);
		sendMessagePanel.add(sendMessage, BorderLayout.CENTER);
		return sendMessagePanel;
	}
}
