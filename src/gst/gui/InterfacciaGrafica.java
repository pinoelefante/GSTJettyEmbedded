package gst.gui;

import gst.interfacce.Notificable;
import gst.programma.Settings;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class InterfacciaGrafica implements Notificable {
	private static InterfacciaGrafica sing;
	private JFrame frameOpzioni;
	
	public static InterfacciaGrafica getInstance(){
		if(sing==null)
			sing=new InterfacciaGrafica();
		return sing;
	}
	
	private InterfacciaGrafica(){
		setTray();
	}
	
	private SystemTray tray;

	public void removeTray() {
		TrayIcon[] ic = tray.getTrayIcons();
		if (ic.length > 0)
			tray.remove(ic[0]);
	}

	public void setTray() {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("icon.png", "Gestione Serie TV rel." + Settings.getInstance().getVersioneSoftware() + " by pinoelefante"), "Gestione Serie TV rel." + Settings.getInstance().getVersioneSoftware() + " by pinoelefante");

		tray = SystemTray.getSystemTray();
		final MenuItem restoreWin = new MenuItem("Apri");
		restoreWin.setFont(new Font(null, Font.BOLD, 14));
		MenuItem opzioni = new MenuItem("Opzioni");
		MenuItem exitItem = new MenuItem("Chiudi");

		popup.add(restoreWin);
		popup.add(opzioni);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		try {
			tray.add(trayIcon);
		}
		catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreWin.getActionListeners()[0].actionPerformed(new ActionEvent(trayIcon, 0, null));
			}
		});
		restoreWin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				apriInterfaccia();
			}
		});
		opzioni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraFinestraOpzioni();
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Settings.getInstance().isAskOnClose()){
					if(showConfirmDialog("Conferma chiusura", "Vuoi veramente chiudere Gestione Serie TV?"))
						System.exit(0);
				}
				else
					System.exit(0);
			}
		});
	}
	public static Image createImage(String path, String description) {
		URL imageURL = InterfacciaGrafica.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		}
		return new ImageIcon(imageURL, description).getImage();
	}

	@Override
	public void sendNotify(String text) {
		if(tray != null)
			tray.getTrayIcons()[0].displayMessage("Gestione Serie TV", text, MessageType.INFO);
	}
	public void mostraFinestraOpzioni(){
		if(frameOpzioni==null)
			frameOpzioni=new FrameOpzioni();
		centraFinestra(Toolkit.getDefaultToolkit().getScreenSize(), frameOpzioni);
		frameOpzioni.setVisible(true);
	}
	public boolean showConfirmDialog(String titolo, String text){
		if(JOptionPane.showConfirmDialog(null, text, titolo, JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
			return true;
		return false;
	}
	public void apriInterfaccia(){
		if(Desktop.isDesktopSupported()){
			Desktop d = Desktop.getDesktop();
			try {
				d.browse(new URI("http://localhost:8585"));
			}
			catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else {
			sendNotify("Per aprire l'interfaccia di Gestione Serie TV, visita l'indirizzo 'http://localhost:8585' nel tuo browser web");
		}
	}
	public void showMessageDialog(String text){
		JOptionPane.showMessageDialog(null, text);
	}
	private void centraFinestra(Dimension risoluzione, Frame finestra){
		int x=risoluzione.width/2-finestra.getSize().width/2;
		int y=risoluzione.height/2-finestra.getSize().width/2;
		finestra.setLocation(x, y);
	}
}
