package gst.sottotitoli;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.PreferenzeSottotitoli;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.services.TaskAggiornaElenchi;
import gst.services.TaskAssociaSerie;
import gst.services.TaskRicercaSottotitoli;
import gst.sottotitoli.addic7ed.Addic7ed;
import gst.sottotitoli.italiansubs.ItalianSubs;
import gst.sottotitoli.localhost.LocalSubs;
import gst.sottotitoli.opensubtitles.OpenSubtitles;
import gst.sottotitoli.podnapisi.Podnapisi;
import gst.sottotitoli.subspedia.Subspedia;
import gst.sottotitoli.subspedia.Subspedia2;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom.Document;
import org.jdom.Element;

public class GestoreSottotitoli implements Notifier{
	private static GestoreSottotitoli instance;

	public final static int LOCALE=0, ITASA=1, SUBSFACTORY=2, SUBSPEDIA=3, PODNAPISI=4, OPENSUBTITLES=5, ADDIC7ED=6; 
	private ProviderSottotitoli itasa;
	//private ProviderSottotitoli subsfactory;
	private ProviderSottotitoli subspedia;
	private ProviderSottotitoli addic7ed;
	//private ProviderSottotitoli podnapisi;
	private ProviderSottotitoli opensubtitles;
	private LocalSubs localsubs;
	private Settings settings;
	private Timer timer;
	private TimerTask aggiornaElenchi, associaSerie, ricercaSottotitoli;
	
	public static GestoreSottotitoli getInstance(){
		if(instance==null)
			instance=new GestoreSottotitoli();
		return instance;
	}
	private GestoreSottotitoli(){
		itasa=ItalianSubs.getInstance();
		//subsfactory=Subsfactory.getInstance();
		subspedia=Subspedia2.getInstance();
		localsubs=LocalSubs.getInstance();
		addic7ed = Addic7ed.getInstance();
		//podnapisi = Podnapisi.getInstance();
		opensubtitles = OpenSubtitles.getInstance();
		notificable=new ArrayList<Notificable>(2);
		settings=Settings.getInstance();
		timer = new Timer();
		timer.scheduleAtFixedRate(aggiornaElenchi=new TaskAggiornaElenchi(), 1000, 43200000); //12 ore
		timer.scheduleAtFixedRate(associaSerie=new TaskAssociaSerie(), 10000, 3600000); //1 ora
		if(settings.isRicercaSottotitoli())
			avviaRicercaAutomatica();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				System.out.println("shutdown hook subs");
				aggiornaElenchi.cancel();
				associaSerie.cancel();
				stopRicercaAutomatica();
				timer.cancel();
				timer.purge();
			}
		});
	}
	public void avviaRicercaAutomatica(){
		if(ricercaSottotitoli==null)
			timer.scheduleAtFixedRate(ricercaSottotitoli=new TaskRicercaSottotitoli(), 30000, 1800000); //30 minuti
	}
	public void stopRicercaAutomatica(){
		if(ricercaSottotitoli!=null){
			ricercaSottotitoli.cancel();
			timer.purge();
		}
	}
	public void aggiornaElenco(int idProvider){
		switch(idProvider){
			case ITASA:
				itasa.aggiornaElencoSerieOnline();
				break;
				/*
			case SUBSFACTORY:
				subsfactory.aggiornaElencoSerieOnline();
				break;
				*/
			case SUBSPEDIA:
				subspedia.aggiornaElencoSerieOnline();
				break;
			case ADDIC7ED:
				addic7ed.aggiornaElencoSerieOnline();
				break;
		}
	}
	public void associaSerie(SerieTV s){
		if(s.getIDItasa()<=0){
			itasa.associaSerie(s);
		}
		/*	
		if(s.getIDDBSubsfactory()<=0){
			subsfactory.associaSerie(s);
		}
		*/
		if(s.getIDSubspedia()<=0){
			subspedia.associaSerie(s);
		}
		if(s.getIDAddic7ed()<=0){
			addic7ed.associaSerie(s);
		}
	}
	public boolean associaSerie(int idSerie, int idProvider, int idSerieSub){
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		if(serie==null)
			return false;
		switch(idProvider){
			case ITASA:
				return itasa.associa(idSerie, idSerieSub);
			/*
			case SUBSFACTORY:
				return subsfactory.associa(idSerie, idSerieSub);
				break;
			*/
			case SUBSPEDIA:
				return subspedia.associa(idSerie, idSerieSub);
			case ADDIC7ED:
				return addic7ed.associa(idSerie, idSerieSub);
		}
		return false;
	}
	public boolean disassociaSerie(int idSerie, int idProvider){
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		if(serie==null)
			return false;
		switch(idProvider){
			case ITASA:
				return itasa.disassocia(idSerie);
				/*
			case SUBSFACTORY:
				return subsfactory.disassocia(idSerie);
				break;
				*/
			case SUBSPEDIA:
				return subspedia.disassocia(idSerie);
			case ADDIC7ED:
				return addic7ed.disassocia(idSerie);
		}
		return false;
	}
	
	public boolean scaricaSottotitolo(int idSub){
		if(idSub<=0)
			return false;
		Episodio ep = ProviderSerieTV.getEpisodio(idSub);
		if(ep==null)
			return false;
		return scaricaSottotitolo(ep);
	}
	public boolean scaricaSottotitolo(Episodio e){
		SerieTV s = ProviderSerieTV.getSerieByID(e.getSerie());
		if(s==null)
			return false;
		PreferenzeSottotitoli p_sub = s.getPreferenzeSottotitoli();
		String[] langs = p_sub.getPreferenze();
		boolean ok= false;
		for(int i=0;i<langs.length;i++){
			if(scaricaSottotitolo(s, e, langs[i], langs.length==1))
				ok=true;
		}
		return ok;
	}
	public boolean scaricaSottotitolo(int idSub, String lang){
		if(idSub<=0)
			return false;
		Episodio ep = ProviderSerieTV.getEpisodio(idSub);
		if(ep==null)
			return false;
		return scaricaSottotitolo(ep, lang);
	}
	public boolean scaricaSottotitolo(Episodio e, String lang){
		SerieTV s = ProviderSerieTV.getSerieByID(e.getSerie());
		if(s==null)
			return false;
		return scaricaSottotitolo(s, e, lang, s.getPreferenzeSottotitoli().getPreferenze().length==1);
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e, String lang, boolean uniqueLang){
		boolean scaricato = true;
		boolean online = true;
		String episodio="S"+(e.getStagione()<10?"0"+e.getStagione():e.getStagione())+"E"+(e.getEpisodio()<10?"0"+e.getEpisodio():e.getEpisodio());
		
		if(localsubs.scaricaSottotitolo(s, e, lang, uniqueLang)){
			online=false;
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+localsubs.getProviderName());
			//inserisciLog(e, localsubs, lang);
		}
		else if(itasa.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+itasa.getProviderName());
			inserisciLog(e, itasa, lang);
		}
		/*
		else if(subsfactory.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+subsfactory.getProviderName());
			inserisciLog(e, subsfactory, lang);
		}
		*/
		else if(subspedia.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+subspedia.getProviderName());
			inserisciLog(e, subspedia, lang);
		}
		else if(addic7ed.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + " "  + episodio + " - Sottotitolo scaricato - "+addic7ed.getProviderName());
			inserisciLog(e, addic7ed, lang);
		}
		/*
		else if(podnapisi.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+podnapisi.getProviderName());
			inserisciLog(e, podnapisi, lang);
		}
		*/
		else if(opensubtitles.scaricaSottotitolo(s, e, lang)) {
			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+opensubtitles.getProviderName());
			inserisciLog(e, opensubtitles, lang);
		}
		else 
			scaricato = false;
		
		if(scaricato){
			String query="UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo=0 WHERE id="+e.getId();
			Database.updateQuery(query);
		}
		if(scaricato && online){
			localsubs.scaricaSottotitolo(s, e, lang, uniqueLang); //rename
		}
		return scaricato;
	}
	private void inserisciLog(Episodio e, ProviderSottotitoli p, String lang){
		String query = "INSERT INTO "+Database.TABLE_LOGSUB+" (episodio, provider, lingua) VALUES ("+e.getId()+","+p.getProviderID()+",\""+lang+"\")";
		Database.updateQuery(query);
	}
	public ArrayList<SerieSub> getElencoSerie(int provider){
		switch(provider){
			case ITASA:
				return itasa.getElencoSerie();
				/*
			case SUBSFACTORY:
				return subsfactory.getElencoSerie();
				*/
			case SUBSPEDIA:
				return subspedia.getElencoSerie();
			case ADDIC7ED:
				return addic7ed.getElencoSerie();
		}
		return null;
	}
	public ProviderSottotitoli getProvider(int provider){
		switch(provider){
			case LOCALE:
				return localsubs;
			case ITASA:
				return itasa;
				/*
			case SUBSFACTORY:
				return subsfactory;
				*/
			case SUBSPEDIA:
				return subspedia;
			case ADDIC7ED:
				return addic7ed;
				/*
			case PODNAPISI:
				return podnapisi;
				*/
			case OPENSUBTITLES:
				return opensubtitles;
		}
		return null;
	}
	
	private ArrayList<Notificable> notificable;
	public void subscribe(Notificable e) {
		notificable.add(e);
	}
	public void unsubscribe(Notificable e) {
		notificable.remove(e);
	}
	public void inviaNotifica(String text) {
		for(int i=0;i<notificable.size();i++)
			notificable.get(i).sendNotify(text);
	}
	public ArrayList<Episodio> getSottotitoliDaScaricare(){
		ArrayList<Episodio> eps = new ArrayList<Episodio>();
		String query = "SELECT * FROM "+Database.TABLE_EPISODI+" WHERE sottotitolo=1";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for(int i=0;i<res.size();i++){
			Episodio e=ProviderSerieTV.parseEpisodio(res.get(i));
			eps.add(e);
		}
		return eps;
	}
	public ArrayList<Entry<Integer, String>> getSottotitoliDaScaricareLangs(){
		ArrayList<Entry<Integer, String>> subDown = new ArrayList<Map.Entry<Integer,String>>();
		String query = "SELECT * FROM "+Database.TABLE_SUBDOWN;
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for(int i=0;i<res.size();i++){
			int id = (int) res.get(i).getValueByKey("episodio");
			String lang = (String) res.get(i).getValueByKey("lingua");
			Entry<Integer, String> e = new AbstractMap.SimpleEntry<Integer,String>(id, lang);
			subDown.add(e);
		}
		return subDown;
	}
	public Document GetSottotitoliDaScaricare()
	{
		String query = "SELECT st.id AS id_serie, ep.id AS id_episodio, st.nome, ep.stagione, ep.episodio FROM episodi AS ep JOIN serietv AS st WHERE ep.serie=st.id AND sottotitolo=1 AND serie IN (SELECT id_serie FROM preferiti) ORDER BY nome, stagione, episodio ASC";
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		Element subs = new Element("episodi");
		for(KVResult<String, Object> row : res)
		{
			Element episodio = new Element("episodio");
			Element idEpisodio = new Element("id_episodio");
			idEpisodio.addContent((int)row.getValueByKey("id_episodio")+"");
			episodio.addContent(idEpisodio);
			
			String nomeSerie = row.getValueByKey("nome").toString();
			int episodeNum = (int)row.getValueByKey("episodio");
			int stagioneNum = (int)row.getValueByKey("stagione");
			String title = String.format("%s - %dx%02d", nomeSerie, stagioneNum, episodeNum);
			Element titolo = new Element("titolo");
			titolo.addContent(title);
			episodio.addContent(titolo);
			
			subs.addContent(episodio);
		}
		root.addContent(subs);
		return new Document(root);
	}
	public Map<ProviderSottotitoli, ArrayList<SerieSub>> getProviders(){
		Map<ProviderSottotitoli, ArrayList<SerieSub>> map = new HashMap<ProviderSottotitoli, ArrayList<SerieSub>>();
		for(int i=1;i<=6;i++){
			ProviderSottotitoli p = GestoreSottotitoli.getInstance().getProvider(i);
			if(p==null)
				continue;
			ArrayList<SerieSub> s = p.getElencoSerie();
			map.put(p, s);
		}
		return map;
	}
	public Document GetProviders()
	{
		String query = "SELECT id,nome,6 AS provider FROM addic7ed UNION SELECT id,nome,1 AS provider FROM itasa UNION SELECT id,nome,3 FROM subspedia ORDER BY provider, nome";
		ArrayList<KVResult<String,Object>> res = Database.selectQuery(query);
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		int currProvider = -1;
		Element providerElem = null;
		for(KVResult<String,Object> row : res)
		{
			int provider = (int)row.getValueByKey("provider");
			if(provider!=currProvider)
			{
				providerElem = new Element("provider");
				providerElem.setAttribute("id_provider", provider+"");
				root.addContent(providerElem);
				currProvider = provider;
			}
			Element serie = new Element("serie");
			Element nomeSerie = new Element("nome");
			nomeSerie.addContent(row.getValueByKey("nome").toString());
			serie.addContent(nomeSerie);
			Element idSerie = new Element("id");
			idSerie.addContent((int)row.getValueByKey("id")+"");
			serie.addContent(idSerie);
			providerElem.addContent(serie);
		}
		return new Document(root);
	}
	public static void rimuoviSub(int id){
		String query = "UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo=0 WHERE id="+id;
		Database.updateQuery(query);
		
		String query2="DELETE FROM "+Database.TABLE_SUBDOWN+" WHERE episodio="+id;
		Database.updateQuery(query2);
	}
	public static void setSottotitoloDownload(int idEpisodio, boolean stato, String lang){
		String query = "UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo="+(stato?1:0)+" WHERE id="+idEpisodio;
		Database.updateQuery(query);
		
		if(stato == true) {
			Episodio ep = ProviderSerieTV.getEpisodio(idEpisodio);
			SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
			String[] langs = serie.getPreferenzeSottotitoli().getPreferenze();
			for(int i=0;i<langs.length;i++){
				String query1 = "INSERT INTO "+Database.TABLE_SUBDOWN + " (episodio, lingua) VALUES ("+idEpisodio+",\""+langs[i]+"\")";
				Database.updateQuery(query1);
			}
		}
		else {
			String query1 = "DELETE FROM "+Database.TABLE_SUBDOWN+" WHERE episodio="+idEpisodio+" AND lingua=\""+lang+"\"";
			Database.updateQuery(query1);
		}
	}
	public Document GetLastLogSub(int limit){
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		
		String query = "SELECT st.nome AS nome_serie,ep.id AS id_episodio, ep.stagione,ep.episodio,log.provider, log.lingua FROM logsub AS log JOIN episodi AS ep JOIN serietv AS st WHERE log.episodio = ep.id AND ep.serie=st.id ORDER BY log.id DESC LIMIT "+limit;
		ArrayList<KVResult<String,Object>>res = Database.selectQuery(query);
		Element subs = new Element("subs");
		for(KVResult<String,Object> row : res)
		{
			Element sub = new Element("sub");
			Element nomeSerie = new Element("nomeSerie");
			nomeSerie.addContent(row.getValueByKey("nome_serie").toString());
			
			Element id_episodio = new Element("id_episodio");
			id_episodio.addContent((int)row.getValueByKey("id_episodio")+"");
			
			Element stagione = new Element("stagione");
			stagione.addContent((int)row.getValueByKey("stagione")+"");
			
			Element episodio = new Element("episodio");
			episodio.addContent((int)row.getValueByKey("episodio")+"");
			
			Element provider = new Element("provider");
			provider.addContent(getProvider((int)row.getValueByKey("provider")).getProviderName());
			
			Element lingua = new Element("lingua");
			lingua.addContent(row.getValueByKey("lingua").toString());
			
			sub.addContent(nomeSerie);
			sub.addContent(id_episodio);
			sub.addContent(stagione);
			sub.addContent(episodio);
			sub.addContent(provider);
			sub.addContent(lingua);
			subs.addContent(sub);
		}
		root.addContent(subs);
		return new Document(root);
	}
	public void close() {
		timer.cancel();
		timer.purge();
	}
	public boolean aggiungiLinguaASerie(int idSerie, String lang){
		SerieTV s = ProviderSerieTV.getSerieByID(idSerie);
		PreferenzeSottotitoli p = s.getPreferenzeSottotitoli();
		p.addPreferenza(lang);
		String langs = p.getPreferenzeU();
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET preferenze_sottotitoli=\""+langs+"\" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}
}