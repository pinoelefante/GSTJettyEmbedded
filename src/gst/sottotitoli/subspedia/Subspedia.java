package gst.sottotitoli.subspedia;

import gst.database.Database;
import gst.download.Download;
import gst.naming.Renamer;
import gst.programma.ManagerException;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.rss.SubspediaRSSItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Subspedia implements ProviderSottotitoli {
	private final String BASEURL = "http://subspedia.weebly.com";
	private final String URL_ELENCO_SERIE=BASEURL+"/serie-tv.html";
	private final String URLFeedRSS=BASEURL+"/1/feed";
	private long time_update=(1000*60)* 20L/*minuti*/;
	private long last_update=0L;
	private static ArrayList<SubspediaRSSItem> rss;
	private Settings settings;
	
	public Subspedia(){
		rss=new ArrayList<SubspediaRSSItem>();
		settings = Settings.getInstance();
	}
	
	public boolean scaricaSottotitolo(SerieTV s, Episodio e) {
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		
		String link=cercaSottotitoloLink(s, t);
		if(link==null)
			return false;
		else {
			link=link.replace(" ", "%20");
			if(scaricaSub(link, Renamer.generaNomeDownload(t), s.getFolderSerie())){
				e.setSubDownload(false);
				GestoreSottotitoli.setSottotitoloDownload(e.getId(), false);
				return true;
			}
			return false;
		}
	}
	private boolean scaricaSub(String url, String nome, String folder){
		String dir_s=settings.getDirectoryDownload()+(settings.getDirectoryDownload().endsWith(File.pathSeparator)?folder:(File.separator+folder));
		String destinazione=dir_s+File.separator+nome;
		try {
			Download.downloadFromUrl(url, destinazione);
			
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
	}
	public SerieSub getSerieAssociata(SerieTV serie) {return null;}
	
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
	public ArrayList<SerieSub> getElencoSerie() {return null;}
	
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
				if(url.startsWith("http"))
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
	
	private void stampaFeed(){
		for(int i=0;i<rss.size();i++){
			System.out.println(rss.get(i));
		}
	}
	
	public static void main(String[] args){
		Subspedia sp=new Subspedia();
		sp.scaricaFeed();
		sp.stampaFeed();
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

}
