package gst.download;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import util.httpOperations.HttpOperations;
import util.os.Os;
import util.os.ProcessFinder;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{
	private String pathEseguibile;
	private String address="localhost", port, utorrent_user, utorrent_pass;
	private UTorrentAPI api;
	
	public UTorrent() {}
	public UTorrent(String path) {
		pathEseguibile=path;
	}
	public void setUsername(String u){
		utorrent_user=u;
	}
	public void setPassword(String p){
		utorrent_pass=p;
	}
	public void setPort(String p) {
		port = p;
	}
	
	@Override
	public boolean haveWebAPI() {
		return true;
	}

	@Override
	public boolean isWebAPIEnabled() {
		return HttpOperations.isOnline(address, port);
	}
	
	@Override
	public String auth(String username, String pass) {
		if(api==null){
			InetSocketAddress a = new InetSocketAddress(address, Integer.parseInt(port));
			api = new UTorrentAPI(a, utorrent_user, utorrent_pass);
		}
		return null;
	}

	@Override
	public boolean setDirectoryDownload(String dir) {
		String resp = api.get("action=setsetting&&s=dir_active_download_flag&v=1&s=dir_active_download&v="+dir);
		return resp!=null;
	}

	@Override
	public synchronized boolean downloadTorrent(Torrent t, String path) {
		if(haveWebAPI()){
			boolean d = downloadWebUI(t, path);
			if(d==false && Os.isWindows()){
				return downloadCLI(t, path);
			}
			return false;
		}
		else {
			if(Os.isWindows())
				return downloadCLI(t, path);
		}
		return false;
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
		if (!isRunning()){
			avviaClient();
			int retry = 0;
			while(retry <= 5 && isWebAPIEnabled()){
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				retry++;
			}
		}
		if(setDirectoryDownload(path)){
    		String cmd="action=add-url&s="+t.getUrl();
    		return api.get(cmd)!=null;
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
	public boolean isRunning(){
		return getUTorrentPid()>0;
	}
	private int getUTorrentPid(){
		String processName=null;
		if(Os.isWindows())
			processName="utorrent.exe";
		else if(Os.isLinux())
			processName="utserver";
		else if(Os.isMacOS())
			processName="uTorrent";
		return ProcessFinder.getPid(processName);
	}
	private void avviaClient(){
		if(Os.isWindows() || Os.isLinux()){
			String[] cmd = {
				pathEseguibile	
			};
			try {
				Runtime.getRuntime().exec(cmd);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			String[] cmd = {
					"open",
					"-a",
					"/Applications/uTorrent.app"
			};
			try {
				Runtime.getRuntime().exec(cmd);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args){
		UTorrent u=new UTorrent(UTorrent.rilevaInstallazione());
		u.setUsername("admin");
		u.setPassword("admin");
		u.setPort("8080");
		u.auth(null, null);
		System.out.println(u.api.get("action=add-url&s=magnet:?xt=urn:btih:OABX6EEMFEW247HD3R6SWUEFRECOFTBU&dn=Married.S01E05.HDTV.x264-KILLERS&tr=udp://tracker.openbittorrent.com:80&tr=udp://tracker.publicbt.com:80&tr=udp://tracker.istole.it:80&tr=udp://open.demonii.com:80&tr=udp://tracker.coppersurfer.tk:80"));
	}
}
