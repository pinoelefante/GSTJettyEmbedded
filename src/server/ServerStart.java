package server;

import java.io.File;

import gst.database.Database;
import gst.gui.InterfacciaGrafica;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.programma.importer.Importer;
import gst.serieTV.GestioneSerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.system.Sistema;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import util.httpOperations.HttpOperations;


public class ServerStart {
	private static JettyServer jettyServer;
	private static InterfacciaGrafica ui;
	private static GestioneSerieTV gst;
	private static GestoreSottotitoli subManager;
	private static Sistema sistema;
	
	public static void main(String[] args) {
		
		try {
    		if(HttpOperations.GET_withBoolean("http://localhost:8585/OperazioniSistemaServlet?action=isOpen")){
    			System.out.println("Un'altra istanza è in esecuzione");
    			HttpOperations.GET_withBoolean("http://localhost:8585/OperazioniSistemaServlet?action=show");
    			System.exit(0);
    		}
		}
		catch(Exception e){}
		
		final Settings settings = Settings.getInstance();
		
		sistema = Sistema.getInstance();
		if(sistema.isUpdateAvailable()){
			sistema.aggiorna();
			System.exit(0);
		}
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		try {
			jettyServer.start();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		ui = InterfacciaGrafica.getInstance();
		
		Database.Connect();
		
		gst = GestioneSerieTV.getInstance();
		
		String dbPath = Settings.getInstance().getUserDir()+File.separator+"database2.sqlite";
		if(settings.isFirstStart() && OperazioniFile.fileExists(dbPath)){
			Importer importer = new Importer();
			boolean importer_ask;
			if(importer_ask=ui.showConfirmDialog("Importa", "Vuoi importare i dati da una versione precedente di Gestione Serie TV?")){
    			importer.subscribe(ui);
    			importer.startImport();
    			importer.unsubscribe(ui);
			}
			if(ui.showConfirmDialog("Opzioni", "Vuoi modificare le impostazioni predefinite dell'applicazione?"))
				ui.mostraFinestraOpzioni();
			if(importer_ask)
				importer.importStage2();
			
			settings.setFirstStart(false);
			settings.salvaSettings();
		}
		
		gst.init(ui);
		subManager=GestoreSottotitoli.getInstance();
		subManager.subscribe(ui);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Shutdown hook - main");
				if(jettyServer.isStarted()) {
					try {
						jettyServer.stop();
					} 
					catch (Exception exception) {
						exception.printStackTrace();
					}
				}
				Database.Disconnect();
				gst.close();
				ui.removeTray();
				System.exit(0);
			}
		},"Stop Jetty Hook")); 
	}
	public static void close(){
		System.out.println("Closing GSTJ");
		if(jettyServer.isStarted()) {
			try {
				jettyServer.stop();
			} 
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		Database.Disconnect();
		gst.close();
		subManager.close();
		ui.removeTray();
		Runtime.getRuntime().halt(0);
	}
}
