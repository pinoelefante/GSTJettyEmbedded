package gst.serieTV;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.download.Download;
import gst.programma.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import util.os.DirectoryNotAvailableException;

public abstract class ProviderSerieTV {
	protected final static int PROVIDER_FILESYSTEM = 0;
	protected final static int PROVIDER_EZTV=1;
	public final static int PROVIDER_SHOWRSS=2;
	public final static int PROVIDER_SHOWRSS2=3;
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
		int id_addic7ed = (int) res.getValueByKey("id_addic7ed");
		int id_provider = (int) res.getValueByKey("provider");
		int preferenze_d = (int) res.getValueByKey("preferenze_download");
		String pref_sub = (String) res.getValueByKey("preferenze_sottotitoli");
		boolean escludiSelezionaTutto = ((int) res.getValueByKey("escludi_seleziona_tutto")==0?false:true);
		int id_karmorra = (int) res.getValueByKey("id_karmorra");
		int id_showrss_new = (int) res.getValueByKey("id_showrss_new");
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
		st.setPreferenzeSottotitoli(new PreferenzeSottotitoli(pref_sub));
		st.setEscludiSelezionaTutto(escludiSelezionaTutto);
		st.setIDAddic7ed(id_addic7ed);
		st.setIDKarmorra(id_karmorra);
		st.SetIdShowRss(id_showrss_new);
		return st;
	}
	public static SerieTV getSerieByURL(String url, int provider){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE url='"+url+"' AND provider="+provider;
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
			String query = "INSERT INTO "+Database.TABLE_PREFERITI+" (id_serie) VALUES (?)";
			String query2 = "UPDATE "+Database.TABLE_SERIETV+" SET preferenze_sottotitoli=? WHERE id=?";
			boolean pref = Database.updateQuery(query, serie.getIDDb());
			Database.updateQuery(query2, Settings.getInstance().getLingua(), serie.getIDDb());
			return pref;
		}
	}
	public static boolean removeSerieDaPreferiti(int serie, boolean resetEpisodi){
		String update_serie = "UPDATE serietv SET stop_search=0, escludi_seleziona_tutto=0 WHERE id=?";
		Database.updateQuery(update_serie, serie);
		if(resetEpisodi){
			String resetEp="UPDATE "+Database.TABLE_EPISODI+" SET stato_visualizzazione=0, sottotitolo=0 WHERE serie=?";
			Database.updateQuery(resetEp, serie);
		}
		
		String query="DELETE FROM "+Database.TABLE_PREFERITI+" WHERE id_serie=?";
		return Database.updateQuery(query,serie);
	}
	public boolean aggiungiSerieADatabase(SerieTV s, int provider){
		if(getSerieByURL(s.getUrl(), provider)!=null)
			return false;
		else {
			addSerieToDB(s);
			SerieTV s1=getSerieByURL(s.getUrl(), provider);
			s.setIDDb(s1.getIDDb());
			s1=null;
			nuove_serie.add(s);
			return true;
		}
	}
	public ArrayList<SerieTV> getElencoSerieNuove(){
		ArrayList<SerieTV> serie = new ArrayList<SerieTV>(nuove_serie.size());
		serie.addAll(nuove_serie);
		return serie;
	}
	public void setConclusa(SerieTV s) {
		String query = "UPDATE " + Database.TABLE_SERIETV + " set conclusa = 1, stop_search = 1 where id = " + s.getIDDb();
		Database.updateQuery(query);
	}
	private void addSerieToDB(SerieTV s){
		String query = "INSERT INTO "+Database.TABLE_SERIETV+" (nome, url, provider,conclusa, preferenze_download, preferenze_sottotitoli) VALUES (?,?,?,?,?,?)";
		Database.updateQuery(query, s.getNomeSerie(),s.getUrl(),getProviderID(),(s.isConclusa()?1:0),s.getPreferenze().toValue(),s.getPreferenzeSottotitoli().getPreferenzeU());
	}
	public boolean isEnded(SerieTV s) {
		if (s.getIDTvdb() == 0)
			return s.isConclusa();
		String query = "SELECT * from " + Database.TABLE_TVDB_SERIE + " where id = " + s.getIDTvdb();
		ArrayList<KVResult<String,Object>> result = Database.selectQuery(query);
		if (result.isEmpty()) 
			return false;
		return result.get(0).getValueByKey("stato").toString().equals("Ended");
	}
	public boolean isUpgrading(){
		return update_in_corso;
	}
	public static String getProviderNameByID(int id){
		switch(id){
			case PROVIDER_EZTV:
				return "eztv.it";
			case PROVIDER_SHOWRSS:
				return "showRss.info - old";
			case PROVIDER_SHOWRSS2:
				return "showRss.info";
		}
		return "unknown";
	}
	public static int isEpisodioPresente(int id_serie, int stagione, int episodio){
		String query = "SELECT * FROM episodi WHERE serie="+id_serie+" AND stagione="+stagione+" AND episodio="+episodio;
		ArrayList<KVResult<String, Object>> res =Database.selectQuery(query);
		if(/*res!=null &&*/ res.size()==0)
			return 0;
		else {
			return (int) res.get(0).getValueByKey("id");
		}
	}
	public static int aggiungiEpisodioSerie(int idSerie, int stagione, int episodio){
		int id = isEpisodioPresente(idSerie, stagione, episodio);
		if(id==0){
			String query = "INSERT INTO episodi (serie, stagione, episodio) VALUES (?,?,?)";
			Database.updateQuery(query, idSerie, stagione, episodio);
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
			String query = "INSERT INTO torrent (episodio, qualita, url) VALUES (?,?,?)";
			Database.updateQuery(query, id_episodio, qualita, url);
		}
	}
	public static ArrayList<Episodio> getEpisodiDaScaricare(int idSerie){
		ArrayList<Episodio> episodi = new ArrayList<>();
		String query = "SELECT * FROM episodi WHERE serie="+idSerie+" AND stato_visualizzazione=0 ORDER BY stagione,episodio ASC";
		ArrayList<KVResult<String, Object>> res1=Database.selectQuery(query);
		for(int i=0;i<res1.size();i++){
			episodi.add(parseEpisodio(res1.get(i)));
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
	public static ArrayList<Episodio> getEpisodiSerieLite(int idSerie){
		ArrayList<Episodio> episodi = new ArrayList<>();
		String query = "SELECT * FROM episodi WHERE serie="+idSerie+" ORDER BY stagione,episodio ASC";
		ArrayList<KVResult<String, Object>> res1=Database.selectQuery(query);
		for(int i=0;i<res1.size();i++){
			episodi.add(parseEpisodio(res1.get(i)));
		}
		return episodi;
	}
	public static Episodio parseEpisodio(KVResult<String, Object> res){
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
				String query2 = "UPDATE episodi SET stato_visualizzazione=? WHERE id=?";
				return Database.updateQuery(query2, nuovoStato, idEp);
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
	public static boolean downloadEpisodio(int idEp) throws DirectoryNotAvailableException{
		Episodio ep = getEpisodio(idEp);
		if(ep!=null){
			SerieTV serie = getSerieByID(ep.getSerie());
			List<Torrent> torrent = searchTorrent(serie.getPreferenze(), ep.getLinks());
			if(torrent == null || torrent.size()==0){
				return false;
			}
			boolean download = false;
			for(int i=0;i<torrent.size();i++){
    			try {
					if(Download.downloadTorrent(serie, torrent.get(i))){
						download = true;
						if(ep.getStatoVisualizzazione()!=Episodio.SCARICATO && ep.getStatoVisualizzazione()!=Episodio.VISTO)
							changeStatusEpisodio(idEp, Episodio.SCARICATO);
					}
				}
				catch (DirectoryNotAvailableException e) {
					e.printStackTrace();
					throw e;
				}
			}
			return download;
		}
		return false;
	}
	public static List<Torrent> searchTorrent(Preferenze p, ArrayList<Torrent> list){
		if (Objects.isNull(list))
			return null;
		List<Torrent> ultraHd = new ArrayList<Torrent>();
		List<Torrent> fullHd = new ArrayList<Torrent>();
		List<Torrent> hd = new ArrayList<Torrent>();
		List<Torrent> sd = new ArrayList<Torrent>();
		
		list.forEach(torrent -> {
			if (torrent.getStats().isPreair() && !p.isDownloadPreair())
				return;
			if (torrent.getStats().is2160p())
				ultraHd.add(torrent);
			else if (torrent.getStats().is1080p())
				fullHd.add(torrent);
			else if (torrent.getStats().is720p())
				hd.add(torrent);
			else
				sd.add(torrent);
		});
		ultraHd.stream().sorted((x, y) -> x.compareTo(y));
		fullHd.stream().sorted((x, y) -> x.compareTo(y));
		hd.stream().sorted((x, y) -> x.compareTo(y));
		sd.stream().sorted((x, y) -> x.compareTo(y));
		
		if (p.isScaricaTutto()) {
			List<Torrent> download = new ArrayList<Torrent>(4);
			if (!ultraHd.isEmpty())
				download.add(ultraHd.get(0));
			if (!fullHd.isEmpty())
				download.add(fullHd.get(0));
			if (!hd.isEmpty())
				download.add(hd.get(0));
			if (!sd.isEmpty())
				download.add(sd.get(0));
			return download;
			
		} else {
			if (p.isPreferisciUltraHD() && !ultraHd.isEmpty())
				return Collections.singletonList(ultraHd.get(0));
			if (p.isPreferisciFullHD() && !fullHd.isEmpty())
				return Collections.singletonList(fullHd.get(0));
			if (p.isPreferisciHD() && !hd.isEmpty())
				return Collections.singletonList(hd.get(0));
			if (!sd.isEmpty())
				return Collections.singletonList(sd.get(0));
			return Collections.singletonList(list.get(0));
		}
	}
	public static boolean associaSerieTVDB(int idSerie, int idTVDB){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_tvdb=? WHERE id=?";
		return Database.updateQuery(query, idTVDB, idSerie);
	}
	
	public abstract String getProviderName();
	public abstract String getBaseURL();
	public abstract void aggiornaElencoSerie();
	public abstract int getProviderID();
	public abstract void caricaEpisodiOnline(SerieTV serie);
	public abstract void init();
}
