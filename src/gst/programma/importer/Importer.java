package gst.programma.importer;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gst.database.Database;
import gst.interfacce.Notificable;
import gst.interfacce.Notifier;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Preferenze;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.tda.db.KVResult;

public class Importer implements Notifier{
	private Connection dbCon;
	
	public Importer(){
		subscribers=new ArrayList<Notificable>();
	}
	
	public void startImport(){
		String dbPath = Settings.getInstance().getUserDir()+File.separator+"database2.sqlite"; 
		if(OperazioniFile.fileExists(dbPath)){
			OperazioniFile.copyfile(dbPath, dbPath+".bak");
			try {
				dbCon = Database.ConnectToDB(dbPath);
				String query = "SELECT * FROM serietv";
				ArrayList<KVResult<String,Object>> res = Database.selectQuery(dbCon, query);
				ArrayList<SerieTVOld> preferiti = new ArrayList<>();
				System.out.println("Avvio la lettura dell'elenco delle serie");
				for(int i=0;i<res.size();i++){
					SerieTVOld s = parseSerie(res.get(i));
					addSerieToDB(s);
					if(s.isInserita()){
						preferiti.add(s);
						inviaNotifica("Aggiungo "+s.getNomeSerie()+" ai preferiti");
						ProviderSerieTV.aggiungiSerieAPreferiti(s);
					}
				}
				Map<Integer, Integer> statoEpisodi = new HashMap<Integer, Integer>();
				Map<Integer, Boolean> subDownload  = new HashMap<Integer, Boolean>();
				for(int i=0;i<preferiti.size();i++){
					SerieTVOld s = preferiti.get(i);
					inviaNotifica("Importo gli episodi di "+s.getNomeSerie());
					query = "SELECT * FROM episodi WHERE id_serie="+s.getIDDb();
					res = Database.selectQuery(dbCon, query);
					for(int j=0;j<res.size();j++){
						TorrentOld t = parseTorrent(res.get(j));
						int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(t.idSerie, t.getStats().getStagione(), t.getStats().getEpisodio());
						ProviderSerieTV.aggiungiLink(episodio_id, t.getStats().value(), t.getUrl());
						
						if(statoEpisodi.containsKey(episodio_id)){
							if(statoEpisodi.get(episodio_id)<t.statoVisualizzazione)
								statoEpisodi.put(episodio_id, t.statoVisualizzazione);
						}
						else if(t.statoVisualizzazione>0)
							statoEpisodi.put(episodio_id, t.statoVisualizzazione);
						
						if(t.subDown)
							subDownload.put(episodio_id, true);
					}
				}
				for(Entry<Integer, Integer> entry: statoEpisodi.entrySet()){
					ProviderSerieTV.changeStatusEpisodio(entry.getKey(), entry.getValue());
				}
				for(Entry<Integer, Boolean> subDEntry: subDownload.entrySet()){
					GestoreSottotitoli.setSottotitoloDownload(subDEntry.getKey(), subDEntry.getValue());
				}
				dbCon.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args){
		Settings.getInstance();
		Database.Connect();
		Importer imp = new Importer();
		imp.startImport();
	}
	private void addSerieToDB(SerieTV s){
		String query = "INSERT INTO "+Database.TABLE_SERIETV+" (id, nome, url, provider,conclusa) VALUES ("+s.getIDDb()+", \""+s.getNomeSerie()+"\",\""+s.getUrl()+"\","+s.getProviderID()+","+(s.isConclusa()?1:0)+")";
		Database.updateQuery(query);
	}
	private SerieTVOld parseSerie(KVResult<String, Object> res){	
		int id_db = (int) res.getValueByKey("id");
		String url = (String) res.getValueByKey("url");
		String nome = (String) res.getValueByKey("nome");
		boolean inserita = ((int)res.getValueByKey("inserita")==0?false:true);
		boolean conclusa = ((int) res.getValueByKey("conclusa") == 0 ? false : true);
		boolean stop_search = ((int) res.getValueByKey("stop_search") == 0 ? false : true);
		int id_itasa = (int) res.getValueByKey("id_itasa");
		int id_subsf = (int) res.getValueByKey("id_subsfactory");
		int id_subsp = (int) res.getValueByKey("id_subspedia");
		int id_tvdb = (int) res.getValueByKey("id_tvdb");
		int preferenze_d = (int) res.getValueByKey("preferenze_download");
		SerieTVOld st = new SerieTVOld(1, nome, url);
		st.setIDDb(id_db);
		st.setConclusa(conclusa);
		st.setIDItasa(id_itasa);
		st.setIDSubsfactory(id_subsf);
		st.setIDSubspedia(id_subsp);
		st.setIDTvdb(id_tvdb);
		st.setStopSearch(stop_search);
		st.setPreferenze(new Preferenze(preferenze_d));
		st.setInserita(inserita);
		return st;
	}
	private TorrentOld parseTorrent(KVResult<String, Object> res) {
		int idSerie = (int) res.getValueByKey("id_serie");
		String url = (String) res.getValueByKey("url");
		int stato = (int) res.getValueByKey("vista");
		boolean subD = ((int)res.getValueByKey("sottotitolo")==0?false:true);
		
		TorrentOld t = new TorrentOld(url, 0, 0);
		t.idSerie=idSerie;
		t.statoVisualizzazione=stato;
		t.subDown=subD;
		
		return t;
	}
	class SerieTVOld extends SerieTV {
		boolean inserita;
		public SerieTVOld(int provider, String nomeserie, String url) {
			super(provider, nomeserie, url);
		}
		public void setInserita(boolean t){
			inserita=t;
		}
		public boolean isInserita(){
			return inserita;
		}
	}
	class TorrentOld extends Torrent {
		private int idSerie, statoVisualizzazione;
		private boolean subDown; 
		public TorrentOld(String link, int idDB, int idEpisodio) {
			super(link, idDB, idEpisodio, true);
		}
		
	}
	
	private ArrayList<Notificable> subscribers;
	@Override
	public void subscribe(Notificable e) {
		if(e!=null)
			subscribers.add(e);		
	}
	@Override
	public void unsubscribe(Notificable e) {
		subscribers.remove(e);
	}
	@Override
	public void inviaNotifica(String text) {
		for(int i=0;i<subscribers.size();i++)
			subscribers.get(i).sendNotify(text);
	}
}
