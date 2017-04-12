package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Utilities;
import javax.swing.text.View;

import controller.Controller;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L; // just so that eclipse
														// does not complain
	private JPanel contentPane;
	private JTextField textFieldMessage;
	private ArrayList<Message> logMessages;
	private ArrayList<Contact> contacts;

	private JList<Message> listMessages;
	private JList<Contact> listContacts;
	private JButton btnSend;
	private JLabel lblErrorMessage;
	private JLabel lblNamefield;

	private Controller controller;
	private Contact currentSelectedContact = null;

	private NewContactPopup newContactFrame = new NewContactPopup(this);

	public MainWindow(Controller c) {
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage("/home/gereon/git/NetworkSystemsProject/ChatProject/chaticon.png"));
		setResizable(false);
		// settings of the frame
		setTitle("Ad-Hoc Chat " + Controller.getMyIP());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 1200, 526);

		contentPane = new JPanel();

		logMessages = new ArrayList<>();
		contacts = new ArrayList<>();
		/*
		 * contacts.add(new Contact("WrapTest",0,(ArrayList<Message>)
		 * Arrays.asList(new Message[] { new Message(true, "sads" ) })));
		 */
		controller = c;

		// Messages list
		listMessages = new JList<>();
		listMessages.setEnabled(false);

		listMessages.setCellRenderer(new ListCellRenderer<Message>() {

			public int countLines(JTextArea t) {
				int i = (int) (t.getText().length() / t.getPreferredSize().getWidth());
				if (t.getText().length() % t.getPreferredSize().getWidth() != 0) {
					i++;
				}
				return i;
			}

			@Override
			public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
					boolean isSelected, boolean cellHasFocus) {
				// init
				JTextArea t = new JTextArea(10, 10);
				t.setLineWrap(true);

				// formatting
				if (value.isSentBySelf()) {
					t.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					t.setForeground(Color.GRAY);
				} else {
					t.setForeground(Color.BLUE);
				}
				
				// set text
				if (value.equals(null)) {
					t.setText("");
				} else {
					t.setText(value.toString());
				}

				//Dimension d = t.getPreferredSize();
				//t.setPreferredSize(new Dimension((2 * d.width) / 3, d.height));
				t.setRows(countLines(t) + 1);

				// t.setWrapStyleWord(true); // wraps words only instead of
				// characters

				return t;
			}

		});

		// Contact list
		listContacts = new JList<>();
		listContacts.setBorder(new LineBorder(Color.GRAY));
		refreshContactList();
		listContacts.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				if (listContacts.getSelectedIndex() != -1) {
					if (listContacts.getSelectedValue().toString().equals("Log")) {
						lblNamefield.setText("System Log");
						currentSelectedContact = new Contact("System Log", 0, logMessages);
					} else if (listContacts.getSelectedValue().toString().equals("+")) {
						lblNamefield.setText("");
						currentSelectedContact = null;
						newContactFrame.setVisible(true);
					} else {
						lblNamefield.setText(listContacts.getSelectedValue().getName());
						currentSelectedContact = listContacts.getSelectedValue();
					}
				}
			}

		});

		// Send button
		btnSend = new JButton("Send");
		getRootPane().setDefaultButton(btnSend);
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String input = textFieldMessage.getText();
				if (!input.equals("") && currentSelectedContact != null && currentSelectedContact.getDevice() != 0) {
					controller.sendMessage(currentSelectedContact.getDevice(), input);
					currentSelectedContact.addMessage(true, input);
					textFieldMessage.setText("");
				}
			}

		});

		// text field for the message
		textFieldMessage = new JTextField();
		textFieldMessage.setColumns(10);

		// Label for bottom line error message
		lblErrorMessage = new JLabel("Connection established.");

		JScrollPane jsp = new JScrollPane(listMessages);
		jsp.setAutoscrolls(true);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		lblNamefield = new JLabel("");

		JLabel lblContacts = new JLabel("Contacts:");

		// Layout
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING).addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 237,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(lblContacts))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_contentPane.createSequentialGroup()
														.addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 845,
																Short.MAX_VALUE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(btnSend))
												.addComponent(jsp, GroupLayout.DEFAULT_SIZE, 921, Short.MAX_VALUE)
												.addComponent(lblNamefield, Alignment.LEADING)))
						.addComponent(lblErrorMessage)).addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblNamefield)
						.addComponent(lblContacts))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(jsp, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE)
						.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false).addComponent(textFieldMessage)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED, 50, Short.MAX_VALUE).addComponent(lblErrorMessage)
				.addContainerGap()));
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);

		// start thread that refreshes the messages
		new Thread(new RefreshMessageRunnable()).start();
	}

	public void refreshContactList() {
		Contact[] c = new Contact[contacts.size() + 2];
		System.arraycopy(contacts.toArray(new Contact[0]), 0, c, 0, contacts.size());
		c[c.length - 2] = new Contact("Log", 0, new ArrayList<Message>());
		c[c.length - 1] = new Contact("+", 0, new ArrayList<Message>());
		listContacts.setListData(c);
	}

	public void refreshMessages() {
		if (currentSelectedContact == null) {
			listMessages.setListData(new Message[0]);
		} else {
			listMessages.setListData(currentSelectedContact.getMessagesRaw());
		}
	}

	public void log(String msg) {
		setBottomLine(msg);
		logMessages.add(new Message(false, msg));
	}

	public void setBottomLine(String txt) {
		lblErrorMessage.setText(txt);
	}

	public void addContact(String name, int device) {
		Contact found = null;
		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				found = c;
			}
		}

		if (found == null) {
			contacts.add(new Contact(name, device, new ArrayList<Message>()));
		} else {
			setBottomLine("Contact already added: " + found.getName());
		}
	}

	public void addMessage(int device, String text) {
		int index = listContacts.getSelectedIndex();

		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				c.addMessage(false, text);
				return;
			}
		}
		ArrayList<Message> msg = new ArrayList<>();
		msg.add(new Message(false, text));
		contacts.add(new Contact("Unknown", device, msg));

		listContacts.setSelectedIndex(index);
	}

	public class RefreshMessageRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				Controller.mainWindow.refreshMessages();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {

				}
			}
		}

	}
}
