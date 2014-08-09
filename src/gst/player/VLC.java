package gst.player;

import gst.programma.OperazioniFile;
import gst.programma.Settings;

    public class VLC implements VideoPlayer {
    	
    	public String rilevaVLC(){
    		if(Settings.getInstance().isWindows()){
    			//TODO HKEY_LOCAL_MACHINE\SOFTWARE\VideoLAN\VLC
    			
    		}
    		else if(Settings.getInstance().isLinux()){
    			if(OperazioniFile.fileExists("/usr/bin/vlc"))
    				return "/usr/bin/vlc";
    		}
    		else if(Settings.getInstance().isMacOS()){
    			
    		}
    		return "";
    	}
}
