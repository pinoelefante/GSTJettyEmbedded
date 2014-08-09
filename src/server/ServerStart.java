package server;

import java.awt.Desktop;
import java.net.URI;

import gst.database.Database;
import gst.gui.InterfacciaGrafica;
import gst.programma.Settings;
import gst.programma.importer.Importer;
import gst.serieTV.GestioneSerieTV;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;


public class ServerStart {
	public static void main(String[] args) throws Exception{
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		final JettyServer jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		jettyServer.start();
		
		Settings settings = Settings.getInstance();
		
		final InterfacciaGrafica ui = InterfacciaGrafica.getInstance();
		
		Database.Connect();
		
		final GestioneSerieTV gst = GestioneSerieTV.getInstance();
		
		if(settings.isFirstStart()){
			//TODO chiedere se si vogliono importare i vecchi episodi
			Importer importer = new Importer();
			importer.subscribe(ui);
			importer.startImport();
			importer.unsubscribe(ui);
			
			//TODO mostrare finestra che fa scegliere il percorso di download
			
			settings.setFirstStart(false);
			settings.salvaSettings();
		}
		
		gst.init();
		
		gst.subscribe(ui);
		
		if(!settings.isStartHidden()){
			Desktop d = Desktop.getDesktop();
			d.browse(new URI("http://localhost:8585"));
		}
		
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if(jettyServer.isStarted()) {
					try {
						jettyServer.stop();
						Database.Disconnect();
						gst.unsubscribe(ui);
						ui.removeTray();
					} 
					catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		},"Stop Jetty Hook")); 
	}
}
