package gst.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FrameOpzioni extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public FrameOpzioni(){
		super("Gestione Serie TV - Opzioni");
		setUndecorated(true);
		setSize(640, 480);
		setResizable(false);
		setAlwaysOnTop(true);
		getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 445, 640, 35);
		getContentPane().add(panel);
		
		JButton btnSalva = new JButton("Salva");
		panel.add(btnSalva);
		
		JButton btnChiudi = new JButton("Chiudi");
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrameOpzioni.this.setVisible(false);
			}
		});
		panel.add(btnChiudi);
	}
}
