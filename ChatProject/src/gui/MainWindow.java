package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldMessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		// settings of the frame
		setTitle("Ad-Hoc Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 661, 550);
		
		contentPane = new JPanel();
		
		// Messages list
		JList<Message> listMessages = new JList<>();
		listMessages.setEnabled(false);
		listMessages.setCellRenderer( new ListCellRenderer<Message>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
					boolean isSelected, boolean cellHasFocus) {
				JLabel l = new JLabel(value.getText());
				
				if (value.isSentBySelf()) {
					l.setHorizontalAlignment(JLabel.RIGHT);
					l.setForeground(Color.RED);
				}
				else {
					l.setForeground(Color.BLUE);
				}
				
				return l;
			}

			
		});
		
		// dummy contacts - just for debug
		Contact[] contacts = new Contact[3];
		contacts[0] = new Contact("sadasd",null,new ArrayList<Message>());
		contacts[0].addMessage(true, "askdjasdklj");
		contacts[0].addMessage(false, "sadsadas");
		contacts[1] = new Contact("sss",null,new ArrayList<Message>());
		contacts[1].addMessage(true, "sddadsds");
		contacts[1].addMessage(false, "sadsad");
		contacts[1].addMessage(false, "asdsad");
		contacts[2] = new Contact("dffgfg",null,new ArrayList<Message>());
		contacts[2].addMessage(false, "sdfsfdgfg");
		
		// Contact list
		JList<Contact> listContacts = new JList<>();
		listContacts.setListData(contacts);
		listContacts.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				listMessages.setListData(listContacts.getSelectedValue().getMessagesRaw());
			}
			
		});
		
		
		// Send button
		JButton btnSend = new JButton("Send");
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
		JLabel lblErrorMessage = new JLabel("Connection established.");
		
		// Layout
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
									.addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnSend))
								.addComponent(listMessages, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)))
						.addComponent(lblErrorMessage))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(listMessages, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE)
						.addComponent(listContacts, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(textFieldMessage)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
					.addComponent(lblErrorMessage)
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);
	}
}
