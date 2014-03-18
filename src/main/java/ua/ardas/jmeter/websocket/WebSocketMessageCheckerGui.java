package ua.ardas.jmeter.websocket;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WebSocketMessageCheckerGui extends AbstractSamplerGui {
	private static final Logger LOG = LoggingManager.getLoggerForClass();
	public static final MessageCheckType DEFAULT_MESSAGE_CHECK_TYPE = MessageCheckType.ALL;

	private boolean displayName = true;
	private ButtonGroup messageCheckTypeGroup;
	private JRadioButton messageCheckTypeAllButton;
	private JRadioButton messageCheckTypeEachButton;

	private JTextArea  recvMessage;
	private JTextField  recvMessageCount;
	private JPanel recvMessagesCountPanel;
	private JTextField  timeout;
	private MessageCheckType messageCheckType = DEFAULT_MESSAGE_CHECK_TYPE;


	public WebSocketMessageCheckerGui() {
		this(true);
	}

	public WebSocketMessageCheckerGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public String getStaticLabel() {
		return ResourcesHelper.getResString("websocket_message_checker_title");
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		recvMessage.setText(element.getPropertyAsString(WebSocketMessageChecker.RECV_MESSAGE));
		setMessageCheckTypeSelection(element);
		recvMessageCount.setText(element.getPropertyAsString(WebSocketMessageChecker.RECV_MESSAGE_COUNT));
		timeout.setText(element.getPropertyAsString(WebSocketMessageChecker.RECV_TIMEOUT, "20000"));
	}

	private void setMessageCheckTypeSelection(TestElement element) {
		configureMessageCheckType(element);
		ButtonModel buttonModel = null;
		switch (messageCheckType) {
		case ALL:
			buttonModel = messageCheckTypeAllButton.getModel();
			recvMessagesCountPanel.setVisible(false);
			break;
		case EACH:
			buttonModel = messageCheckTypeEachButton.getModel();
			recvMessagesCountPanel.setVisible(true);
			break;
		}
		messageCheckTypeGroup.setSelected(buttonModel, true);
	}

	private void configureMessageCheckType(TestElement element) {
		String messageCheckTypeStr = element.getPropertyAsString(WebSocketMessageChecker.RECV_MESSAGE_CHECK_TYPE);
		try {
			messageCheckType = MessageCheckType.valueOf(messageCheckTypeStr);
		} catch (Exception e) {
			messageCheckType = DEFAULT_MESSAGE_CHECK_TYPE;
			LOG.error(String.format("Can't parse MessageCheckType: %s", messageCheckTypeStr));
		}
	}

	@Override
	public TestElement createTestElement() {
		WebSocketMessageChecker element = new WebSocketMessageChecker();
		modifyTestElement(element);
		return element;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(WebSocketMessageChecker.RECV_MESSAGE, recvMessage.getText());
		element.setProperty(WebSocketMessageChecker.RECV_MESSAGE_CHECK_TYPE, messageCheckType.toString());
		element.setProperty(WebSocketMessageChecker.RECV_MESSAGE_COUNT, recvMessageCount.getText());
		element.setProperty(WebSocketMessageChecker.RECV_TIMEOUT, timeout.getText());
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}
		createMessagesCountPanel();
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(getRadioPanel());
		mainPanel.add(getRecvMessagePanel());
		mainPanel.add(recvMessagesCountPanel);
		mainPanel.add(getTimeoutPanel());
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel getRadioPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		messageCheckTypeGroup = new ButtonGroup();

		messageCheckTypeAllButton = createMessageCheckRadioButton("websocket_recv_message_search_all", true, MessageCheckType.ALL);
		messageCheckTypeEachButton = createMessageCheckRadioButton("websocket_recv_message_search_each", false, MessageCheckType.EACH);
		messageCheckTypeGroup.add(messageCheckTypeAllButton);
		messageCheckTypeGroup.add(messageCheckTypeEachButton);
		panel.add(messageCheckTypeAllButton);
		panel.add(messageCheckTypeEachButton);
		return panel;
	}

	private JRadioButton createMessageCheckRadioButton(String key, boolean isSelected, final MessageCheckType checkType) {
		JRadioButton radioButton = new JRadioButton(ResourcesHelper.getResString(key));
		radioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				messageCheckType = checkType;
				recvMessagesCountPanel.setVisible(MessageCheckType.EACH == checkType);
			}
		});
		radioButton.setSelected(isSelected);
		return radioButton;
	}

	private JPanel getRecvMessagePanel() {
		JLabel recvMessageLabel = new JLabel(ResourcesHelper.getResString("websocket_recv_message"));
		recvMessage = new JTextArea(3, 0);
		recvMessage.setLineWrap(true);
		recvMessageLabel.setLabelFor(recvMessage);

		JPanel recvMessagePanel = new JPanel(new BorderLayout(5, 0));
		recvMessagePanel.add(recvMessageLabel, BorderLayout.WEST);
		recvMessagePanel.add(recvMessage, BorderLayout.CENTER);
		return recvMessagePanel;
	}

	private void createMessagesCountPanel() {
		recvMessageCount = new JTextField(15);
		JLabel recvMessagesCountLabel = new JLabel(ResourcesHelper.getResString("websocket_recv_message_count"));
		recvMessagesCountLabel.setLabelFor(recvMessageCount);

		recvMessagesCountPanel = new JPanel(new BorderLayout(5, 0));
		recvMessagesCountPanel.add(recvMessagesCountLabel, BorderLayout.WEST);
		recvMessagesCountPanel.add(recvMessageCount, BorderLayout.CENTER);
		recvMessagesCountPanel.setVisible(false);
	}

	private JPanel getTimeoutPanel() {
		timeout = new JTextField(15);
		JLabel timeoutLabel = new JLabel(ResourcesHelper.getResString("websocket_recv_message_timeout"));
		timeoutLabel.setLabelFor(timeout);

		JPanel timeoutPanel = new JPanel(new BorderLayout(5, 0));
		timeoutPanel.add(timeoutLabel, BorderLayout.WEST);
		timeoutPanel.add(timeout, BorderLayout.CENTER);
		return timeoutPanel;
	}
}
