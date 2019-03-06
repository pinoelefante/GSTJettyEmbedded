package gst.download;

import java.io.IOException;

import gst.serietv.Torrent;
import server.settings.SettingsController;
import util.OperazioniFile;

public class UTorrent {
	private SettingsController settings;
	private static class SingletonHelper{
		private final static UTorrent INSTANCE = new UTorrent();
	}
	private UTorrent() {
		settings = SettingsController.getInstance();
	}
	public synchronized static UTorrent getInstance()
	{
		return SingletonHelper.INSTANCE;
	}
	
	public synchronized boolean downloadTorrent(Torrent t, String path) {	
		return downloadCLI(t, path);
	}
	private static long nextTorrentCLI = 0L;
	private boolean downloadCLI(Torrent t, String path){
		if(!isUtorrentPathValid())
			return false;
		long diff = nextTorrentCLI - System.currentTimeMillis();
		if(diff > 0)
		{
			try
			{
				Thread.sleep(diff);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
		}
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
	private boolean isUtorrentPathValid() {
		return OperazioniFile.fileExists(settings.getUTorrentPath());
	}
	private String getPathInstallazione() {
		return settings.getUTorrentPath();
	}
}
