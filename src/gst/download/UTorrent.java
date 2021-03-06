package gst.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.Map;

import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;

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
		
		if(Os.isWindows() && downloadCLI(t, path)){
			return true;
		}
		else if(haveWebAPI()){
			boolean d = downloadWebUI(t, path);
			if(d==false && Os.isWindows()){
				return downloadCLI(t, path);
			}
			return d;
		}
		return false;
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
	private static long nextTorrentTime = 0L;
	public synchronized boolean downloadWebUI(Torrent t, String path){
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
		while(System.currentTimeMillis()<=nextTorrentTime){
			try {
				Thread.sleep(100L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(setDirectoryDownload(path)){
    		String cmd="action=add-url&s="+t.getUrl();
    		System.out.println("http://localhost:"+port);
    		String r = api.get(cmd);
    		nextTorrentTime=System.currentTimeMillis()+500;
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
	public void injectOptions(){
		Map<String, NSObject> options = getOptionMap();
		if(options==null){
			System.out.println("Opzioni null");
			return;
		}
		boolean toClose = false;
		if(options.get("webui.enable_listen")==null){
			System.out.println("webui.enable_listen non in settings.dat");
			options.put("webui.enable_listen", new NSNumber(true));
			toClose=true;
		}
		if(options.get("webui.port")==null){
			options.put("webui.port", new NSNumber(Integer.parseInt(port)));
			toClose = true;
		}
		if(options.get("webui.enable").toString().compareTo("0")==0){
			options.put("webui.enable", new NSNumber(true));
			toClose = true;
		}
		
		boolean toStart = false;
		if(toClose){
			if(isRunning()){
				ProcessFinder.closeProcessByPID(getUTorrentPid());
				toStart=true;
			}
			BencodingOutputStream out = null;
			try {
				out = new BencodingOutputStream(new FileOutputStream(getOptionFile()));
				out.writeMap(options);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if(out!=null)
						out.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					if(toStart)
						avviaClient();
				}
			}
		}
	}
	private Map<String, NSObject> getOptionMap(){
		FileInputStream in=null;
		BencodingInputStream file=null;
		try {
			in=new FileInputStream(getOptionFile());
    		file = new BencodingInputStream(in);
    		Map<String, NSObject> options = file.readMap(NSObject.class);
    		file.close();
    		return options;
		}
		catch(Exception e){}
		finally {
			try {
				if(in!=null)
					in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(file!=null)
					file.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public void readOptionFile(){
		/*
    	Map<String, Object> options = getOptionMap();
    	
    	if(!options.containsKey("webui.enable_listen")){
    		port = options.get("bind_port").toString();
    	}
    	
    	for(Entry<String, Object> e : options.entrySet()){
    		System.out.println(e.getKey()+"="+e.getValue());
    	}
    	*/
	}
	public static void main(String[] args){
		UTorrent u = new UTorrent();
		Map<String, NSObject> opts = u.getOptionMap();
		System.out.println("webui.enable_listen contenuto="+opts.containsKey("webui.enable_listen"));
		System.out.println("webui.enable_listen="+opts.get("webui.enable_listen"));
		opts.put("webui.enable_listen", new NSNumber(1));
		System.out.println("webui.enable_listen="+opts.get("webui.enable_listen"));
	}
	private File getOptionFile(){
		File f = null;
		if(Os.isWindows()){
			f = new File(System.getenv("APPDATA")+File.separator+"uTorrent"+File.separator+"settings.dat");
		}
		else if(Os.isLinux()){
			String pathConf = pathEseguibile.substring(0, pathEseguibile.lastIndexOf("/"));
			pathConf = pathConf + File.separator + "settings.dat";
			f = new File(pathConf);
			System.out.println(f.getAbsolutePath());
		}
		else if(Os.isMacOS()){
			String path = System.getProperty("user.home")+File.separator+"Library"+File.separator+"Application Support"+File.separator+"uTorrent"+File.separator+"settings.dat"; 
			f = new File(path);
		}
		return f;
	}
}
