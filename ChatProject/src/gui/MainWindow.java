package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import applicationLayer.FileService;
import controller.Controller;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L; // just so that eclipse
														// does not complain
	private JPanel contentPane;
	private JTextField textfieldMessage;
	private ArrayList<Message> logMessages;
	private ArrayList<Contact> contacts;

	private JList<Message> listMessages;
	private JList<Contact> listContacts;
	private JButton buttonSend;
	private JLabel labelErrorMessage;
	private JLabel labelContactname;
	private JScrollPane scrollpanelChat;

	private Controller controller;
	private Contact currentSelectedContact = null;

	private NewContactPopup newContactFrame = new NewContactPopup(this);
	private JMenuItem menuItemRename;
	private JMenuItem menuItemDelete;
	private JCheckBoxMenuItem menuItemBlock;
	private JMenu optionsMenu;
	private JMenuItem menuItemNewContact;
	private JMenuBar menubar;

	public MainWindow(Controller c) {
		setResizable(false);
		// settings of the frame
		setTitle("Ad-Hoc Chat " + Controller.getMyIP());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 1195, 541);

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
				if (value != null) {
					if (value.getType() == Message.TYPE_TEXT) {
						// init
						JTextArea t = new JTextArea(10, 10);

						// formatting
						if (value.isSentBySelf()) {
							t.setForeground(Color.GRAY);
						} else {
							t.setForeground(Color.BLUE);
						}

						t.setLineWrap(true);

						// set text
						t.setText(value.toString());

						t.setRows(countLines(t));

						// wraps words only instead ofcharacters
						t.setWrapStyleWord(true);

						return t;
					} else if (value.getType() == Message.TYPE_IMAGE) {

						JLabel l = null;
						try {
							// create empty label with the Image as the icon
							BufferedImage img = ImageIO.read(new File(value.getText()));
							l = new JLabel(new ImageIcon(img));

						} catch (IOException e) {
							e.printStackTrace();
						}
						return l;
					} else {
						JLabel l = new JLabel("[FILE]" + value.getText());

						// formatting
						if (value.isSentBySelf()) {
							l.setForeground(Color.GRAY);
						} else {
							l.setForeground(Color.BLUE);
						}

						return l;
					}
				} else {
					return new JLabel("hehe");
				}
			}

		});

		// Contact list
		listContacts = new JList<>();
		listContacts.setBounds(10, 31, 237, 368);
		listContacts.setBorder(new LineBorder(Color.GRAY));
		refreshContactList();
		listContacts.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				if (listContacts.getSelectedIndex() != -1) {
					if (listContacts.getSelectedValue().toString().equals("Log")) {
						labelContactname.setText("System Log");
						currentSelectedContact = new Contact("System Log", 0, logMessages);
					} else if (listContacts.getSelectedValue().toString().equals("+")) {
						labelContactname.setText("");
						currentSelectedContact = null;
						newContactFrame.setVisible(true);
					} else {
						labelContactname.setText(listContacts.getSelectedValue().getName());
						currentSelectedContact = listContacts.getSelectedValue();
					}
				}
			}

		});

		// Send button
		buttonSend = new JButton("Send");
		buttonSend.setBackground(new Color(0, 153, 255));
		buttonSend.setBounds(1076, 405, 108, 23);
		getRootPane().setDefaultButton(buttonSend);
		buttonSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String input = textfieldMessage.getText();
				if (!input.equals("") && currentSelectedContact != null && currentSelectedContact.getDevice() != 0) {
					controller.sendMessage(currentSelectedContact.getDevice(), input);
					currentSelectedContact.addMessage(true, input, Message.TYPE_TEXT);
					textfieldMessage.setText("");
				}
			}

		});

		// text field for the message
		textfieldMessage = new JTextField();
		textfieldMessage.setBounds(253, 405, 817, 52);
		textfieldMessage.setColumns(10);

		// Label for bottom line error message
		labelErrorMessage = new JLabel("Connection established.");
		labelErrorMessage.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelErrorMessage.setBounds(10, 468, 1174, 14);

		scrollpanelChat = new JScrollPane(listMessages);
		scrollpanelChat.setBounds(253, 31, 931, 368);
		scrollpanelChat.setAutoscrolls(true);
		scrollpanelChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		labelContactname = new JLabel("");
		labelContactname.setFont(new Font("Tahoma", Font.PLAIN, 12));
		labelContactname.setBounds(253, 11, 705, 13);

		// Select File button
		JButton buttonSelectFile = new JButton("Select File");
		buttonSelectFile.setBackground(new Color(0, 153, 255));
		buttonSelectFile.setBounds(1076, 434, 108, 23);
		buttonSelectFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(null);

				File file = fc.getSelectedFile();
				if (file != null) {
					int device = currentSelectedContact.getDevice();

					String a = FileService.getAppendix(file.getPath());

					if (a.equals("gif") || a.equals("png") || a.equals("jpeg") || a.equals("jpg")) {
						currentSelectedContact.addMessage(true, file.getAbsolutePath(), Message.TYPE_IMAGE);
					} else {
						currentSelectedContact.addMessage(true, file.getAbsolutePath(), Message.TYPE_FILE);
					}

					controller.sendFile(device, file);
				}
			}

		});

		menubar = new JMenuBar();
		setJMenuBar(menubar);

		optionsMenu = new JMenu("Options");
		menubar.add(optionsMenu);
		optionsMenu.setFont(new Font("Tahoma", Font.PLAIN, 13));

		menuItemNewContact = new JMenuItem("New Contact");
		menuItemNewContact.setFont(new Font("Tahoma", Font.PLAIN, 13));
		optionsMenu.add(menuItemNewContact);

		menuItemRename = new JMenuItem("Rename");
		optionsMenu.add(menuItemRename);
		menuItemRename.setFont(new Font("Tahoma", Font.PLAIN, 13));

		menuItemDelete = new JMenuItem("Delete");
		optionsMenu.add(menuItemDelete);
		menuItemDelete.setFont(new Font("Tahoma", Font.PLAIN, 13));

		menuItemBlock = new JCheckBoxMenuItem("Block");
		optionsMenu.add(menuItemBlock);
		menuItemBlock.setFont(new Font("Tahoma", Font.PLAIN, 13));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.add(listContacts);
		contentPane.add(textfieldMessage);
		contentPane.add(buttonSelectFile);
		contentPane.add(buttonSend);
		contentPane.add(scrollpanelChat);
		contentPane.add(labelContactname);
		contentPane.add(labelErrorMessage);

		JLabel labelContacts = new JLabel("Contacts");
		labelContacts.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelContacts.setBounds(10, 11, 237, 14);
		contentPane.add(labelContacts);

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
		labelErrorMessage.setText(txt);
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

	public void addMessage(int device, String text, int type) {
		// current index
		int index = listContacts.getSelectedIndex();

		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				c.addMessage(false, text, type);
				return;
			}
		}
		ArrayList<Message> msg = new ArrayList<>();
		Contact c = new Contact("Unknown", device, msg);
		c.addMessage(false, text, type);
		contacts.add(c);

		listContacts.setSelectedIndex(index);
	}

	public void refreshScrollbar() {
		JScrollBar vertical = scrollpanelChat.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());

		// TODO
	}

	public class RefreshMessageRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				Controller.mainWindow.refreshMessages();
				// refreshScrollbar();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
			}
		}

	}
}
