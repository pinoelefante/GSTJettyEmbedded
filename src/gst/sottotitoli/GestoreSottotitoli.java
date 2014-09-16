package gst.sottotitoli;

import gst.database.Database;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.ManagerException;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.sottotitoli.italiansubs.ItalianSubs;
import gst.sottotitoli.subsfactory.Subsfactory;
import gst.sottotitoli.subspedia.Subspedia;
import gst.tda.db.KVResult;

import java.util.ArrayList;

public class GestoreSottotitoli implements Notifier{
	private static GestoreSottotitoli instance;
	public static GestoreSottotitoli getInstance(){
		if(instance==null)
			instance=new GestoreSottotitoli();
		return instance;
	}
	
	class AssociatoreAutomatico extends Thread {
		public void run(){
			System.out.println("Avvio associatore");
			ArrayList<SerieTV> st=GestioneSerieTV.getInstance().getElencoSeriePreferite();
			for(int i=0;i<st.size();i++){
				SerieTV s=st.get(i);
				associaSerie(s);
			}
		}
	}
	class RicercaSottotitoliAutomatica extends Thread{
		public void run(){
			long sleep_time=/*un minuto*/(60*1000)*10/*10 minuti*/;
		
			try {
				Thread t=new AssociatoreAutomatico();
				t.start();
				t.join();
			} 
			catch (InterruptedException e1) {
				e1.printStackTrace();
				ManagerException.registraEccezione(e1);
			}
			
			while(true) {
				ArrayList<Episodio> episodi = getSottotitoliDaScaricare();
				for(int i=0;i<episodi.size();i++){
					scaricaSottotitolo(episodi.get(i));
				}
				episodi.clear();
				episodi=null;
				try {
					sleep(sleep_time);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public final static int ITASA=1, SUBSFACTORY=2, SUBSPEDIA=3; 
	private Thread ricerca_automatica;
	private ProviderSottotitoli itasa;
	private ProviderSottotitoli subsfactory;
	private ProviderSottotitoli subspedia;
	private Settings settings;
		
	private GestoreSottotitoli(){
		itasa=ItalianSubs.getInstance();
		subsfactory=Subsfactory.getInstance();
		subspedia=new Subspedia();
		notificable=new ArrayList<Notificable>(2);
		settings=Settings.getInstance();
		if(settings.isRicercaSottotitoli())
			avviaRicercaAutomatica();
	}
	public void avviaRicercaAutomatica(){
		if(ricerca_automatica==null || !ricerca_automatica.isAlive())
			ricerca_automatica=new RicercaSottotitoliAutomatica();
		else if(ricerca_automatica.isAlive())
			return;
		ricerca_automatica.start();
	}
	public void stopRicercaAutomatica(){
		if(ricerca_automatica!=null && ricerca_automatica.isAlive()){
			ricerca_automatica.interrupt();
			ricerca_automatica=null;
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