package gst.download;

import java.io.File;
import java.io.IOException;

import util.httpOperations.HttpOperations;
import util.os.Os;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{
	private String pathEseguibile;
	private String address, port,authToken, utorrent_user, utorrent_pass;
	
	public UTorrent() {}
	public UTorrent(String path) {
		pathEseguibile=path;
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
		String cmd =  "http://"+address+":"+port+"/gui/?action=setsetting&&s=dir_active_download_flag&v=1&s=dir_active_download&v="+dir+"&token="+authToken;
		try {
			boolean resp = HttpOperations.GET_withBoolean(cmd);
			return resp;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public synchronized boolean downloadTorrent(Torrent t, String path) {
		if(haveWebAPI() && isWebAPIEnabled()){
			return downloadWebUI(t, path);
		}
		else {
			return downloadCLI(t, path);
		}
	}
	
	public void setPathInstallazione(String p){
		pathEseguibile=p;
	}
	public String getPathInstallazione(){
		return pathEseguibile;
	}
	
	public boolean downloadCLI(Torrent t, String path){
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
	public boolean downloadWebUI(Torrent t, String path){
		if(setDirectoryDownload(path)){
    		String cmd="http://"+address+":"+port+"/gui/?action=add-url&s="+t.getUrl()+"&token="+authToken;
    		
    		try {
    			boolean b=HttpOperations.GET_withBoolean_AuthBasic(address, port, utorrent_user, utorrent_pass, cmd);
    			return b;
    		}
    		catch (Exception e) {
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
			path = Settings.getInstance().getCurrentDir() + File.separator + "uTorrent.exe";
			if(OperazioniFile.fileExists(path))
				return path;
		}
		else if(Os.isMacOS()){
			if(OperazioniFile.fileExists("/Applications/uTorrent.app/Contents/MacOS/uTorrent"))
				return "/Applications/uTorrent.app/Contents/MacOS/uTorrent";
		}
		return null;
	}
}
