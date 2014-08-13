package gst.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.TitledBorder;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class FrameOpzioni extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JPasswordField passwordField;
	
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
		
		JPanel panel_itasa = new JPanel();
		panel_itasa.setBorder(new TitledBorder(null, "Italiansubs.net", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_itasa.setBounds(10, 270, 620, 89);
		getContentPane().add(panel_itasa);
		panel_itasa.setLayout(new GridLayout(2, 3, 0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_itasa.add(panel_1);
		
		JCheckBox chckbxAbilita = new JCheckBox("Abilita");
		panel_1.add(chckbxAbilita);
		
		JPanel panel_2 = new JPanel();
		panel_itasa.add(panel_2);
		
		JLabel lblUsername = new JLabel("Username");
		panel_2.add(lblUsername);
		
		textField = new JTextField();
		panel_2.add(textField);
		textField.setColumns(10);
		
		JPanel panel_3 = new JPanel();
		panel_itasa.add(panel_3);
		
		JButton btnVerifica = new JButton("Verifica");
		panel_3.add(btnVerifica);
		
		JPanel panel_4 = new JPanel();
		panel_itasa.add(panel_4);
		
		JPanel panel_5 = new JPanel();
		panel_itasa.add(panel_5);
		
		JLabel lblPassword = new JLabel("Password");
		panel_5.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		panel_5.add(passwordField);
		
		JPanel panel_6 = new JPanel();
		panel_itasa.add(panel_6);
		
		JButton btnSalva_1 = new JButton("Salva");
		panel_6.add(btnSalva_1);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Generale", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_7.setBounds(10, 10, 620, 125);
		getContentPane().add(panel_7);
		panel_7.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel_8 = new JPanel();
		panel_7.add(panel_8);
		
		JCheckBox chckbxApriGestioneSerie = new JCheckBox("Apri Gestione Serie TV all'avvio");
		panel_8.add(chckbxApriGestioneSerie);
		
		JPanel panel_9 = new JPanel();
		panel_7.add(panel_9);
		
		JCheckBox chckbxAvviaRidottoCome = new JCheckBox("Avvia ridotto come icona");
		panel_9.add(chckbxAvviaRidottoCome);
		
		JPanel panel_10 = new JPanel();
		panel_7.add(panel_10);
		
		JCheckBox chckbxChiediConfermaPrima = new JCheckBox("Chiedi conferma prima di chiudere il programma");
		panel_10.add(chckbxChiediConfermaPrima);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBorder(new TitledBorder(null, "Serie TV", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_11.setBounds(10, 140, 620, 125);
		getContentPane().add(panel_11);
		panel_11.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel_12 = new JPanel();
		panel_11.add(panel_12);
		
		JCheckBox chckbxAbilitaDownloadAutomatico = new JCheckBox("Abilita download automatico");
		panel_12.add(chckbxAbilitaDownloadAutomatico);
		
		JPanel panel_13 = new JPanel();
		panel_11.add(panel_13);
		
		JSpinner minRicerca = new JSpinner();
		minRicerca.setModel(new SpinnerNumberModel(480, 60, 1440, 5));
		panel_13.add(minRicerca);
		
		JLabel lblMinutiTraOgni = new JLabel("minuti tra ogni ricerca");
		panel_13.add(lblMinutiTraOgni);
		
		JPanel panel_14 = new JPanel();
		panel_11.add(panel_14);
		
		JCheckBox chckbxAbilitaDownloadSottotitoli = new JCheckBox("Abilita download sottotitoli");
		panel_14.add(chckbxAbilitaDownloadSottotitoli);
	}
}
