package util;

import util.os.Os;
import gst.programma.Settings;

public class UserAgent {
	public static String get(){
		Settings s = Settings.getInstance();
		String os = Os.getOSName();
		return "Gestione Serie TV (Jetty)/rel."+s.getVersioneSoftware() + (!os.isEmpty()?" ("+os+"/"+Os.getVMArch()+")":"");
	}
}
