package gst.download;

import java.io.File;
import java.io.IOException;

import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{
	private String pathInstallazione;
	
	public UTorrent() {}
	public UTorrent(String path) {
		pathInstallazione=path;
	}
	
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
		if(haveWebAPI() && isWebAPIEnabled()){
			return false;
		}
		else {
			return downloadCLI(t, path);
		}
	}
	
	public void setPathInstallazione(String p){
		pathInstallazione=p;
	}
	public String getPathInstallazione(){
		return pathInstallazione;
	}
	
	public boolean downloadCLI(Torrent t, String path){
		String[] cmd={
				getPathInstallazione(),
				"/NOINSTALL",
				"/DIRECTORY",
				("\"" + path + "\""),
				t.getUrl()
		};
		
		for(int i=0;i<cmd.length;i++)
			System.out.print(cmd[i]+" ");
		
		try {
			Process p = Runtime.getRuntime().exec(cmd);
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
}
