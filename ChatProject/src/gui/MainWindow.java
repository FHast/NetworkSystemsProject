package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import connection.DATAservice;

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

	private boolean openedContactWindow = false;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		// settings of the frame
		setTitle("Ad-Hoc Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 661, 550);

		contentPane = new JPanel();

		logMessages = new ArrayList<>();
		contacts = new ArrayList<>();
		
		addContact("sadksad",1);
		addContact("jnfkjnk",2);
		addContact("fgjkk",4);

		// Messages list
		listMessages = new JList<>();
		listMessages.setEnabled(false);
		listMessages.setCellRenderer(new ListCellRenderer<Message>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel l = new JLabel(value.getText());

				if (value.isSentBySelf()) {
					l.setHorizontalAlignment(JLabel.RIGHT);
					l.setForeground(Color.RED);
				} else {
					l.setForeground(Color.BLUE);
				}

				return l;
			}

		});

		// Contact list
		listContacts = new JList<>();
		refreshContactList();
		MainWindow self = this;
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

		// Send button
		btnSend = new JButton("Send");
		getRootPane().setDefaultButton(btnSend);
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO actually sending a message
				textFieldMessage.setText("");
			}

		});

		// text field for the message
		textFieldMessage = new JTextField();
		textFieldMessage.setColumns(10);

		// Label for bottom line error message
		lblErrorMessage = new JLabel("Connection established.");

		// Layout
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
						.createSequentialGroup()
						.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
										.addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSend))
								.addComponent(listMessages, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)))
						.addComponent(lblErrorMessage))
				.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(listMessages, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE)
						.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false).addComponent(textFieldMessage)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED, 79, Short.MAX_VALUE).addComponent(lblErrorMessage)
				.addContainerGap()));
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);
	}

	public void refreshContactList() {
		Contact[] c = new Contact[contacts.size() + 2];
		System.arraycopy(contacts.toArray(new Contact[0]), 0, c, 0, contacts.size());
		c[c.length - 2] = new Contact("Log", null, new ArrayList<Message>());
		c[c.length - 1] = new Contact("+", null, new ArrayList<Message>());
		listContacts.setListData(c);
	}

	public void log(String msg) {
		logMessages.add(new Message(false, "[" + LocalTime.now() + "]" + msg));
	}

	public void setBottomLine(String txt) {
		lblErrorMessage.setText(txt);
	}

	public void addContact(String name, int device) {
		try {
			contacts.add(new Contact(name, InetAddress.getByName("192.168.5." + device), new ArrayList<Message>()));
		} catch (UnknownHostException e) {
			setBottomLine("Error: Invalid device");
		}
	}
	
	public void setOpenedContactWindow(boolean b) {
		openedContactWindow = b;
	}
}
