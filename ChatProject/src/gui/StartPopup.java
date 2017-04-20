package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.Controller;

/**
 * Popup shown at the start to show error messages (e.g. if the program is already running).
 */
public class StartPopup extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	

	/**
	 * Show frame.
	 * @param title Title of the frame
	 * @param t1 Text for the label at the top
	 * @param t2 Text for the label at the bottom
	 */
	public StartPopup(String title, String t1, String t2) {
		setType(Type.UTILITY);
		
		Controller.mainWindow.setVisible(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 325, 130);
		setResizable(false);
		contentPane = new JPanel();
		
		setTitle(title);
		JLabel lblText1 = new JLabel(t1);
		lblText1.setBounds(12, 12, 295, 20);
		JLabel lblText2 = new JLabel(t2);
		lblText2.setBounds(12, 33, 295, 20);
		
		JButton btnOk = new JButton("OK");
		btnOk.setForeground(Color.DARK_GRAY);
		btnOk.setBackground(new Color(0, 153, 255));
		btnOk.setBounds(106, 65, 109, 25);
		btnOk.addActionListener(new ActionListener() {
			// exit program since errors occurred
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.add(lblText1);
		contentPane.add(lblText2);
		contentPane.add(btnOk);
	}
}
