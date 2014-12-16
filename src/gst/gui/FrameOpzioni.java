package gst.gui;

import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.sottotitoli.italiansubs.ItalianSubs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import util.os.Os;

public class FrameOpzioni extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField dirUtorrent;
	private JTextField dirQbittorrent;
	private JCheckBox chkbAutostart;
	private JCheckBox chkbStartHidden;
	private JCheckBox chckbxAskOnClose;
	private JButton btnSalva;
	private JButton btnChiudi;
	private JTextField dirDownload;
	private JButton btnSfogliaDirDownload;
	
	private Settings s=Settings.getInstance();
	
	private JButton btnSalva3;
	private JButton btnSfogliaUtorrent;
	private JButton btnSfogliaQbittorrent;
	private JButton btnChiudi3;
	private JTextField utorrentUser;
	private JPasswordField utorrentPassword;
	private JTextField utorrentPorta;
	private JComboBox<Lingua> lingua;
	private JTextField itasaUsername;
	private JPasswordField itasaPassword;
	private JTextField dirDownload2;
	private JCheckBox chkboxDownloadAuto;
	private JCheckBox chkDownloadSubs;
	private JButton btnSfogliaDirDownload2;
	private JSpinner minFreeSpace;
	private JButton btnSalva2;
	private JButton btnChiudi2;
	
	public FrameOpzioni(){
		super("Gestione Serie TV - Opzioni");
		setUndecorated(true);
		setSize(480, 480);
		setResizable(false);
		setAlwaysOnTop(true);
		getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		tabbedPane.setBounds(0, 0, 480, 480);
		getContentPane().add(tabbedPane);
		
		JPanel panel_15 = new JPanel();
		tabbedPane.addTab("Generale", null, panel_15, null);
		panel_15.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 405, 475, 35);
		panel_15.add(panel);
		
		btnSalva = new JButton("Salva");
		panel.add(btnSalva);
		
		btnChiudi = new JButton("Chiudi");
		panel.add(btnChiudi);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBounds(10, 70, 453, 114);
		panel_15.add(panel_7);
		panel_7.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Generale", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_7.setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel_8 = new JPanel();
		FlowLayout flowLayout_8 = (FlowLayout) panel_8.getLayout();
		flowLayout_8.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_8);
		
		chkbAutostart = new JCheckBox("Apri Gestione Serie TV all'avvio");
		panel_8.add(chkbAutostart);
		
		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_9 = (FlowLayout) panel_9.getLayout();
		flowLayout_9.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_9);
		
		chkbStartHidden = new JCheckBox("Avvia ridotto come icona");
		panel_9.add(chkbStartHidden);
		
		JPanel panel_10 = new JPanel();
		FlowLayout flowLayout_10 = (FlowLayout) panel_10.getLayout();
		flowLayout_10.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_10);
		
		chckbxAskOnClose = new JCheckBox("Chiedi conferma prima di chiudere il programma");
		panel_10.add(chckbxAskOnClose);
		
		JPanel panel_20 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_20.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_20.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Directory Download principale", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_20.setBounds(10, 190, 453, 55);
		panel_15.add(panel_20);
		
		dirDownload = new JTextField();
		panel_20.add(dirDownload);
		dirDownload.setColumns(30);
		
		btnSfogliaDirDownload = new JButton("Sfoglia");
		panel_20.add(btnSfogliaDirDownload);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Lingua", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_5.setBounds(10, 10, 453, 58);
		panel_15.add(panel_5);
		
		lingua = new JComboBox<Lingua>();
		lingua.addItem(new Lingua("Deutsch", "de"));
		lingua.addItem(new Lingua("English", "en"));
		lingua.addItem(new Lingua("Español", "es"));
		lingua.addItem(new Lingua("Français", "fr"));
		lingua.addItem(new Lingua("Italiano", "it"));
		lingua.addItem(new Lingua("Português", "pr"));
		String lang = s.getLingua();
		boolean found = false;
		for(int i=0;i<lingua.getItemCount();i++){
			Lingua l = lingua.getItemAt(i);
			if(l.getValue().compareToIgnoreCase(lang)==0){
				lingua.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if(!found)
			lingua.setSelectedIndex(1);
		panel_5.add(lingua);
		
		JPanel panel_27 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_27.getLayout();
		flowLayout_6.setAlignment(FlowLayout.LEFT);
		panel_27.setBorder(new TitledBorder(null, "Directory Download secondaria", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_27.setBounds(10, 248, 453, 55);
		panel_15.add(panel_27);
		
		dirDownload2 = new JTextField();
		panel_27.add(dirDownload2);
		dirDownload2.setColumns(30);
		
		btnSfogliaDirDownload2 = new JButton("Sfoglia");
		panel_27.add(btnSfogliaDirDownload2);
		
		JPanel panel_28 = new JPanel();
		panel_28.setBorder(new TitledBorder(null, "Spazio minimo su disco", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout_7 = (FlowLayout) panel_28.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEFT);
		panel_28.setBounds(10, 309, 453, 55);
		panel_15.add(panel_28);
		
		minFreeSpace = new JSpinner();
		minFreeSpace.setModel(new SpinnerNumberModel(512, 128, 16384, 64));
		panel_28.add(minFreeSpace);
		
		JPanel panel_6 = new JPanel();
		tabbedPane.addTab("SerieTV", null, panel_6, null);
		panel_6.setLayout(null);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBounds(12, 5, 451, 57);
		panel_11.setBorder(new TitledBorder(null, "Serie TV", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_6.add(panel_11);
		panel_11.setLayout(new GridLayout(1, 2, 0, 0));
		
		JPanel panel_12 = new JPanel();
		FlowLayout flowLayout_11 = (FlowLayout) panel_12.getLayout();
		flowLayout_11.setAlignment(FlowLayout.LEFT);
		panel_11.add(panel_12);
		
		chkboxDownloadAuto = new JCheckBox("Abilita download automatico");
		panel_12.add(chkboxDownloadAuto);
		
		JPanel panel_13 = new JPanel();
		FlowLayout flowLayout_12 = (FlowLayout) panel_13.getLayout();
		flowLayout_12.setAlignment(FlowLayout.LEFT);
		panel_11.add(panel_13);
		
		chkDownloadSubs = new JCheckBox("Abilita download sottotitoli");
		panel_13.add(chkDownloadSubs);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Italiansubs.net", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_1.setBounds(12, 70, 451, 90);
		panel_6.add(panel_1);
		panel_1.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2);
		
		JLabel label = new JLabel("Username");
		panel_2.add(label);
		
		itasaUsername = new JTextField();
		itasaUsername.setColumns(12);
		panel_2.add(itasaUsername);
		
		JLabel label_1 = new JLabel("Password");
		panel_2.add(label_1);
		
		itasaPassword = new JPasswordField();
		panel_2.add(itasaPassword);
		itasaPassword.setColumns(12);
		
		JPanel panel_14 = new JPanel();
		panel_1.add(panel_14);
		
		creaAccountItasa = new JButton("Crea account");
		panel_14.add(creaAccountItasa);
		
		verificaItasaButton = new JButton("Verifica");
		panel_14.add(verificaItasaButton);
		
		verificaItasaLabel = new JLabel("");
		panel_14.add(verificaItasaLabel);
		
		JPanel panel_29 = new JPanel();
		panel_29.setBounds(0, 405, 475, 35);
		panel_6.add(panel_29);
		
		btnSalva2 = new JButton("Salva");
		panel_29.add(btnSalva2);
		
		btnChiudi2 = new JButton("Chiudi");
		panel_29.add(btnChiudi2);
		
		JPanel panel_33 = new JPanel();
		panel_33.setBorder(new TitledBorder(null, "Podnapisi.net", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_33.setBounds(12, 165, 451, 90);
		panel_6.add(panel_33);
		panel_33.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel panel_35 = new JPanel();
		panel_33.add(panel_35);
		
		JLabel lblUsername = new JLabel("Username");
		panel_35.add(lblUsername);
		
		podnapisi_username = new JTextField();
		panel_35.add(podnapisi_username);
		podnapisi_username.setColumns(12);
		
		JLabel lblPassword = new JLabel("Password");
		panel_35.add(lblPassword);
		
		podnapisi_password = new JPasswordField();
		panel_35.add(podnapisi_password);
		podnapisi_password.setColumns(12);
		
		JPanel panel_36 = new JPanel();
		panel_33.add(panel_36);
		
		creaAccountPodnapisi = new JButton("Crea account");
		panel_36.add(creaAccountPodnapisi);
		
		JPanel panel_38 = new JPanel();
		panel_38.setBorder(new TitledBorder(null, "OpenSubtitles", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel_38.setBounds(12, 265, 451, 90);
		panel_6.add(panel_38);
		panel_38.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel panel_39 = new JPanel();
		panel_38.add(panel_39);
		
		JLabel lblUsername_2 = new JLabel("Username");
		panel_39.add(lblUsername_2);
		
		opensubtitles_username = new JTextField();
		panel_39.add(opensubtitles_username);
		opensubtitles_username.setColumns(12);
		
		JLabel lblPassword_2 = new JLabel("Password");
		panel_39.add(lblPassword_2);
		
		opensubtitles_password = new JPasswordField();
		opensubtitles_password.setColumns(12);
		panel_39.add(opensubtitles_password);
		
		JPanel panel_40 = new JPanel();
		panel_38.add(panel_40);
		
		btnCreaAccountOpenSubtitles = new JButton("Crea Account");
		panel_40.add(btnCreaAccountOpenSubtitles);
		
		JPanel panel_16 = new JPanel();
		tabbedPane.addTab("Client", null, panel_16, null);
		panel_16.setLayout(null);
		
		JPanel panel_17 = new JPanel();
		panel_17.setBounds(0, 405, 475, 35);
		panel_16.add(panel_17);
		
		btnSalva3 = new JButton("Salva");
		panel_17.add(btnSalva3);
		
		btnChiudi3 = new JButton("Chiudi");
		panel_17.add(btnChiudi3);
		
		JPanel panel_21 = new JPanel();
		panel_21.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "uTorrent", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_21.setBounds(12, 12, 451, 190);
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
		panel_22.setLayout(new GridLayout(3, 2, 0, 0));
		
		JPanel panel_23 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_23.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_23);
		
		JLabel lblUsername_1 = new JLabel("Username");
		panel_23.add(lblUsername_1);
		
		JPanel panel_24 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_24.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_24);
		
		utorrentUser = new JTextField();
		panel_24.add(utorrentUser);
		utorrentUser.setColumns(15);
		
		JPanel panel_25 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_25.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_25);
		
		JLabel lblPassword_1 = new JLabel("Password");
		panel_25.add(lblPassword_1);
		
		JPanel panel_30 = new JPanel();
		FlowLayout flowLayout_14 = (FlowLayout) panel_30.getLayout();
		flowLayout_14.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_30);
		
		utorrentPassword = new JPasswordField();
		panel_30.add(utorrentPassword);
		utorrentPassword.setColumns(15);
		
		JPanel panel_31 = new JPanel();
		FlowLayout flowLayout_13 = (FlowLayout) panel_31.getLayout();
		flowLayout_13.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_31);
		
		JLabel lblPorta = new JLabel("Porta       ");
		panel_31.add(lblPorta);
		
		JPanel panel_32 = new JPanel();
		FlowLayout flowLayout_15 = (FlowLayout) panel_32.getLayout();
		flowLayout_15.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_32);
		
		utorrentPorta = new JTextField();
		panel_32.add(utorrentPorta);
		utorrentPorta.setColumns(5);
		
		JPanel panel_26 = new JPanel();
		panel_26.setBorder(new TitledBorder(null, "qBittorrent", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_26.setBounds(10, 215, 453, 85);
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
		btnSalva.addActionListener(getActionListenerSalva());
		btnSalva2.addActionListener(getActionListenerSalva());
		btnSalva3.addActionListener(getActionListenerSalva());
		
		btnChiudi.addActionListener(getActionListenerChiudi());
		btnChiudi2.addActionListener(getActionListenerChiudi());
		btnChiudi3.addActionListener(getActionListenerChiudi());
		
		creaAccountPodnapisi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					OperazioniFile.esploraWeb("https://uid.si/signup/");
				}
				catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(FrameOpzioni.this, "Visita https://uid.si/signup per creare un'account");
				}
			}
		});
		creaAccountItasa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					OperazioniFile.esploraWeb("http://www.italiansubs.net/forum/index.php?action=register");
				}
				catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(FrameOpzioni.this, "Visita http://www.italiansubs.net per creare un'account");
				}
			}
		});
		verificaItasaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = itasaUsername.getText().trim();
				if(username.isEmpty()){
					verificaItasaLabel.setText("Username vuoto");
					return;
				}
				String pass = new String(itasaPassword.getPassword());
				if(pass.isEmpty()){
					verificaItasaLabel.setText("Password vuota");
					return;
				}
				boolean login=ItalianSubs.VerificaLogin(username, pass);
				String text = login?
						"<html><font color='green'>Login OK!</font></html>":
						"<html><font color='red'>Login fallito!</font></html>";
				verificaItasaLabel.setText(text);
			}
		});
		
		btnCreaAccountOpenSubtitles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					OperazioniFile.esploraWeb("http://www.opensubtitles.org/newuser");
				}
				catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(FrameOpzioni.this, "Visita http://www.opensubtitles.org/newuser per creare un'account");
				}
			}
		});
		
		btnSfogliaDirDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String destinazione_path = s.getDirectoryDownload();
				JFileChooser chooser = new JFileChooser(destinazione_path.isEmpty() ? null : destinazione_path);
				chooser.setDialogTitle("Seleziona cartella download principale");
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
		btnSfogliaDirDownload2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String destinazione_path = s.getDirectoryDownload2();
				JFileChooser chooser = new JFileChooser(destinazione_path.isEmpty() ? null : destinazione_path);
				chooser.setDialogTitle("Seleziona cartella download alternativa");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileHidingEnabled(false);

				if (chooser.showOpenDialog(FrameOpzioni.this) == JFileChooser.APPROVE_OPTION) {
					destinazione_path = chooser.getSelectedFile().getAbsolutePath();
				}
				else {
					return;
				}
				dirDownload2.setText(destinazione_path);
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
		initLingua();
		chkboxDownloadAuto.setSelected(s.isDownloadAutomatico());
		chkDownloadSubs.setSelected(s.isRicercaSottotitoli());
		chckbxAskOnClose.setSelected(s.isAskOnClose());
		chkbAutostart.setSelected(s.isAutostart());
		chkbStartHidden.setSelected(s.isStartHidden());
		dirDownload.setText(s.getDirectoryDownload());
		dirDownload2.setText(s.getDirectoryDownload2());
		minFreeSpace.setValue(s.getMinFreeSpace());
		dirQbittorrent.setText(s.getQBittorrentPath());
		dirUtorrent.setText(s.getUTorrentPath());
		itasaUsername.setText(s.getItasaUsername());
		itasaPassword.setText(s.getItasaPassword());
		verificaItasaLabel.setText("");
		podnapisi_username.setText(s.getPodnapisiUsername());
		podnapisi_password.setText(s.getPodnapisiPassword());
		opensubtitles_username.setText(s.getOpenSubtitlesUsername());
		opensubtitles_password.setText(s.getOpenSubtitlesPassword());
		utorrentUser.setText(s.getUTorrentUsername());
		utorrentPassword.setText(s.getUTorrentPassword());
		utorrentPorta.setText(s.getUTorrentPort());
	}
	private ActionListener al_chiudi;
	private ActionListener al_salva;
	private JTextField podnapisi_username;
	private JPasswordField podnapisi_password;
	private JButton creaAccountItasa;
	private JButton creaAccountPodnapisi;
	private JTextField opensubtitles_username;
	private JPasswordField opensubtitles_password;
	private JButton btnCreaAccountOpenSubtitles;
	private JButton verificaItasaButton;
	private JLabel verificaItasaLabel;
	
	private ActionListener getActionListenerChiudi(){
		if(al_chiudi==null){
			al_chiudi = new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					FrameOpzioni.this.setVisible(false);
				}
			};
		}
		return al_chiudi;
	}
	private ActionListener getActionListenerSalva(){
		if(al_salva==null){
			al_salva = new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean askClose=chckbxAskOnClose.isSelected();
					boolean subDown = chkDownloadSubs.isSelected();
					boolean hidden=chkbStartHidden.isSelected();
					boolean autodown=chkboxDownloadAuto.isSelected();
					boolean autostart = chkbAutostart.isSelected();
					String dirDown = dirDownload.getText().trim();
					String dirDown2 = dirDownload2.getText().trim();
					String fs = minFreeSpace.getValue()+"";
					int freespace = 0;
					try {
						freespace = Integer.parseInt(fs);
					}
					catch(Exception e){
						freespace = 512;
					}
					String usernameItasa=itasaUsername.getText();
					String passItasa=new String(itasaPassword.getPassword());
					String usernamePodnapisi=podnapisi_username.getText();
					String passPodnapisi=new String(podnapisi_password.getPassword());
					String lang = ((Lingua)lingua.getSelectedItem()).getValue();
					String usernameOpenSubs = opensubtitles_username.getText();
					String passOpenSubs = new String(opensubtitles_password.getPassword());
						
					s.setAskOnClose(askClose);
					s.setRicercaSottotitoli(subDown,true);
					s.setStartHidden(hidden);
					s.setDownloadAutomatico(autodown);
					s.setAutostart(autostart);
					s.setDirectoryDownload(dirDown);
					s.setDirectoryDownload2(dirDown2);
					s.setMinFreeSpace(freespace);
					s.setItasaUsername(usernameItasa);
					s.setItasaPassword(passItasa);
					s.setPodnapisiUsername(usernamePodnapisi);
					s.setPodnapisiPassword(passPodnapisi);
					s.setOpenSubtitlesUsername(usernameOpenSubs);
					s.setOpenSubtitlesPassword(passOpenSubs);
					s.setLingua(lang);
					
					String ut=dirUtorrent.getText().trim();
					String qbit=dirQbittorrent.getText().trim();
					String ut_user=utorrentUser.getText().trim();
					String ut_pass=new String(utorrentPassword.getPassword()); 
					String ut_port=utorrentPorta.getText();
					s.setUTorrentPath(ut);
					s.setQBittorrentPath(qbit);
					s.setUTorrentUsername(ut_user);
					s.setUTorrentPassword(ut_pass);
					s.setUTorrentPort(ut_port);
					
					s.salvaSettings();
					init();
				}
			};
		}
		return al_salva;
	}
	private void initLingua(){
		String lang = s.getLingua();
		for(int i=0;i<lingua.getItemCount();i++){
			if(lingua.getItemAt(i).getValue().compareTo(lang)==0){
				lingua.setSelectedIndex(i);
				return;
			}
		}
	}
}
