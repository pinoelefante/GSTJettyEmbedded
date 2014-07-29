package gst.programma;

import gst.database.Database;
import gst.download.BitTorrentClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class Settings {
	private static final int	VersioneSoftware					= 122;
	public static final String	IndirizzoDonazioni					= "http://pinoelefante.altervista.org/donazioni/donazione_gst.html";
	private static String		current_dir							= "";
	private static String		user_dir							= "";
	private static String		DirectoryDownload					= "";
	private static String		ClientPath							= "";
	private static boolean		TrayOnIcon							= true;
	private static boolean		AskOnClose							= false;
	private static boolean		StartHidden							= false;
	private static boolean		Autostart							= true;
	private static boolean		DownloadAutomatico					= false;
	private static int			MinRicerca							= 480;
	private static String		SistemaOperativo					= "";
	private static boolean		canStartDownloadAutomatico			= false;
	
	private static boolean		NewUpdate							= true;
	private static int			LastVersion							= 0;
	private static boolean		RicercaSottotitoli					= true;
	private static boolean 		alwaysontop							= true;
	private static String		VLCPath								= "";
	private static boolean		VLCAutoload							= false;
	private static String		Itasa_Username						= "";
	private static String		Itasa_Password						= "";
	private static String		ClientID 							= "";
	private static boolean 		lettore_nascondi_ignore				= true;
	private static boolean 		lettore_nascondi_rimosso			= true;
	
	private static boolean 		extenal_VLC							= false;
	private static boolean 		enableITASA							= true;
	private static boolean 		lettore_nascondi_viste				= true;
	private static int			lettore_ordine						= 0;
	private static int			default_regola_download_episodi		= 0;
	
	public static int getRegolaDownloadDefault(){
		return default_regola_download_episodi;
	}
	public static void setRegolaDownloadDefault(int d){
		default_regola_download_episodi=d;
	}
	public static int getVersioneSoftware() {
		return VersioneSoftware;
	}
	public static String getCurrentDir() {
		return current_dir;
	}
	public static void setCurrentDir(String current_dir) {
		Settings.current_dir = current_dir;
	}
	public static String getClientPath() {
		if(ClientPath.compareToIgnoreCase("null")==0)
			return "";
		return ClientPath;
	}
	public static void setClientPath(String clientPath) {
		ClientPath = clientPath;
	}
	public static String getDirectoryDownload() {
		return DirectoryDownload+(DirectoryDownload.endsWith(File.separator)?"":File.separator);
	}
	public static void setDirectoryDownload(String directoryDownload) {	
		File f=new File(directoryDownload);
		if(!f.exists())
			f.mkdirs();
		DirectoryDownload = directoryDownload;
	}
	public static boolean isTrayOnIcon() {
		return TrayOnIcon;
	}
	public static void setTrayOnIcon(boolean trayOnIcon) {
		TrayOnIcon = trayOnIcon;
	}
	public static boolean isAskOnClose() {
		return AskOnClose;
	}
	public static void setAskOnClose(boolean askOnClose) {
		AskOnClose = askOnClose;
	}
	public static boolean isStartHidden() {
		return StartHidden;
	}
	public static void setStartHidden(boolean startHidden) {
		StartHidden = startHidden;
	}
	public static boolean isAutostart() {
		return Autostart;
	}
	public static boolean setAutostart(boolean autostart) {
		Autostart = autostart;
		
		if(isAutostart())
			createAutoStart();
		else
			removeAutostart();
		return autostart;
	}
	public static boolean isDownloadAutomatico() {
		return DownloadAutomatico;
	}
	public static void setDownloadAutomatico(boolean downloadAutomatico) {
		DownloadAutomatico = downloadAutomatico;
	}
	public static String getSistemaOperativo() {
		return SistemaOperativo;
	}
	public static void setSistemaOperativo(String sistemaOperativo) {
		SistemaOperativo = sistemaOperativo;
	}
	public static boolean isCanStartDownloadAutomatico() {
		return canStartDownloadAutomatico;
	}
	public static void setCanStartDownloadAutomatico(boolean canStartDownloadAutomatico) {
		Settings.canStartDownloadAutomatico = canStartDownloadAutomatico;
	}
	public static boolean isNewUpdate() {
		return NewUpdate;
	}
	public static void setNewUpdate(boolean newUpdate) {
		NewUpdate = newUpdate;
	}
	public static int getLastVersion() {
		return LastVersion;
	}
	public static void setLastVersion(int lastVersion) {
		LastVersion = lastVersion;
	}
	public static boolean isRicercaSottotitoli() {
		return RicercaSottotitoli;
	}
	public static void setRicercaSottotitoli(boolean ricercaSottotitoli) {
		RicercaSottotitoli = ricercaSottotitoli;
	}
	public static boolean isAlwaysOnTop() {
		return alwaysontop;
	}
	public static void setAlwaysOnTop(boolean alwaysontop) {
		Settings.alwaysontop = alwaysontop;
	}
	public static String getVLCPath() {
		if(VLCPath.compareToIgnoreCase("null")==0)
			return "";
		return VLCPath;
	}
	public static void setVLCPath(String vLCPath) {
		VLCPath = vLCPath;
	}
	public static String getItasaUsername() {
		return Itasa_Username;
	}
	public static void setItasaUsername(String itasa_Username) {
		Itasa_Username = itasa_Username;
	}
	public static String getItasaPassword() {
		return Itasa_Password;
	}
	public static void setItasaPassword(String itasa_Password) {
		Itasa_Password = itasa_Password;
	}
	public static int getMinRicerca() {
		return MinRicerca;
	}
	public static void setMinRicerca(int minRicerca) {
		MinRicerca = minRicerca;
	}
	public static void setClientID(String id){
		ClientID=id;
	}
	public static String getClientID(){
		return ClientID;
	}
	//TODO Settaggi default
	public static void setDefault() {
		setTrayOnIcon(true);
		setAskOnClose(false);
		setStartHidden(false);
		setAutostart(true);
		setDownloadAutomatico(false);
		setMinRicerca(480);
		setRicercaSottotitoli(true);
		setAlwaysOnTop(true);
	}

	public static void baseSettings(){
		SistemaOperativo = System.getProperty("os.name");
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
		DirectoryDownload=user_dir+"Download";
	}
	public static String getUserDir(){
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
	public static void defaultSettings(){
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
	public static void CaricaSetting(){
		caricaFile();
	}

	@SuppressWarnings("unused")
	public static void createAutoStart() {
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
	public static void removeAutostart() {
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
	public static void main (String[] args){
		baseSettings();
		removeAutostart();
	}
	public static boolean verificaUtorrent(){
		String nome_client=ClientPath.substring(ClientPath.lastIndexOf(File.separator)+1);
		if(nome_client.compareToIgnoreCase("utorrent.exe")!=0)
			return false;
		return true;
	}
	public static void salvaSettings() {
		salvaFile();
	}
	public static boolean isWindows(){
		return getSistemaOperativo().contains("Windows");
	}
	public static boolean isLinux(){
		return getSistemaOperativo().contains("Linux");
	}
	public static boolean isMacOS(){
		return getSistemaOperativo().contains("Mac");
	}
	
	//TODO completare rilevamento vlc
	public static String rilevaVLC(){
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
	public static boolean isLettoreNascondiIgnore() {
		return lettore_nascondi_ignore;
	}
	public static void setLettoreNascondiIgnore(boolean lettore_nascondi_ignore) {
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
	public static boolean isVLC(){
		String path=getVLCPath();
		if(path.isEmpty())
			return false;
		
		if(isWindows()){
			return path.toLowerCase().endsWith("vlc.exe");
		}
		else if(isLinux())
			return path.toLowerCase().endsWith("vlc");
		else
			return true;
	}
	public static boolean is32bit(){
		String arch_vm = System.getProperty("os.arch");
		boolean x86 = arch_vm.contains("x86")||arch_vm.contains("i386");
		return x86;
	}
	public static boolean is64bit(){
		return !is32bit();
	}
	public static String getOSName(){
		String name="";
		
		if(isWindows())
			name="win32";
		else if(isLinux())
			name="linux";
		else if(isMacOS())
			name="macos";
		
		return name;
	}
	public static String getVMArch(){
		if(is32bit())
			return "i386";
		else
			return "amd64";
	}
	public static boolean isExtenalVLC() {
		return extenal_VLC;
	}
	public static void setExtenalVLC(boolean extenal_VLC) {
		Settings.extenal_VLC = extenal_VLC;
	}
	public static boolean isEnableITASA() {
		return enableITASA;
	}
	public static void setEnableITASA(boolean enableITASA) {
		Settings.enableITASA = enableITASA;
	}
	public static boolean isLettoreNascondiViste() {
		return lettore_nascondi_viste;
	}
	public static void setLettoreNascondiViste(boolean lettore_nascondi_viste) {
		Settings.lettore_nascondi_viste = lettore_nascondi_viste;
	}
	public static int getLettoreOrdine() {
		return lettore_ordine;
	}
	public static void setLettoreOrdine(int lettore_ordine) {
		Settings.lettore_ordine = lettore_ordine;
	}
	public static boolean isVLCAutoload() {
		return VLCAutoload;
	}
	public static void setVLCAutoload(boolean vLCAutoload) {
		VLCAutoload = vLCAutoload;
	}
	public static String getEXEName(){
		String exe=System.getProperty("sun.java.command");
		return exe;
	}
	private static synchronized void salvaFile(){
		String path=getUserDir()+"settings.dat";
		FileWriter fw=null;
		try {
			fw=new FileWriter(path);
			fw.append("always_on_top="+isAlwaysOnTop()+"\n");
			fw.append("download_path="+getDirectoryDownload()+"\n");
			fw.append("utorrent="+getClientPath()+"\n");
			fw.append("vlc="+getVLCPath()+"\n");
			fw.append("itasa_user="+getItasaUsername()+"\n");
			fw.append("itasa_pass="+getItasaPassword()+"\n");
			fw.append("client_id="+getClientID()+"\n");
			fw.append("tray_on_icon="+isTrayOnIcon()+"\n");
			fw.append("start_hidden="+isStartHidden()+"\n");
			fw.append("ask_on_close="+isAskOnClose()+"\n");
			fw.append("always_on_top="+isAlwaysOnTop()+"\n");
			fw.append("autostart="+isAutostart()+"\n");
			fw.append("download_auto="+isDownloadAutomatico()+"\n");
			fw.append("min_download_auto="+getMinRicerca()+"\n");
			fw.append("new_update="+isNewUpdate()+"\n");
			fw.append("last_version="+getLastVersion()+"\n");
			fw.append("download_sottotitoli="+isRicercaSottotitoli()+"\n");
			fw.append("external_vlc="+isExtenalVLC()+"\n");
			fw.append("vlc_autoload="+isVLCAutoload()+"\n");
			fw.append("itasa="+isEnableITASA()+"\n");
			fw.append("hide_viste="+isLettoreNascondiViste()+"\n");
			fw.append("hide_ignorate="+isLettoreNascondiIgnore()+"\n");
			fw.append("hide_rimosse="+isLettoreNascondiRimosso()+"\n");
			fw.append("ordine_lettore="+getLettoreOrdine()+"\n");
			fw.append("regola_download="+getRegolaDownloadDefault());
		} 
		catch (IOException e) {
			ManagerException.registraEccezione(e);
			e.printStackTrace();
		}
		finally {
			if(fw!=null)
				try {
					fw.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	private static void caricaFile() {
		String path=getUserDir()+"settings.dat";
		FileReader fr=null;
		Scanner file=null;
		try {
			fr=new FileReader(path);
			file=new Scanner(fr);
			while(file.hasNextLine()){
				String option=file.nextLine().trim();
				if(option.contains("=")){
					String[] kv=option.split("=");
					try {
						switch(kv[0]){
							case "always_on_top":
								setAlwaysOnTop(Boolean.parseBoolean(kv[1]));
								break;
							case "download_path":
								setDirectoryDownload(kv[1]);
								break;
							case "utorrent":
								setClientPath(kv[1]);
								break;
							case "vlc":
								setVLCPath(kv[1]);
								break;
							case "itasa_user":
								setItasaUsername(kv[1]);
								break;
							case "itasa_pass":
								setItasaPassword(kv[1]);
								break;
							case "client_id":
								setClientID(kv[1]);
								break;
							case "tray_on_icon":
								setTrayOnIcon(Boolean.parseBoolean(kv[1]));
								break;
							case "start_hidden":
								setStartHidden(Boolean.parseBoolean(kv[1]));
								break;
							case "ask_on_close":
								setAskOnClose(Boolean.parseBoolean(kv[1]));
								break;
							case "autostart":
								setAutostart(Boolean.parseBoolean(kv[1]));
								break;
							case "download_auto":
								setDownloadAutomatico(Boolean.parseBoolean(kv[1]));
								break;
							case "min_download_auto":
								setMinRicerca(Integer.parseInt(kv[1]));
								break;
							case "new_update":
								setNewUpdate(Boolean.parseBoolean(kv[1]));
								break;
							case "last_version":
								setLastVersion(Integer.parseInt(kv[1]));
								break;
							case "download_sottotitoli":
								setRicercaSottotitoli(Boolean.parseBoolean(kv[1]));
								break;
							case "external_vlc":
								setExtenalVLC(Boolean.parseBoolean(kv[1]));
								break;
							case "vlc_autoload":
								setVLCAutoload(Boolean.parseBoolean(kv[1]));
								break;
							case "itasa":
								setEnableITASA(Boolean.parseBoolean(kv[1]));
								break;
							case "hide_viste":
								setLettoreNascondiViste(Boolean.parseBoolean(kv[1]));
								break;
							case "hide_ignorate":
								setLettoreNascondiIgnore(Boolean.parseBoolean(kv[1]));
								break;
							case "hide_rimosse":
								setLettoreNascondiRimosso(Boolean.parseBoolean(kv[1]));
								break;
							case "ordine_lettore":
								setLettoreOrdine(Integer.parseInt(kv[1]));
								break;
							case "regola_download":
								setRegolaDownloadDefault(Integer.parseInt(kv[1]));
								break;
							/*	
							case "":
								break;
							*/
							default:
								ManagerException.registraEccezione("Opzione non valida: "+kv[0]);
						}
					}
					catch(Exception e){
						ManagerException.registraEccezione(e);
					}
				}
				else {
					ManagerException.registraEccezione("Opzione non valida: "+option);
				}
			}
		} 
		catch (FileNotFoundException e) {
			if(Database.isFreshNew())
				setLastVersion(0);
			else
				setLastVersion(117);
			e.printStackTrace();
		}
		finally {
			try {
				if(file!=null)
					file.close();
				if(fr!=null)
					fr.close();
			}
			catch(IOException e){}
		}
	}
	public static BitTorrentClient getClientTorrent(){
		//TODO client torrent finder
		return null;
	}
}
