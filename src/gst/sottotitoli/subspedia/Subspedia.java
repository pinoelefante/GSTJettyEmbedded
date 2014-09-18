package gst.sottotitoli.subspedia;

import gst.database.Database;
import gst.download.Download;
import gst.programma.ManagerException;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.SerieSubConDirectory;
import gst.sottotitoli.rss.SubspediaRSSItem;
import gst.tda.db.KVResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.zip.ArchiviZip;

public class Subspedia implements ProviderSottotitoli {
	private static Subspedia instance;
	private final String BASEURL = "http://subspedia.weebly.com";
	private final String URL_ELENCO_SERIE=BASEURL+"/serie-tv.html";
	private final String URLFeedRSS=BASEURL+"/1/feed";
	private long time_update=(1000*60)* 20L/*minuti*/;
	private long last_update=0L;
	private static ArrayList<SubspediaRSSItem> rss;
	private Settings settings;
	
	private Map<Integer, ArrayList<SottotitoloSubspedia>> cache;
	
	public static Subspedia getInstance(){
		if(instance==null)
			instance= new Subspedia();
		return instance;
	}
	
	private Subspedia(){
		rss=new ArrayList<SubspediaRSSItem>();
		settings = Settings.getInstance();
		cache = new HashMap<Integer, ArrayList<SottotitoloSubspedia>>();
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e) {
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		SerieSubConDirectory ssub = getSerieAssociata(s);
		if(s==null)
			return false;
		
		String link = cercaInCartella(ssub, t);
		if(link==null)
			link=cercaSottotitoloLink(s, t);
		
		if(link==null)
			return false;
		else {
			link=link.replace(" ", "%20");
			String zip=settings.getDirectoryDownload()+s.getFolderSerie()+File.separator+s.getFolderSerie()+"_"+t.getStats().getStagione()+"_"+t.getStats().getEpisodio()+".zip";
			try {
				Download.downloadFromUrl(link, zip);
				ArchiviZip.estrai_tutto(zip, settings.getDirectoryDownload()+s.getFolderSerie());
				e.setSubDownload(false);
				GestoreSottotitoli.setSottotitoloDownload(e.getId(), false);
				return true;
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
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
		scaricaFeed();
		for(int i=0;i<rss.size();i++){
			SubspediaRSSItem item=rss.get(i);
			if(item.getTitolo().compareToIgnoreCase(s.getNomeSerie())==0){
				if(item.getStagione()==t.getStats().getStagione()){
					if(item.getEpisodio()==t.getStats().getEpisodio())
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
			org.jsoup.nodes.Document page = Jsoup.connect(URL_ELENCO_SERIE).get();
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
		try {
			rss.clear();
			
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			dbfactory.setNamespaceAware(true);
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(URLFeedRSS);
			
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
	}
	
	public static void main(String[] args){
		Subspedia subspedia = getInstance();
		SerieSubConDirectory s = new SerieSubConDirectory("", 0, "/the-100.html");
		ArrayList<SottotitoloSubspedia> subs = subspedia.caricaCartella(s);
		for(int i=0;i<subs.size();i++){
			System.out.println(subs.get(i).getUrlDownload());
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
	private void associa(int idSerie, int idSubspedia) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subspedia="+idSubspedia+" WHERE id="+idSerie;
		Database.updateQuery(query);
	}
	
	private ArrayList<SottotitoloSubspedia> caricaCartella(SerieSubConDirectory serie){
		ArrayList<SottotitoloSubspedia> subs = new ArrayList<SottotitoloSubspedia>();
		try {
			org.jsoup.nodes.Document doc = Jsoup.connect(BASEURL+serie.getDirectory()).get();
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
			if(t.getStats().getEpisodio()==sub.getEpisodio() && t.getStats().getStagione()==sub.getStagione())
				return sub.getUrlDownload();
		}
		
		return null;
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSPEDIA;
	}
}
