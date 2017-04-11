package gui;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import connection.Controller;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class StartPopup extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	

	public StartPopup(String title, String t1, String t2) {
		
		Controller.mainWindow.setVisible(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 325, 130);
		contentPane = new JPanel();
		
		setTitle(title);
		JLabel lblText1 = new JLabel(t1);
		JLabel lblText2 = new JLabel(t2);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblText1)
								.addComponent(lblText2)))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(128)
							.addComponent(btnOk)))
					.addContainerGap(135, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblText1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblText2)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOk)
					.addContainerGap(18, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
		setContentPane(contentPane);
	}
}
