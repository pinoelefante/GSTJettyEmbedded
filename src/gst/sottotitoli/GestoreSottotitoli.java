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
import gst.sottotitoli.subsfactory.Subsfactory;
import gst.sottotitoli.subspedia.Subspedia;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import util.Object4Value;

public class GestoreSottotitoli implements Notifier{
	private static GestoreSottotitoli instance;

	public final static int LOCALE=0, ITASA=1, SUBSFACTORY=2, SUBSPEDIA=3, PODNAPISI=4, OPENSUBTITLES=5, ADDIC7ED=6; 
	private ProviderSottotitoli itasa;
	private ProviderSottotitoli subsfactory;
	private ProviderSottotitoli subspedia;
	private ProviderSottotitoli addic7ed;
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
		subsfactory=Subsfactory.getInstance();
		subspedia=Subspedia.getInstance();
		localsubs=LocalSubs.getInstance();
		addic7ed = Addic7ed.getInstance();
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
			timer.scheduleAtFixedRate(ricercaSottotitoli=new TaskRicercaSottotitoli(), 30000, 900000); //15 minuti
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
			case SUBSFACTORY:
				subsfactory.aggiornaElencoSerieOnline();
				break;
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
			
		if(s.getIDDBSubsfactory()<=0){
			subsfactory.associaSerie(s);
		}
		
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
			case SUBSFACTORY:
				return subsfactory.associa(idSerie, idSerieSub);
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
			case SUBSFACTORY:
				return subsfactory.disassocia(idSerie);
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
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+localsubs.getProviderName());
			//inserisciLog(e, localsubs, lang);
		}
		else if(itasa.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+itasa.getProviderName());
			inserisciLog(e, itasa, lang);
		}
		else if(subsfactory.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subsfactory.getProviderName());
			inserisciLog(e, subsfactory, lang);
		}
		else if(subspedia.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subspedia.getProviderName());
			inserisciLog(e, subspedia, lang);
		}
		else if(addic7ed.scaricaSottotitolo(s, e, lang)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+addic7ed.getProviderName());
			inserisciLog(e, addic7ed, lang);
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
			case SUBSFACTORY:
				return subsfactory.getElencoSerie();
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
			case SUBSFACTORY:
				return subsfactory;
			case SUBSPEDIA:
				return subspedia;
			case ADDIC7ED:
				return addic7ed;
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
	public Map<SerieTV, ArrayList<Episodio>> sottotitoliDaScaricare() {
		Map<SerieTV, ArrayList<Episodio>> map = new HashMap<SerieTV, ArrayList<Episodio>>();
		ArrayList<SerieTV> series = GestioneSerieTV.getInstance().getElencoSeriePreferite();
		for(int i=0;i<series.size();i++){
			SerieTV s = series.get(i);
			String query = "SELECT * FROM "+Database.TABLE_EPISODI+" WHERE sottotitolo=1 AND serie="+s.getIDDb()+" ORDER BY stagione, episodio ASC";
			ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
			ArrayList<Episodio> eps = new ArrayList<Episodio>();
			for(int j=0;j<res.size();j++){
				Episodio e = ProviderSerieTV.parseEpisodio(res.get(j));
				eps.add(e);
			}
			if(eps.size()>0){
				map.put(s, eps);
			}
		}
		return map;
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
	public ArrayList<Object4Value<ProviderSottotitoli, SerieTV, Episodio, String>> getLast50LogSub(){
		String query = "SELECT * FROM logsub ORDER BY id DESC LIMIT 50";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<Object4Value<ProviderSottotitoli, SerieTV, Episodio, String>> list = new ArrayList<Object4Value<ProviderSottotitoli,SerieTV,Episodio, String>>();
		for(int i=res.size()-1;i>=0;i--){
			KVResult<String,Object> r = res.get(i);
			int idEpisodio = (int) r.getValueByKey("episodio");
			int provider = (int) r.getValueByKey("provider");
			String lingua = (String) r.getValueByKey("lingua");
			Episodio ep = ProviderSerieTV.getEpisodio(idEpisodio);
			SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
			ProviderSottotitoli prov = getProvider(provider);
			Object4Value<ProviderSottotitoli, SerieTV, Episodio, String> val = new Object4Value<ProviderSottotitoli, SerieTV, Episodio, String>(prov, serie, ep, lingua);
			list.add(val);
		}
		return list;
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