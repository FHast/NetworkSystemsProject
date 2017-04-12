package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
				if (value.getType() == Message.TYPE_TEXT) {
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

					// Dimension d = t.getPreferredSize();
					// t.setPreferredSize(new Dimension((2 * d.width) / 3,
					// d.height));
					t.setRows(countLines(t) + 1);

					// t.setWrapStyleWord(true); // wraps words only instead of
					// characters

					return t;
				} else if (value.getType() == Message.TYPE_IMAGE) {

					JLabel l = null;
					try {
						// create empty label with the Image as the icon
						BufferedImage img = ImageIO.read(new File(value.getText()));
						l = new JLabel(new ImageIcon(img));
						
						// positioning
						if (value.isSentBySelf()) {
							l.setHorizontalAlignment(SwingConstants.RIGHT);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return l;
				}
				else {
					JLabel l = new JLabel("[FILE]" + value.getText());
					
					// formatting
					if (value.isSentBySelf()) {
						l.setHorizontalAlignment(SwingConstants.RIGHT);
						l.setForeground(Color.GRAY);
					}
					else {
						l.setForeground(Color.BLUE);
					}
					
					return l;
				}
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
		
		
		// Select File button
		JButton btnSelFi = new JButton("Select File");
		btnSelFi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				System.out.print(fc.showOpenDialog(null));
			}

		});

		// Layout
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblContacts))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(btnSelFi)
										.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)))
								.addComponent(jsp, GroupLayout.DEFAULT_SIZE, 927, Short.MAX_VALUE)
								.addComponent(lblNamefield, Alignment.LEADING)))
						.addComponent(lblErrorMessage))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNamefield)
						.addComponent(lblContacts))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(jsp, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE)
						.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnSend)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSelFi))
						.addComponent(textFieldMessage))
					.addGap(84)
					.addComponent(lblErrorMessage)
					.addContainerGap())
		);
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
