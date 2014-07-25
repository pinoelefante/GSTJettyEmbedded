package gst.serieTV;

import gst.sottotitoli.GestoreSottotitoli;
import gst.tda.serietv.Episodio;

import java.util.ArrayList;

public class GestioneSerieTV {
	private static boolean instanced=false; 
	private static ArrayList<ProviderSerieTV> providers;

	private static GestoreSottotitoli submanager;
	
	public static void instance(){
		if(!instanced){
			providers=new ArrayList<ProviderSerieTV>(1);
    		submanager=new GestoreSottotitoli();
    		providers.add(new EZTV());
    		instanced=true;
		}
	}
	
	public static void carica_serie_database(){
		for(int i=0;i<providers.size();i++)
			providers.get(i).caricaSerieDB();
	}
	
	public static ArrayList<SerieTV> getElencoSerieCompleto(){
		ArrayList<SerieTV> res=new ArrayList<SerieTV>();
		for(int i=0;i<providers.size();i++){
			for(int j=0;j<providers.get(i).getSeriesCount();j++){
				SerieTV st=providers.get(i).getSerieAt(j);
				if(st!=null)
					res.add(st);
			}
		}
		return res;
	}
	public static boolean aggiungiSeriePreferita(SerieTV serie){
		boolean res=serie.getProvider().addSeriePreferita(serie);
		serie.getProvider().caricaEpisodiDB(serie);
		getSubManager().associaSerie(serie);
		//TODO aggiungere altro (cercare id su tvdb)
		return res;
	}
	public static ArrayList<SerieTV> getElencoSerieInserite(){
		ArrayList<SerieTV> res=new ArrayList<SerieTV>();
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV provider=providers.get(i);
			for(int j=0;j<provider.getPreferiteSerieCount();j++){
				res.add(provider.getPreferiteSerieAt(j));
			}
		}
		return res;
	}
	
	public static void caricaElencoSerieOnline() {
		for(int i=0;i<providers.size();i++){
			providers.get(i).aggiornaElencoSerie();
		}
	}
	public static void rimuoviSeriePreferita(SerieTV st){
		st.getProvider().rimuoviSeriePreferita(st);
	}
	public static GestoreSottotitoli getSubManager(){
		return submanager;
	}
	public static ArrayList<Episodio> caricaEpisodiDaScaricare(){
		class ThreadUpdate extends Thread {
			private SerieTV serie;
			public ThreadUpdate(SerieTV s){
				serie=s;
			}
			public void run(){
				serie.aggiornaEpisodiOnline();
			}
		}
		ArrayList<Episodio> episodi=new ArrayList<Episodio>();
		if(!isLoading()){
			loading=true;
			ThreadGroup tg=new ThreadGroup("AggiornamentoEpisodiSerie");
			for(int i=0;i<providers.size();i++){
				ProviderSerieTV p=providers.get(i);
				for(int j=0;j<p.getPreferiteSerieCount();j++){
					SerieTV s=p.getPreferiteSerieAt(j);
					Thread t=new Thread(tg, new ThreadUpdate(s));
					t.start();
					try {
						Thread.sleep(250);
					}catch (InterruptedException e) {}
				}
				while(tg.activeCount()>0)
					try {
						Thread.sleep(500L);
					}catch (InterruptedException e) {}
				for(int j=0;j<p.getPreferiteSerieCount();j++){
					SerieTV s=p.getPreferiteSerieAt(j);
					episodi.addAll(p.nuoviEpisodi(s));
				}
			}
		}
		loading=false;
		firstLoading=true;
		return episodi;
	}
	public static ArrayList<Episodio> caricaEpisodiDaScaricareOffline(){
		ArrayList<Episodio> episodi=new ArrayList<Episodio>();
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV p=providers.get(i);
			for(int j=0;j<p.getPreferiteSerieCount();j++){
				episodi.addAll(p.nuoviEpisodi(p.getPreferiteSerieAt(j)));
			}
		}
		return episodi;
	}
	public static ArrayList<SerieTV> getElencoNuoveSerie(){
		ArrayList<SerieTV> newseries=new ArrayList<SerieTV>(5);
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV p=providers.get(i);
			for(int j=0;j<p.getNuoveSerieCount();j++){
				newseries.add(p.getNuoveSerieAt(j));
			}
		}
		return newseries;
	}
	private static boolean loading=false;
	public static boolean isLoading() {
		return loading;
	}
	private static boolean firstLoading=false;
	public static boolean isFirstLoaded(){
		return firstLoading;
	}
}
