package gst.player;

import java.io.File;
import java.io.IOException;

import util.os.Os;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import gst.programma.OperazioniFile;
import gst.programma.Settings;

    public class VLC implements VideoPlayer {
    	private String pathExe;
    	
    	public VLC(String vlcPath) {
    		pathExe=vlcPath;
		}
		public static String rilevaVLC(){
    		if(Os.isWindows()){
    			try {
    				if(Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, "Software\\VideoLAN\\VLC", "")){
        				String dirVLC = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\VideoLAN\\VLC", "");
        				if(OperazioniFile.fileExists(dirVLC))
        					return dirVLC;
        			}
    			}
    			catch(Exception e){
    				e.printStackTrace();
    			}
    			
    			String path=null;
				if(Os.is32bit()){
    				path = System.getenv("PROGRAMFILES")+File.separator+"VideoLAN"+File.separator+"VLC"+File.separator+"vlc.exe";
    				if(OperazioniFile.fileExists(path))
    					return path;
    			}
    			else {
    				path = System.getenv("PROGRAMFILES")+File.separator+"VideoLAN"+File.separator+"VLC"+File.separator+"vlc.exe";
    				if(OperazioniFile.fileExists(path))
    					return path;
    				path = System.getenv("PROGRAMFILES(x86)")+File.separator+"VideoLAN"+File.separator+"VLC"+File.separator+"vlc.exe";
    				if(OperazioniFile.fileExists(path))
    					return path;
    			}
				path = Settings.getInstance().getCurrentDir() + File.separator + "vlc"+File.separator+"vlc.exe";
				if(OperazioniFile.fileExists(path))
					return path;
    		}
    		else if(Os.isLinux()){
    			if(OperazioniFile.fileExists("/usr/bin/vlc"))
    				return "/usr/bin/vlc";
    		}
    		else if(Os.isMacOS()){
    			
    		}
    		return null;
    	}
    	public static void main(String[] args){
    		Settings.getInstance();
    		System.out.println(rilevaVLC());
    	}
		@Override
		public boolean playVideo(String pathVideo) {
			String[] cmd = {
					pathExe,
					"\"file:///"+pathVideo+"\"",
					"-f",
					"--disable-screensaver",
					"--no-video-title-show",
					"--one-instance"
			};
			try {
				Runtime.getRuntime().exec(cmd);
				return true;
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
}
