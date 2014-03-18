package ua.ardas.jmeter.websocket;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class WebSocketWaiterGui extends AbstractSamplerGui {

	private boolean displayName = true;
	private JTextField  threads;
	private JTextField  key;
	private JTextField  timeout;

	public WebSocketWaiterGui() {
		this(true);
	}

	public WebSocketWaiterGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public String getStaticLabel() {
		return ResourcesHelper.getResString("threads_waiter_title");
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		threads.setText(element.getPropertyAsString(WebSocketWaiter.THREADS));
		key.setText(element.getPropertyAsString(WebSocketWaiter.KEY));
		timeout.setText(element.getPropertyAsString(WebSocketWaiter.TIMEOUT));
	}

	@Override
	public TestElement createTestElement() {
		WebSocketWaiter element = new WebSocketWaiter();

		element.setName(getName());
		element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());

		modifyTestElement(element);
		return element;
	}

	@Override
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(WebSocketWaiter.THREADS, threads.getText());
		element.setProperty(WebSocketWaiter.KEY, key.getText());
		element.setProperty(WebSocketWaiter.TIMEOUT, timeout.getText());
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}
		VerticalPanel mainPanel = new VerticalPanel();

		threads = addToContainerAndGetElement(mainPanel, ResourcesHelper.getResString("threads_waiter_threads"));
		key = addToContainerAndGetElement(mainPanel, ResourcesHelper.getResString("threads_waiter_key"));
		timeout = addToContainerAndGetElement(mainPanel, ResourcesHelper.getResString("threads_waiter_timeout"));

		add(mainPanel, BorderLayout.CENTER);
	}

	private JTextField addToContainerAndGetElement(VerticalPanel container, String label) {
		HorizontalPanel panel = new HorizontalPanel();
		JLabel jLabel = new JLabel(label);
		JTextField element = new JTextField();
		jLabel.setLabelFor(element);
		panel.add(jLabel);
		panel.add(element);
		container.add(panel);
		return element;
	}
}
