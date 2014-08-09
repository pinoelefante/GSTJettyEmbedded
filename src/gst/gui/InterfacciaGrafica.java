package gst.gui;

import gst.interfacce.Notificable;
import gst.programma.Settings;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class InterfacciaGrafica implements Notificable {
	private static InterfacciaGrafica sing;
	
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
		MenuItem exitItem = new MenuItem("Chiudi");

		popup.add(restoreWin);
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
				Desktop desk = Desktop.getDesktop();
				try {
					desk.browse(new URI("http://localhost:8585"));
				}
				catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Settings.getInstance().isAskOnClose()){
					if(JOptionPane.showConfirmDialog(null, "Vuoi veramente chiudere Gestione Serie TV?")==JOptionPane.YES_OPTION)
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
}
