package gst.download;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import util.httpOperations.HttpOperations;
import util.os.Os;
import util.os.ProcessFinder;
import gst.download.bencode.BencodingInputStream;
import gst.download.bencode.BencodingOutputStream;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{
	private String pathEseguibile;
	private String address="localhost", port="8080", utorrent_user="admin", utorrent_pass="";
	private boolean auth = false;
	private UTorrentAPI api;
	
	public UTorrent() {}
	public UTorrent(String path) {
		pathEseguibile=path;
		injectOptions();
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
		String resp = api.get("action=setsetting&s=dir_active_download_flag&v=1");
		String resp2;
		try {
			resp2 = api.get("action=setsetting&s=dir_active_download&v="+URLEncoder.encode(dir,"UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			return false;
		}
		return resp!=null && resp2!=null;
	}

	@Override
	public synchronized boolean downloadTorrent(Torrent t, String path) {
		System.out.println("download torrent "+t.getUrl());
		if(haveWebAPI()){
			boolean d = downloadWebUI(t, path);
			if(d==false && Os.isWindows()){
				return downloadCLI(t, path);
			}
			return d;
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
					Thread.sleep(3000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				retry++;
			}
			if(!isWebAPIEnabled())
				return false;
		}
		
		if(auth==false){
			auth=true;
			auth(null, null);
		}
		if(setDirectoryDownload(path)){
    		String cmd="action=add-url&s="+t.getUrl();
    		System.out.println("http://localhost:"+port);
    		String r = api.get(cmd);
    		return r!=null;
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
		else if(Os.isLinux()){
			path = "tools"+File.separator+"utorrent"+File.separator+"linux"+File.separator+(Os.is32bit()?"32bit":"64bit")+File.separator+"utserver";
			if(OperazioniFile.fileExists(path))
				return path;
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
		System.out.println("Avvio il client torrent");
		if(Os.isWindows()){
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
		else if(Os.isLinux()){
			String[] cmd = {
					System.getProperty("user.home")+File.separator+pathEseguibile	
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
	private void injectOptions(){
		File f = null;
		if(Os.isWindows()){
			f = new File(System.getenv("APPDATA")+File.separator+"uTorrent"+File.separator+"settings.dat");
		}
		else if(Os.isLinux()){
			String pathConf = pathEseguibile.substring(0, pathEseguibile.lastIndexOf("/"));
			pathConf = "./"+pathConf + File.separator + "settings.dat";
			f = new File(pathConf);
			System.out.println(f.getAbsolutePath());
		}
		else if(Os.isMacOS()){
			String path = System.getProperty("user.home")+File.separator+"Library"+File.separator+"Application Support"+File.separator+"uTorrent"+File.separator+"settings.dat"; 
			f = new File(path);
		}
		
		if(!f.exists()){
			if(isRunning()){
				ProcessFinder.closeProcessByPID(getUTorrentPid());
				avviaClient();
			}
			else {
				avviaClient();
				ProcessFinder.closeProcessByPID(getUTorrentPid());
				avviaClient();
			}
		}
		
		try {
			BencodingInputStream file = new BencodingInputStream(new FileInputStream(f));
			Map<String, Object> options = file.readMap(Object.class);
			file.close();
			Object lastEnable = options.put("webui.enable", 1);
			Object lastPort = options.put("webui.enable_listen", 1);
			if((lastEnable==null || ((BigInteger)lastEnable).intValue()!=1) || (lastPort==null || ((BigInteger)lastPort).intValue()!=1)){
				System.out.println("Le opzioni di utorrent devono essere cambiate");
				ProcessFinder.closeProcessByPID(getUTorrentPid());
				BencodingOutputStream fileO = new BencodingOutputStream(new FileOutputStream(f));
				fileO.writeMap(options);
				fileO.close();
				injectOptions();
				if(isRunning()){
					ProcessFinder.closeProcessByPID(getUTorrentPid());
					avviaClient();
				}
			}
			else {
				Object portS = options.get("webui.port");
				if(portS==null)
					port="8080";
				else
					port = portS.toString();
				
			}
		}
		catch (FileNotFoundException e) {
			port="8080";
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException, URISyntaxException {
		UTorrent u = new UTorrent(UTorrent.rilevaInstallazione());
		u.injectOptions();
		u.avviaClient();
		Desktop d = Desktop.getDesktop();
		d.browse(new URI("http://"+u.address+":"+u.port+"/gui"));
	}
}
