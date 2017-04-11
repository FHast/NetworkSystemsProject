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

public class NewContactPopup extends JFrame {

	private JPanel contentPane;
	private JTextField txtDevice;
	private JTextField txtName;
	private JLabel lblErrorMsg;

	/**
	 * Create the frame.
	 */
	public NewContactPopup(MainWindow window) {
		setTitle("New contact");
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 330, 226);
		contentPane = new JPanel();

		JLabel lblTop = new JLabel("Please enter the contact details!");

		JLabel lblDevice = new JLabel("Device:");

		txtDevice = new JTextField();
		txtDevice.setColumns(10);

		JLabel lblName = new JLabel("Name:");

		txtName = new JTextField();
		txtName.setColumns(10);

		JButton btnFinish = new JButton("Finish");
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

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
							.addComponent(lblTop)
							.addComponent(lblDevice)
							.addComponent(lblName)
							.addComponent(txtDevice, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
							.addComponent(txtName))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnFinish)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblErrorMsg)))
					.addContainerGap(19, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblTop)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblDevice)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtDevice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblName)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnFinish)
						.addComponent(lblErrorMsg))
					.addContainerGap(25, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);
	}
}
