package gst.system;

import gst.download.Download;
import gst.programma.OperazioniFile;
import gst.programma.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import server.ServerStart;
import util.MD5Hash;
import util.os.Os;

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
		if(setts.getClientID().isEmpty() || !setts.getClientID().startsWith("gst")){
			assignClientID();
			setts.salvaSettings();
		}
		aggiornaLauncher();
	}
	
	public boolean isUpdateAvailable(){
		Download downloader=new Download("http://gestioneserietv.altervista.org/checkVersion.php?version="+setts.getVersioneSoftware()+"&id_client="+setts.getClientID(), setts.getUserDir()+"version");
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
	public void assignClientID(){
		try {
			Download.downloadFromUrl("http://gestioneserietv.altervista.org/assignClientID.php", setts.getUserDir()+"clientID");
			FileReader f=new FileReader(setts.getUserDir()+"clientID");
			Scanner file=new Scanner(f);
			String id ="";
			if(file.hasNext()){
				id=file.next();
				setts.setClientID(id);
			}
			file.close();
			f.close();
			OperazioniFile.deleteFile(setts.getUserDir()+"clientID");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void aggiornaLauncher(){
		if(Os.isWindows()){
			String pathLauncher = setts.getCurrentDir()+File.separator+"gstLauncher.exe";
			String md5 = MD5Hash.hashFile(pathLauncher);
			if(!OperazioniFile.fileExists(pathLauncher) || md5==null || !verificaHashLauncher(md5)){
				try {
					Download.downloadFromUrl("http://gestioneserietv.altervista.org/gstLauncher.exe", pathLauncher+".new");
					OperazioniFile.copyfile(pathLauncher+".new", pathLauncher);
				}
				catch (IOException e) {
					e.printStackTrace();
					OperazioniFile.deleteFile(pathLauncher+".new");
				}
			}
		}
		else {
			String pathLauncher = setts.getCurrentDir()+File.separator+"gstLauncher.jar";
			String md5 = MD5Hash.hashFile(pathLauncher);
			if(!OperazioniFile.fileExists(pathLauncher) || md5==null || !verificaHashLauncher(md5)){
				try {
					Download.downloadFromUrl("http://gestioneserietv.altervista.org/gstLauncher.jar", pathLauncher+".new");
					OperazioniFile.copyfile(pathLauncher+".new", pathLauncher);
				}
				catch (IOException e) {
					e.printStackTrace();
					OperazioniFile.deleteFile(pathLauncher+".new");
				}
			}
		}
	}
	private boolean verificaHashLauncher(String md5) {
		try {
			Download.downloadFromUrl("http://gestioneserietv.altervista.org/verificaHashLauncher.php", setts.getUserDir()+"hashLauncher");
			FileReader f=new FileReader(setts.getUserDir()+"hashLauncher");
			Scanner file=new Scanner(f);
			boolean hashOK = true;
			if(file.hasNextBoolean()){
				hashOK=file.nextBoolean();
			}
			file.close();
			f.close();
			OperazioniFile.deleteFile(setts.getUserDir()+"hashLauncher");
			return hashOK;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void aggiorna() {
		try {
			avviaLauncher();
			ServerStart.close();
			System.exit(0);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void avviaLauncher() throws IOException {
		aggiornaLauncher();
		if(Os.isWindows()){
			String[] cmd = {setts.getCurrentDir()+File.separator+"gstLauncher.exe", "5"};
			Runtime.getRuntime().exec(cmd);
		}
		else {
			String[] cmd = {"java","-jar", setts.getCurrentDir()+File.separator+"gstLauncher.jar", "5"};
			Runtime.getRuntime().exec(cmd);
		}
	}
}
