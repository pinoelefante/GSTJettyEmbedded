package gst.programma;

import gst.download.BitTorrentClient;
import gst.download.UTorrent;
import gst.player.VLC;
import gst.player.VideoPlayer;
import gst.serieTV.Preferenze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import util.os.Os;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class Settings {
	public static Settings getInstance(){
		if(singleton==null)
			singleton=new Settings();
		return singleton;
	}
	private static Settings		singleton;
	private static final int	VersioneSoftware		= 122;
	
	private String	current_dir							= "";
	private String	user_dir							= "";
	
	private Properties	opzioni;
	
	private BitTorrentClient bitClient					= null;
	private VideoPlayer		 videoPlayer				= null;	
	
	
	private Settings(){
		opzioni = new Properties();
		baseSettings();
		caricaSettings();
		validaOpzioni();
		salvaSettings();
	}
	
	public void aggiungiOpzione(String k, String v){
		if(opzioni!=null){
			opzioni.put(k, v);
		}
	}
	private void baseSettings(){
		current_dir = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		if(Os.isWindows() && current_dir.startsWith("/")){
			current_dir=current_dir.substring(1).replace("%20", " ").replace("\\", File.separator).replace("/", File.separator);
		}
		System.setProperty("user.dir", current_dir);
		
		String u_dir=System.getProperty("user.home");
		if(u_dir != null){
			user_dir=u_dir+File.separator+".gst";
			if(!user_dir.endsWith(File.separator)){
				user_dir+=File.separator;
			}
			File udir=new File(user_dir);
			if(!udir.exists())
				udir.mkdirs();
		}
		else {
			user_dir=current_dir;
		}
	}
	public void caricaSettings(){
		opzioni.clear();
		FileInputStream file = null;
		try {
			file = new FileInputStream(getUserDir()+File.separator+"settings.properties"); 
			opzioni.load(file);
			file.close();
		}
		catch (IOException e) {
			defaultSettings();
		}
		finally {
			if(file!=null)
				try {
					file.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	@SuppressWarnings("unused")
	public void createAutoStart() {
		if(Os.isWindows()){
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV", getCurrentDir()+getEXEName());
		}
		else if(Os.isLinux()){
			String path_exec=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java -jar "+getCurrentDir()+getEXEName();
			//TODO
		}
		else if(Os.isMacOS()){
			String path_exec=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java -jar "+getCurrentDir()+getEXEName();
			//TODO
		}
	}
	public void defaultSettings(){
		setAskOnClose(true);
		setAutostart(true);
		setDirectoryDownload(getUserDir()+"Download");
		setDownloadAutomatico(false);
		setEnableITASA(true);
		setFirstStart(true);
		setMinRicerca(480);
		setRegolaDownloadDefault(Preferenze.HD);
		setRicercaSottotitoli(true);
		setStartHidden(false);
	}
	public BitTorrentClient getClientTorrent() throws Exception{
		if(bitClient!=null)
			return bitClient;
		else {
			String pathClient = /*QBittorrent.rilevaInstallazione();*/UTorrent.rilevaInstallazione();
			System.out.println(pathClient);
			if(pathClient!=null){
				bitClient = /*new QBittorrent(pathClient);*/new UTorrent(pathClient);
				return bitClient;
			}
			else
				throw new Exception("Client torrent non trovato");
		}
	}
	public VideoPlayer getVideoPlayer() throws Exception{
		if(videoPlayer!=null){
			return videoPlayer;
		}
		else {
			String vlcPath = VLC.rilevaVLC();
			if(vlcPath!=null){
				videoPlayer=new VLC(vlcPath);
				return videoPlayer;
			}
			else
				throw new Exception("VideoPlayer non trovato");
		}
	}
	public String getCurrentDir() {
		return current_dir;
	}
	public String getDirectoryDownload() {
		return getOpzione("download_path")+(getOpzione("download_path").endsWith(File.separator)?"":File.separator);
	}
	public String getEXEName(){
		String exe=System.getProperty("sun.java.command");
		return exe;
	}
	public String getItasaPassword() {
		return getOpzione("itasa_pass");
	}
	public String getItasaUsername() {
		return getOpzione("itasa_user");
	}
	public int getLastVersion() {
		return Integer.parseInt(getOpzione("last_version"));
	}
	public int getMinRicerca() {
		return Integer.parseInt(getOpzione("min_download_auto"));
	}
	public String getOpzione(String k){
		if(opzioni!=null){
			return opzioni.getProperty(k, "");
		}
		throw new RuntimeException();
	}
	public int getRegolaDownloadDefault(){
		return Integer.parseInt(getOpzione("regola_download"));
	}
	
	public String getUserDir(){
		return user_dir;
	}
	public int getVersioneSoftware() {
		return VersioneSoftware;
	}
	public boolean isAskOnClose() {
		return Boolean.parseBoolean(getOpzione("ask_on_close"));
	}
	public boolean isAutostart() {
		return Boolean.parseBoolean(getOpzione("autostart"));
	}
	public boolean isDownloadAutomatico() {
		return Boolean.parseBoolean(getOpzione("download_auto"));
	}
	public boolean isEnableITASA() {
		return Boolean.parseBoolean(getOpzione("itasa"));
	}
	public boolean isFirstStart(){
		return Boolean.parseBoolean(getOpzione("first_start"));
	}
	public boolean isRicercaSottotitoli() {
		return Boolean.parseBoolean(getOpzione("download_sottotitoli"));
	}
	public boolean isStartHidden() {
		return Boolean.parseBoolean(getOpzione("start_hidden"));
	}
	public void removeAutostart() {
		if(Os.isWindows()){
			if(Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV")){
				Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV");
			}
		}
		else if(Os.isLinux()){
			//TODO
		}
		else if(Os.isMacOS()){
			//TODO
		}
	}
	public void salvaSettings(){
		if(opzioni==null)
			return;
		FileOutputStream file = null;
		try {
			file = new FileOutputStream(getUserDir()+File.separator+"settings.properties");
			opzioni.store(file, "");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if(file!=null)
				try {
					file.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void setAskOnClose(boolean askOnClose) {
		aggiungiOpzione("ask_on_close", askOnClose+"");
	}
	public boolean setAutostart(boolean autostart) {
		aggiungiOpzione("autostart", autostart+"");
		
		if(isAutostart())
			createAutoStart();
		else
			removeAutostart();
		return autostart;
	}
	public void setCurrentDir(String current_dir) {
		this.current_dir = current_dir;
	}
	public void setDirectoryDownload(String directoryDownload) {	
		File f=new File(directoryDownload);
		if(!f.exists())
			f.mkdirs();
		if(!directoryDownload.endsWith(File.separator))
			directoryDownload+=File.separator;
		opzioni.put("download_path", directoryDownload);
	}
	public void setDownloadAutomatico(boolean downloadAutomatico) {
		aggiungiOpzione("download_auto", downloadAutomatico+"");
	}
	public void setEnableITASA(boolean enableITASA) {
		aggiungiOpzione("itasa", enableITASA+"");
	}
	public void setFirstStart(boolean firstStart){
		aggiungiOpzione("first_start", firstStart+"");
	}
	
	public void setItasaPassword(String itasa_Password) {
		aggiungiOpzione("itasa_pass", itasa_Password+"");
	}
	
	public void setItasaUsername(String itasa_Username) {
		aggiungiOpzione("itasa_user", itasa_Username);
	}
	
	public void setLastVersion(int lastVersion) {
		aggiungiOpzione("last_version", lastVersion+"");
	}
	public void setMinRicerca(int minRicerca) {
		aggiungiOpzione("min_download_auto", minRicerca+"");
	}
	
	public void setRegolaDownloadDefault(int d){
		aggiungiOpzione("regola_download", d+"");
	}
	
	public void setRicercaSottotitoli(boolean ricercaSottotitoli) {
		aggiungiOpzione("download_sottotitoli", ricercaSottotitoli+"");
	}
	public void setStartHidden(boolean startHidden) {
		aggiungiOpzione("start_hidden", startHidden+"");
	}
	public void setQBittorrentPath(String p){
		aggiungiOpzione("qbittorrent_path", p);
	}
	public String getQBittorrentPath(){
		return getOpzione("qbittorrent_path");
	}
	public void setUTorrentPath(String p){
		aggiungiOpzione("utorrent_path", p);
	}
	public String getUTorrentPath(){
		return getOpzione("utorrent_path");
	}
	private void validaOpzioni(){
		Enumeration<Object> listKey= opzioni.keys();
		while(listKey.hasMoreElements()){
			String item = (String) listKey.nextElement();
			switch(item){
				case "ask_on_close":
				case "autostart":
				case "download_auto":
				case "download_path":
				case "download_sottotitoli":
				case "first_start":
				case "itasa":
				case "itasa_user":
				case "itasa_pass":
				case "last_version":
				case "min_download_auto":
				case "regola_download":
				case "start_hidden":
				case "utorrent_path":
				case "qbittorrent_path":
					break;
				default:
					opzioni.remove(item);
					break;
			}
		}
	}
	
}
