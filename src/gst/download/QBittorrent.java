	package gst.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import util.httpOperations.HttpOperations;
import util.os.Os;
import gst.programma.OperazioniFile;
import gst.serieTV.Torrent;

public class QBittorrent implements BitTorrentClient {
	private String pathExe="", pathConfig="", configFile="";
	private String address, port, version="3.1.9.2";

	public QBittorrent(String exe) {
		if(Os.isWindows()){
			pathConfig = System.getenv("APPDATA")+File.separator+"qBittorrent";
			configFile="qBittorrent.ini";
		}
		else if(Os.isLinux()){
			pathConfig = System.getProperty("user.home")+File.separator+".config"+File.separator+"qBittorrent";
			configFile="qBittorrent.conf";
		}
		else {
			pathConfig = System.getProperty("user.home")+File.separator+"Library"+File.separator+"Preferences";
			configFile = "com.qbittorrent.qBittorrent.plist";
		}
		File dirConfig=new File(pathConfig);
		dirConfig.mkdirs();
		pathExe=exe;
		injectOption();
	}

	public boolean haveWebAPI() {
		return true;
	}

	public boolean isWebAPIEnabled() {
		return true;
	}

	public boolean setDirectoryDownload(String dir) {
		try {
			int compare = compareVersion(version, "3.2.0");
			if(compare<0){
				if(Os.isWindows() || Os.isLinux()) {
					modificaParametroFileConfig(readOptionFile(), "Downloads\\SavePath", dir.replace(File.separator, "/"));
				}
				else {
					NSDictionary options = (NSDictionary) readOptionFile();
					options.put("Preferences.Downloads.SavePath", new NSString(dir));
					salvaOpzioni(options);
				}
    			reloadSettings();
    			return true;
			}
			else {
				List<NameValuePair> parametri = new ArrayList<>();
				parametri.add(new BasicNameValuePair("json", "{\"save_path\":\""+dir.replace(File.separator, "/")+"\"}"));
				boolean r = HttpOperations.POST_withBoolean("http://"+address+":"+port+"/command/setPreferences", parametri);
				return r;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean reloadSettings() { /* setta un parametro casuale in modo da ricaricare i settings */
		List<NameValuePair> parametri = new ArrayList<>();
		try {
			parametri.add(new BasicNameValuePair("json", "{\"dht\":\""+true+"\"}"));
			boolean r = HttpOperations.POST_withBoolean("http://"+address+":"+port+"/command/setPreferences", parametri);
			return r;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized boolean downloadTorrent(Torrent t, String path) {
		if(!isRunning()){
			avviaClient();
			while(!isWebServiceOnline()){
    			try {
    				Thread.sleep(1000L);
    			}
    			catch (InterruptedException e) {
    				e.printStackTrace();
    			}
			}
		}
		else if(!isWebServiceOnline()){
			injectOption();
			while(!isWebServiceOnline()){
    			try {
    				Thread.sleep(1000L);
    			}
    			catch (InterruptedException e) {
    				e.printStackTrace();
    			}
			}
		}
		if(setDirectoryDownload(path)){
			List<NameValuePair> parametri = new ArrayList<>();
			parametri.add(new BasicNameValuePair("urls", t.getUrl()));
			try {
				return HttpOperations.POST_withBoolean("http://"+address+":"+port+"/command/download", parametri);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private void avviaClient() {
		if(Os.isWindows() || Os.isLinux()){
			String[] cmd = {
				pathExe
			};
			System.out.println(pathExe);
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
				pathExe
			};
			try {
				Runtime.getRuntime().exec(cmd);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isRunning() {
		return getQBittorrentPID() > 0;
	}

	private void injectOption() {
		Object opzioni = readOptionFile();
		if(modificaOpzioni(opzioni)){
			try {
				salvaOpzioni(opzioni);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if(isRunning())
				reloadSettings();
		}
		trovaParametriWebInterface(opzioni);
	}

	@SuppressWarnings("unchecked")
	private void trovaParametriWebInterface(Object opzioniO) {
		address="localhost";
		if(Os.isWindows() || Os.isLinux()){
			if(opzioniO instanceof ArrayList){
    			ArrayList<String> opzioni = (ArrayList<String>)opzioniO;
        		for(int i=0;i<opzioni.size();i++){
        			if(opzioni.get(i).startsWith("WebUI\\Port")){
        				String[] kv=opzioni.get(i).split("=");
        				port=kv[1];
        				break;
        			}
        		}
			}
		}
		else {
			NSDictionary opzioni=(NSDictionary)opzioniO;
			NSObject porta = opzioni.get("Preferences.WebUI.Port");
			if(porta != null){
				port = ""+((NSNumber)porta).intValue();
			}
			else
				port = "8080";
		}
	}
	
	@SuppressWarnings("unchecked")
	private void modificaParametroFileConfig(Object opzioniO, String param, Object value) {
		if(Os.isWindows() || Os.isLinux()){
			ArrayList<String> opzioni=(ArrayList<String>)opzioniO;
    		boolean found = false;
    		for(int i=0;i<opzioni.size();i++){
    			if(opzioni.get(i).startsWith(param)){
    				opzioni.remove(i);
    				opzioni.add(i,param+"="+value.toString());
    				found = true;
    				break;
    			}
    		}
    		
    		if(found){
    			try {
    				salvaOpzioni(opzioni);
    			}
    			catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
		}
		else {
			NSDictionary opzioni=(NSDictionary)opzioniO;
			opzioni.put(param, value);
			try {
				salvaOpzioni(opzioni);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Object readOptionFile() {
		ArrayList<String> fileS=new ArrayList<String>();
		if(Os.isWindows() || Os.isLinux()){
    		Scanner file = null;
    		try {
    			FileReader fr=new FileReader(pathConfig+File.separator+configFile);
    			file = new Scanner(fr);
    			while(file.hasNextLine())
    				fileS.add(file.nextLine().trim());
    		}
    		catch (FileNotFoundException e) {
    			e.printStackTrace();
    		}
    		finally {
    			if(file!=null)
    				file.close();
    		}
    		fileS.trimToSize();
    		return fileS;
		}
		else {
			File file = new File(pathConfig+File.separator+configFile);
			try {
				return PropertyListParser.parse(file);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean modificaOpzioni(Object opzioniO){
		if(Os.isWindows() || Os.isLinux()){
			ArrayList<String> opzioni = (ArrayList<String>)opzioniO;
			boolean isPreferences=false, prefFound=false, modificato=false;
			boolean webUiEnabledFound=false,webUiHttpsFound=false, webUiLocalFound = false; 
			int index_webUi=0;
			for(int i=0;i<opzioni.size();i++){
				if(opzioni.get(i).compareTo("[Preferences]")==0){
					prefFound=true;
					isPreferences=true;
					continue;
				}
				else if (opzioni.get(i).startsWith("[")){
					isPreferences=false;
					if(prefFound)
						break;
				}
				if(isPreferences && opzioni.get(i).startsWith("WebUI")){
					if(index_webUi==0)
						index_webUi=i;
					String[] kv=opzioni.get(i).split("=");
					switch(kv[0]){
						case "WebUI\\Enabled":
							webUiEnabledFound=true;
							if(!Boolean.parseBoolean(kv[1])){
								modificato=true;
								opzioni.remove(i);
								opzioni.add(i, "WebUI\\Enabled=true");
							}
							break;
						case "WebUI\\HTTPS\\Enabled":
							webUiHttpsFound=true;
							if(Boolean.parseBoolean(kv[1])){
								modificato=true;
								opzioni.remove(i);
								opzioni.add(i, "WebUI\\HTTPS\\Enabled=false");
							}
							break;
						case "WebUI\\LocalHostAuth":
							webUiLocalFound=true;
							if(Boolean.parseBoolean(kv[1])){
								modificato=true;
								opzioni.remove(i);
								opzioni.add(i, "WebUI\\LocalHostAuth=false");
							}
							break;
					}
				}
			}
			if(!prefFound){
				opzioni.add("[Preferences]");
				opzioni.add("WebUI\\Enabled=true");
				opzioni.add("WebUI\\Port=8080");
				opzioni.add("WebUI\\HTTPS\\Enabled=false");
				opzioni.add("WebUI\\LocalHostAuth=false");
				opzioni.add("WebUI\\Username=admin");
				modificato=true;
			}
			else {
				if(!webUiEnabledFound){
					opzioni.add(index_webUi++,"WebUI\\Enabled=true");
					modificato=true;
				}
				if(!webUiHttpsFound){
					opzioni.add(index_webUi++,"WebUI\\HTTPS\\Enabled=false");
					modificato=true;
				}
				if(!webUiLocalFound){
					opzioni.add(index_webUi, "WebUI\\LocalHostAuth=false");
					modificato=true;
				}
			}
			return modificato;
		}
		else {
			NSDictionary opzioni = (NSDictionary)opzioniO;
			opzioni.put("Preferences.WebUI.Enabled", new NSNumber(true));
			opzioni.put("Preferences.WebUI.HTTPS.Enabled",new NSNumber(false));
			opzioni.put("Preferences.WebUI.LocalHostAuth",new NSNumber(false));
			return true;
		}
	}
	@SuppressWarnings("unchecked")
	private void salvaOpzioni(Object opzioniO) throws IOException {
		if(Os.isWindows() || Os.isLinux()){
			ArrayList<String> opzioni = (ArrayList<String>)opzioniO;
			FileWriter file = new FileWriter(pathConfig+File.separator+configFile);
			for(int i=0;i<opzioni.size();i++){
				file.append(opzioni.get(i)+"\n");
			}
			file.close();
		}
		else {
			NSDictionary opzioni = (NSDictionary)opzioniO;
			PropertyListParser.saveAsBinary(opzioni, new File(pathConfig+File.separator+configFile));
		}
	}

	private int getQBittorrentPID() {
		int pid = -1;
		if (Os.isWindows()) {
			try {
				String line;
				String[] cmd = { System.getenv("windir") + File.separator + "system32" + File.separator + "tasklist.exe", "/fo", "csv", "/nh" };
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					String[] list = line.split(",");
					if (list[0].replace("\"", "").toLowerCase().compareTo("qbittorrent.exe") == 0) {
						pid = Integer.parseInt(list[1].replace("\"", "").trim());
						break;
					}
				}
				input.close();
			}
			catch (Exception err) {
				err.printStackTrace();
			}
		}
		else if(Os.isLinux() || Os.isMacOS()){
			try {
				String line;
				String[] cmd = {"ps", "-e", "-c"};
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					line=line.replace("\t"," ");
					Scanner scanner = new Scanner(line);
					ArrayList<String> list=new ArrayList<String>();
					while(scanner.hasNext()){
						list.add(scanner.next().trim());	
					}
					scanner.close();
					if(!list.isEmpty()){
						if(list.get(list.size()-1).toLowerCase().compareTo("qbittorrent")==0){
							pid=Integer.parseInt(list.get(0));
							list.clear();
							break;					
						}
					}
					list.clear();
				}
				input.close();
			}
			catch (Exception err) {
				err.printStackTrace();
			}
		}
		return pid;
	}
	private void chiudiApplicazione(int pid){
		if(Os.isWindows()){
			String[] cmd = {
				System.getenv("windir") + File.separator + "system32" + File.separator + "taskkill.exe",
				"/PID",
				""+pid,
				"/F"
			};
			try {
				Runtime.getRuntime().exec(cmd).waitFor();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(Os.isLinux() || Os.isMacOS()) {
			String[] cmd = {
					"kill",
					"-9",
					""+pid
			};
			try {
				Runtime.getRuntime().exec(cmd).waitFor();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private boolean isWebServiceOnline(){
		try {
			HttpOperations.GET_withBoolean("http://"+address+":"+port+"/json/torrents");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public String auth(String username, String pass) {
		return "";
	}

	private int compareVersion(String c1, String c2){
		if(c1.compareTo(c2)==0)
			return 0;
		String[] numeri1 = c1.split(".");
		String[] numeri2 = c2.split(".");
		for(int i=0;i<numeri1.length && i<numeri2.length;i++){
			Integer n1 = Integer.parseInt(numeri1[i]);
			Integer n2 = Integer.parseInt(numeri2[i]);
			if(n1>n2)
				return 1;
		}
		return -1;
	}
	
	public static String rilevaInstallazione(){
		if(Os.isWindows()){
			if(Os.is32bit()){
				try{
    				if(Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, "Software\\qbittorrent", "InstallLocation")){
        				String dirQB = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\qbittorrent", "InstallLocation");
        				if(OperazioniFile.fileExists(dirQB+File.separator+"qbittorrent.exe"))
        					return dirQB+File.separator+"qbittorrent.exe";
    				}
				}
				catch(Exception e){}
			}
			else {
    			try{	
    				if(Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\qbittorrent", "InstallLocation")){
        				String dirQB = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\qbittorrent", "InstallLocation");
        				if(OperazioniFile.fileExists(dirQB+File.separator+"qbittorrent.exe"))
        					return dirQB+File.separator+"qbittorrent.exe";
    				}
    			}
    			catch(Exception e){}
			}
		}
		else if(Os.isLinux()){
			String path = "/usr/bin/qbittorrent";
			if(OperazioniFile.fileExists(path))
				return path;
			else
				System.out.println("qbittorrent non trovato");
		}
		else if(Os.isMacOS()){
			return "qbittorrent.app";
		}
		return null;
	}
}
