package gst.download;

import java.io.File;
import java.io.IOException;

import util.os.Os;
import gst.programma.OperazioniFile;
import gst.serieTV.Torrent;

public class UTorrent {
	private static UTorrent instance;
	
	public synchronized static UTorrent getInstance()
	{
		if(instance == null)
			instance = new UTorrent();
		return instance;
	}
	
	private String pathEseguibile;
	
	private UTorrent() {}
	
	public synchronized boolean downloadTorrent(Torrent t, String path) {
		System.out.println("download torrent "+t.getUrl());
		
		return downloadCLI(t, path);
	}
	
	public void setPathInstallazione(String p){
		pathEseguibile=p;
	}
	public String getPathInstallazione(){
		return pathEseguibile;
	}
	private static long nextTorrentCLI = 0L;
	public synchronized boolean downloadCLI(Torrent t, String path){
		while(System.currentTimeMillis()<=nextTorrentCLI){
			try {
				Thread.sleep(100L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(Os.isWindows()){
			String[] cmd={
					getPathInstallazione(),
					"/NOINSTALL",
					"/DIRECTORY",
					("\"" + path + "\""),
					t.getUrl()
			};
			
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				nextTorrentCLI = System.currentTimeMillis()+250;
				if(p==null){
					return false;
				}
				return true;
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public static String rilevaInstallazione(){
		String path = null;
		if(Os.isWindows()){
			path = System.getenv("APPDATA")+File.separator+"uTorrent"+File.separator+"uTorrent.exe";
			if(OperazioniFile.fileExists(path))
				return path;
			if(Os.is32bit()){
				path = System.getenv("PROGRAMFILES")+File.separator+"uTorrent"+File.separator+"uTorrent.exe";
				if(OperazioniFile.fileExists(path))
					return path;
			}
			else {
				path = System.getenv("PROGRAMFILES")+File.separator+"uTorrent"+File.separator+"uTorrent.exe";
				if(OperazioniFile.fileExists(path))
					return path;
				path = System.getenv("PROGRAMFILES(x86)")+File.separator+"uTorrent"+File.separator+"uTorrent.exe";
				if(OperazioniFile.fileExists(path))
					return path;
			}
//			path = Settings.getInstance().getCurrentDir() + File.separator + "uTorrent.exe";
//			if(OperazioniFile.fileExists(path))
//				return path;
		}
		return null;
	}
}
