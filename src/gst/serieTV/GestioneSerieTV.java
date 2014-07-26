package gst.serieTV;

import gst.sottotitoli.GestoreSottotitoli;
import gst.tda.serietv.Episodio;

import java.util.ArrayList;

public class GestioneSerieTV {
	private static GestioneSerieTV instance;
	private ArrayList<ProviderSerieTV> providers;

	private GestoreSottotitoli submanager;
	
	public static GestioneSerieTV getInstance(){
		if(instance==null){
			instance=new GestioneSerieTV();    		
		}
		return instance;
	}
	private GestioneSerieTV(){
		providers=new ArrayList<ProviderSerieTV>(1);
		//TODO
		//submanager=new GestoreSottotitoli();
		
		providers.add(new EZTV());
	}
	public ArrayList<ProviderSerieTV> getProviders(){
		return providers;
	}
	
	public void carica_serie_database(){
		for(int i=0;i<providers.size();i++)
			providers.get(i).caricaSerieDB();
	}
	
	public ArrayList<SerieTV> getElencoSerieCompleto(){
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
	public boolean aggiungiSeriePreferita(SerieTV serie){
		boolean res=serie.getProvider().addSeriePreferita(serie);
		serie.getProvider().caricaEpisodiDB(serie);
		getSubManager().associaSerie(serie);
		//TODO aggiungere altro (cercare id su tvdb)
		return res;
	}
	public ArrayList<SerieTV> getElencoSerieInserite(){
		ArrayList<SerieTV> res=new ArrayList<SerieTV>();
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV provider=providers.get(i);
			for(int j=0;j<provider.getPreferiteSerieCount();j++){
				res.add(provider.getPreferiteSerieAt(j));
			}
		}
		return res;
	}
	
	public void caricaElencoSerieOnline() {
		for(int i=0;i<providers.size();i++){
			providers.get(i).aggiornaElencoSerie();
		}
	}
	public void rimuoviSeriePreferita(SerieTV st){
		st.getProvider().rimuoviSeriePreferita(st);
	}
	public GestoreSottotitoli getSubManager(){
		return submanager;
	}
	public ArrayList<Episodio> caricaEpisodiDaScaricare(){
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
	public ArrayList<Episodio> caricaEpisodiDaScaricareOffline(){
		ArrayList<Episodio> episodi=new ArrayList<Episodio>();
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV p=providers.get(i);
			for(int j=0;j<p.getPreferiteSerieCount();j++){
				episodi.addAll(p.nuoviEpisodi(p.getPreferiteSerieAt(j)));
			}
		}
		return episodi;
	}
	public ArrayList<SerieTV> getElencoNuoveSerie(){
		ArrayList<SerieTV> newseries=new ArrayList<SerieTV>(5);
		for(int i=0;i<providers.size();i++){
			ProviderSerieTV p=providers.get(i);
			for(int j=0;j<p.getNuoveSerieCount();j++){
				newseries.add(p.getNuoveSerieAt(j));
			}
		}
		return newseries;
	}
	private boolean loading=false;
	public boolean isLoading() {
		return loading;
	}
	private boolean firstLoading=false;
	public boolean isFirstLoaded(){
		return firstLoading;
	}
}
