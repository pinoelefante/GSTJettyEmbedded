package gst.services;

import java.util.ArrayList;
import java.util.TimerTask;

import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.Settings;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;

public class TaskRicercaEpisodi extends TimerTask implements Notifier {
	private static GestioneSerieTV manager;
	private ArrayList<Notificable> notificable;
	private SearchListener		 listener;
	private boolean				searching;
	private int					count_search = 0;
	private Settings setts;
	private final static long UPDATE_LIMIT = 28800000;

	public TaskRicercaEpisodi() {
		manager = GestioneSerieTV.getInstance();
		notificable = new ArrayList<Notificable>();
		setts = Settings.getInstance();
	}

	public void run() {
		if(System.currentTimeMillis() < setts.getUltimoAggiornamentoSerie()+UPDATE_LIMIT){
			if(listener!=null)
				listener.searchFirstEnd();;
			return;
		}
		if (listener != null)
			listener.searchStart();

		ArrayList<SerieTV> serie = manager.getElencoSeriePreferite();
		int count_episodiNuovi = 0;
		for (int i = 0; i < serie.size(); i++) {
			SerieTV s = serie.get(i);
			manager.aggiornaEpisodiSerie(s.getIDDb(), s.getProviderID());
			count_episodiNuovi += manager.getEpisodiDaScaricareBySerie(s.getIDDb()).size();
		}
		count_search++;
		searching = false;
		if (listener != null) {
			if (count_search == 1)
				listener.searchFirstEnd();
			listener.searchEnd();
		}
		inviaNotifica("Sono presenti " + count_episodiNuovi + " episodi da scaricare");
		setts.setUltimoAggiornamentoSerie(System.currentTimeMillis());
		setts.salvaSettings();
	}

	public boolean isSearching() {
		return searching;
	}

	@Override
	public void subscribe(Notificable e) {
		if (e != null)
			notificable.add(e);
	}

	@Override
	public void unsubscribe(Notificable e) {
		notificable.remove(e);
	}

	@Override
	public void inviaNotifica(String text) {
		for (int i = 0; i < notificable.size(); i++) {
			notificable.get(i).sendNotify(text);
		}
	}

	public void addSearchListener(SearchListener list) {
		listener = list;
	}

	public void removeSearchListener() {
		listener = null;
	}
	public static void main(String[] args){
		System.out.println(System.currentTimeMillis());
	}
}
