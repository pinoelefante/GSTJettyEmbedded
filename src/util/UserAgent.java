package util;

import util.os.Os;
import gst.programma.Settings;

public class UserAgent {
	public final static String OPENSUBTITLES_UA = "Gestione Serie TV (Jetty)";
	public static String DEFAULT_UA = ""; 
	public static String get(){
		if(DEFAULT_UA.isEmpty()){
    		Settings s = Settings.getInstance();
    		String os = Os.getOSName();
    		DEFAULT_UA = "Gestione Serie TV (Jetty)/rel."+s.getVersioneSoftware() + (!os.isEmpty()?" ("+os+"/"+Os.getVMArch()+")":"");
		}
		return DEFAULT_UA;
	}
	public static String getOpenSubtitles(){
		Settings s = Settings.getInstance();
		return "Gestione Serie TV (Jetty) v"+s.getVersioneSoftware();
	}
}
