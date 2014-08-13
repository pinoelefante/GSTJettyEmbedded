package gst.serieTV;

import gst.database.Database;
import gst.download.Download;
import gst.tda.db.KVResult;

import java.util.ArrayList;

public abstract class ProviderSerieTV {
	protected final static int PROVIDER_EZTV=1;
	protected final static int PROVIDER_KARMORRA=2;
	protected boolean update_in_corso;
	
	protected ArrayList<SerieTV> nuove_serie;
	
	public ProviderSerieTV(int id){
		nuove_serie=new ArrayList<SerieTV>();
	}
	protected ArrayList<SerieTV> getElencoSerieDB(){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+ " WHERE provider="+getProviderID()+" ORDER BY nome ASC";
		ArrayList<SerieTV> elenco = new ArrayList<>();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for(int i=0;i<res.size();i++)
			elenco.add(parseSerie(res.get(i)));
		return elenco;
	}
	public static SerieTV getSerieByID(int idSerie){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE id="+idSerie;
		ArrayList<KVResult<String,Object>> res = Database.selectQuery(query);
		if(res.size()==0)
			return null;
		else {
			SerieTV serie = parseSerie(res.get(0));
			return serie;
		}
	}
	public static SerieTV parseSerie(KVResult<String, Object> res){
		int id_db = (int) res.getValueByKey("id");
		String url = (String) res.getValueByKey("url");
		String nome = (String) res.getValueByKey("nome");
		boolean conclusa = ((int) res.getValueByKey("conclusa") == 0 ? false : true);
		boolean stop_search = ((int) res.getValueByKey("stop_search") == 0 ? false : true);
		int id_itasa = (int) res.getValueByKey("id_itasa");
		int id_subsf = (int) res.getValueByKey("id_subsfactory");
		int id_subsp = (int) res.getValueByKey("id_subspedia");
		int id_opensub = (int) res.getValueByKey("id_opensubtitles");
		int id_tvdb = (int) res.getValueByKey("id_tvdb");
		int id_provider = (int) res.getValueByKey("provider");
		int preferenze_d = (int) res.getValueByKey("preferenze_download");
		SerieTV st = new SerieTV(id_provider, nome, url);
		st.setIDDb(id_db);
		st.setConclusa(conclusa);
		st.setProvider(id_provider);
		st.setIDItasa(id_itasa);
		st.setIDSubsfactory(id_subsf);
		st.setIDSubspedia(id_subsp);
		st.setIDTvdb(id_tvdb);
		st.setIDOpenSubtitles(id_opensub);
		st.setStopSearch(stop_search);
		st.setPreferenze(new Preferenze(preferenze_d));
		return st;
	}
	public static SerieTV getSerieByURL(String url){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE url='"+url+"'";
		ArrayList<KVResult<String,Object>> res = Database.selectQuery(query);
		if(res.size()==0)
			return null;
		else {
			SerieTV serie = parseSerie(res.get(0));
			return serie;
		}
	}
	private static boolean isSerieInPreferiti(SerieTV s){
		String query="SELECT id_serie FROM "+Database.TABLE_PREFERITI+" WHERE id_serie="+s.getIDDb();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		boolean presente = res.size() > 0;
		res.clear();
		res = null;
		return presente;
	}
	public static boolean aggiungiSerieAPreferiti(SerieTV serie){
		if(isSerieInPreferiti(serie)){
			System.out.println("Serie già presente");
			return false;
		}
		else {
			String query = "INSERT INTO "+Database.TABLE_PREFERITI+" (id_serie) VALUES ("+serie.getIDDb()+")";
			return Database.updateQuery(query);
		}
	}
	public static boolean removeSerieDaPreferiti(int serie, boolean resetEpisodi){
		String update_serie = "UPDATE serietv SET stop_search=0 WHERE id="+serie;
		Database.updateQuery(update_serie);
		if(resetEpisodi){
			String resetEp="UPDATE episodi SET stato_visualizzazione=0 WHERE serie="+serie;
			Database.updateQuery(resetEp);
		}
		
		String query="DELETE FROM "+Database.TABLE_PREFERITI+" WHERE id_serie="+serie;
		
		return Database.updateQuery(query);
	}
	public boolean aggiungiSerieADatabase(SerieTV s){
		if(getSerieByURL(s.getUrl())!=null)
			return false;
		else {
			addSerieToDB(s);
			SerieTV s1=getSerieByURL(s.getUrl());
			nuove_serie.add(s1);
			return true;
		}
	}
	public ArrayList<SerieTV> getElencoSerieNuove(){
		ArrayList<SerieTV> serie = new ArrayList<SerieTV>(nuove_serie.size());
		serie.addAll(nuove_serie);
		return serie;
	}
	private void addSerieToDB(SerieTV s){
		String query = "INSERT INTO "+Database.TABLE_SERIETV+" (nome, url, provider,conclusa) VALUES (\""+s.getNomeSerie()+"\",\""+s.getUrl()+"\","+getProviderID()+","+(s.isConclusa()?1:0)+")";
		Database.updateQuery(query);
	}
	public boolean isUpgrading(){
		return update_in_corso;
	}
	public static String getProviderNameByID(int id){
		switch(id){
			case PROVIDER_EZTV:
				return "eztv.it";
			case PROVIDER_KARMORRA:
				return "Karmorra";
		}
		return "unknown";
	}
	public static int isEpisodioPresente(int id_serie, int stagione, int episodio){
		String query = "SELECT * FROM episodi WHERE serie="+id_serie+" AND stagione="+stagione+" AND episodio="+episodio;
		ArrayList<KVResult<String, Object>> res =Database.selectQuery(query);
		if(res.size()==0)
			return 0;
		else {
			return (int) res.get(0).getValueByKey("id");
		}
	}
	public static int aggiungiEpisodioSerie(int idSerie, int stagione, int episodio){
		int id = isEpisodioPresente(idSerie, stagione, episodio);
		if(id==0){
			String query = "INSERT INTO episodi (serie, stagione, episodio) VALUES ("+idSerie+","+stagione+","+episodio+")";
			Database.updateQuery(query);
			id=isEpisodioPresente(idSerie, stagione, episodio);
		}
		return id;
	}
	public static boolean isLinkPresente(String link, int idepisodio){
		String query="SELECT * FROM torrent WHERE episodio="+idepisodio+" AND url=\""+link+"\"";
		return Database.selectQuery(query).size()>0;
	}
	public static void aggiungiLink(int id_episodio, int qualita, String url){
		if(!isLinkPresente(url, id_episodio)){
			String query = "INSERT INTO torrent (episodio, qualita, url) VALUES ("+id_episodio+","+qualita+",\""+url+"\")";
			Database.updateQuery(query);
		}
	}
	public static ArrayList<Episodio> getEpisodiDaScaricare(int idSerie){
		ArrayList<Episodio> episodi = new ArrayList<>();
		String query = "SELECT * FROM episodi WHERE serie="+idSerie+" AND stato_visualizzazione=0 ORDER BY stagione,episodio ASC";
		ArrayList<KVResult<String, Object>> res1=Database.selectQuery(query);
		for(int i=0;i<res1.size();i++){
			episodi.add(parseEpisodio(res1.get(i)));
		}
		for(int i=0;i<episodi.size();i++){
			Episodio ep = episodi.get(i);
			String query2 = "SELECT * FROM torrent WHERE episodio="+ep.getId();
			ArrayList<KVResult<String, Object>> res2=Database.selectQuery(query2);
			for(int j=0;j<res2.size();j++){
				ep.aggiungiLink(parseTorrent(res2.get(j)));
			}
		}
		return episodi;
	}
	public static ArrayList<Episodio> getEpisodiSerie(int idSerie){
		ArrayList<Episodio> episodi = new ArrayList<>();
		String query = "SELECT * FROM episodi WHERE serie="+idSerie+" ORDER BY stagione,episodio ASC";
		ArrayList<KVResult<String, Object>> res1=Database.selectQuery(query);
		for(int i=0;i<res1.size();i++){
			episodi.add(parseEpisodio(res1.get(i)));
		}
		for(int i=0;i<episodi.size();i++){
			Episodio ep = episodi.get(i);
			String query2 = "SELECT * FROM torrent WHERE episodio="+ep.getId();
			ArrayList<KVResult<String, Object>> res2=Database.selectQuery(query2);
			for(int j=0;j<res2.size();j++){
				ep.aggiungiLink(parseTorrent(res2.get(j)));
			}
		}
		return episodi;
	}
	private static Episodio parseEpisodio(KVResult<String, Object> res){
		int id = (int) res.getValueByKey("id");
		int serie = (int) res.getValueByKey("serie");
		int stagione = (int) res.getValueByKey("stagione");
		int episodio = (int) res.getValueByKey("episodio");
		int stato = (int) res.getValueByKey("stato_visualizzazione");
		boolean sub_down = ((int)res.getValueByKey("sottotitolo"))==0?false:true;
		int idTvDB = (int) res.getValueByKey("id_tvdb");
		Episodio ep = new Episodio(stagione, episodio);
		ep.setId(id);
		ep.setSerie(serie);
		ep.setStatoVisualizzazione(stato);
		ep.setIdTvDB(idTvDB);
		ep.setSubDownload(sub_down);
		return ep;
	}
	private static Torrent parseTorrent(KVResult<String, Object> res){
		int id = (int) res.getValueByKey("id");
		int idEpisodio = (int) res.getValueByKey("episodio");
		int qualita = (int) res.getValueByKey("qualita");
		String link = (String) res.getValueByKey("url");
		Torrent tor = new Torrent(link, id, idEpisodio);
		tor.setStats(qualita);
		return tor;
	}
	public static boolean changeStatusEpisodio(int idEp, int nuovoStato){
		Episodio ep = getEpisodio(idEp);
		if(ep==null)
			return false;
		else {
			if(ep.getStatoVisualizzazione()==nuovoStato)
				return false;
			else {
				String query2 = "UPDATE episodi SET stato_visualizzazione="+nuovoStato+" WHERE id="+idEp;
				return Database.updateQuery(query2);
			} 
		}
	}
	public static Episodio getEpisodio(int id){
		String query = "SELECT * FROM episodi WHERE id="+id;
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()==0 || res.size()>1)
			return null;
		else {
			Episodio ep = parseEpisodio(res.get(0));
			String query2 = "SELECT * FROM torrent WHERE episodio="+ep.getId();
			ArrayList<KVResult<String, Object>> res2=Database.selectQuery(query2);
			for(int j=0;j<res2.size();j++){
				ep.aggiungiLink(parseTorrent(res2.get(j)));
			}
			return ep;
		}
	}
	public static boolean downloadEpisodio(int idEp){
		Episodio ep = getEpisodio(idEp);
		if(ep!=null){
			SerieTV serie = getSerieByID(ep.getSerie());
			Torrent torrent = searchTorrent(serie.getPreferenze(), ep.getLinks());
			if(torrent == null){
				return false;
			}
			if(Download.downloadTorrent(serie, torrent)){
				changeStatusEpisodio(idEp, Episodio.SCARICATO);
				return true;
			}
		}
		return false;
	}
	private static Torrent searchTorrent(Preferenze p, ArrayList<Torrent> list){
		if(p.isPreferisciHD()){
			for(int i=0;i<list.size();i++){
				if(list.get(i).getStats().is720p())
					return list.get(i);
			}
		}
		else {
			for(int i=0;i<list.size();i++){
				if(!list.get(i).getStats().is720p()){
					return list.get(i);
				}
			}
		}
		if(list.size()>0)
			return list.get(0);
		return null;
	}
	
	public abstract String getProviderName();
	public abstract String getBaseURL();
	public abstract void aggiornaElencoSerie();
	public abstract int getProviderID();
	public abstract void caricaEpisodiOnline(SerieTV serie);
}
