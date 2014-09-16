package gst.sottotitoli.subsfactory;

import gst.database.Database;
import gst.download.Download;
import gst.naming.Renamer;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.EZTV;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.SerieSubConDirectory;
import gst.sottotitoli.rss.RSSItemSubsfactory;
import gst.tda.db.KVResult;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

//TODO utilizzare serie sub al posto di serie tv
public class Subsfactory implements ProviderSottotitoli {
	private static Subsfactory instance;
	
	private final static String URL_ELENCO_SERIE="http://subsfactory.it/subtitle/index.php?&direction=0&order=nom";
	private final static String URL_FEED_RSS="http://subsfactory.it/subtitle/rss.php";
	
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=900000L;  //15 minuti
	private ArrayList<RSSItemSubsfactory> feed_rss;
	
	private static int download_corrente=0;
	
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
	
	public String scaricaSottotitolo(SerieSubConDirectory ssub, Torrent t){
		if(t==null)
			return null;
		
		System.out.println(ssub.getNomeSerie()+" - id_subsfactory: "+ssub.getDirectory());
		if(ssub.getDirectory().isEmpty())
			return null;
		
		String url="";
		switch(0){
			case 0:
				url=cercaFeed(ssub.getDirectory(), t);
				if(url!=null){
					if(url.length()>0)
						break;
				}
			case 1:
				url=cercaURLInCartella(t);
				if(url!=null){
					if(url.length()>0)
						break;
					else
						return null;
				}
				else
					return null;
		}
		
		if(url!=null && !url.isEmpty()){
			return url;
		}
		return null;
	}
	@Override
	public boolean scaricaSottotitolo(SerieTV s, Episodio e) {
		if(s==null || s.getIDDBSubsfactory()<=0)
			return false;
		SerieSubConDirectory ssubs = new SerieSubConDirectory(s.getIDDBSubsfactory());
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		String url = scaricaSottotitolo(ssubs, t);
		if(url!=null){
			if(url.length()>0){
				url=url.replace(" ", "%20");
				if(scaricaSub(url, Renamer.generaNomeDownload(t), s.getFolderSerie())){
					e.setSubDownload(false);
					GestoreSottotitoli.setSottotitoloDownload(e.getId(), false);
					return true;
				}
			}
		}
		return false;
	}
	private String cercaURLInCartella(SerieSubConDirectory serie_sub, Torrent t){
		caricaCartella(serie_sub, "");
		ArrayList<SottotitoloSubsfactory> cartella = cache_dir.get(serie_sub.getIDDB());
		for(int i=0;i<cartella.size();i++){
			SottotitoloSubsfactory sub=cartella.get(i);
			if(sub!=null){
				if(t.getStats().getEpisodio()==sub.getEpisodio()){
					if(t.getStats().getStagione()==sub.getStagione()){
						return sub.getUrlDownload();
					}
				}
			}
		}
		return null;
	}
	private boolean scaricaSub(String url, String nome, String folder){
		String cartella_destinazione=settings.getDirectoryDownload()+(settings.getDirectoryDownload().endsWith(File.pathSeparator)?folder:(File.separator+folder));
		String destinazione=cartella_destinazione+File.separator+nome;
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
	@Override
	public SerieSub getSerieAssociata(SerieTV serie) {
		int iddb=serie.getIDDBSubsfactory();
		if(iddb>0){
			for(int i=0;i<elenco_serie.size();i++){
				SerieSub s=elenco_serie.get(i);
				if(s.getIDDB()==iddb)
					return s;
			}
		}
			
		for(int i=0;i<elenco_serie.size();i++){
			SerieSubConDirectory s=(SerieSubConDirectory) elenco_serie.get(i);
			if(serie.getNomeSerie().compareToIgnoreCase(s.getNomeSerie())==0)
				return s;
		}
		return null;
	}

	@Override
	public boolean cercaSottotitolo(Torrent t) {
		System.out.println("Subsfactory.it - "+t.getNomeSerie());
		SerieTV st=t.getSerieTV();
		
		String url=cercaURLInCartella(t);
		if(url!=null)
			return true;
		 
		//cerca in feed
		return cercaFeed(st.getSubsfactoryDirectory(), t)==null?false:true;
	}
	
	//Verifica all'interno della pagina della serie
	private void caricaCartella(SerieSubConDirectory s_subs, String id_cartella){
		String id_serie=s_subs.getDirectory();
		if(s_subs.isCartellaOnlineCaricata())
			return;
		/*
		 http://subsfactory.it/subtitle/index.php?&direction=0&order=nom&directory=Serie%20USA/Arrow 
		*/
		String url="http://subsfactory.it/subtitle/index.php?&direction=0&order=nom&directory="+id_serie.replace(" ", "%20")+"/"+id_cartella.replace(" ", "%20");
		try {
			//String path=url.substring(url.indexOf("directory=")+"directory=".length());
			int id_download=download_corrente++;
			Download.downloadFromUrl(url, settings.getUserDir()+"subsf_response_"+id_download);
			FileReader f=new FileReader(settings.getUserDir()+"subsf_response_"+id_download);
			Scanner file=new Scanner(f);
			while(file.hasNextLine()){
				String linea=file.nextLine().trim();
				if(linea.contains(".zip") && linea.contains("title=")){
					//System.out.println(linea);
					String nome_file=linea.substring(linea.indexOf("title=\"")+"title=\"".length(), linea.indexOf(".zip")+".zip".length());
					SottotitoloSubsfactory sub=new SottotitoloSubsfactory(nome_file, id_serie);
					if(linea.toLowerCase().contains("normale") && linea.toLowerCase().contains("normale")){
						sub.setNormale(true);
						sub.set720p(true);
					}
					else if(linea.toLowerCase().contains("720p")){
						sub.set720p(true);
						sub.setNormale(false);
					}
					else {
						sub.set720p(false);
						sub.setNormale(true);
					}
					String path_d="http://www.subsfactory.it/subtitle/index.php?action=downloadfile"+"&directory="+id_serie+(id_cartella.length()>0?("/"+id_cartella):""+"&filename="+sub.getNomeFile());
					sub.setUrlDownload(path_d);
					s_subs.addSub(sub);
					System.out.println(sub.getStagione()+"x"+sub.getEpisodio()+" - "+path_d);
				}
				else {
					//TODO ricerca cartelle
				}
			}
			s_subs.setCartellaOnlineCaricata();
			file.close();
			f.close();
			OperazioniFile.deleteFile(settings.getUserDir()+"subsf_response_"+id_download);
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
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
	
	private String cercaFeed(String id_subs, Torrent t){
		if(verificaTempo(update_time_rss, RSS_UltimoAggiornamento)){
			System.out.println("Aggiornando il feed RSS - Subsfactory.it");
			aggiornaFeedRSS();
		}
		for(int i=0;i<feed_rss.size();i++){
			RSSItemSubsfactory rss=feed_rss.get(i);
			//System.out.println(rss.getStagione()+" "+rss.getEpisodio()+" "+ rss.getUrlDownload());
			//System.out.println("ID: "+rss.getID()+" - "+id_subs);
			if(rss.getID().toLowerCase().startsWith(id_subs.toLowerCase())){
				//System.out.println("Stagione: "+rss.getStagione() + " - "+t.getStagione());
				if(rss.getStagione()==t.getStats().getStagione()){
					//System.out.println("Puntata: "+rss.getEpisodio()+" - "+t.getEpisodio());
					if(rss.getEpisodio()==t.getStats().getEpisodio()){
						//System.out.println("Risoluzione: Rss("+rss.is720p()+rss.isNormale()+")"+" - Torrent("+t.is720p()+!t.is720p()+")");
						if(rss.isNormale()==!t.getStats().is720p())
							return rss.getUrlDownload();
						else if(rss.is720p()==t.getStats().is720p())
							return rss.getUrlDownload();
					}
				}
			}
		}
		return null;
	}
	private void aggiornaFeedRSS(){
		RSS_UltimoAggiornamento=new GregorianCalendar();
		feed_rss.clear();
		try {
			//Download.downloadFromUrl(URL_FEED_RSS, Settings.getCurrentDir()+"feed_subs");
			//System.out.println("Aggiornamento feed RSS Subsfactory");
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
			//OperazioniFile.deleteFile(Settings.getCurrentDir()+"feed_subs");
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
		String query="SELECT * FROM "+Database.TABLE_SUBSFACTORY+" ORDER BY nome_serie ASC";
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
		String nome=(String) r.getValueByKey("nome_serie");
		return new SerieSubConDirectory(nome, db ,path);
	}
	public String toString(){
		return "Subsfactory";
	}

	@Override
	public void associaSerie(SerieTV s) {
		// TODO Auto-generated method stub
		
	}
	public synchronized void aggiornaElencoSerieOnline() {
		try {
			org.jsoup.nodes.Document page = Jsoup.connect(URL_ELENCO_SERIE).get();
			org.jsoup.select.Elements select = page.getElementsByAttributeValue("name", "loc");
			org.jsoup.select.Elements options = select.select("option");
			for(int i=0;i<options.size();i++){
				org.jsoup.nodes.Element o=options.get(i);
				String dir = o.val().replace("files/", "");
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
	
}
