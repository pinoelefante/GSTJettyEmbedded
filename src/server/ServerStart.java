package server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import server.settings.SettingsController;

public class ServerStart {
	private static JettyServer jettyServer;
	
	public static void main(String[] args) {
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		try {
			jettyServer.start();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);;
		}
		
		SettingsController.getInstance().setUTorrentPath("C:\\Users\\pinoe\\AppData\\Roaming\\uTorrent\\uTorrent.exe");
		SettingsController.getInstance().setDownloadDir("D:\\SerieTV");
		SettingsController.getInstance().setVLCPath("C:\\Program Files\\VideoLAN\\VLC\\vlc.exe");
	}
}
