package gst.sottotitoli;

import gst.database.Database;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.sottotitoli.italiansubs.ItalianSubs;
import gst.sottotitoli.subsfactory.Subsfactory;
import gst.sottotitoli.subspedia.Subspedia;
import gst.tda.db.KVResult;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GestoreSottotitoli implements Notifier{
	private static GestoreSottotitoli instance;

	public final static int ITASA=1, SUBSFACTORY=2, SUBSPEDIA=3; 
	private ProviderSottotitoli itasa;
	private ProviderSottotitoli subsfactory;
	private ProviderSottotitoli subspedia;
	private Settings settings;
	private Timer timer;
	private TimerTask aggiornaElenchi, associaSerie, ricercaSottotitoli;
	
	public static void main(String[] args){
		Settings.getInstance();
		Database.Connect();
		GestoreSottotitoli g = getInstance();
	}
	
	public static GestoreSottotitoli getInstance(){
		if(instance==null)
			instance=new GestoreSottotitoli();
		return instance;
	}
	private GestoreSottotitoli(){
		itasa=ItalianSubs.getInstance();
		subsfactory=Subsfactory.getInstance();
		subspedia=Subspedia.getInstance();
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
	
	public boolean scaricaSottotitolo(Episodio e){
		SerieTV s = ProviderSerieTV.getSerieByID(e.getSerie());
		return scaricaSottotitolo(s, e);
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e){
		boolean scaricato = true;
		
		String episodio="S"+(e.getStagione()<10?"0"+e.getStagione():e.getStagione())+"E"+(e.getEpisodio()<10?"0"+e.getEpisodio():e.getEpisodio());
		if(itasa.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+itasa.getProviderName());
		}
		else if(subsfactory.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subsfactory.getProviderName());
		}
		else if(subspedia.scaricaSottotitolo(s, e)){
			inviaNotifica(s.getNomeSerie() + episodio + " - Sottotitolo scaricato - "+subspedia.getProviderName());
		}
		else 
			scaricato = false;
		
		if(scaricato){
			String query="UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo=0 WHERE id="+e.getId();
			Database.updateQuery(query);
			//TODO renamer
		}
		
		return scaricato;
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
	public static void setSottotitoloDownload(int idEpisodio, boolean stato){
		String query = "UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo="+(stato?1:0)+" WHERE id="+idEpisodio;
		Database.updateQuery(query);
	}
}