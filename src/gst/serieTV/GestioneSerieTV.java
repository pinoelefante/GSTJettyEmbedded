package gst.serieTV;

import gst.database.Database;
import gst.sottotitoli.GestoreSottotitoli;
import gst.tda.db.KVResult;

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
	public void init(){
		for(int i=0;i<providers.size();i++)
			providers.get(i).aggiornaElencoSerie();
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
	
	public ArrayList<SerieTV> getSerieFromProvider(int id){
		ProviderSerieTV provider=checkProvider(id);
		if(provider==null)
			return null;
		
		while(provider.isUpgrading()){
			try {
				Thread.sleep(300L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return provider.getElencoSerieDB();
	}
	public ProviderSerieTV checkProvider(int id){
		for(int i=0;i<providers.size();i++)
			if(providers.get(i).getProviderID()==id)
				return providers.get(i);
		return null;
	}
	
	public ArrayList<SerieTV> getSerieNuoveByProvider(int id){
		ProviderSerieTV p = checkProvider(id);
		if(p==null)
			return null;
		return p.getElencoSerieNuove();
	}
	
	public boolean aggiungiSerieAPreferiti(int provider, int idSerie){
		ProviderSerieTV p = checkProvider(provider);
		if(p!=null){
			SerieTV st=ProviderSerieTV.getSerieByID(idSerie);
			if(st!=null){
				boolean r = ProviderSerieTV.aggiungiSerieAPreferiti(st);
				System.out.println("aggiungi a preferiti:"+r);
				return r;
			}
		}
		return false;
	}
	public boolean aggiornaListaSerie(int idProvider){
		ProviderSerieTV p = checkProvider(idProvider);
		if(p!=null){
			p.aggiornaElencoSerie();
			return true;
		}
		return false;
	}
	
	public ArrayList<SerieTV> getElencoSeriePreferite(){
		String query = "SELECT * FROM preferiti pref JOIN serietv serie ON pref.id_serie = serie.id ORDER BY serie.nome ASC";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<SerieTV> preferiti = new ArrayList<>();
		for(int i=0;i<res.size();i++){
			SerieTV s = ProviderSerieTV.parseSerie(res.get(i));
			preferiti.add(s);
		}
		return preferiti;
	}
	public boolean rimuoviSeriePreferita(int id, boolean removeEpisodi){
		return ProviderSerieTV.removeSerieDaPreferiti(id, removeEpisodi);
	}
	public void aggiornaEpisodiSerie(int idSerie, int idProvider){
		ProviderSerieTV p = checkProvider(idProvider);
		SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
		p.caricaEpisodiOnline(serie);
	}
	public ArrayList<Episodio> getEpisodiDaScaricareBySerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiDaScaricare(idSerie);
		return episodi;
	}
	public ArrayList<Episodio> getEpisodiSerie(int idSerie){
		ArrayList<Episodio> episodi = ProviderSerieTV.getEpisodiSerie(idSerie);
		return episodi;
	}
	
	public boolean downloadEpisodio(int idEp) {
		return ProviderSerieTV.downloadEpisodio(idEp);
	}
	
	public GestoreSottotitoli getSubManager(){
		return submanager;
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
