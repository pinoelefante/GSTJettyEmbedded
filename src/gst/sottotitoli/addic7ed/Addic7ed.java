package gst.sottotitoli.addic7ed;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gst.database.Database;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.player.FileFinder;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.tda.db.KVResult;

public class Addic7ed implements ProviderSottotitoli {
	private static Addic7ed instance;
	private final static String URL_SHOWLIST = "http://www.addic7ed.com/ajax_getShows.php";
	private final static String URL_GET_EPISODES = "http://www.addic7ed.com/ajax_loadShow.php?show=<IDSHOW>&season=<SEASON>&langs=<LANG>&hd=<HD>&hi=0";
	private static final int LIMITE_DOWNLOAD = 0;
	private Map<String, String> lingue_disponibili;
	private int downloads;
	private TimerTask task_reset_addic7ed_downloads;
	private Timer timer ;
	
	private Addic7ed() {
		downloads = 0;
		timer = new Timer();
		task_reset_addic7ed_downloads=new ResetCapLimitTask();
		lingue_disponibili = new HashMap<>();
		lingue_disponibili.put(INGLESE, "|1|");
		lingue_disponibili.put(ITALIANO, "|7|");
		lingue_disponibili.put(FRANCESE, "|8|");
		lingue_disponibili.put(PORTOGHESE, "|9|10|");
		lingue_disponibili.put(TEDESCO, "|11|");
		lingue_disponibili.put(SPAGNOLO, "|4|5|6|");
		
		map_lang_addicted = new HashMap<String, ArrayList<String>>();
		ArrayList<String> en = new ArrayList<String>();
		en.add("English");
		map_lang_addicted.put(INGLESE, en);
		ArrayList<String> it = new ArrayList<String>();
		it.add("Italian");
		map_lang_addicted.put(ITALIANO, it);
		ArrayList<String> fr = new ArrayList<String>();
		fr.add("French");
		map_lang_addicted.put(FRANCESE, fr);
		ArrayList<String> pr = new ArrayList<String>();
		pr.add("Portuguese");
		pr.add("Portuguese (Brazilian)");
		map_lang_addicted.put(PORTOGHESE, pr);
		ArrayList<String> de = new ArrayList<String>();
		de.add("German");
		map_lang_addicted.put(TEDESCO, de);
		ArrayList<String> es = new ArrayList<String>();
		es.add("Spanish (Spain)");
		es.add("Spanish");
		es.add("Spanish (Latin America)");
		map_lang_addicted.put(SPAGNOLO, es);
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
		if(downloads>=15)
			return false;
		if(serie.getIDAddic7ed()<=0)
			return false;
		String langS = getLangString(lang);
		if(langS==null)
			return false;
		ArrayList<File> videos = FileFinder.getInstance().cercaFileVideo(serie, ep);
		boolean downloadOK = false;
		for(int i=0;i<videos.size();i++){
			File f = videos.get(i);
			CaratteristicheFile stat = Naming.parse(f.getName(), null);
			ArrayList<String> urls = cercaSottotitoli(serie.getIDAddic7ed(), stat.getStagione(), stat.getEpisodio(), langS, stat.is720p(), getAddictedLanguages(lang));
			if(urls==null)
				continue;
			else {
				for(int j=0;j<urls.size();j++){
					String path = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator))+serie.getNomeSerie()+"_"+ep.getStagione()+"x"+ep.getEpisodio()+".srt";
					try {
						ArrayList<Entry<String, String>> headers = new ArrayList<Map.Entry<String,String>>();
						headers.add(new AbstractMap.SimpleEntry<String, String>("Host","www.addic7ed.com"));
						headers.add(new AbstractMap.SimpleEntry<String, String>("Referer",getAPIUrl(serie.getIDAddic7ed(), ep.getStagione(), langS, stat.is720p())));
						Download.downloadCustomHeaders(urls.get(j), path, headers);
						downloadOK = true;
						downloads++;
						GestoreSottotitoli.setSottotitoloDownload(ep.getId(), false, lang);
						if(downloads >=LIMITE_DOWNLOAD){
							timer.schedule(task_reset_addic7ed_downloads, 86400000, 1);
							break;
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return downloadOK;
	}
	private String getAPIUrl(int idShow, int stagione,String lang, boolean hd){
		return URL_GET_EPISODES.replace("<IDSHOW>", idShow+"").replace("<SEASON>", stagione+"").replace("<LANG>", lang).replace("<HD>", hd?"1":"0");
	}
	private ArrayList<String> cercaSottotitoli(int idShow, int stagione, int episodio, String lang, boolean hd, ArrayList<String> langs){
		String apiCall = getAPIUrl(idShow, stagione, lang, hd);
		System.out.println(apiCall);
		try {
			Document doc = Jsoup.connect(apiCall).get();
			Elements righe = doc.select("tr.epeven");
			ArrayList<String> urls = new ArrayList<String>();
			for(int i=0;i<righe.size();i++){
				Elements tds = righe.get(i).select("td");
				try {
					int s = Integer.parseInt(tds.get(0).text());
					if(s!=stagione)
						continue;
					int e = Integer.parseInt(tds.get(1).text());
					if(e!=episodio)
						continue;
					String l = tds.get(3).text();
					if(!isLangOK(l, langs))
						continue;
					String complete = tds.get(5).text();
					if(complete.compareTo("Completed")!=0)
						continue;
					String p720 = tds.get(8).text();
					boolean hd720 = !p720.isEmpty();
					if(hd720!=hd)
						continue;
					String link = tds.get(9).select("a").get(0).attr("href");
					urls.add("http://www.addic7ed.com"+link);
				}
				catch(Exception e){}
			}
			return urls;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private boolean isLangOK(String lang, ArrayList<String> langs){
		if(langs==null)
			return false;
		for(int i=0;i<langs.size();i++){
			if(langs.get(i).compareToIgnoreCase(lang)==0)
				return true;
		}
		return false;
	}
	private String getLangString(String lang){
		switch(lang){
			case ITALIANO:
				return lingue_disponibili.get(ITALIANO);
			case INGLESE:
				return lingue_disponibili.get(INGLESE);
			case FRANCESE:
				return lingue_disponibili.get(FRANCESE);
			case PORTOGHESE:
				return lingue_disponibili.get(PORTOGHESE);
			case TEDESCO:
				return lingue_disponibili.get(TEDESCO);
			case SPAGNOLO:
				return lingue_disponibili.get(SPAGNOLO);
		}
		return null;
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
	private Map<String, ArrayList<String>> map_lang_addicted;
	private ArrayList<String> getAddictedLanguages(String lang){
		switch(lang){
			case INGLESE:
				return map_lang_addicted.get(INGLESE);
			case ITALIANO:
				return map_lang_addicted.get(ITALIANO);
			case FRANCESE:
				return map_lang_addicted.get(FRANCESE);
			case PORTOGHESE:
				return map_lang_addicted.get(PORTOGHESE);
			case SPAGNOLO:
				return map_lang_addicted.get(SPAGNOLO);
			case TEDESCO:
				return map_lang_addicted.get(TEDESCO);
		}
		return null;
	}
	class ResetCapLimitTask extends TimerTask {
		public void run() {
			downloads = 0;
		}
		
	}
}
