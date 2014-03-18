package ua.ardas.jmeter.websocket;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;

public class WebSocketConnectionControllerGui extends AbstractControllerGui {
	private static final Logger LOG = LoggingManager.getLoggerForClass();

	private boolean displayName = true;
	private JTextField domain;
	private JTextField port;
	private JTextField protocol;
	private JTextField contentEncoding;
	private JTextField path;
	private HTTPArgumentsPanel argsPanel;


	public WebSocketConnectionControllerGui() {
		this(true);
	}

	public WebSocketConnectionControllerGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public String getStaticLabel() {
		return ResourcesHelper.getResString("websocket_connection_controller_title");
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		domain.setText(element.getPropertyAsString(WebSocketConnectionController.DOMAIN));
		port.setText(element.getPropertyAsString(WebSocketConnectionController.PORT));
		protocol.setText(element.getPropertyAsString(WebSocketConnectionController.PROTOCOL));
		path.setText(element.getPropertyAsString(WebSocketConnectionController.PATH));
		contentEncoding.setText(element.getPropertyAsString(WebSocketConnectionController.CONTENT_ENCODING));

		Arguments arguments = (Arguments) element.getProperty(WebSocketConnectionController.ARGUMENTS).getObjectValue();
		argsPanel.configure(arguments);
	}

	@Override
	public TestElement createTestElement() {
		try {
			WebSocketConnectionController element = new WebSocketConnectionController();
			modifyTestElement(element);
			return element;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(WebSocketConnectionController.DOMAIN, domain.getText());
		element.setProperty(WebSocketConnectionController.PATH, path.getText());
		element.setProperty(WebSocketConnectionController.PORT, port.getText());
		element.setProperty(WebSocketConnectionController.PROTOCOL, protocol.getText());
		element.setProperty(WebSocketConnectionController.CONTENT_ENCODING, contentEncoding.getText());

		Arguments args = (Arguments) argsPanel.createTestElement();
		HTTPArgument.convertArgumentsToHTTP(args);
		element.setProperty(new TestElementProperty(WebSocketConnectionController.ARGUMENTS, args));
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));

		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}

		// MAIN PANEL
		VerticalPanel mainPanel = new VerticalPanel();
		JPanel webRequestPanel = new HorizontalPanel();
		JPanel serverPanel = new JPanel();
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.X_AXIS));
		serverPanel.add(getDomainPanel());
		serverPanel.add(getPortPanel());

		webRequestPanel.add(serverPanel, BorderLayout.NORTH);
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.add(getProtocolAndPathPanel());

		webRequestPanel.add(northPanel, BorderLayout.CENTER);
		argsPanel = new HTTPArgumentsPanel();
		webRequestPanel.add(argsPanel, BorderLayout.SOUTH);

		mainPanel.add(webRequestPanel);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel getDomainPanel() {
		domain = new JTextField(20);

		JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain"));
		label.setLabelFor(domain);

		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(label, BorderLayout.WEST);
		panel.add(domain, BorderLayout.CENTER);
		return panel;
	}

	private JPanel getPortPanel() {
		port = new JTextField(4);

		JLabel label = new JLabel(JMeterUtils.getResString("web_server_port"));
		label.setLabelFor(port);

		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(label, BorderLayout.WEST);
		panel.add(port, BorderLayout.CENTER);

		return panel;
	}

	protected Component getProtocolAndPathPanel() {
		// PATH
		path = new JTextField(15);
		JLabel pathLabel = new JLabel(JMeterUtils.getResString("path"));
		pathLabel.setLabelFor(path);

		// PROTOCOL
		protocol = new JTextField(4);
		JLabel protocolLabel = new JLabel(JMeterUtils.getResString("protocol"));
		protocolLabel.setLabelFor(protocol);

		// CONTENT_ENCODING
		contentEncoding = new JTextField(10);
		JLabel contentEncodingLabel = new JLabel(JMeterUtils.getResString("content_encoding"));
		contentEncodingLabel.setLabelFor(contentEncoding);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(pathLabel);
		panel.add(path);
		panel.add(Box.createHorizontalStrut(5));

		panel.add(protocolLabel);
		panel.add(protocol);
		panel.add(Box.createHorizontalStrut(5));

		panel.add(contentEncodingLabel);
		panel.add(contentEncoding);
		panel.setMinimumSize(panel.getPreferredSize());

		return panel;
	}

}
