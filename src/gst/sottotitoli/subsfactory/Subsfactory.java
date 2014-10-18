package gst.sottotitoli.subsfactory;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
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
import gst.sottotitoli.rss.RSSItemSubsfactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.os.FileFinder;
import util.zip.ArchiviZip;

public class Subsfactory implements ProviderSottotitoli {
	private static Subsfactory instance;
	
	private final static String URL_ELENCO_SERIE="http://subsfactory.it/subtitle/index.php?&direction=0&order=nom";
	private final static String URL_FEED_RSS="http://subsfactory.it/subtitle/rss.php";
	
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=900000L;  //15 minuti
	private ArrayList<RSSItemSubsfactory> feed_rss;
	
	private Settings settings;
	
	private Map<Integer, ArrayList<SottotitoloSubsfactory>> cache_dir;
	
	public static Subsfactory getInstance(){
		if(instance==null)
			instance=new Subsfactory();
		return instance;
	}
	
	private Subsfactory() {
		feed_rss=new ArrayList<RSSItemSubsfactory>();
		cache_dir = new HashMap<Integer, ArrayList<SottotitoloSubsfactory>>();
		settings = Settings.getInstance();
	}
	
	@Override
	public boolean scaricaSottotitolo(SerieTV s, Episodio e, String lang) {
		if(!hasLanguage(lang))
			return false;
		if(s==null || s.getIDDBSubsfactory()<=0)
			return false;
		SerieSubConDirectory ssubs = new SerieSubConDirectory(s.getIDDBSubsfactory());
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		ArrayList<File> videoFiles = FileFinder.getInstance().cercaFileVideo(s, e);
		ArrayList<String> urls = new ArrayList<String>();
		if(videoFiles.size()>0){
			for(int i=0;i<videoFiles.size();i++){
				CaratteristicheFile stat = Naming.parse(videoFiles.get(i).getName(), null);
				String url = cercaURLInCartella(ssubs, stat);
				if(url == null || url.isEmpty())
					url = cercaFeed(ssubs.getDirectory(), stat);
				if(url!=null)
					urls.add(url);
			}
		}
		else {
			if(t==null)
				return false;
			String url = cercaURLInCartella(ssubs, t);
			if(url == null || url.isEmpty())
				url = cercaFeed(ssubs.getDirectory(), t);
			if(url!=null)
				urls.add(url);
		}
		
		if(urls.size()>0){
			boolean down = false;
			for(int i=0;i<urls.size();i++){
				String url = urls.get(i).replace(" ", "%20");
				try {
					String zip=settings.getDirectoryDownload()+s.getFolderSerie()+File.separator+s.getFolderSerie()+"_"+t.getStats().getStagione()+"_"+t.getStats().getEpisodio()+"_"+i+".zip";
					Download.downloadFromUrl(url, zip);
					ArchiviZip.estrai_tutto(zip, settings.getDirectoryDownload()+s.getFolderSerie());
					down = true;
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(down){
				e.setSubDownload(!down);
				GestoreSottotitoli.setSottotitoloDownload(e.getId(), false, ITALIANO);
			}
			return down;
		}
		return false;
	}
	private String cercaURLInCartella(SerieSubConDirectory s, Torrent t){
		return cercaURLInCartella(s, t.getStats());
	}
	private String cercaURLInCartella(SerieSubConDirectory serie_sub, CaratteristicheFile t){
		ArrayList<SerieSubConDirectory> dirs=getDirectoryAssociate(serie_sub);
		for(int i=0;i<dirs.size();i++){
			ArrayList<SottotitoloSubsfactory> subs=cache_dir.get(dirs.get(i).getIDDB());
			if(subs == null){
				subs = caricaCartella(dirs.get(i));
				if(subs==null)
					continue;
				cache_dir.put(dirs.get(i).getIDDB(), subs);
			}
			for(int j=0;j<subs.size();j++){
				SottotitoloSubsfactory sub = subs.get(j);
				if(t.is720p()==sub.is720p()){
					if(t.getEpisodio()==sub.getEpisodio()){
						if(t.getStagione()==sub.getStagione())
							return sub.getUrlDownload();
					}
				}
			}
		}
		return null;
	}
	
	private ArrayList<SottotitoloSubsfactory> caricaCartella(SerieSubConDirectory s_subs){
		ArrayList<SottotitoloSubsfactory> list = new ArrayList<SottotitoloSubsfactory>();
		try {
			org.jsoup.nodes.Document doc = Jsoup.connect("http://subsfactory.it/subtitle/index.php?&direction=0&order=nom&directory="+s_subs.getDirectory()).get();
			org.jsoup.select.Elements tds=doc.select("td");
			for(int i=0;i<tds.size();i++){
				org.jsoup.nodes.Element td = tds.get(i);
				if(td.hasAttr("title") && td.attr("title").toLowerCase().endsWith(".zip")){
					SottotitoloSubsfactory sub = new SottotitoloSubsfactory(td.attr("title"));
					sub.setUrlDownload("http://subsfactory.it/subtitle/index.php?action=downloadfile&filename="+td.attr("title")+"&directory="+s_subs.getDirectory());
					list.add(sub);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		return getSerieDB();
	}

	@Override
	public String getProviderName() {
		return "Subsfactory.it";
	}
	private boolean isPresente(String directory){
		String query = "SELECT * FROM "+Database.TABLE_SUBSFACTORY+" WHERE directory=\""+directory+"\"";
		return Database.selectQuery(query).size()>0;
	}
	private boolean addSerie(SerieSubConDirectory s){
		if(isPresente(s.getDirectory()))
			return false;
		String query = "INSERT INTO "+Database.TABLE_SUBSFACTORY+" (nome, directory) VALUES (\""+s.getNomeSerie()+"\",\""+s.getDirectory()+"\")";
		return Database.updateQuery(query);
	}
	private String cercaFeed(String id, Torrent t){
		return cercaFeed(id, t.getStats());
	}
	private String cercaFeed(String id_subs, CaratteristicheFile t){
		if(verificaTempo(update_time_rss, RSS_UltimoAggiornamento)){
			System.out.println("Aggiornando il feed RSS - Subsfactory.it");
			aggiornaFeedRSS();
		}
		for(int i=0;i<feed_rss.size();i++){
			RSSItemSubsfactory rss=feed_rss.get(i);
			if(rss.getID().toLowerCase().startsWith(id_subs.toLowerCase())){
				if(rss.getStagione()==t.getStagione()){
					if(rss.getEpisodio()==t.getEpisodio()){
						if(rss.isNormale()==!t.is720p())
							return rss.getUrlDownload();
						else if(rss.is720p()==t.is720p())
							return rss.getUrlDownload();
					}
				}
			}
		}
		return null;
	}
	public static void main(String[] args){
		Subsfactory s = getInstance();
		s.aggiornaFeedRSS();
		s.stampa_feed();
	}
	private void aggiornaFeedRSS(){
		RSS_UltimoAggiornamento=new GregorianCalendar();
		feed_rss.clear();
		try {
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(URL_FEED_RSS);
			
			NodeList elementi=doc.getElementsByTagName("item");
			for(int i=0;i<elementi.getLength();i++){
				Node item=elementi.item(i);
				NodeList attributi=item.getChildNodes();
				String titolo="", descrizione="", link="";
				for(int j=0;j<attributi.getLength();j++){
					Node attributo=attributi.item(j);
					if(attributo instanceof Element){
						Element attr=(Element)attributo;
						switch(attr.getTagName()){
							case "title":
								titolo=attr.getTextContent();
								break;
							case "description":
								descrizione=attr.getTextContent();
								break;
							case "link":
								link=attr.getTextContent();
								break;
						}
					}
				}
				RSSItemSubsfactory rss=new RSSItemSubsfactory(titolo, descrizione, link);
				if(rss.isValid())
					feed_rss.add(rss);
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
	private boolean verificaTempo(long maxdif, GregorianCalendar last){
		if(last==null)
			return true;
		GregorianCalendar adesso=new GregorianCalendar();
		long time_now=adesso.getTimeInMillis();
		adesso=null;
		long time_last=last.getTimeInMillis();
		if((time_now-time_last)>maxdif)
			return true;
		return false;
	}
	public void stampa_feed(){
		for(int i=0;i<feed_rss.size();i++)
			System.out.println(feed_rss.get(i));
	}

	private ArrayList<SerieSub> getSerieDB(){
		String query="SELECT * FROM "+Database.TABLE_SUBSFACTORY+" ORDER BY nome ASC";
		ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
		ArrayList<SerieSub> elenco_serie=new ArrayList<SerieSub>();
		for(int i=0;i<res.size();i++){
			KVResult<String, Object> r=res.get(i);
			SerieSubConDirectory s=parseSerieDB(r);
			if(s.getDirectory().split("/").length>2){
				s=null;
				continue;
			}
			elenco_serie.add(s);
		}
		elenco_serie.trimToSize();
		return elenco_serie;
	}
	private SerieSubConDirectory parseSerieDB(KVResult<String, Object> r){
		String path=(String) r.getValueByKey("directory");
		int db=(int) r.getValueByKey("id");
		String nome=(String) r.getValueByKey("nome");
		return new SerieSubConDirectory(nome, db ,path);
	}
	public String toString(){
		return "Subsfactory";
	}

	@Override
	public void associaSerie(SerieTV s) {
		ArrayList<SerieSub> serieDB = getSerieDB();
		for(int i=0;i<serieDB.size();i++){
			if(s.getNomeSerie().compareToIgnoreCase(serieDB.get(i).getNomeSerie())==0){
				s.setIDSubsfactory(serieDB.get(i).getIDDB());
				associa(s,serieDB.get(i).getIDDB());
				return;
			}
		}
	}
	private void associa(SerieTV s, int id){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subsfactory="+id+" WHERE id="+s.getIDDb();
		Database.updateQuery(query);
	}
	public synchronized void aggiornaElencoSerieOnline() {
		try {
			org.jsoup.nodes.Document page = Jsoup.connect(URL_ELENCO_SERIE).get();
			org.jsoup.select.Elements select = page.getElementsByAttributeValue("name", "loc");
			org.jsoup.select.Elements options = select.select("option");
			for(int i=0;i<options.size();i++){
				org.jsoup.nodes.Element o=options.get(i);
				String dir = o.val().replace("files/", "");
				if(dir.endsWith("/")){
					dir=dir.substring(0, dir.length()-1);
				}
				if(!dir.isEmpty()){
					String nomeSerie = o.html().replace("&nbsp;", "").trim();
					addSerie(new SerieSubConDirectory(nomeSerie, 0, dir));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	private ArrayList<SerieSubConDirectory> getDirectoryAssociate(SerieSubConDirectory s){
		String query = "SELECT * FROM "+Database.TABLE_SUBSFACTORY+" WHERE directory LIKE \""+s.getDirectory()+"%\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		ArrayList<SerieSubConDirectory> serie = new ArrayList<SerieSubConDirectory>();
		for(int i=0;i<res.size();i++){
			serie.add(parseSerieDB(res.get(i)));
		}
		return serie;
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSFACTORY;
	}
	@Override
	public boolean associa(int idSerie, int idSub) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subsfactory="+idSub+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean disassocia(int idSerie) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_subsfactory=0 WHERE id="+idSerie;
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
}
