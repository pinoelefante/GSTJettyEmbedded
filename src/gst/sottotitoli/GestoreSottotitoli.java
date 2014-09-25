package gst.sottotitoli;

import gst.database.Database;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.services.TaskAggiornaElenchi;
import gst.services.TaskAssociaSerie;
import gst.services.TaskRicercaSottotitoli;
import gst.sottotitoli.italiansubs.ItalianSubs;
import gst.sottotitoli.localhost.LocalSubs;
import gst.sottotitoli.subsfactory.Subsfactory;
import gst.sottotitoli.subspedia.Subspedia;
import gst.tda.db.KVResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import util.Object3Value;

public class GestoreSottotitoli implements Notifier{
	private static GestoreSottotitoli instance;

	public final static int LOCALE=0, ITASA=1, SUBSFACTORY=2, SUBSPEDIA=3; 
	private ProviderSottotitoli itasa;
	private ProviderSottotitoli subsfactory;
	private ProviderSottotitoli subspedia;
	private ProviderSottotitoli localsubs;
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
		return scaricaSottotitolo(s, e);
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e){
		boolean scaricato = true;
		boolean online = true;
		String episodio="S"+(e.getStagione()<10?"0"+e.getStagione():e.getStagione())+"E"+(e.getEpisodio()<10?"0"+e.getEpisodio():e.getEpisodio());
		
		if(localsubs.scaricaSottotitolo(s, e)){
			online=false;
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+localsubs.getProviderName());
			inserisciLog(e, localsubs);
		}
		else if(itasa.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+itasa.getProviderName());
			inserisciLog(e, itasa);
		}
		else if(subsfactory.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subsfactory.getProviderName());
			inserisciLog(e, subsfactory);
		}
		else if(subspedia.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subspedia.getProviderName());
			inserisciLog(e, subspedia);
		}
		else 
			scaricato = false;
		
		if(scaricato){
			String query="UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo=0 WHERE id="+e.getId();
			Database.updateQuery(query);
		}
		if(scaricato && online){
			localsubs.scaricaSottotitolo(s, e); //rename
		}
		return scaricato;
	}
	private void inserisciLog(Episodio e, ProviderSottotitoli p){
		String query = "INSERT INTO "+Database.TABLE_LOGSUB+" (episodio, provider) VALUES ("+e.getId()+","+p.getProviderID()+")";
		Database.updateQuery(query);
	}
	public ArrayList<SerieSub> getElencoSerie(int provider){
		switch(provider){
			case ITASA:
				return itasa.getElencoSerie();
			case SUBSFACTORY:
				return subsfactory.getElencoSerie();
			case SUBSPEDIA:
				System.out.println("Subspedia - getElencoSerie() - Funzione non supportata");
		}
		return null;
	}
	public ProviderSottotitoli getProvider(int provider){
		switch(provider){
			case ITASA:
				return itasa;
			case SUBSFACTORY:
				return subsfactory;
			case SUBSPEDIA:
				return subspedia;
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
		for(int i=1;i<=3;i++){
			ProviderSottotitoli p = GestoreSottotitoli.getInstance().getProvider(i);
			ArrayList<SerieSub> s = p.getElencoSerie();
			map.put(p, s);
		}
		return map;
	}
	public static void setSottotitoloDownload(int idEpisodio, boolean stato){
		String query = "UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo="+(stato?1:0)+" WHERE id="+idEpisodio;
		Database.updateQuery(query);
	}
	public ArrayList<Object3Value<ProviderSottotitoli, SerieTV, Episodio>> getLast50LogSub(){
		String query = "SELECT * FROM logsub ORDER BY id DESC LIMIT 50";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<Object3Value<ProviderSottotitoli, SerieTV, Episodio>> list = new ArrayList<Object3Value<ProviderSottotitoli,SerieTV,Episodio>>();
		for(int i=res.size()-1;i>=0;i--){
			KVResult<String,Object> r = res.get(i);
			int idEpisodio = (int) r.getValueByKey("episodio");
			int provider = (int) r.getValueByKey("provider");
			Episodio ep = ProviderSerieTV.getEpisodio(idEpisodio);
			SerieTV serie = ProviderSerieTV.getSerieByID(ep.getSerie());
			ProviderSottotitoli prov = getProvider(provider);
			Object3Value<ProviderSottotitoli, SerieTV, Episodio> val = new Object3Value<ProviderSottotitoli, SerieTV, Episodio>(prov, serie, ep);
			list.add(val);
		}
		return list;
	}
}