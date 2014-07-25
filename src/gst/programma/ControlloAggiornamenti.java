package gst.programma;

import gst.database.Database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class ControlloAggiornamenti {
	private final static String URL_DB="version.dat";
	private final static String URL_PROGRAMMA="GestioneSerieTV5.exe";
	private final static String URL_PROGRAMMA_JAR="st.jar";
	private final static String URL_BASE="http://pinoelefante.altervista.org/software/GST2/"; 
	private int versione_online;
	
	private void retrieveVersioneOnline(){
		Download downloader=new Download(URL_BASE+URL_DB, Settings.getUserDir()+"version.dat");
		downloader.avviaDownload();
		try {
			downloader.getDownloadThread().join();
			FileReader f=new FileReader(Settings.getUserDir()+"version.dat");
			Scanner file=new Scanner(f);
			if(file.hasNextInt()){
				versione_online=file.nextInt();
			}
			file.close();
			f.close();
			OperazioniFile.deleteFile(Settings.getUserDir()+"version.dat");
		} 
		catch (InterruptedException e) {
			return;
		} 
		catch (FileNotFoundException e) {
			versione_online=Settings.getVersioneSoftware();
		} 
		catch (IOException e) {	}
	}
	public int getVersioneOnline(){
		retrieveVersioneOnline();
		return versione_online;
	}
	public boolean scarica(){
		String path_download=URL_BASE;
		if(Settings.isWindows()){
			path_download+=URL_PROGRAMMA;
		}
		else {
			path_download+=URL_PROGRAMMA_JAR;
		}
		Download d=new Download(path_download, Settings.getCurrentDir()+"newupdate.gst");
		d.avviaDownload();
		try {
			d.getDownloadThread().join();
		}
		catch (InterruptedException e) {
			return false;
		}
		if(d.getFileSizeDowloaded()<d.getFileSize())
			return false;
		else {
			if(OperazioniFile.fileExists(Settings.getCurrentDir()+"newupdate.gst"))
				return true;
			else
				return false;
		}
	}
	public void update() {
		System.out.println("Current:"+Settings.getVersioneSoftware()+"\nOnline:"+getVersioneOnline());
		if(getVersioneOnline()>Settings.getVersioneSoftware()){
    		if(scarica()) {
    			String[] cmd={
    					System.getProperty("java.home")+File.separator+"bin"+File.separator+"java"+(Settings.isWindows()?".exe":""), 
    					"-jar", 
    					Settings.getCurrentDir()+"gst_updater.jar",
    					Settings.getEXEName(),
    					Settings.isWindows()?"windows":"other"
    			};
    			try {
    				OperazioniFile.deleteFile(Settings.getUserDir()+"eccezioni.txt");
    				gst.manutenzione.Manutenzione.esportaDBinSQL(Database.Connect(), Settings.getUserDir()+File.separator+"backup");
    				Runtime.getRuntime().exec(cmd);
    				System.exit(0);
    			}
    			catch (IOException e) {
    				e.printStackTrace();
    				ManagerException.registraEccezione(e);
    			}
    		}
		}
	}
}
