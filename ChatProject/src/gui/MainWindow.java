package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
import networkLayer.tables.FTableEntry;
import networkLayer.tables.ForwardingTableService;

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
	private JMenuItem menuItemDelete;
	private JMenu optionsMenu;
	private JMenuItem menuItemNewContact;
	private JMenuBar menubar;
	private JMenuItem menuItemRename;
	private JMenuItem menuItemLog;
	private JMenuItem menuItemOpenFolder;

	public MainWindow(Controller c) {
		setResizable(false);
		// settings of the frame
		setTitle("Ad-Hoc Chat " + Controller.getMyIP());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 1195, 541);

		contentPane = new JPanel();

		logMessages = new ArrayList<>();
		contacts = new ArrayList<>();
		
		/*
		//TODO remove ===========================
		ArrayList<Message> msg = new ArrayList<>();
		msg.add(new Message(true, "askjhfassadadhjfgkh", LocalTime.now()));
		msg.add(new Message(false, "askjhfasjgfashjfgkh", LocalTime.now()));
		msg.add(new Message(true, "askjhfasjgfashjfgkh", LocalTime.now()));
		contacts.add(new Contact("Sample1", 1,msg));
		contacts.add(new Contact("Sample2", 1,msg));
		contacts.get(1).addMessage(false, "daskljsdfakjfh",  Message.TYPE_TEXT, LocalTime.now());
		// TODO remove ==========================
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

						// load image
						try {
							BufferedImage img = ImageIO.read(new File(value.getText()));
							img = (BufferedImage) getScaledImage(img, 400, 300);
							l = new JLabel(new ImageIcon(img));
						} catch (IOException e) {
							// file does not exist
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

		// CellRenderer for contact list
		listContacts.setCellRenderer(new ListCellRenderer<Contact>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends Contact> list, Contact value, int index,
					boolean isSelected, boolean cellHasFocus) {
				if (value != null) {
					// init
					JLabel l = new JLabel(value.toString());

					// set font
					if (value.hasUnreadMessages()) {
						l.setFont(new Font("Tahoma", Font.BOLD, 12));
					} else {
						l.setFont(new Font("Tahoma", Font.PLAIN, 12));
					}

					// set background if selected
					l.setOpaque(true);
					if (value.equals(currentSelectedContact)) {
						l.setBackground(Color.LIGHT_GRAY);
					}
					else {
						l.setBackground(Color.WHITE);
					}

					// return
					return l;
				} else {
					return new JLabel("");
				}
			}

		});
		
		refreshContactList(); // TODO remove
		
		// Send button
		buttonSend = new JButton("Send");
		buttonSend.setForeground(Color.DARK_GRAY);
		buttonSend.setBackground(new Color(0, 153, 255));
		buttonSend.setBounds(1076, 405, 108, 23);
		getRootPane().setDefaultButton(buttonSend);
		buttonSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String input = textfieldMessage.getText();
				if (!input.equals("") && currentSelectedContact != null && currentSelectedContact.getDevice() != 0) {
					controller.sendMessage(currentSelectedContact.getDevice(), input);
					currentSelectedContact.addMessage(true, input, Message.TYPE_TEXT, LocalTime.now());
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
						currentSelectedContact.addMessage(true, file.getAbsolutePath(), Message.TYPE_IMAGE,
								LocalTime.now());
					} else {
						currentSelectedContact.addMessage(true, file.getAbsolutePath(), Message.TYPE_FILE,
								LocalTime.now());
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

		// MenuItem New Contact
		menuItemNewContact = new JMenuItem("New Contact");
		menuItemNewContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				labelContactname.setText("");
				currentSelectedContact = null;

				newContactFrame.setDevice("");
				newContactFrame.setName("");
				newContactFrame.setRename(false);
				newContactFrame.setVisible(true);
			}
		});
		menuItemNewContact.setFont(new Font("Tahoma", Font.PLAIN, 13));
		optionsMenu.add(menuItemNewContact);

		// MenuItem Rename
		menuItemRename = new JMenuItem("Rename");
		optionsMenu.add(menuItemRename);
		menuItemRename.setFont(new Font("Tahoma", Font.PLAIN, 13));
		menuItemRename.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listContacts.isSelectionEmpty()) {
					newContactFrame.setRename(true);
					newContactFrame.setDevice(String.valueOf(listContacts.getSelectedValue().getDevice()));
					newContactFrame.setName("");
					newContactFrame.setVisible(true);
				} else {
					setBottomLine("Please select a contact first!");
				}
			}

		});

		// MenuItem Delete
		menuItemDelete = new JMenuItem("Delete");
		optionsMenu.add(menuItemDelete);
		menuItemDelete.setFont(new Font("Tahoma", Font.PLAIN, 13));
		menuItemDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!listContacts.isSelectionEmpty()) {
					deleteContact(listContacts.getSelectedValue().getDevice());
				} else {
					setBottomLine("Please select a contact first!");
				}
			}

		});

		// MenuItem Log
		menuItemLog = new JMenuItem("Show log");
		menuItemLog.setFont(new Font("Tahoma", Font.PLAIN, 13));
		menuItemLog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				labelContactname.setText("System Log");
				currentSelectedContact = new Contact("System Log", 0, logMessages);
				listContacts.clearSelection();
			}

		});
		optionsMenu.add(menuItemLog);

		// MenuItem Open Folder
		menuItemOpenFolder = new JMenuItem("Open \"received\" Folder");
		menuItemOpenFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Desktop.getDesktop().open(new File("received"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					File file = new File("received");
					file.mkdir();
					try {
						Desktop.getDesktop().open(new File("received"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		menuItemOpenFolder.setFont(new Font("Dialog", Font.PLAIN, 13));
		optionsMenu.add(menuItemOpenFolder);

		// debug
		JMenuItem showFT = new JMenuItem("Add FT to log");
		showFT.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				log("====================");
				log("Current forwarding Table:");
				for (FTableEntry ex : ForwardingTableService.getEntries()) {
					log(ex.destinationAddress.toString() + " -> " + ex.nextHopAddress.toString());
				}
				log("====================");
			}

		});
		optionsMenu.add(showFT);

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
		listContacts.setListData(contacts.toArray(new Contact[0]));
	}

	private static Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	public void refreshMessages() {
		if (currentSelectedContact == null) {
			listMessages.setListData(new Message[0]);
		} else {
			listMessages.setListData(currentSelectedContact.getMessagesRaw());
			currentSelectedContact.setUnreadMessages(false);
		}
	}

	public void log(String msg) {
		setBottomLine(msg);
		logMessages.add(new Message(false, msg, LocalTime.now()));
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

	public void renameContact(String newName, int device) {
		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				c.setName(newName);
			}
		}
		refreshContactList();
	}

	public void deleteContact(int device) {
		ArrayList<Contact> newList = new ArrayList<>();
		for (Contact c : contacts) {
			if (c.getDevice() != device) {
				newList.add(c);
			}
		}
		contacts = newList;
		refreshContactList();
	}

	public void addMessage(int device, String text, int type, LocalTime sendTime) {
		// current index
		int index = listContacts.getSelectedIndex();

		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				c.addMessage(false, text, type, sendTime);
				return;
			}
		}

		// contact not in list
		ArrayList<Message> msg = new ArrayList<>();
		Contact c = new Contact("Unknown", device, msg);
		c.addMessage(false, text, type, sendTime);
		contacts.add(c);

		listContacts.setSelectedIndex(index);

		refreshContactList();
	}

	public void setContactFocus(int device) {
		int i = 0;
		for (Contact c : contacts) {
			if (c.getDevice() == device) {
				listContacts.setSelectedIndex(i);
				return;
			}
			i++;
		}
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
