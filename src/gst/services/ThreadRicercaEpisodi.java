package gst.services;

import java.util.ArrayList;

import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;

public class ThreadRicercaEpisodi extends Thread implements Notifier{
	private static int min_tra_ricerca;
	private static GestioneSerieTV manager;
	private ArrayList<Notificable> notificable;
	private SearchListener listener;
	private boolean searching;
	private int count_search = 0;
	
	public ThreadRicercaEpisodi(){
		this(480);
	}
	public ThreadRicercaEpisodi(int min){
		min_tra_ricerca=min;
		manager = GestioneSerieTV.getInstance();
		notificable=new ArrayList<Notificable>();
	}
	public void run(){
		while(true) {
			searching=true;
			if(listener!=null)
				listener.searchStart();
			
			ArrayList<SerieTV> serie = manager.getElencoSeriePreferite();
			int count_episodiNuovi = 0;
			for(int i=0;i<serie.size();i++){
				SerieTV s = serie.get(i);
				manager.aggiornaEpisodiSerie(s.getIDDb(), s.getProviderID());
				count_episodiNuovi+=manager.getEpisodiDaScaricareBySerie(s.getIDDb()).size();
			}
			count_search++;
			searching=false;
			if(listener!=null){
				if(count_search==1)
					listener.searchFirstEnd();
				listener.searchEnd();
			}
			inviaNotifica("Sono presenti "+count_episodiNuovi+" episodi da scaricare");
			
			for(int i=0;i<min_tra_ricerca;i++){
				try {
					sleep(60000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	public boolean isSearching(){
		return searching;
	}
	
	@Override
	public void subscribe(Notificable e) {
		if(e!=null)
			notificable.add(e);
	}
	@Override
	public void unsubscribe(Notificable e) {
		notificable.remove(e);
	}
	@Override
	public void inviaNotifica(String text) {
		for(int i=0;i<notificable.size();i++){
			notificable.get(i).sendNotify(text);
		}
	}
	public void addSearchListener(SearchListener list){
		listener = list;
	}
	public void removeSearchListener(){
		listener = null;
	}
}
