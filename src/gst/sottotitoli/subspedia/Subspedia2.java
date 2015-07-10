package gst.sottotitoli.subspedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;

import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;
import util.zip.ArchiviZip;
import gst.database.Database;
import gst.database.tda.KVResult;
import gst.download.Download;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

public class Subspedia2 implements ProviderSottotitoli {
	private static Subspedia2 instance;
	private static final String URL_ELENCO_SERIE = "http://www.subspedia.tv/API/getAllSeries.php";
	private static final String URL_ELENCO_EPISODI_SERIE = "http://subspedia.tv/API/getBySerie.php?serie={ID}";
	
	public static Subspedia2 getInstance(){
		if(instance==null)
			instance= new Subspedia2();
		return instance;
	}
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		if(!hasLanguage(lang))
			return false;
		String link = getLinkDownload(serie.getIDSubspedia(), ep.getStagione(), ep.getEpisodio());
		if(link==null)
			return false;
		boolean isZip = link.toLowerCase().endsWith(".zip");
		
		ArrayList<File> videoFiles = DirectoryManager.getInstance().cercaFileVideo(serie, ep);
		String baseDir = null;
		try {
			baseDir = DirectoryManager.getInstance().getAvailableDirectory()+File.separator+serie.getFolderSerie();
		}
		catch (DirectoryNotAvailableException e) {
			e.printStackTrace();
			return false;
		}
		if(videoFiles.size()>0)
			baseDir = videoFiles.get(0).getAbsolutePath().substring(0, videoFiles.get(0).getAbsolutePath().lastIndexOf(File.separator));
		
		String pathDownload = baseDir + link.substring(link.lastIndexOf("/"));
		boolean down = false;
		try {
			Download.downloadFromUrl(link, pathDownload);
			down = true;
			if(isZip)
				ArchiviZip.estrai_tutto(pathDownload, baseDir);
		}
		catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		ep.setSubDownload(!down);
		GestoreSottotitoli.setSottotitoloDownload(ep.getId(), !down, ITALIANO);
		
		return down;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		String query = "SELECT * FROM "+Database.TABLE_SUBSPEDIA+" ORDER BY nome";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<SerieSub> el = new ArrayList<SerieSub>();
		for(int i=0;i<res.size();i++){
			el.add(parseDB(res.get(i)));
		}
		return el;
	}

	@Override
	public String getProviderName() {
		return "Subspedia.tv";
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSPEDIA;
	}

	@Override
	public void associaSerie(SerieTV s) {
		ArrayList<SerieSub> dbSerie = getElencoSerie();
		for(int i=0;i<dbSerie.size();i++){
			if(dbSerie.get(i).getNomeSerie().compareToIgnoreCase(s.getNomeSerie())==0){
				associa(s.getIDDb(), dbSerie.get(i).getIDDB());
				break;
			}
		}
	}
	public static void main(String[] args){
		Subspedia2 s = new Subspedia2();
		s.aggiornaElencoSerieOnline();
	}
	@Override
	public void aggiornaElencoSerieOnline() {
		try {
			String json = Jsoup.connect(URL_ELENCO_SERIE).ignoreContentType(true).execute().body();
			JSONParser p = new JSONParser();
			Object arrayO = p.parse(json);
			JSONArray listSerie = (JSONArray)arrayO;
			for(int i=0;i<listSerie.size();i++){
				JSONObject serie = (JSONObject)listSerie.get(i);
				Long id = (Long)serie.get("id_serie");
				String nome = serie.get("nome_serie").toString();
				addSerie(id.intValue(), nome);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void addSerie(int id, String nome){
		if(isPresente(id))
			return;
		String query = "INSERT INTO "+Database.TABLE_SUBSPEDIA+" (id,nome) VALUES ("+id+",\""+nome+"\")";
		Database.updateQuery(query);
	}
	private boolean isPresente(int id){
		String query = "SELECT * FROM "+Database.TABLE_SUBSPEDIA+" WHERE id="+id;
		return Database.selectQuery(query).size()>0;
	}
	private String getLinkDownload(int idSerieSub, int stagione, int episodio){
		try {
			String url = URL_ELENCO_EPISODI_SERIE.replace("{ID}", ""+idSerieSub);
			String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JSONParser p = new JSONParser();
			Object array0 = p.parse(json);
			JSONArray listEpisodi = (JSONArray)array0;
			for(int i=0;i<listEpisodi.size();i++){
				JSONObject ep = (JSONObject)listEpisodi.get(i);
				Long stag = (Long) ep.get("num_stagione");
				Long epis = (Long) ep.get("num_episodio");
				if(episodio == epis && stag == stagione)
					return ep.get("link_file").toString();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean associa(int idSerie, int idSub) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subspedia="+idSub+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean disassocia(int idSerie) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subspedia=0 WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean hasLanguage(String lang) {
		switch(lang){
			case ITALIANO:
				return true;
		}
		return false;
	}
	private SerieSub parseDB(KVResult<String, Object> r){
		int id = (Integer)r.getValueByKey("id");
		String nome = (String)r.getValueByKey("nome");
		SerieSub s = new SerieSub(nome, id);
		return s;
	}
}
