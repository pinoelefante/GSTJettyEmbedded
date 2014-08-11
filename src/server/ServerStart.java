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
			if(ui.showConfirmDialog("Importa", "Vuoi importare i dati da una versione precedente di Gestione Serie TV?")){
    			Importer importer = new Importer();
    			importer.subscribe(ui);
    			importer.startImport();
    			importer.unsubscribe(ui);
			}
			if(ui.showConfirmDialog("Opzioni", "Vuoi modificare le impostazioni predefinite dell'applicazione?"))
				ui.mostraFinestraOpzioni();
			
			settings.setFirstStart(false);
			settings.salvaSettings();
		}
		
		gst.init();
		
		gst.subscribe(ui);
		
		if(!settings.isStartHidden()){
			if(Desktop.isDesktopSupported()){
    			Desktop d = Desktop.getDesktop();
    			d.browse(new URI("http://localhost:8585"));
			}
			else {
				ui.sendNotify("Per aprire l'interfaccia di Gestione Serie TV, visita l'indirizzo 'http://localhost:8585' nel tuo browser web");
			}
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
