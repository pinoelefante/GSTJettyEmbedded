package gst.serieTV;

import gst.database.Database;
import gst.gui.InterfacciaGrafica;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.player.FileFinder;
import gst.player.VideoPlayer;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.services.SearchListener;
import gst.services.TaskRicercaEpisodi;
import gst.sottotitoli.GestoreSottotitoli;
import gst.tda.db.KVResult;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Timer;

public class GestioneSerieTV implements Notifier {
	private static GestioneSerieTV instance;
	private ArrayList<ProviderSerieTV> providers;
	private TaskRicercaEpisodi t_search;
	private Settings settings;
	private Timer timer;
	
	public static GestioneSerieTV getInstance(){
		if(instance==null){
			instance=new GestioneSerieTV();   		
		}
		return instance;
	}
	public void init(Notificable ui){
		for(ProviderSerieTV p : getProviders()){
			p.init();
		}
		
		subscribe(ui);
		
		//Aggiorna l'elenco delle serie
		int count_serie_nuove = 0;
		for(int i=0;i<providers.size();i++){
			providers.get(i).aggiornaElencoSerie();
			count_serie_nuove+=providers.get(i).getElencoSerieNuove().size();
		}
		inviaNotifica("Sono state trovate "+count_serie_nuove+" nuove serie");
		
		//Avvia la ricerca dei nuovi episodi
		t_search = new TaskRicercaEpisodi();
		//timer.scheduleAtFixedRate(t_search, 0, 28800000L);
		t_search.subscribe(ui);
		t_search.addSearchListener(new SearchListener() {
			
			@Override
			public void searchStart() {
				
			}
			
			@Override
			public void searchEnd() {
				if(settings.isDownloadAutomatico()){
					inviaNotifica("Avvio il download dei nuovi episodi");
					ArrayList<SerieTV> preferiti=getElencoSeriePreferite();
					for(int i=0;i<preferiti.size();i++){
						ArrayList<Episodio> eps=getEpisodiDaScaricareBySerie(preferiti.get(i).getIDDb());
						for(int j=0;j<eps.size();j++){
							downloadEpisodio(eps.get(i).getId());
						}
					}
				}
			}

			@Override
			public void searchFirstEnd() {
				if(!settings.isStartHidden()){
					InterfacciaGrafica.getInstance().apriInterfaccia();
				}
			}
		});
	}
	private GestioneSerieTV(){
		providers=new ArrayList<ProviderSerieTV>(1);
		notificable=new ArrayList<Notificable>();
		providers.add(new EZTV());
		settings = Settings.getInstance();
		timer = new Timer();
	}
	public ArrayList<ProviderSerieTV> getProviders(){
		return providers;
	}
	
	public ArrayList<SerieTV> getSerieFromProvider(int id){
		ProviderSerieTV provider=checkProvider(id);
		if(provider==null)
			return null;
		
		while(provider.isUpgrading()){
			try {
				Thread.sleep(300L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return provider.getElencoSerieDB();
	}
	public ArrayList<SerieTV> getSerie(){
		ArrayList<ArrayList<SerieTV>> serie = new ArrayList<ArrayList<SerieTV>>();
		for(int i=0;i<providers.size();i++){
			serie.add(providers.get(i).getElencoSerieDB());
		}
		return Merger.mergeListsSerieTV(serie);
	}
	public ProviderSerieTV checkProvider(int id){
		for(int i=0;i<providers.size();i++)
			if(providers.get(i).getProviderID()==id)
				return providers.get(i);
		return null;
	}
	
	public ArrayList<SerieTV> getSerieNuoveByProvider(int id){
		ProviderSerieTV p = checkProvider(id);
		if(p==null)
			return null;
		return p.getElencoSerieNuove();
	}
	
	public ArrayList<SerieTV> getSerieNuove(){
		ArrayList<ArrayList<SerieTV>> serie=new ArrayList<ArrayList<SerieTV>>();
		for(int i=0;i<providers.size();i++){
			serie.add(providers.get(i).getElencoSerieNuove());
		}
		return Merger.mergeListsSerieTV(serie);
	}
	
	public boolean aggiungiSerieAPreferiti(int idSerie){
		SerieTV st=ProviderSerieTV.getSerieByID(idSerie);
		if(st!=null){
			boolean r = ProviderSerieTV.aggiungiSerieAPreferiti(st);
			if(r && settings.isRicercaSottotitoli())
				GestoreSottotitoli.getInstance().associaSerie(st);
			return r;
		}
		return false;
	}
	
	public boolean aggiornaListeSerie(){
		for(int i=0;i<providers.size();i++){
			providers.get(i).aggiornaElencoSerie();
		}
		return true;
	}
	
	public ArrayList<SerieTV> getElencoSeriePreferite(){
		String query = "SELECT * FROM preferiti pref JOIN serietv serie ON pref.id_serie = serie.id ORDER BY serie.nome ASC";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<SerieTV> preferiti = new ArrayList<>();
		for(int i=0;i<res.size();i++){
			SerieTV s = ProviderSerieTV.parseSerie(res.get(i));
			preferiti.add(s);
		}
		return preferiti;
	}
	public boolean rimuoviSeriePreferita(int id, boolean removeEpisodi){
		return ProviderSerieTV.removeSerieDaPreferiti(id, removeEpisodi);
	}
	public void aggiornaEpisodiSerie(int idSerie, int idProvider){
		ProviderSerieTV p = checkProvider(idProvider);
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		inviaNotifica("Aggiorno gli episodi di: "+serie.getNomeSerie());
		p.caricaEpisodiOnline(serie);
	}
	public ArrayList<Episodio> getEpisodiDaScaricareBySerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiDaScaricare(idSerie);
		return episodi;
	}
	public ArrayList<Episodio> getEpisodiSerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiSerie(idSerie);
		return episodi;
	}
	
	public boolean downloadEpisodio(int idEp) {
		boolean status = ProviderSerieTV.downloadEpisodio(idEp);
		if(status){
			if(settings.isRicercaSottotitoli()){
				GestoreSottotitoli.setSottotitoloDownload(idEp, true, "");
			}
		}
		return status;
	}
	public Torrent getLinkDownload(int idEp){
		Episodio ep = ProviderSerieTV.getEpisodio(idEp);
		if(ep==null)
			return null;
		SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
		if(serie==null)
			return null;
		Torrent t=ProviderSerieTV.searchTorrent(serie.getPreferenze(), ep.getLinks()).get(0);
		return t;
	}
	
	public boolean deleteEpisodio(int idEp){
		Episodio ep = ProviderSerieTV.getEpisodio(idEp);
		if(ep==null)
			return false;
		SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
		if(serie==null)
			return false;
		int deleted = 0;
		ArrayList<File> files=new ArrayList<File>();
		files.addAll(FileFinder.getInstance().cercaFileVideo(serie, ep));
		files.addAll(FileFinder.getInstance().cercaFileVideo(serie, ep));
		int fileTrovati = files.size();
		for(int i=0;i<files.size();i++){
			if(files.get(i).delete())
				deleted++;
		}
		files.clear();
		files=null;
		return deleted > 0 && fileTrovati>0;
	}
	
	public boolean playVideo(int idEp){
		Episodio ep = ProviderSerieTV.getEpisodio(idEp);
		SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
		ArrayList<File> files= FileFinder.getInstance().cercaFileVideo(serie, ep);
		if(files.size()==0){
			ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.RIMOSSO);
			return false;
		}
		else {
			VideoPlayer videoPlayer;
			try {
				if(settings.isRicercaSottotitoli() && FileFinder.getInstance().cercaFileSottotitoli(serie, ep, files.get(0).getName()).size()==0){
					inviaNotifica("Attendere la ricerca dei sottotitoli...");
					boolean f=GestoreSottotitoli.getInstance().scaricaSottotitolo(ep);
					if(!f)
						inviaNotifica("Sottotitoli non trovati");
				}
				videoPlayer = Settings.getInstance().getVideoPlayer();
				videoPlayer.playVideo(files.get(0).getAbsolutePath());
				if(ep.getStatoVisualizzazione()!=Episodio.VISTO)
					ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.VISTO);
				return true;
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	private ArrayList<Notificable> notificable;
	public void subscribe(Notificable e) {
		if(e!=null)
			notificable.add(e);
	}
	public void unsubscribe(Notificable e) {
		notificable.remove(e);
	}
	public void inviaNotifica(String text){
		for(int i=0;i<notificable.size();i++){
			notificable.get(i).sendNotify(text);
		}
	}
	public void close(){
		timer.cancel();
		timer.purge();
	}
	public ArrayList<Entry<SerieTV, ArrayList<Episodio>>> getEpisodiDaVedere(){
		ArrayList<Entry<SerieTV, ArrayList<Episodio>>> results = new ArrayList<>();
		for(SerieTV st: getElencoSeriePreferite()){
			String query = "SELECT * FROM episodi WHERE serie="+st.getIDDb()+" AND stato_visualizzazione=1 "+(settings.isRicercaSottotitoli()?("AND sottotitolo=0"):"")+" ORDER BY stagione, episodio ASC";
			ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
			ArrayList<Episodio> eps = new ArrayList<Episodio>();
			for(int i=0;i<res.size();i++){
				Episodio ep = ProviderSerieTV.parseEpisodio(res.get(i));
				eps.add(ep);
			}
			Entry<SerieTV, ArrayList<Episodio>> entry = new AbstractMap.SimpleEntry<SerieTV, ArrayList<Episodio>>(st, eps);
			results.add(entry);
			
		}
		return results;
	}
	public boolean ignoraEpisodio(int idEp){
		Episodio ep = ProviderSerieTV.getEpisodio(idEp);
		if(ep.getStatoVisualizzazione()==Episodio.SCARICARE){
			return ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.IGNORATO);
		}
		return false;
	}
	public boolean deleteFolderSerie(int idSerie){
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		String path = settings.getDirectoryDownload()+serie.getFolderSerie();
		return OperazioniFile.DeleteDirectory(new File(path));
	}
	public boolean setSerieNonSelezionabile(int idSerie, boolean s){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET escludi_seleziona_tutto="+(s?1:0)+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}
	public boolean setLingueSub(int idSerie, String lingue){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET preferenze_sottotitoli=\""+lingue+"\" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}
}
