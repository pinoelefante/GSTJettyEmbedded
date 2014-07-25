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
	protected ArrayList<SerieTV> getElencoSerieDB(){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+ " WHERE provider="+providerID+" ORDER BY nome ASC";
		ArrayList<SerieTV> elenco = new ArrayList<>();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for(int i=0;i<res.size();i++)
			elenco.add(parseSerie(res.get(i)));
		return elenco;
	}
	private SerieTV parseSerie(KVResult<String, Object> res){
		int id_db = (int) res.getValueByKey("id");
		String url = (String) res.getValueByKey("url");
		String nome = (String) res.getValueByKey("nome");
		boolean conclusa = ((int) res.getValueByKey("conclusa") == 0 ? false : true);
		boolean stop_search = ((int) res.getValueByKey("stop_search") == 0 ? false : true);
		int id_itasa = (int) res.getValueByKey("id_itasa");
		int id_subsf = (int) res.getValueByKey("id_subsfactory");
		int id_subsp = (int) res.getValueByKey("id_subspedia");
		int id_tvdb = (int) res.getValueByKey("id_tvdb");
		int preferenze_d = (int) res.getValueByKey("preferenze_download");
		SerieTV st = new SerieTV(this, nome, url);
		st.setIDDb(id_db);
		st.setConclusa(conclusa);
		st.setIDItasa(id_itasa);
		st.setIDSubsfactory(id_subsf);
		st.setIDSubspedia(id_subsp);
		st.setIDTvdb(id_tvdb);
		st.setStopSearch(stop_search);
		st.setPreferenze(new Preferenze(preferenze_d));
		return st;
	}
	public SerieTV getSerieByID(int id){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE id="+id;
		ArrayList<KVResult<String,Object>> res = Database.selectQuery(query);
		if(res.size()==0)
			return null;
		else {
			SerieTV serie = parseSerie(res.get(0));
			return serie;
		}
	}
	public SerieTV getSerieByURL(String url){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE url='"+url+"'";
		ArrayList<KVResult<String,Object>> res = Database.selectQuery(query);
		if(res.size()==0)
			return null;
		else {
			SerieTV serie = parseSerie(res.get(0));
			return serie;
		}
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
		//TODO SET STOP_SEARCH A FALSE
		return Database.updateQuery(query);
	}
	public boolean aggiungiSerieADatabase(SerieTV s){
		if(getSerieByURL(s.getUrl())==null)
			return false;
		else {
			addSerieToDB(s);
			nuove_serie.add(s);
			return true;
		}
	}
	private void addSerieToDB(SerieTV s){
		String query = "INSERT INTO "+Database.TABLE_SERIETV+" (nome, url, provider,conclusa) VALUES ('"+s.getNomeSerie()+"','"+s.getUrl()+"',"+providerID+","+(s.isConclusa()?1:0)+")";
		Database.updateQuery(query);
	}

	public abstract String getProviderName();
	public abstract String getBaseURL();
	public abstract void aggiornaElencoSerie();
	public abstract ArrayList<Episodio> nuoviEpisodi(SerieTV serie);
	public abstract void caricaEpisodiDB(SerieTV serie);
	protected abstract void salvaSerieInDB(SerieTV s);
	protected abstract void salvaEpisodioInDB(Torrent t);
	public abstract int getProviderID();
	public abstract void caricaEpisodiOnline(SerieTV serie);
}
