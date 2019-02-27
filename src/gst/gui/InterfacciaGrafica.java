package gst.gui;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;

public class InterfacciaGrafica {
	private static InterfacciaGrafica singleton = new InterfacciaGrafica();
	
	private InterfacciaGrafica(){
		setTray();
	}
	
	public static InterfacciaGrafica getInstance()
	{
		return singleton;
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
		final TrayIcon trayIcon = new TrayIcon(createImage("icon.png", "Gestione Serie TV"), "Gestione Serie TV");

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
				apriOpzioni();
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
	}
	public void apriOpzioni()
	{
		if(Desktop.isDesktopSupported()){
			Desktop d = Desktop.getDesktop();
			try {
				d.browse(new URI("http://localhost:8585/settings"));
			}
			catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean openFolder(String string) {
		File dir=new File(string);
		if(dir.isDirectory()){
			Desktop d=Desktop.getDesktop();
			try {
				d.open(dir);
				return true;
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
}
