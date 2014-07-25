package gst.programma;

import java.awt.GridLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import GUI.PanelFileCopy;

public class FileManager {
	private static JPanel panel_download;
	private static JScrollPane scroll;
	private static int downloading_files;
	
	
	public static void instance(){
		panel_download=new JPanel();
		panel_download.setLayout(new GridLayout(4, 1));
		scroll=new JScrollPane(panel_download);
		
		addListener();
	}
	
	private static void addListener() {
		panel_download.addContainerListener(new ContainerListener() {
			public void componentRemoved(ContainerEvent arg0) {
				GridLayout lay=(GridLayout) panel_download.getLayout();
				
				if(panel_download.getComponentCount()>4)
					lay.setRows(lay.getRows()-1);
				else
					lay.setRows(4);
				
				panel_download.revalidate();
				panel_download.repaint();
				
			}
			
			public void componentAdded(ContainerEvent arg0) {
				GridLayout lay=(GridLayout) panel_download.getLayout();
				
				if(panel_download.getComponentCount()>4)
					lay.setRows(lay.getRows()+1);
				else
					lay.setRows(4);
				
				panel_download.revalidate();
				panel_download.repaint();
				
			}
		});
	}

	public static void addDownloadFile(Download d){
		try {
			PanelFileCopy p=new PanelFileCopy(d);
			panel_download.add(p);
		}
		catch(NullPointerException e){
			instance();
			addDownloadFile(d);
		}
	}
	public static JComponent getPanel(){
		return scroll;
	}
	public static void removePanel(JPanel p){
		panel_download.remove(p);
	}
	public static synchronized boolean isAnotherCopyNow(){
		System.out.println("Download in corso:"+downloading_files);
		if(downloading_files>0)
			return true;
		return false;
	}
	public static synchronized void downloadStarted(){
		downloading_files++;
	}
	public static synchronized void downloadStopped(){
		downloading_files--;
	}
}
