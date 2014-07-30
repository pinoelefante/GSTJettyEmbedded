package gst.download;

import java.io.File;

import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{

	@Override
	public boolean haveWebAPI() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWebAPIEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String auth(String username, String pass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setDirectoryDownload(String dir) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean downloadTorrent(Torrent t, String path) {
		// TODO Auto-generated method stub
		return false;
	}
	public static String rilevaInstallazione(){
		String path = null;
		if(Settings.isWindows()){
			path = System.getenv("APPDATA")+File.separator+"uTorrent"+File.separator+"uTorrent.exe";
			if(OperazioniFile.fileExists(path))
				return path;
			if(Settings.is32bit()){
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
			path = Settings.getCurrentDir() + File.separator + "uTorrent.exe";
		}
		return null;
	}
	public static void main(String[] args){
		Settings.baseSettings();
		String p = rilevaInstallazione();
		System.out.println(p==null?"Percorso non trovato":p);
	}

}
