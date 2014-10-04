package gst.sottotitoli.addic7ed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gst.database.Database;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.tda.db.KVResult;

public class Addic7ed implements ProviderSottotitoli {
	private static Addic7ed instance;
	private final static String URL_SHOWLIST = "http://www.addic7ed.com/ajax_getShows.php";
	private final static String URL_GET_EPISODES = "http://www.addic7ed.com//ajax_loadShow.php?show=<IDSHOW>&season=<SEASON>&langs=|<LANG>|&hd=<HD>&hi=0";
	private Map<String, Integer> lingue_disponibili;
	
	private Addic7ed() {
		lingue_disponibili = new HashMap<>();
		lingue_disponibili.put(INGLESE, 1);
		lingue_disponibili.put(ITALIANO, 7);
		lingue_disponibili.put(FRANCESE, 8);
		lingue_disponibili.put(PORTOGHESE, 10);
		lingue_disponibili.put(TEDESCO, 11);
		lingue_disponibili.put(SPAGNOLO, 4);
	}
	public static ProviderSottotitoli getInstance() {
		if(instance==null)
			instance = new Addic7ed();
		return instance;
	}
	
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		if(!hasLanguage(lang))
			return false;
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		String query = "SELECT * FROM "+Database.TABLE_ADDIC7ED+" ORDER BY nome ASC";
		ArrayList<KVResult<String, Object>>	res = Database.selectQuery(query);
		ArrayList<SerieSub> el = new ArrayList<SerieSub>();
		for(KVResult<String, Object> r: res){
			el.add(parse(r));
		}
		return el;
	}
	private SerieSub parse(KVResult<String, Object> r){
		int id = (int) r.getValueByKey("id");
		String nome = (String) r.getValueByKey("nome");
		return new SerieSub(nome, id);
	}

	@Override
	public String getProviderName() {
		return "Addic7ed";
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.ADDIC7ED;
	}

	@Override
	public void associaSerie(SerieTV s) {
		ArrayList<SerieSub> serieDB = getElencoSerie();
		for(int i=0;i<serieDB.size();i++){
			if(s.getNomeSerie().compareToIgnoreCase(serieDB.get(i).getNomeSerie())==0){
				s.setIDSubsfactory(serieDB.get(i).getIDDB());
				associa(s.getIDDb(),serieDB.get(i).getIDDB());
				return;
			}
		}
	}

	@Override
	public void aggiornaElencoSerieOnline() {
		try {
			Document doc = Jsoup.connect(URL_SHOWLIST).get();
			Elements opts = doc.select("#qsShow option");
			for(int i=0;i<opts.size();i++){
				Element opt = opts.get(i);
				try {
					int idSerie = Integer.parseInt(opt.val());
					if(idSerie > 0){
						String nome = opt.text();
						salvaInDB(nome, idSerie);
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean isPresente(int id){
		String query = "SELECT * FROM "+Database.TABLE_ADDIC7ED+" WHERE id="+id;
		return Database.selectQuery(query).size()==1;
	}
	private boolean salvaInDB(String nome, int id){
		if(isPresente(id))
			return false;
		String query = "INSERT INTO "+Database.TABLE_ADDIC7ED+" (id, nome) VALUES ("+id+",\""+nome+"\")";
		return Database.updateQuery(query);
	}
	public static void main(String[] args){
		Addic7ed a = new Addic7ed();
		a.aggiornaElencoSerieOnline();
	}

	@Override
	public boolean associa(int idSerie, int idSub) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_addic7ed="+idSub+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean disassocia(int idSerie) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_addic7ed=0 WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean hasLanguage(String lang) {
		switch(lang){
			case INGLESE:
			case ITALIANO:
			case FRANCESE:
			case PORTOGHESE:
			case SPAGNOLO:
			case TEDESCO:
				return true;
		}
		return false;
	}

}
