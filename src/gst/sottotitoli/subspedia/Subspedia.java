package gst.sottotitoli.subspedia;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.programma.ManagerException;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.SerieSubConDirectory;
import gst.sottotitoli.rss.SubspediaRSSItem;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.UserAgent;
import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;
import util.zip.ArchiviZip;

public class Subspedia implements ProviderSottotitoli {
	private static Subspedia instance;
	private final String BASEURL = "http://subspedia.weebly.com";
	private final String URL_ELENCO_SERIE=BASEURL+"/serie-tv.html";
	private final String URLFeedRSS=BASEURL+"/1/feed";
	private long time_update=(1000*60)* 20L/*minuti*/;
	private long last_update=0L;
	private static ArrayList<SubspediaRSSItem> rss;
	
	private Map<Integer, ArrayList<SottotitoloSubspedia>> cache;
	
	public static Subspedia getInstance(){
		if(instance==null)
			instance= new Subspedia();
		return instance;
	}
	
	private Subspedia(){
		rss=new ArrayList<SubspediaRSSItem>();
		cache = new HashMap<Integer, ArrayList<SottotitoloSubspedia>>();
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e, String lang) {
		if(!hasLanguage(lang))
			return false;
		if(s.getIDSubspedia()<=0)
			return false;
		
		SerieSubConDirectory ssub = getSerieAssociata(s);
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		ArrayList<File> videoFiles = DirectoryManager.getInstance().cercaFileVideo(s, e);
		ArrayList<String> urls = new ArrayList<String>();
		String baseDir = null;
		if(videoFiles.size()>0){
			for(int i=0;i<videoFiles.size();i++){
				CaratteristicheFile stat = Naming.parse(videoFiles.get(i).getName(), null);
				String url = cercaInCartella(ssub, stat);
				if(url==null)
					url=cercaSottotitoloLink(s, stat);
				if(url!=null)
					urls.add(url);
			}
			baseDir = videoFiles.get(0).getAbsolutePath().substring(0, videoFiles.get(0).getAbsolutePath().lastIndexOf(File.separator));
		}
		else {
			if(t==null)
				return false;
			String link = cercaInCartella(ssub, t);
			if(link==null)
				link=cercaSottotitoloLink(s, t);
			if(link!=null)
				urls.add(link);
			try {
				baseDir = DirectoryManager.getInstance().getAvailableDirectory();
				baseDir+=File.separator+s.getFolderSerie();
			}
			catch (DirectoryNotAvailableException e1) {
				e1.printStackTrace();
				return false;
			}
		}
	
		if(urls.size()>0) {
			boolean down = false;
			
			for(int i=0;i<urls.size();i++){
				String link = urls.get(i).replace(" ", "%20");
				String zip=baseDir+File.separator+s.getFolderSerie()+"_"+t.getStats().getStagione()+"_"+t.getStats().getEpisodio()+"_"+i+".zip";
				try {
					Download.downloadFromUrl(link, zip);
					ArchiviZip.estrai_tutto(zip, baseDir);
					down = true;
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(down){
    			e.setSubDownload(!down);
    			GestoreSottotitoli.setSottotitoloDownload(e.getId(), !down, ITALIANO);
			}
			return down;
			
		}
		return false;
	}
	public SerieSubConDirectory getSerieAssociata(SerieTV serie) {
		String query = "SELECT * FROM "+Database.TABLE_SUBSPEDIA+" WHERE id="+serie.getIDSubspedia();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()==1)
			return parseDB(res.get(0));
		return null;
	}
	
	public boolean cercaSottotitolo(SerieTV s, Torrent t) {
		scaricaFeed();
		for(int i=0;i<rss.size();i++){
			SubspediaRSSItem item=rss.get(i);
			if(item.getTitolo().compareToIgnoreCase(s.getNomeSerie())==0){
				if(item.getStagione()==t.getStats().getStagione()){
					if(item.getEpisodio()==t.getStats().getEpisodio())
						return true;
				}
			}
		}
		return false;
	}
	private String cercaSottotitoloLink(SerieTV s, Torrent t) {
		return cercaSottotitoloLink(s, t.getStats());
	}
	private String cercaSottotitoloLink(SerieTV s, CaratteristicheFile t) {
		scaricaFeed();
		for(int i=0;i<rss.size();i++){
			SubspediaRSSItem item=rss.get(i);
			if(item.getTitolo().compareToIgnoreCase(s.getNomeSerie())==0){
				if(item.getStagione()==t.getStagione()){
					if(item.getEpisodio()==t.getEpisodio())
						return item.getLink();
				}
			}
		}
		return null;
	}
	public ArrayList<SerieSub> getElencoSerie() {
		String query = "SELECT * FROM "+Database.TABLE_SUBSPEDIA;
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<SerieSub> el = new ArrayList<SerieSub>();
		for(int i=0;i<res.size();i++){
			el.add(parseDB(res.get(i)));
		}
		return el;
	}
	private SerieSubConDirectory parseDB(KVResult<String, Object> r){
		int id = (Integer)r.getValueByKey("id");
		String nome = (String)r.getValueByKey("nome");
		String url = (String)r.getValueByKey("url");
		SerieSubConDirectory s = new SerieSubConDirectory(nome, id, url);
		return s;
	}
	public String getProviderName() {
		return "Subspedia";
	}
	public void aggiornaElencoSerieOnline() {
		try {
			org.jsoup.nodes.Document page = Jsoup.connect(URL_ELENCO_SERIE)
					.header("User-Agent", UserAgent.get())
					.timeout(10000)
					.get();
			org.jsoup.select.Elements select = page.select("div#wsite-content").select("div.paragraph");
			org.jsoup.select.Elements series = select.select("a");
			for(int i=0;i<series.size();i++){
				org.jsoup.nodes.Element o=series.get(i);
				String nome = o.html().replace("&nbsp;", " ").replace("&amp;", "&").trim().replaceAll("\\<[^>]*>", "").replaceAll("[^\\w ]", "").trim();
				String url = o.attr("href").replace(BASEURL, "");
				if(url.startsWith("http") || nome.isEmpty())
					continue;
				addSerie(nome, url);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void addSerie(String nome, String url){
		if(isPresente(url))
			return;
		String query = "INSERT INTO "+Database.TABLE_SUBSPEDIA+" (nome, url) VALUES (\""+nome+"\",\""+url+"\")";
		Database.updateQuery(query);
	}
	private boolean isPresente(String dir){
		String query = "SELECT * FROM "+Database.TABLE_SUBSPEDIA+" WHERE url=\""+dir+"\"";
		return Database.selectQuery(query).size()>0;
	}
	
	private void scaricaFeed() {
		if(System.currentTimeMillis()-last_update<time_update)
			return;
		
		last_update=System.currentTimeMillis();
		HttpURLConnection connection = null; 
		try {
			rss.clear();
			
			connection = (HttpURLConnection) (new URL(URLFeedRSS).openConnection());
			connection.setRequestProperty("User-Agent", UserAgent.get());
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			dbfactory.setNamespaceAware(true);
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(connection.getInputStream());
			
			NodeList elementi=doc.getElementsByTagName("item");
			for(int i=0;i<elementi.getLength();i++){
				Node item=elementi.item(i);
				NodeList attributi=item.getChildNodes();
				String titolo="", link="";
				for(int j=0;j<attributi.getLength();j++){
					Node attributo=attributi.item(j);
					if(attributo instanceof Element){
						Element attr=(Element)attributo;
						switch(attr.getTagName()){
							case "title":
								titolo=attr.getTextContent();
								break;
							case "content:encoded":
								if(attr.getTextContent().contains("a href")){
									if(attr.getTextContent().contains(".zip")){
										link=attr.getTextContent().substring(attr.getTextContent().indexOf("a href")+"a href".length()+2, attr.getTextContent().indexOf(".zip")+".zip".length());
										link=link.replace("http://www.weebly.com", "");
									}
								}
								break;
						}
					}
				}
				rss.add(new SubspediaRSSItem(titolo, link));
			}
		} 
		catch (IOException e) {	
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (SAXException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		finally {
			if(connection!=null)
				connection.disconnect();
		}
	}
	
	public static void main(String[] args){
		
		try {
			
			org.jsoup.nodes.Document page = Jsoup.connect("http://www.subspedia.tv/API/getAllSeries.php")
					.ignoreContentType(true)
					.header("User-Agent", UserAgent.get())
					.timeout(30000)
					.get();
			/*
			org.jsoup.select.Elements series = page.select("td.titoloSerie").select("a");
			for(int i=0;i<series.size();i++){
				org.jsoup.nodes.Element o=series.get(i);
				String nome = o.text();
				String href = o.attr("href");
				System.out.println(nome +" "+ href);
			}
			*/
			//System.out.println(page.text());
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(page.text());
				JSONArray list = (JSONArray)obj;
				for(int i = 0;i<list.size();i++){
					JSONObject serie = (JSONObject) list.get(i);
					//System.out.println(serie.toJSONString());
					int id = Integer.parseInt(serie.get("id_serie").toString());
					String nome = serie.get("nome_serie").toString();
					System.out.println(nome + " "+ id);
				}
			}
			catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
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
	public boolean associa(int idSerie, int idSubspedia) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subspedia="+idSubspedia+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}
	
	@Override
	public boolean disassocia(int idSerie) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subspedia=0 WHERE id="+idSerie;
		return Database.updateQuery(query);
	}
	
	private ArrayList<SottotitoloSubspedia> caricaCartella(SerieSubConDirectory serie){
		ArrayList<SottotitoloSubspedia> subs = new ArrayList<SottotitoloSubspedia>();
		try {
			org.jsoup.nodes.Document doc = Jsoup.connect(BASEURL+serie.getDirectory())
					.header("User-Agent", UserAgent.get())
					.timeout(10000)
					.get();
			Elements links = doc.select("a");
			for(int i=0;i<links.size();i++){
				org.jsoup.nodes.Element a = links.get(i);
				if(a.hasAttr("href") && a.attr("href").toLowerCase().endsWith("zip")){
					String nomefile = a.attr("href").substring(a.attr("href").lastIndexOf("/"));
					SottotitoloSubspedia sub = new SottotitoloSubspedia(nomefile);
					sub.setUrlDownload(a.attr("href").startsWith("http")?a.attr("href"):BASEURL+a.attr("href"));
					subs.add(sub);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return subs;
	}
	private String cercaInCartella(SerieSubConDirectory serie, Torrent t) {
		return cercaInCartella(serie, t.getStats());
	}
	private String cercaInCartella(SerieSubConDirectory serie, CaratteristicheFile t) {
		ArrayList<SottotitoloSubspedia> subs = cache.get(serie.getIDDB());
		if(subs == null){
			subs = caricaCartella(serie);
			if(subs == null)
				return null;
			else
				cache.put(serie.getIDDB(), subs);
		}
		
		for(int i=0;i<subs.size();i++){
			SottotitoloSubspedia sub = subs.get(i);
			if(t.getEpisodio()==sub.getEpisodio() && t.getStagione()==sub.getStagione())
				return sub.getUrlDownload();
		}
		
		return null;
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSPEDIA;
	}
	@Override
	public boolean hasLanguage(String lang) {
		switch(lang){
			case ITALIANO:
				return true;
		}
		return false;
	}
}
