package server;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import gst.database.Database;
import gst.gui.InterfacciaGrafica;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.programma.importer.Importer;
import gst.serieTV.GestioneSerieTV;
import gst.services.SearchListener;
import gst.services.ThreadRicercaEpisodi;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import util.httpOperations.HttpOperations;


public class ServerStart {
	public static void main(String[] args) {
		
		try {
    		if(HttpOperations.GET_withBoolean("http://localhost:8585/OperazioniSistemaServlet?action=isOpen")){
    			System.out.println("Un'altra istanza è in esecuzione");
    			HttpOperations.GET_withBoolean("http://localhost:8585/OperazioniSistemaServlet?action=show");
    			System.exit(0);
    		}
		}
		catch(Exception e){}
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		final JettyServer jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		try {
			jettyServer.start();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		final Settings settings = Settings.getInstance();
		
		final InterfacciaGrafica ui = InterfacciaGrafica.getInstance();
		
		Database.Connect();
		
		final GestioneSerieTV gst = GestioneSerieTV.getInstance();
		
		String dbPath = Settings.getInstance().getUserDir()+File.separator+"database2.sqlite";
		if(settings.isFirstStart() && OperazioniFile.fileExists(dbPath)){
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
		
		ThreadRicercaEpisodi t_search = new ThreadRicercaEpisodi(settings.getMinRicerca());
		t_search.subscribe(ui);
		t_search.addSearchListener(new SearchListener() {
			
			@Override
			public void searchStart() {
				ui.sendNotify("Inizio la ricerca di nuovi episodi");
			}
			
			@Override
			public void searchEnd() {}

			@Override
			public void searchFirstEnd() {
				if(!settings.isStartHidden()){
					if(Desktop.isDesktopSupported()){
		    			Desktop d = Desktop.getDesktop();
		    			try {
							d.browse(new URI("http://localhost:8585"));
						}
						catch (IOException | URISyntaxException e) {
							ui.sendNotify("Per aprire l'interfaccia di Gestione Serie TV, visita l'indirizzo 'http://localhost:8585' nel tuo browser web");
							e.printStackTrace();
						}
					}
					else {
						ui.sendNotify("Per aprire l'interfaccia di Gestione Serie TV, visita l'indirizzo 'http://localhost:8585' nel tuo browser web");
					}
				}
			}
		});
		t_search.start();
		
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
