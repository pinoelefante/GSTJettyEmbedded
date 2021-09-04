package gst.serieTV;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.gui.InterfacciaGrafica;
import gst.infoManager.thetvdb.SerieTVDB;
import gst.infoManager.thetvdb.TheTVDB;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.player.VideoPlayer;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.services.SearchListener;
import gst.services.TaskRicercaEpisodi;
import gst.sottotitoli.GestoreSottotitoli;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.jdom.Document;
import org.jdom.Element;

import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;

public class GestioneSerieTV implements Notifier {
	private static GestioneSerieTV instance;
	private ArrayList<ProviderSerieTV> providers;
	private EZTV eztv;
	private ShowRSS showrss;
	private ShowRSS2 showrss2;
	private TaskRicercaEpisodi t_search;
	private Settings settings;
	private Timer timer;
	
	public static GestioneSerieTV getInstance(){
		if(instance==null){
			instance=new GestioneSerieTV();   		
		}
		return instance;
	}
	public void init(Notificable ui, boolean updateEpisodi){
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
		System.out.println("Aggiorna episodi = "+updateEpisodi);
		if(updateEpisodi){
    		t_search = new TaskRicercaEpisodi();
    		t_search.addSearchListener(new SearchListener() {
    			public void searchStart() {}
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
    			public void searchFirstEnd() {
    				if(!settings.isStartHidden()){
    					InterfacciaGrafica.getInstance().apriInterfaccia();
    				}
    			}
    		});
    		timer.scheduleAtFixedRate(t_search, 0, 28800000L);
    		t_search.subscribe(ui);
		}
	}
	private GestioneSerieTV(){
		providers=new ArrayList<ProviderSerieTV>(1);
		notificable=new ArrayList<Notificable>();
		eztv = new EZTV();
		providers.add(eztv);
		showrss = new ShowRSS();
		providers.add(showrss);
		showrss2 = new ShowRSS2();
		providers.add(showrss2);
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
		switch(id){
			case 1:
				return eztv;
			case 2:
				return showrss;
			case 3:
				return showrss2;
		}
		return null;
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
			if (s.getIDTvdb() > 0) {
				SerieTVDB serieTVDB = TheTVDB.getInstance().caricaSerieMain(s.getIDTvdb());
				if (serieTVDB != null) {
					s.setVoto(serieTVDB.getRating());
				}
			}
			preferiti.add(s);
		}
		return preferiti;
	}
	public boolean rimuoviSeriePreferita(int id, boolean removeEpisodi){
		boolean ok=ProviderSerieTV.removeSerieDaPreferiti(id, removeEpisodi);
		if(ok){
			String q = "DELETE FROM "+Database.TABLE_SUBDOWN+" WHERE episodio IN (SELECT list.episodio FROM list_subdown AS list JOIN episodi AS ep ON list.episodio=ep.id AND ep.serie=?)";
			Database.updateQuery(q, id);
		}
		return ok;
	}
	public void aggiornaEpisodiSerie(int idSerie, int idProvider){
		ProviderSerieTV p = checkProvider(idProvider);
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		inviaNotifica(p.getProviderName()+": Aggiorno gli episodi di: "+serie.getNomeSerie());
		p.caricaEpisodiOnline(serie);
		if(p==eztv)
		{
			if(serie.getIDKarmorra()>0)
			{
    			showrss.caricaEpisodiOnline(serie);
    			inviaNotifica(showrss.getProviderName()+": Aggiorno gli episodi di: "+serie.getNomeSerie());
			}
			if(serie.GetIdShowRss()>0)
			{
				showrss2.caricaEpisodiOnline(serie);
				inviaNotifica(showrss.getProviderName()+": Aggiorno gli episodi di: "+serie.getNomeSerie());
			}
		}
	}
	public int GetNumEpisodiDaScaricare()
	{
		return Database.getCount("SELECT COUNT(*) FROM episodi WHERE stato_visualizzazione = 0 AND serie IN (SELECT id_serie FROM preferiti)");
	}
	public Document GetEpisodiDaScaricare()
	{
		
		String query = "SELECT st.id AS id_serie,ep.id AS id_episodio,st.nome, ep.stagione,ep.episodio, st.escludi_seleziona_tutto AS escludi_selezione FROM episodi AS ep JOIN serietv AS st WHERE ep.serie=st.id AND  ep.serie IN (SELECT id_serie FROM preferiti) AND stato_visualizzazione = 0 ORDER BY escludi_selezione, st.nome, ep.stagione,ep.episodio";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		int idCurrSerie = -1;
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		Element serieElem = null;
		for(KVResult<String, Object> row : res)
		{
			int idSerie = (int)row.getValueByKey("id_serie");
			if(idCurrSerie != idSerie)
			{
				serieElem = new Element("serie");
				serieElem.setAttribute("id", idSerie+"");
				serieElem.setAttribute("nome", row.getValueByKey("nome").toString());
				serieElem.setAttribute("noselect", ((int)row.getValueByKey("escludi_selezione")==1?true:false)+"");
				root.addContent(serieElem);
				idCurrSerie = idSerie;
			}
			Element episodio = new Element("episodio");
			Element titolo=new Element("titolo");
			Element id=new Element("id");
			int epNum = (int)row.getValueByKey("episodio");
			titolo.addContent(row.getValueByKey("nome").toString()+" "+(int)row.getValueByKey("stagione")+"x"+(epNum<10?"0"+epNum:epNum));
			id.addContent((int)row.getValueByKey("id_episodio")+"");
			episodio.addContent(titolo);
			episodio.addContent(id);
			serieElem.addContent(episodio);
		}
		return new Document(root);
	}
	public ArrayList<Episodio> getEpisodiDaScaricareBySerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiDaScaricare(idSerie);
		return episodi;
	}
	public ArrayList<Episodio> getEpisodiSerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiSerieLite(idSerie);
		return episodi;
	}
	public Map<SerieTV,ArrayList<Episodio>> getEpisodiPreferiteAll(){
		HashMap<SerieTV,ArrayList<Episodio>> list = new HashMap<>();
		for(SerieTV st : getElencoSeriePreferite()){
			ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiSerieLite(st.getIDDb());
			list.put(st, episodi);
		}
		return list;
	}
	public boolean downloadEpisodio(int idEp) {
		boolean status = false;
		try {
			status = ProviderSerieTV.downloadEpisodio(idEp);
		}
		catch (DirectoryNotAvailableException e) {
			e.printStackTrace();
			inviaNotifica("Errore durante il download: controlla che i dischi siano collegati e che lo spazio sia sufficiente");
			return false;
		}
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
		files.addAll(DirectoryManager.getInstance().cercaFileVideo(serie, ep));
		files.addAll(DirectoryManager.getInstance().cercaFileVideo(serie, ep));
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
		ArrayList<File> files= DirectoryManager.getInstance().cercaFileVideo(serie, ep);
		if(files.size()==0){
			ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.RIMOSSO);
			return false;
		}
		else {
			VideoPlayer videoPlayer;
			try {
				if(settings.isRicercaSottotitoli() && DirectoryManager.getInstance().cercaFileSottotitoli(serie, ep, files.get(0).getName()).size()==0){
					inviaNotifica("Attendere la ricerca dei sottotitoli...");
					boolean f=GestoreSottotitoli.getInstance().scaricaSottotitolo(ep);
					if(!f)
						inviaNotifica("Sottotitoli non trovati");
				}
				videoPlayer = Settings.getInstance().getVideoPlayer();
				if(videoPlayer.canPlayVideo()){
    				videoPlayer.playVideo(files.get(0).getAbsolutePath());
    				if(ep.getStatoVisualizzazione()!=Episodio.VISTO)
    					ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.VISTO);
    				return true;
				}
				else{
					InterfacciaGrafica.getInstance().sendNotify("Verifica il percorso del video player");
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	public String getVideoFile(int idEp)
	{
		Episodio ep = ProviderSerieTV.getEpisodio(idEp);
		SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
		ArrayList<File> files= DirectoryManager.getInstance().cercaFileVideo(serie, ep);
		if(files.size()==0)
		{
			ProviderSerieTV.changeStatusEpisodio(idEp, Episodio.RIMOSSO);
			return null;
		}
		return files.get(0).getAbsolutePath();
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
	public Document GetEpisodiDaVedere()
	{
		String query = "SELECT st.id AS id_serie, st.nome, ep.id AS id_episodio, ep.stagione, ep.episodio FROM episodi AS ep JOIN serietv AS st WHERE ep.serie IN (SELECT id_serie FROM preferiti ) AND ep.serie=st.id AND stato_visualizzazione=1 AND sottotitolo=0 ORDER BY st.nome, ep.stagione, ep.episodio";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		int idCurrSerie = -1;
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		Element serieElem = null;
		for(KVResult<String, Object> row : res)
		{
			int idSerie = (int)row.getValueByKey("id_serie");
			if(idCurrSerie != idSerie)
			{
				serieElem = new Element("serie");
				serieElem.setAttribute("id", idSerie+"");
				serieElem.setAttribute("nome", row.getValueByKey("nome").toString());
				root.addContent(serieElem);
				idCurrSerie = idSerie;
			}
			Element episodio = new Element("episodio");
			Element titolo=new Element("titolo");
			Element id=new Element("id");
			int epNum = (int)row.getValueByKey("episodio");
			titolo.addContent(row.getValueByKey("nome").toString()+" "+(int)row.getValueByKey("stagione")+"x"+(epNum<10?"0"+epNum:epNum));
			id.addContent((int)row.getValueByKey("id_episodio")+"");
			episodio.addContent(titolo);
			episodio.addContent(id);
			serieElem.addContent(episodio);
		}
		return new Document(root);
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
		String path2 = settings.getDirectoryDownload2()+serie.getFolderSerie();
		boolean op = OperazioniFile.DeleteDirectory(new File(path));
		OperazioniFile.DeleteDirectory(new File(path2));
		String resetEp="UPDATE "+Database.TABLE_EPISODI+" SET stato_visualizzazione=0, sottotitolo=0 WHERE serie=?";
		Database.updateQuery(resetEp,idSerie);
		return op;
	}
	public boolean setSerieNonSelezionabile(int idSerie, boolean s){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET escludi_seleziona_tutto=? WHERE id=?";
		return Database.updateQuery(query,(s?1:0),idSerie);
	}
	public boolean setLingueSub(int idSerie, String lingue){
		SerieTV serie=ProviderSerieTV.getSerieByID(idSerie);
		PreferenzeSottotitoli ps=serie.getPreferenzeSottotitoli();
		ArrayList<String> lingueNuove = ps.getNewLangs(lingue);
		ArrayList<String> rimosse = ps.getRemovedLangs(lingue);
		
		if(rimosse.size()>0){
			for(int i=0;i<rimosse.size();i++){
				String q = "DELETE FROM "+Database.TABLE_SUBDOWN+" WHERE episodio IN (SELECT list.episodio FROM list_subdown AS list JOIN episodi AS ep ON list.episodio=ep.id AND ep.serie=? AND list.lingua=?)";
				Database.updateQuery(q, idSerie, rimosse.get(i));
			}
		}
		if(lingueNuove.size()>0){
			for(int i=0;i<lingueNuove.size();i++){
				String q = "INSERT INTO list_subdown(episodio, lingua) SELECT id, \""+lingueNuove.get(i)+"\" FROM "+Database.TABLE_EPISODI+" WHERE serie=? AND sottotitolo=1";
				Database.updateQuery(q, idSerie);
			}
		}
 		String query = "UPDATE "+Database.TABLE_SERIETV+" SET preferenze_sottotitoli=? WHERE id=?";
		return Database.updateQuery(query, lingue, idSerie);
	}
	public boolean setPreferenzeDownload(int id, int pref_down) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET preferenze_download=? WHERE id=?";
		return Database.updateQuery(query, pref_down, id);
	}
	public boolean associaEpisodioTVDB(int idEpisodio, int idTVDB){
		String query = "UPDATE "+Database.TABLE_EPISODI+" SET id_tvdb=? WHERE id=?";
		return Database.updateQuery(query, idTVDB, idEpisodio);
	}
	public boolean changeDefaultVideoQualityForAll(int pref)
	{
		ArrayList<SerieTV> preferite = getElencoSeriePreferite();
		for(SerieTV serie : preferite)
			setPreferenzeDownload(serie.getIDDb(), pref);
		return true;
	}
}
