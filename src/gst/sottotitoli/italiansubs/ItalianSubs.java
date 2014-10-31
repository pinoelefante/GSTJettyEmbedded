package gst.sottotitoli.italiansubs;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.rss.RSSItemItalianSubs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;
import util.zip.ArchiviZip;

public class ItalianSubs implements ProviderSottotitoli{
	private static ItalianSubs itasa;
	public final static String HDTV = "Normale",	
							HD720p = "720p",
							HD1080i = "1080i",
							HD1080p = "1080p",
							WEB_DL = "web-dl",
							DVDRIP = "dvdrip",
							BLUERAY = "bluray", 
							BRRIP = "bdrip";
	
	private ArrayList<RSSItemItalianSubs> feed_rss; 
	private ArrayList<SerieSub> elenco_serie;
	
	private ItasaAPI api;
	
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=/*1 minuto*/60000L*15;  //15 minuti
	
	private boolean loggato=false;
	private Settings settings;
	
	public static ItalianSubs getInstance(){
		if(itasa==null)
			itasa = new ItalianSubs();
		return itasa;
	}
	
	private ItalianSubs(){
		settings = Settings.getInstance();
		api = new ItasaAPI();
		feed_rss = new ArrayList<RSSItemItalianSubs>();
	}
	private boolean logga(){
		loggato=api.login(!settings.getItasaUsername().isEmpty()?settings.getItasaUsername():"GestioneSerieTV",
				  		  !settings.getItasaPassword().isEmpty()?settings.getItasaPassword():"gestione@90");
		return loggato;
	}
	
	public boolean scaricaSottotitolo(SerieTV serie, Episodio episodio, String lang) {
		if(!hasLanguage(lang))
			return false;
		if(serie.getIDItasa()<=0)
			return false;
		
		if(!loggato)
			logga();
		
		Torrent link = GestioneSerieTV.getInstance().getLinkDownload(episodio.getId());
		ArrayList<File> videoFiles=DirectoryManager.getInstance().cercaFileVideo(serie, episodio);
		ArrayList<Integer> ids = new ArrayList<Integer>(2);
		if(videoFiles.size()>0){
			for(int i=0;i<videoFiles.size();i++){
				CaratteristicheFile stat = Naming.parse(videoFiles.get(i).getName(), null);
				int id=api.cercaSottotitolo(serie, stat);
				if(id>0)
					ids.add(id);
			}
		}
		else {	
			if(link==null)
				return false;
			int id = api.cercaSottotitolo(serie, link.getStats());
			if(id>0)
				ids.add(id);
		}
		
		ArrayList<String> subFiles=new ArrayList<String>(ids.size()+1);
		String baseDir;
		try {
			baseDir = DirectoryManager.getInstance().getAvailableDirectory();
		}
		catch (DirectoryNotAvailableException e1) {
			e1.printStackTrace();
			return false;
		}
		String dirDown = baseDir+serie.getFolderSerie();
		if(ids.size()>0){
			for(int i=0;i<ids.size();i++){
				try {
					String pathFile = api.download(ids.get(i), dirDown);
					if(pathFile!=null && !pathFile.isEmpty()){
						subFiles.add(pathFile);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			int idSub=cercaFeed(serie.getIDItasa(), link);
			if(idSub>0){
				try {
					String pathFile=api.download(idSub, dirDown);
					if(pathFile!=null && !pathFile.isEmpty())
						subFiles.add(pathFile);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
			
		if(subFiles.size()>0){
			for(int i=0;i<subFiles.size();i++){
				String pathFile = subFiles.get(i);
				ArchiviZip.estrai_tutto(pathFile, dirDown);
			}
			GestoreSottotitoli.setSottotitoloDownload(episodio.getId(), false, ITALIANO);
			return true;
		}
		return false;
	}
	private int cercaFeed(int iditasa, Torrent t){
		if(verificaTempo(update_time_rss, RSS_UltimoAggiornamento)){
			System.out.println("Aggiornando il feed RSS - Italiansubs.net");
			aggiornaFeedRSS();
		}
		for(int i=0;i<feed_rss.size();i++){
			RSSItemItalianSubs rss=feed_rss.get(i);
			if(rss.getIDSerie()==iditasa){
				if(rss.is720p()==t.getCaratteristiche().is720p()){
					if(rss.isNormale()==!t.getCaratteristiche().is720p()){
						if(rss.getStagione()==t.getCaratteristiche().getStagione()){
							if(rss.getEpisodio()==t.getCaratteristiche().getEpisodio()){
								return rss.getIDSub();
							}
						}
					}
				}
			}
		}
		return -1;
	}
	private void aggiornaFeedRSS(){
		RSS_UltimoAggiornamento=new GregorianCalendar();
		feed_rss.clear();
		
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse("http://feeds.feedburner.com/ITASA-Ultimi-Sottotitoli");
			
			NodeList items=doc.getElementsByTagName("item");
			for(int i=0;i<items.getLength();i++){
				NodeList childs = items.item(i).getChildNodes();
				
				String nome = null, url = null;
				for(int j=0;j<childs.getLength();j++){
					Node node = childs.item(j);
					Element attr;
					if(node instanceof Element)
						attr = (Element)node;
					else
						continue;
					switch(attr.getTagName()){
						case "title":
							nome=attr.getTextContent();
							break;
						case "guid":
							url=attr.getTextContent().replace("&amp;", "&");
							break;
					}
				}
				RSSItemItalianSubs itemRss=new RSSItemItalianSubs(nome, url);
				feed_rss.add(itemRss);
			}
		}
		catch(ParserConfigurationException e){
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean isSeriePresente(int id){
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" WHERE id="+id;
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		return res.size()>0;
	}
	
	private boolean insertSerie(SerieSub toInsert){
		if(!isSeriePresente(toInsert.getIDDB())){
			String query = "INSERT INTO "+Database.TABLE_ITASA+" (id,nome) VALUES("+toInsert.getIDDB()+",\""+toInsert.getNomeSerie()+"\")";
			return Database.updateQuery(query);
		}
		return false;
	}
	public void aggiornaElencoSerieOnline(){
		ArrayList<SerieSub> on=api.caricaElencoSerieOnlineXML();
		for(int i=0;i<on.size();i++){
			if(!isSeriePresente(on.get(i).getIDDB()))
				insertSerie(on.get(i));
		}
	}
	
	public static boolean VerificaLogin(String username, String password){
		return itasa.api.verificaLogin(username, password)!=null;
	}
	public int cercaSerie(String nome){
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" WHERE nome=\""+nome+"\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()!=1)
			return -1;
		else
			return (int) res.get(0).getValueByKey("id");
	}
	public String toStringFeed(){
		String str="";
		for(int i=0;i<feed_rss.size();i++)
			str+=feed_rss.get(i).toString()+"\n";
		
		return str;
	}
	public String toString(){
		return "Italiansubs";
	}
	private SerieSub getSerie(int id){
		return null;
	}
	public SerieSub getSerieAssociata(SerieTV serie) {
		if(serie.getIDItasa()>0){
			return getSerie(serie.getIDItasa());
		}
		else {
			int id=cercaSerie(serie.getNomeSerie());
			return (id<=0?null:cercaSerieAssociata(serie));
		}
	}

	public String getProviderName() {
		return "ItalianSubs.net";
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
	
	public ArrayList<SerieSub> getElencoSerie(String orderParam){
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" ORDER BY "+orderParam+" ASC";
		ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
		elenco_serie = new ArrayList<SerieSub>();
		for(int i=0;i<res.size();i++){
			KVResult<String, Object> r=res.get(i);
			String nome=(String) r.getValueByKey("nome");
			Integer id=(Integer) r.getValueByKey("id");
			SerieSub serie=new SerieSub(nome, id);
			elenco_serie.add(serie);
		}
		return elenco_serie;
	}

	public SerieSub cercaSerieAssociata(SerieTV serie) {
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" WHERE nome=\"" + serie.getNomeSerie() + "\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()==1){
			SerieSub s=new SerieSub((String) res.get(0).getValueByKey("nome"), (int)(res.get(0).getValueByKey("id")));
			return s;
		}
		return null;
	}
	public void associaSerie(SerieTV s){
		if(s.getIDItasa()>0)
			return;
		
		SerieSub s_sub=getSerieAssociata(s);
		if(s_sub!=null){
			s.setIDItasa(s_sub.getIDDB());
			associaSerie(s, s_sub.getIDDB());
		}
	}
	public void associaSerie(SerieTV serie, int idItasa){
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_itasa="+idItasa+" WHERE id="+serie.getIDDb();
		Database.updateQuery(query);
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		return getElencoSerie("nome");
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.ITASA;
	}

	@Override
	public boolean associa(int idSerie, int idSub) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_itasa="+idSub+" WHERE id="+idSerie;
		return Database.updateQuery(query);
	}

	@Override
	public boolean disassocia(int idSerie) {
		String query = "UPDATE "+Database.TABLE_SERIETV+" SET id_itasa=0 WHERE id="+idSerie;
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

