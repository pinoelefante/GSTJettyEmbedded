package server;

import gst.database.Database;
import gst.programma.Settings;
import gst.serieTV.GestioneSerieTV;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;


public class ServerStart {
	public static void main(String[] args) throws Exception{
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		final JettyServer jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if(jettyServer.isStarted()) {
					try {
						jettyServer.stop();
					} 
					catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		},"Stop Jetty Hook")); 
		jettyServer.start();
		
		Settings.baseSettings();
		Database.Connect();
		GestioneSerieTV m = GestioneSerieTV.getInstance();
	}
}
