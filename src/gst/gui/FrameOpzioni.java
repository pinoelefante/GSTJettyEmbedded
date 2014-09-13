package gst.gui;

import gst.programma.Settings;
import gst.sottotitoli.italiansubs.ItalianSubs;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.TitledBorder;

import java.awt.FlowLayout;

import javax.swing.JCheckBox;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTabbedPane;

import util.os.Os;

import java.awt.BorderLayout;

import javax.swing.border.LineBorder;

import java.awt.Color;

public class FrameOpzioni extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField itasaUsername;
	private JPasswordField itasaPassword;
	private JTextField dirUtorrent;
	private JTextField dirQbittorrent;
	private JCheckBox chkbAutostart;
	private JCheckBox chkbStartHidden;
	private JCheckBox chckbxAskOnClose;
	private JCheckBox chckbxAbilitaDownloadAutomatico;
	private JSpinner minRicerca;
	private JCheckBox chckbxAbilitaDownloadSottotitoli;
	private JButton btnVerificaItasa;
	private JButton btnSalva;
	private JButton btnChiudi;
	private JTextField dirDownload;
	private JButton btnSfogliaDirDownload;
	private JLabel labelItasaLoginStatus;
	
	private Settings s=Settings.getInstance();
	
	private JButton btnSalvaClient;
	private JButton btnSfogliaUtorrent;
	private JButton btnSfogliaQbittorrent;
	private JButton btnChiudi2;
	private JTextField utorrentUser;
	private JPasswordField utorrentPassword;
	private JTextField utorrentPorta;
	
	public FrameOpzioni(){
		super("Gestione Serie TV - Opzioni");
		setUndecorated(true);
		setSize(640, 480);
		setResizable(false);
		setAlwaysOnTop(true);
		getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 640, 480);
		getContentPane().add(tabbedPane);
		
		JPanel panel_15 = new JPanel();
		tabbedPane.addTab("Generale", null, panel_15, null);
		panel_15.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 417, 635, 35);
		panel_15.add(panel);
		
		btnSalva = new JButton("Salva - Generale");
		panel.add(btnSalva);
		
		btnChiudi = new JButton("Chiudi");
		panel.add(btnChiudi);
		
		JPanel panel_itasa = new JPanel();
		panel_itasa.setBounds(10, 265, 615, 89);
		panel_15.add(panel_itasa);
		panel_itasa.setBorder(new TitledBorder(null, "Italiansubs.net", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_itasa.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_itasa.add(panel_1);
		
		JLabel lblUsername = new JLabel("Username");
		panel_1.add(lblUsername);
		
		itasaUsername = new JTextField();
		panel_1.add(itasaUsername);
		itasaUsername.setColumns(20);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_2.getLayout();
		flowLayout_6.setAlignment(FlowLayout.LEFT);
		panel_itasa.add(panel_2);
		
		btnVerificaItasa = new JButton("Verifica");
		panel_2.add(btnVerificaItasa);
		
		JPanel panel_3 = new JPanel();
		panel_itasa.add(panel_3);
		
		JLabel lblPassword = new JLabel("Password");
		panel_3.add(lblPassword);
		
		itasaPassword = new JPasswordField();
		panel_3.add(itasaPassword);
		itasaPassword.setColumns(20);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) panel_4.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEFT);
		panel_itasa.add(panel_4);
		
		labelItasaLoginStatus = new JLabel("");
		panel_4.add(labelItasaLoginStatus);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBounds(10, 11, 615, 125);
		panel_15.add(panel_7);
		panel_7.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Generale", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_7.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel_8 = new JPanel();
		panel_7.add(panel_8);
		
		chkbAutostart = new JCheckBox("Apri Gestione Serie TV all'avvio");
		panel_8.add(chkbAutostart);
		
		JPanel panel_9 = new JPanel();
		panel_7.add(panel_9);
		
		chkbStartHidden = new JCheckBox("Avvia ridotto come icona");
		panel_9.add(chkbStartHidden);
		
		JPanel panel_10 = new JPanel();
		panel_7.add(panel_10);
		
		chckbxAskOnClose = new JCheckBox("Chiedi conferma prima di chiudere il programma");
		panel_10.add(chckbxAskOnClose);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBounds(10, 140, 615, 125);
		panel_15.add(panel_11);
		panel_11.setBorder(new TitledBorder(null, "Serie TV", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_11.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel_12 = new JPanel();
		panel_11.add(panel_12);
		
		chckbxAbilitaDownloadAutomatico = new JCheckBox("Abilita download automatico");
		panel_12.add(chckbxAbilitaDownloadAutomatico);
		
		JPanel panel_13 = new JPanel();
		panel_11.add(panel_13);
		
		minRicerca = new JSpinner();
		minRicerca.setModel(new SpinnerNumberModel(480, 60, 1440, 5));
		panel_13.add(minRicerca);
		
		JLabel lblMinutiTraOgni = new JLabel("minuti tra ogni ricerca");
		panel_13.add(lblMinutiTraOgni);
		
		JPanel panel_14 = new JPanel();
		panel_11.add(panel_14);
		
		chckbxAbilitaDownloadSottotitoli = new JCheckBox("Abilita download sottotitoli");
		panel_14.add(chckbxAbilitaDownloadSottotitoli);
		
		JPanel panel_20 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_20.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_20.setBorder(new TitledBorder(null, "Directory Download", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_20.setBounds(10, 357, 615, 55);
		panel_15.add(panel_20);
		
		dirDownload = new JTextField();
		panel_20.add(dirDownload);
		dirDownload.setColumns(30);
		
		btnSfogliaDirDownload = new JButton("Sfoglia");
		panel_20.add(btnSfogliaDirDownload);
		
		JPanel panel_16 = new JPanel();
		tabbedPane.addTab("Client", null, panel_16, null);
		panel_16.setLayout(null);
		
		JPanel panel_17 = new JPanel();
		panel_17.setBounds(0, 417, 635, 35);
		panel_16.add(panel_17);
		
		btnSalvaClient = new JButton("Salva - Client");
		panel_17.add(btnSalvaClient);
		
		btnChiudi2 = new JButton("Chiudi");
		panel_17.add(btnChiudi2);
		
		JPanel panel_21 = new JPanel();
		panel_21.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "uTorrent", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_21.setBounds(12, 12, 611, 190);
		panel_16.add(panel_21);
		panel_21.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_18 = new JPanel();
		panel_21.add(panel_18, BorderLayout.NORTH);
		FlowLayout flowLayout = (FlowLayout) panel_18.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_18.setBorder(new TitledBorder(null, "Percorso installazione", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		
		dirUtorrent = new JTextField();
		panel_18.add(dirUtorrent);
		dirUtorrent.setColumns(30);
		
		btnSfogliaUtorrent = new JButton("Sfoglia");
		panel_18.add(btnSfogliaUtorrent);
		
		JPanel panel_22 = new JPanel();
		panel_22.setBorder(new TitledBorder(null, "webUI", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_21.add(panel_22, BorderLayout.CENTER);
		panel_22.setLayout(new GridLayout(3, 0, 0, 0));
		
		JPanel panel_23 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_23.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_23);
		
		JLabel lblUsername_1 = new JLabel("Username");
		panel_23.add(lblUsername_1);
		
		utorrentUser = new JTextField();
		panel_23.add(utorrentUser);
		utorrentUser.setColumns(20);
		
		JPanel panel_24 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_24.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_24);
		
		JLabel lblPassword_1 = new JLabel("Password");
		panel_24.add(lblPassword_1);
		
		utorrentPassword = new JPasswordField();
		utorrentPassword.setColumns(20);
		panel_24.add(utorrentPassword);
		
		JPanel panel_25 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_25.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_25);
		
		JLabel lblPorta = new JLabel("Porta       ");
		panel_25.add(lblPorta);
		
		utorrentPorta = new JTextField();
		panel_25.add(utorrentPorta);
		utorrentPorta.setColumns(5);
		
		JPanel panel_26 = new JPanel();
		panel_26.setBorder(new TitledBorder(null, "qBittorrent", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_26.setBounds(10, 229, 615, 85);
		panel_16.add(panel_26);
		panel_26.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_19 = new JPanel();
		panel_26.add(panel_19, BorderLayout.NORTH);
		FlowLayout flowLayout_1 = (FlowLayout) panel_19.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_19.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Percorso installazione", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		
		dirQbittorrent = new JTextField();
		panel_19.add(dirQbittorrent);
		dirQbittorrent.setColumns(30);
		
		btnSfogliaQbittorrent = new JButton("Sfoglia");
		panel_19.add(btnSfogliaQbittorrent);
		
		addListener();
	}
	private void addListener(){
		btnSalva.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean askClose=chckbxAskOnClose.isSelected();
				boolean subDown = chckbxAbilitaDownloadSottotitoli.isSelected();
				boolean hidden=chkbStartHidden.isSelected();
				boolean autodown=chckbxAbilitaDownloadAutomatico.isSelected();
				boolean autostart = chkbAutostart.isSelected();
				String dirDown = dirDownload.getText().trim();
				int min=480;
				try {
					min=Integer.parseInt(minRicerca.getValue().toString());
				}
				catch(Exception e1){}
				String usernameItasa=itasaUsername.getText();
				String passItasa=new String(itasaPassword.getPassword());
					
				s.setAskOnClose(askClose);
				s.setRicercaSottotitoli(subDown);
				s.setStartHidden(hidden);
				s.setDownloadAutomatico(autodown);
				s.setAutostart(autostart);
				s.setDirectoryDownload(dirDown);
				s.setMinRicerca(min);
				s.setItasaUsername(usernameItasa);
				s.setItasaPassword(passItasa);
				s.salvaSettings();
				init();
			}
		});
		btnSalvaClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ut=dirUtorrent.getText().trim();
				String qbit=dirQbittorrent.getText().trim();
				String ut_user=utorrentUser.getText().trim(), 
						ut_pass=new String(utorrentPassword.getPassword()), 
						ut_port=utorrentPorta.getText();
				s.setUTorrentPath(ut);
				s.setQBittorrentPath(qbit);
				s.setUTorrentUsername(ut_user);
				s.setUTorrentPassword(ut_pass);
				s.setUTorrentPort(ut_port);
				s.salvaSettings();
				init();
			}
		});
		btnVerificaItasa.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean login=ItalianSubs.VerificaLogin(itasaUsername.getText(), new String(itasaPassword.getPassword()));
				if(login){
					labelItasaLoginStatus.setText("<html><b><font color='Green'>OK!</font></b></html>");
				}
				else {
					labelItasaLoginStatus.setText("<html><b><font color='Red'>Dati non validi!</font></b></html>");
				}
				
			}
		});
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrameOpzioni.this.setVisible(false);
			}
		});
		btnChiudi2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrameOpzioni.this.setVisible(false);
			}
		});
		btnSfogliaDirDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String destinazione_path = s.getDirectoryDownload();
				JFileChooser chooser = new JFileChooser(destinazione_path.isEmpty() ? null : destinazione_path);
				chooser.setDialogTitle("Seleziona cartella download");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileHidingEnabled(false);

				if (chooser.showOpenDialog(FrameOpzioni.this) == JFileChooser.APPROVE_OPTION) {
					destinazione_path = chooser.getSelectedFile().getAbsolutePath();
				}
				else {
					return;
				}
				dirDownload.setText(destinazione_path);
			}
		});
		btnSfogliaUtorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String destinazione_path = "";
				if(Os.isWindows()){
					destinazione_path=System.getenv("PROGRAMFILES")+File.separator+"utorrent";
				}
				else if(Os.isLinux()){
					destinazione_path="/usr/bin";
				}
				else if(Os.isMacOS()){
					destinazione_path="/Applications";
				}
				JFileChooser chooser = new JFileChooser(destinazione_path.isEmpty() ? null : destinazione_path);
				chooser.setDialogTitle("Percorso uTorrent");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileHidingEnabled(false);
				String filename="";
				if(Os.isWindows()){
					filename="utorrent.exe";
				}
				else if(Os.isLinux()){
					filename="utserver";
				}
				else if(Os.isMacOS()){
					filename="utorrent";
				}
				chooser.setFileFilter(new ClientFilter("uTorrent", filename));

				if (chooser.showOpenDialog(FrameOpzioni.this) == JFileChooser.APPROVE_OPTION) {
					destinazione_path = chooser.getSelectedFile().getAbsolutePath();
				}
				else {
					return;
				}
				dirUtorrent.setText(destinazione_path);
				
			}
		});
		btnSfogliaQbittorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String destinazione_path = "";
				if(Os.isWindows()){
					destinazione_path=System.getenv("PROGRAMFILES")+File.separator+"qbittorrent";
				}
				else if(Os.isLinux()){
					destinazione_path="/usr/bin";
				}
				else if(Os.isMacOS()){
					destinazione_path="/Applications";
				}
				JFileChooser chooser = new JFileChooser(destinazione_path.isEmpty() ? null : destinazione_path);
				chooser.setDialogTitle("Percorso QBittorrent");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileHidingEnabled(false);
				String filename="";
				if(Os.isWindows()){
					filename="qbittorrent.exe";
				}
				else if(Os.isLinux()){
					filename="qbittorrent";
				}
				else if(Os.isMacOS()){
					filename="qbittorrent";
				}
				chooser.setFileFilter(new ClientFilter("QBittorrent", filename));

				if (chooser.showOpenDialog(FrameOpzioni.this) == JFileChooser.APPROVE_OPTION) {
					destinazione_path = chooser.getSelectedFile().getAbsolutePath();
				}
				else {
					return;
				}
				dirQbittorrent.setText(destinazione_path);
			}
		});
	}
	public void init(){
		chckbxAbilitaDownloadAutomatico.setSelected(s.isDownloadAutomatico());
		chckbxAbilitaDownloadSottotitoli.setSelected(s.isRicercaSottotitoli());
		chckbxAskOnClose.setSelected(s.isAskOnClose());
		chkbAutostart.setSelected(s.isAutostart());
		chkbStartHidden.setSelected(s.isStartHidden());
		dirDownload.setText(s.getDirectoryDownload());
		dirQbittorrent.setText(s.getQBittorrentPath());
		dirUtorrent.setText(s.getUTorrentPath());
		itasaUsername.setText(s.getItasaUsername());
		itasaPassword.setText(s.getItasaPassword());
		utorrentUser.setText(s.getUTorrentUsername());
		utorrentPassword.setText(s.getUTorrentPassword());
		utorrentPorta.setText(s.getUTorrentPort());
	}
}
