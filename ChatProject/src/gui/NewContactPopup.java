package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window.Type;

public class NewContactPopup extends JFrame {

	private JPanel contentPane;
	private JTextField txtDevice;
	private JTextField txtName;
	private JLabel lblErrorMsg;

	/**
	 * Create the frame.
	 */
	public NewContactPopup(MainWindow window) {
		setType(Type.POPUP);
		setAlwaysOnTop(true);
		setTitle("New contact");
		setResizable(false);
		setBounds(100, 100, 330, 224);
		contentPane = new JPanel();

		JLabel lblTop = new JLabel("Please enter the contact details!");
		lblTop.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblTop.setBounds(10, 11, 304, 14);

		JLabel lblDevice = new JLabel("Device:");
		lblDevice.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblDevice.setBounds(10, 36, 304, 14);

		txtDevice = new JTextField();
		txtDevice.setBounds(10, 56, 304, 20);
		txtDevice.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblName.setBounds(10, 82, 304, 14);

		txtName = new JTextField();
		txtName.setBounds(10, 102, 304, 20);
		txtName.setColumns(10);

		JButton btnFinish = new JButton("Create");
		btnFinish.setBackground(new Color(0, 153, 255));
		btnFinish.setBounds(237, 158, 76, 23);
		getRootPane().setDefaultButton(btnFinish);
		btnFinish.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!txtName.getText().equals("") && !txtDevice.getText().equals("")) {
					try {
						window.addContact(txtName.getText(), Integer.parseInt(txtDevice.getText()));
						window.refreshContactList();
						txtName.setText("");
						txtDevice.setText("");
						setVisible(false);
					} catch (NumberFormatException ex) {
						txtName.setText("");
						txtDevice.setText("");
						lblErrorMsg.setText("Please enter a number!");
					}
				}
				else {
					lblErrorMsg.setText("You have to enter something..");
				}
			}

		});
		
		lblErrorMsg = new JLabel("");
		lblErrorMsg.setForeground(new Color(204, 51, 51));
		lblErrorMsg.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblErrorMsg.setBounds(10, 133, 304, 14);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.add(lblTop);
		contentPane.add(lblDevice);
		contentPane.add(lblName);
		contentPane.add(txtDevice);
		contentPane.add(txtName);
		contentPane.add(btnFinish);
		contentPane.add(lblErrorMsg);
	}
}
