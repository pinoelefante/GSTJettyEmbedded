package gst.manutenzione;

import gst.programma.ManagerException;
import gst.programma.Settings;

import javax.swing.JFrame;

import java.awt.GridLayout;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.border.EtchedBorder;

import gst.database.Database;

public class InterfacciaManutenzione extends JFrame{
	public static void main(String[] args){
		Settings.baseSettings();
		Settings.CaricaSetting();
		JFrame gui=new InterfacciaManutenzione();
		gui.setVisible(true);
	}
	private static final long serialVersionUID = 1L;
	private JButton btnEsportaDatabase;
	private JButton btnImportaDaSql;
	private JButton btnImportaDaDatabase;
	private JButton btnCancellaTutto;

	public InterfacciaManutenzione(){
		super("Gestione Serie TV - Manutenzione");
		setAlwaysOnTop(true);
		setSize(500, 350);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblEsportaDatabaseIn = new JLabel("Esporta database in file SQL");
		lblEsportaDatabaseIn.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblEsportaDatabaseIn, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		
		btnEsportaDatabase = new JButton("Esporta Database");
		panel_1.add(btnEsportaDatabase);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblImportaDatabase = new JLabel("Importa Database");
		lblImportaDatabase.setHorizontalAlignment(SwingConstants.CENTER);
		panel_2.add(lblImportaDatabase, BorderLayout.NORTH);
		
		JLabel lblAttenzioneVerrannoCancellati = new JLabel("  ATTENZIONE: verranno cancellati i dati esistenti");
		lblAttenzioneVerrannoCancellati.setFont(new Font("Tahoma", Font.BOLD, 11));
		panel_2.add(lblAttenzioneVerrannoCancellati, BorderLayout.SOUTH);
		
		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.CENTER);
		
		btnImportaDaSql = new JButton("Importa da SQL");
		panel_3.add(btnImportaDaSql);
		
		btnImportaDaDatabase = new JButton("Importa da database SQLite");
		panel_3.add(btnImportaDaDatabase);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JLabel lblCancellaContenuto = new JLabel("Cancella contenuto");
		lblCancellaContenuto.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(lblCancellaContenuto, BorderLayout.NORTH);
		
		JLabel lblAttenzioneSiConsiglia = new JLabel("  ATTENZIONE: si consiglia di effettuare un backup prima di procedere");
		lblAttenzioneSiConsiglia.setFont(new Font("Tahoma", Font.BOLD, 11));
		panel_4.add(lblAttenzioneSiConsiglia, BorderLayout.SOUTH);
		
		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5, BorderLayout.CENTER);
		
		btnCancellaTutto = new JButton("Cancella tutto");
		panel_5.add(btnCancellaTutto);
		
		addListener();
	}

	private void addListener() {
		btnEsportaDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(Manutenzione.esportaDBinSQL(Database.Connect(), Settings.getUserDir())){
					JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Esportazione effettuata.\nFile: "+Settings.getUserDir()+"gst_db_backup.sql");
				}
				else {
					JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Si è verificato un errore durante il salvataggio");
				}
			}
		});
		btnImportaDaSql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser(Settings.getUserDir());
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle("Seleziona file SQL da importare");
				fc.setFileHidingEnabled(false);
				if(fc.showOpenDialog(InterfacciaManutenzione.this)==JFileChooser.APPROVE_OPTION){
					String dir=fc.getSelectedFile().getAbsolutePath();
					if(Manutenzione.importaDBdaSQL(Database.Connect(), dir/*Settings.getUserDir()+"gst_db_backup.sql"*/)){
						JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Importazione completata con successo");
					}
					else {
						JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Importazione completata con degli errori o il file non è un database valido");
					}
				}
			}
		});
		btnImportaDaDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser(Settings.getUserDir());
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle("Seleziona database da importare");
				fc.setFileHidingEnabled(false);
				if(fc.showOpenDialog(InterfacciaManutenzione.this)==JFileChooser.APPROVE_OPTION){
					String dir=fc.getSelectedFile().getAbsolutePath();
					if(Manutenzione.importaDBdaSQLite(dir, Database.Connect())){
						JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Importazione completata con successo");
					}
					else {
						JOptionPane.showMessageDialog(InterfacciaManutenzione.this, "Importazione completata con degli errori o il file non è un database valido");
					}
				}
			}
		});
		btnCancellaTutto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int conferma=JOptionPane.showConfirmDialog(InterfacciaManutenzione.this, "Confermi l'eliminazione del contenuto del database?","Conferma eliminazione",JOptionPane.YES_NO_OPTION);
				if(conferma==JOptionPane.YES_OPTION)
					Manutenzione.truncateAll(Database.Connect());
			}
		});
		addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				Database.Disconnect();
				try {
					String exe=Settings.getEXEName();
					System.out.println(exe);
					if(exe.toLowerCase().endsWith("manutenzione")){
						exe=exe.replace("manutenzione", "").trim();
					}
					if(exe.toLowerCase().endsWith(".jar")){
						System.out.println("avvio jar");
						String[] command={System.getProperty("java.home")+File.separator+"bin"+File.separator+"java",
								"-jar",
								"\""+exe+"\""};
						Runtime.getRuntime().exec(command);
					}
					else{
						System.out.println("avvio exe");
						String[] command={"\""+exe+"\""};
						Runtime.getRuntime().exec(command);
					}
				} 
				catch (IOException e) {
					ManagerException.registraEccezione(e);
					e.printStackTrace();
				}
			}
			public void windowClosed(WindowEvent arg0) {}
			public void windowActivated(WindowEvent arg0) {}
		});
	}
}
