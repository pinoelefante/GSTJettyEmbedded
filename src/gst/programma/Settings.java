package gst.programma;

import gst.database.Database;
import gst.download.BitTorrentClient;
import gst.download.UTorrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class Settings {
	private static Settings		singleton;
	private static final int	VersioneSoftware		= 122;
	public static final String	IndirizzoDonazioni		= "http://pinoelefante.altervista.org/donazioni/donazione_gst.html";
	
	private String	current_dir							= "";
	private String	user_dir							= "";
	private String	sistemaOperativo					= "";
		
	private Properties	opzioni;
	private BitTorrentClient bitClient					= null;
	
	public static Settings getInstance(){
		if(singleton==null)
			singleton=new Settings();
		return singleton;
	}
	private Settings(){
		opzioni = new Properties();
		baseSettings();
		caricaSettings();
	}
	
	public int getRegolaDownloadDefault(){
		return default_regola_download_episodi;
	}
	public void setRegolaDownloadDefault(int d){
		default_regola_download_episodi=d;
	}
	public int getVersioneSoftware() {
		return VersioneSoftware;
	}
	public String getCurrentDir() {
		return current_dir;
	}
	public void setCurrentDir(String current_dir) {
		this.current_dir = current_dir;
	}
	public String getDirectoryDownload() {
		return getOpzione("download_path")+(getOpzione("download_path").endsWith(File.separator)?"":File.separator);
	}
	public void setDirectoryDownload(String directoryDownload) {	
		File f=new File(directoryDownload);
		if(!f.exists())
			f.mkdirs();
		opzioni.put("download_path", directoryDownload);
	}
	public boolean isTrayOnIcon() {
		return TrayOnIcon;
	}
	public void setTrayOnIcon(boolean trayOnIcon) {
		TrayOnIcon = trayOnIcon;
	}
	public boolean isAskOnClose() {
		return AskOnClose;
	}
	public void setAskOnClose(boolean askOnClose) {
		AskOnClose = askOnClose;
	}
	public boolean isStartHidden() {
		return StartHidden;
	}
	public void setStartHidden(boolean startHidden) {
		StartHidden = startHidden;
	}
	public boolean isAutostart() {
		return Autostart;
	}
	public boolean setAutostart(boolean autostart) {
		Autostart = autostart;
		
		if(isAutostart())
			createAutoStart();
		else
			removeAutostart();
		return autostart;
	}
	public boolean isDownloadAutomatico() {
		return DownloadAutomatico;
	}
	public void setDownloadAutomatico(boolean downloadAutomatico) {
		DownloadAutomatico = downloadAutomatico;
	}
	public String getSistemaOperativo() {
		return sistemaOperativo;
	}
	public void setSistemaOperativo(String sistemaOperativo) {
		SistemaOperativo = sistemaOperativo;
	}
	public boolean isCanStartDownloadAutomatico() {
		return canStartDownloadAutomatico;
	}
	public void setCanStartDownloadAutomatico(boolean canStartDownloadAutomatico) {
		Settings.canStartDownloadAutomatico = canStartDownloadAutomatico;
	}
	public boolean isNewUpdate() {
		return NewUpdate;
	}
	public void setNewUpdate(boolean newUpdate) {
		NewUpdate = newUpdate;
	}
	public int getLastVersion() {
		return LastVersion;
	}
	public void setLastVersion(int lastVersion) {
		LastVersion = lastVersion;
	}
	public boolean isRicercaSottotitoli() {
		return RicercaSottotitoli;
	}
	public void setRicercaSottotitoli(boolean ricercaSottotitoli) {
		RicercaSottotitoli = ricercaSottotitoli;
	}
	public boolean isAlwaysOnTop() {
		return alwaysontop;
	}
	public void setAlwaysOnTop(boolean alwaysontop) {
		Settings.alwaysontop = alwaysontop;
	}
	public String getVLCPath() {
		if(VLCPath.compareToIgnoreCase("null")==0)
			return "";
		return VLCPath;
	}
	public void setVLCPath(String vLCPath) {
		VLCPath = vLCPath;
	}
	public String getItasaUsername() {
		return Itasa_Username;
	}
	public void setItasaUsername(String itasa_Username) {
		Itasa_Username = itasa_Username;
	}
	public String getItasaPassword() {
		return Itasa_Password;
	}
	public void setItasaPassword(String itasa_Password) {
		Itasa_Password = itasa_Password;
	}
	public int getMinRicerca() {
		return MinRicerca;
	}
	public void setMinRicerca(int minRicerca) {
		MinRicerca = minRicerca;
	}
	public void setClientID(String id){
		ClientID=id;
	}
	public String getClientID(){
		return ClientID;
	}
	//TODO Settaggi default
	public void setDefault() {
		setTrayOnIcon(true);
		setAskOnClose(false);
		setStartHidden(false);
		setAutostart(true);
		setDownloadAutomatico(false);
		setMinRicerca(480);
		setRicercaSottotitoli(true);
		setAlwaysOnTop(true);
	}

	public String getUserDir(){
		return user_dir;
	}

	/* Tabelle database
    "download_path"
    "utorrent"
	"vlc"
    "itasa_user"
    "itasa_pass"
    "client_id"
    "tray_on_icon"
    "start_hidden"
    "ask_on_close"
    "always_on_top"
    "autostart"
    "download_auto"
    "min_download_auto"
    "new_update"
    "last_version"
    "download_sottotitoli"
    "external_vlc"
    "itasa"
    "hide_viste"
    "hide_ignorate"
    "hide_rimosse"
    "ordine_lettore"
    */
	public void defaultSettings(){
		setAlwaysOnTop(true);
		setTrayOnIcon(true);
		setStartHidden(false);
		setAskOnClose(true);
		setAutostart(true);
		setDownloadAutomatico(false);
		setMinRicerca(480);
		setRicercaSottotitoli(true);
		setExtenalVLC(false);
		setVLCAutoload(true);
		setEnableITASA(true);
		setLettoreNascondiViste(false);
		setLettoreNascondiIgnore(true);
		setLettoreNascondiRimosso(true);
		setLettoreOrdine(0);
	}

	@SuppressWarnings("unused")
	public void createAutoStart() {
		if(isWindows()){
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV", getCurrentDir()+"GestioneSerieTV5.exe");
		}
		else if(isLinux()){
			String path_exec=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java -jar "+getCurrentDir()+"st.jar";
			//TODO
		}
		else if(isMacOS()){
			String path_exec=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java -jar "+getCurrentDir()+"st.jar";
			//TODO
		}
	}
	public void removeAutostart() {
		if(isWindows()){
			if(Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV")){
				Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "GestioneSerieTV");
			}
		}
		else if(isLinux()){
			//TODO
		}
		else if(isMacOS()){
			//TODO
		}
	}
	
	public boolean isWindows(){
		return getSistemaOperativo().contains("Windows");
	}
	public boolean isLinux(){
		return getSistemaOperativo().contains("Linux");
	}
	public boolean isMacOS(){
		return getSistemaOperativo().contains("Mac");
	}
	
	//TODO completare rilevamento vlc
	public String rilevaVLC(){
		if(isWindows()){
			//TODO c:\\users\\utente\\roaming\\utorrent
			
		}
		else if(isLinux()){
			if(OperazioniFile.fileExists("/usr/bin/vlc"))
				return "/usr/bin/vlc";
		}
		else if(isMacOS()){
			
		}
		return "";
	}
	public boolean isLettoreNascondiIgnore() {
		return lettore_nascondi_ignore;
	}
	public void setLettoreNascondiIgnore(boolean lettore_nascondi_ignore) {
		Settings.lettore_nascondi_ignore = lettore_nascondi_ignore;
	}
	public static boolean isLettoreNascondiRimosso() {
		return lettore_nascondi_rimosso;
	}
	public static void setLettoreNascondiRimosso(boolean lettore_nascondi_rimosso) {
		Settings.lettore_nascondi_rimosso = lettore_nascondi_rimosso;
	}
	//TODO completare rilevamento client utorrent
	public static String rilevaUtorrent(){
		if(isWindows()){
			if(OperazioniFile.fileExists(getCurrentDir()+"utorrent.exe")){
				return getCurrentDir()+"utorrent.exe";
			}
		}
		else if(isMacOS()){
			if(OperazioniFile.fileExists("/Applications/uTorrent.app/Contents/MacOS/uTorrent"))
				return "/Applications/uTorrent.app/Contents/MacOS/uTorrent";
		}
		else if(isLinux()){
			
		}
		return "";
	}
	public boolean is32bit(){
		String arch_vm = System.getProperty("os.arch");
		boolean x86 = arch_vm.contains("x86")||arch_vm.contains("i386");
		return x86;
	}
	public boolean is64bit(){
		return !is32bit();
	}
	public String getOSName(){
		String name="";
		
		if(isWindows())
			name="win32";
		else if(isLinux())
			name="linux";
		else if(isMacOS())
			name="macos";
		
		return name;
	}
	public String getVMArch(){
		if(is32bit())
			return "i386";
		else
			return "amd64";
	}
	public boolean isExtenalVLC() {
		return extenal_VLC;
	}
	public void setExtenalVLC(boolean extenal_VLC) {
		Settings.extenal_VLC = extenal_VLC;
	}
	public boolean isEnableITASA() {
		return enableITASA;
	}
	public void setEnableITASA(boolean enableITASA) {
		Settings.enableITASA = enableITASA;
	}
	public boolean isLettoreNascondiViste() {
		return lettore_nascondi_viste;
	}
	public void setLettoreNascondiViste(boolean lettore_nascondi_viste) {
		Settings.lettore_nascondi_viste = lettore_nascondi_viste;
	}
	public int getLettoreOrdine() {
		return lettore_ordine;
	}
	public void setLettoreOrdine(int lettore_ordine) {
		Settings.lettore_ordine = lettore_ordine;
	}
	public boolean isVLCAutoload() {
		return VLCAutoload;
	}
	public void setVLCAutoload(boolean vLCAutoload) {
		VLCAutoload = vLCAutoload;
	}
	public static String getEXEName(){
		String exe=System.getProperty("sun.java.command");
		return exe;
	}

	
	public static BitTorrentClient getClientTorrent() throws Exception{
		System.out.println("getClientTorrent Settings.java");
		if(bitClient!=null)
			return bitClient;
		else {
			String pathClient = UTorrent.rilevaInstallazione();
			if(pathClient!=null){
				bitClient = new UTorrent(pathClient);
				return bitClient;
			}
			else
				throw new Exception("Client torrent non trovato");
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
			salvaSettings();
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
	
	private void baseSettings(){
		sistemaOperativo = System.getProperty("os.name");
		current_dir = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		if(isWindows() && current_dir.startsWith("/")){
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
		opzioni.put("directory_download", user_dir+"Download");
	}
	
	public void aggiungiOpzione(String k, String v){
		if(opzioni!=null){
			opzioni.put(k, v);
		}
	}
	public String getOpzione(String k){
		if(opzioni!=null){
			return opzioni.getProperty(k);
		}
		throw new RuntimeException();
	}
}
