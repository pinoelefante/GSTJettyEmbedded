package gst.sottotitoli;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.services.TaskAggiornaElenchi;
import gst.services.TaskRicercaSottotitoli;
import gst.sottotitoli.addic7ed.Addic7ed;
import gst.sottotitoli.localhost.LocalSubs;

public class GestoreSottotitoli {
	private static GestoreSottotitoli instance;

	public final static int LOCALE=0, PODNAPISI=4, OPENSUBTITLES=5, ADDIC7ED=6; 
	private ProviderSottotitoli addic7ed;
	//private ProviderSottotitoli podnapisi;
	private LocalSubs localsubs;
	private Timer timer;
	private TimerTask aggiornaElenchi, associaSerie, ricercaSottotitoli;
	
	public static GestoreSottotitoli getInstance(){
		if(instance==null)
			instance=new GestoreSottotitoli();
		return instance;
	}
	private GestoreSottotitoli(){
		localsubs=LocalSubs.getInstance();
		addic7ed = Addic7ed.getInstance();
		//podnapisi = Podnapisi.getInstance();
		//opensubtitles = OpenSubtitles.getInstance();
		
		
		timer = new Timer();
		timer.scheduleAtFixedRate(aggiornaElenchi=new TaskAggiornaElenchi(), 1000, 43200000); //12 ore
		//timer.scheduleAtFixedRate(associaSerie=new TaskAssociaSerie(), 10000, 3600000); //1 ora
		
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
			case ADDIC7ED:
				addic7ed.aggiornaElencoSerieOnline();
				break;
		}
	}
	
	public boolean scaricaSottotitolo(int idSub){
//		if(idSub<=0)
//			return false;
//		Episodio ep = ProviderSerieTV.getEpisodio(idSub);
//		if(ep==null)
//			return false;
//		return scaricaSottotitolo(ep);
		return false;
	}
	public boolean scaricaSottotitolo(Episodio e){
//		SerieTV s = ProviderSerieTV.getSerieByID(e.getSerie());
//		if(s==null)
//			return false;
//		PreferenzeSottotitoli p_sub = s.getPreferenzeSottotitoli();
//		String[] langs = p_sub.getPreferenze();
//		boolean ok= false;
//		for(int i=0;i<langs.length;i++){
//			if(scaricaSottotitolo(s, e, langs[i], langs.length==1))
//				ok=true;
//		}
//		return ok;
		return false;
	}
	public boolean scaricaSottotitolo(int idSub, String lang){
//		if(idSub<=0)
//			return false;
//		Episodio ep = ProviderSerieTV.getEpisodio(idSub);
//		if(ep==null)
//			return false;
//		return scaricaSottotitolo(ep, lang);
		return false;
	}
	public boolean scaricaSottotitolo(Episodio e, String lang){
//		SerieTV s = ProviderSerieTV.getSerieByID(e.getSerie());
//		if (s == null)
//			return false;
//		return scaricaSottotitolo(s, e, lang, s.getPreferenzeSottotitoli().getPreferenze().length == 1);
		return false;
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e, String lang, boolean uniqueLang){
//		boolean scaricato = true;
//		boolean online = true;
//		String episodio="S"+(e.getStagione()<10?"0"+e.getStagione():e.getStagione())+"E"+(e.getEpisodio()<10?"0"+e.getEpisodio():e.getEpisodio());
//		
//		if(localsubs.scaricaSottotitolo(s, e, lang, uniqueLang)){
//			online=false;
//			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+localsubs.getProviderName());
//			//inserisciLog(e, localsubs, lang);
//		}
//		else if(addic7ed.scaricaSottotitolo(s, e, lang)){
//			inviaNotifica(s.getNomeSerie() + " "  + episodio + " - Sottotitolo scaricato - "+addic7ed.getProviderName());
//			inserisciLog(e, addic7ed, lang);
//		}
//		/*
//		else if(podnapisi.scaricaSottotitolo(s, e, lang)){
//			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+podnapisi.getProviderName());
//			inserisciLog(e, podnapisi, lang);
//		}
//		*/
//		/*
//		else if(opensubtitles.scaricaSottotitolo(s, e, lang)) {
//			inviaNotifica(s.getNomeSerie() + " " + episodio + " - Sottotitolo scaricato - "+opensubtitles.getProviderName());
//			inserisciLog(e, opensubtitles, lang);
//		}
//		*/
//		else 
//			scaricato = false;
//		
//		if(scaricato){
//			String query="UPDATE "+Database.TABLE_EPISODI+" SET sottotitolo=0 WHERE id=?";
//			Database.updateQuery(query, e.getId());
//		}
//		if(scaricato && online){
//			localsubs.scaricaSottotitolo(s, e, lang, uniqueLang); //rename
//		}
//		return scaricato;
		return false;
	}
	public ArrayList<SerieSub> getElencoSerie(int provider){
		switch(provider){
			case ADDIC7ED:
				return addic7ed.getElencoSerie();
		}
		return null;
	}
	public ProviderSottotitoli getProvider(int provider){
		switch(provider){
			case LOCALE:
				return localsubs;
			case ADDIC7ED:
				return addic7ed;
				/*
			case PODNAPISI:
				return podnapisi;
				*/
				/*	
			case OPENSUBTITLES:
				return opensubtitles;
				 */
		}
		return null;
	}
}