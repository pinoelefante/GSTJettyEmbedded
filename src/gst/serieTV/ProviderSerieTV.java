package gst.serieTV;

import gst.database.Database;
import gst.tda.db.KVResult;
import gst.tda.serietv.Episodio;

import java.util.ArrayList;

public abstract class ProviderSerieTV {
	protected final static int PROVIDER_EZTV=1;
	protected final static int PROVIDER_KARMORRA=2;
	
	private int providerID;
	protected ArrayList<SerieTV> nuove_serie;
	
	public ProviderSerieTV(int id){
		providerID=id;
		nuove_serie=new ArrayList<SerieTV>();
	}
	protected ArrayList<SerieTV> getElencoSerie(){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+ " WHERE provider="+providerID+" ORDER BY nome ASC";
		ArrayList<SerieTV> elenco = new ArrayList<>();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		//TODO caricamento serie
		return elenco;
	}
	private SerieTV parseSerie(KVResult<String, Object> res){
		return null;
	}
	public SerieTV getSerie(int id){
		return null;
	}
	private boolean isSerieInPreferiti(SerieTV s){
		String query="SELECT id_serie FROM "+Database.TABLE_PREFERITI+" WHERE id_serie="+s.getIDDb();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		boolean presente = res.size() > 0;
		res.clear();
		res = null;
		return presente;
	}
	public boolean aggiungiSerieAPreferiti(SerieTV serie){
		if(isSerieInPreferiti(serie))
			return false;
		else {
			String query = "INSERT INTO "+Database.TABLE_PREFERITI+" (id_serie) VALUES ("+serie.getIDDb()+")";
			return Database.updateQuery(query);
		}
	}
	public boolean removeSerieDaPreferiti(SerieTV serie){
//		TODO rimozione episodi e torrent
		String query="DELETE FROM "+Database.TABLE_PREFERITI+" WHERE id_serie="+serie.getIDDb();
		return Database.updateQuery(query);
	}
	

	public abstract String getProviderName();
	public abstract String getBaseURL();
	public abstract void aggiornaElencoSerie();
	public abstract ArrayList<Episodio> nuoviEpisodi(SerieTV serie);
	public abstract void caricaEpisodiDB(SerieTV serie);
	public abstract void caricaSerieDB();
	protected abstract void salvaSerieInDB(SerieTV s);
	protected abstract void salvaEpisodioInDB(Torrent t);
	public abstract int getProviderID();
	public abstract void caricaEpisodiOnline(SerieTV serie);
}
