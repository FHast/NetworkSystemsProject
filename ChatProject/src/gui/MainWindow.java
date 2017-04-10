package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import connection.Controller;
import javax.swing.JScrollPane;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L; // just so that eclipse
														// does not complain
	private JPanel contentPane;
	private JTextField textFieldMessage;
	private ArrayList<Message> logMessages;
	private ArrayList<Contact> contacts;
	private JList<Contact> listContacts;
	private JButton btnSend;
	private JLabel lblErrorMessage;
	private JList listMessages;

	private boolean openedContactWindow = false;

	private Controller controller;

	/**
	 * Create the frame.
	 */
	public MainWindow(Controller c) {
		// settings of the frame
		setTitle("Ad-Hoc Chat "+c.myIP);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 600);

		contentPane = new JPanel();

		logMessages = new ArrayList<>();
		contacts = new ArrayList<>();

		controller = c;

		// Contact list
		listContacts = new JList<>();
		refreshContactList();
		MainWindow self = this;
		

		// Send button
		btnSend = new JButton("Send");
		getRootPane().setDefaultButton(btnSend);
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				controller.sendMessage(listContacts.getSelectedValue().getDevice(),textFieldMessage.getText());
				textFieldMessage.setText("");
			}

		});

		// text field for the message
		textFieldMessage = new JTextField();
		textFieldMessage.setColumns(10);

		// Label for bottom line error message
		lblErrorMessage = new JLabel("Connection established.");
		
		JScrollPane scrollPane = new JScrollPane();

		// Layout
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnSend))
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)))
						.addComponent(lblErrorMessage))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(scrollPane)
						.addComponent(listContacts, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(textFieldMessage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
					.addComponent(lblErrorMessage)
					.addContainerGap())
		);
		
		listMessages = new JList();
		
		listContacts.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				if (listContacts.getSelectedIndex() != -1) {
					if (listContacts.getSelectedValue().toString().equals("Log")) {
						listMessages.setListData(logMessages.toArray(new Message[0]));

					} else if (listContacts.getSelectedValue().toString().equals("+")) {
						listMessages.setListData(new Message[0]);

						if (!openedContactWindow) {
							NewContactPopup popup = new NewContactPopup(self);
							popup.setVisible(true);
							openedContactWindow = true;
						}
					} else {
						listMessages.setListData(listContacts.getSelectedValue().getMessagesRaw());
					}
				}
			}

		});
		scrollPane.setViewportView(listMessages);
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);
	}

	public void refreshContactList() {
		Contact[] c = new Contact[contacts.size() + 2];
		System.arraycopy(contacts.toArray(new Contact[0]), 0, c, 0, contacts.size());
		c[c.length - 2] = new Contact("Log", 0, new ArrayList<Message>());
		c[c.length - 1] = new Contact("+", 0, new ArrayList<Message>());
		listContacts.setListData(c);
	}

	public void log(String msg) {
		logMessages.add(new Message(false, "[" + LocalTime.now() + "]" + msg));
	}

	public void setBottomLine(String txt) {
		lblErrorMessage.setText(txt);
	}

	public void addContact(String name, int device) {
		contacts.add(new Contact(name, device, new ArrayList<Message>()));
	}

	public void setOpenedContactWindow(boolean b) {
		openedContactWindow = b;
	}
	
	public void addMessage(int device, String text) {
		for(Contact c : contacts) {
			if (c.getDevice() == device) {
				c.addMessage(false, text);
				return;
			}
		}
		ArrayList<Message> msg = new ArrayList<>();
		msg.add(new Message(false,text));
		contacts.add(new Contact("Unknown", device, msg));
	}
}
