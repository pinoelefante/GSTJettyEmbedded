package gst.system;

import gst.download.Download;
import gst.programma.OperazioniFile;
import gst.programma.Settings;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Sistema {
	private static Sistema instance;
	private Settings setts;
	
	public static Sistema getInstance(){
		if(instance==null)
			instance = new Sistema();
		return instance;
	}
	private Sistema(){
		setts = Settings.getInstance();
	}
	
	public boolean isUpdateAvailable(){
		Download downloader=new Download("http://gestioneserietv.altervista.org/checkVersion.php?version="+setts.getVersioneSoftware()+"&id_client=null", setts.getUserDir()+"version");
		downloader.avviaDownload();
		try {
			downloader.getDownloadThread().join();
			FileReader f=new FileReader(setts.getUserDir()+"version");
			Scanner file=new Scanner(f);
			boolean resp = false;
			if(file.hasNextBoolean()){
				resp=file.nextBoolean();
			}
			file.close();
			f.close();
			OperazioniFile.deleteFile(setts.getUserDir()+"version");
			return resp;
		} 
		catch (IOException e) {}
		catch (InterruptedException e) {}
		
		return false;
	}
}
