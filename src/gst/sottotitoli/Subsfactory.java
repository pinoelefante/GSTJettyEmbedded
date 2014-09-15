package gst.sottotitoli;

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
import gst.sottotitoli.rss.RSSItemSubsfactory;
import gst.tda.db.KVResult;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//TODO utilizzare serie sub al posto di serie tv
public class Subsfactory implements ProviderSottotitoli {
	private final static String URL_ELENCO_SERIE="http://subsfactory.it/subtitle/index.php?&direction=0&order=nom";
	private final static String URL_FEED_RSS="http://subsfactory.it/subtitle/rss.php";
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=900000L;  //15 minuti
	private ArrayList<RSSItemSubsfactory> feed_rss;
	private ArrayList<SerieSub> elenco_serie;
	private static int download_corrente=0;
	private Settings settings;
	
	public Subsfactory() {
		feed_rss=new ArrayList<RSSItemSubsfactory>();
		elenco_serie=new ArrayList<SerieSub>();
		settings = Settings.getInstance();
	}
	
	@Override
	public boolean scaricaSottotitolo(SerieTV st, Episodio e) {
		if(st.getIDDBSubsfactory()<=0)
			return false;
		
		Torrent t = GestioneSerieTV.getInstance().getLinkDownload(e.getId());
		if(t==null)
			return false;
		
		if(st==null)
			return false;
		String id_subsfactory=st.getSubsfactoryDirectory();
		System.out.println(st.getNomeSerie()+" - id_subsfactory: "+id_subsfactory);
		if(id_subsfactory.isEmpty())
			return false;
		
		String url="";
		switch(0){
			case 0:
				url=cercaFeed(st.getSubsfactoryDirectory(), t);
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
						return false;
				}
				else
					return false;
		}
		
		if(url!=null){
			if(url.length()>0){
				url=url.replace(" ", "%20");
				if(scaricaSub(url, Renamer.generaNomeDownload(t), st.getFolderSerie())){
					e.setSubDownload(false);
					GestoreSottotitoli.setSottotitoloDownload(e.getId(), false);
					return true;
				}
			}
		}
		return false;
	}
	private String cercaURLInCartella(SerieTV s, Torrent t){
		SerieSubSubsfactory serie_sub=(SerieSubSubsfactory) getSerieAssociata(s);
		caricaCartella(serie_sub, "");
		for(int i=0;i<serie_sub.getCartellaOnlineSize();i++){
			SottotitoloSubsfactory sub=serie_sub.getSubFromCartellaOnline(i);
			//System.out.println(sub);
			if(sub!=null){
				if(t.getStats().getEpisodio()==sub.getEpisodio()){
					//System.out.println("Episodio OK");
					if(t.getStats().getStagione()==sub.getStagione()){
						//System.out.println("Stagione OK");
						return sub.getUrlDownload();
					}
					else {
						//System.out.println("Stagione non OK");
					}
				}
				else {
					//System.out.println("Episodio non OK - "+t.getEpisodio()+" diverso da "+sub.getEpisodio());
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
			SerieSubSubsfactory s=(SerieSubSubsfactory) elenco_serie.get(i);
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
	private void caricaCartella(SerieSubSubsfactory s_subs, String id_cartella){
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
		return elenco_serie;
	}

	@Override
	public String getProviderName() {
		return "Subsfactory.it";
	}
	private boolean isPresente(String directory){
		for(int i=0;i<elenco_serie.size();i++){
			SerieSubSubsfactory s=(SerieSubSubsfactory) elenco_serie.get(i);
			if((s.getDirectory().compareTo(directory))==0)
				return true;
		}
		return false;
	}
	private boolean addSerie(SerieSub s){
		boolean inserita=false;
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s1=elenco_serie.get(i);
			int compare=s.getNomeSerie().compareToIgnoreCase(s1.getNomeSerie());
			if(compare<0){
				elenco_serie.add(i, s);
				return true;
			}
			else if(compare==0)
				return false;
		}
		if(!inserita){
			elenco_serie.add(s);
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized void aggiornaElencoSerieOnline() {
		FileReader f_r;
		try {
			Download.downloadFromUrl(URL_ELENCO_SERIE, settings.getUserDir()+"response_subs");
			f_r=new FileReader(settings.getUserDir()+"response_subs");
		}
		catch (IOException e) {
			OperazioniFile.deleteFile(settings.getUserDir()+"response_subs");
			ManagerException.registraEccezione(e);
			return;
		}
		Scanner file=new Scanner(f_r);
		
		while(!file.nextLine().contains("<select name=\"loc\""));
		
		while(true){
			String riga=file.nextLine().trim();
			if(riga.startsWith("<option value=")){
				String path=riga.substring("<option value=\"files/".length(), riga.indexOf("\">")-1).trim();
				if(path.startsWith("Film"))
					continue;
				String nome=riga.substring(riga.indexOf("\">")+2, riga.indexOf("</option>")).trim().replace("&nbsp;", "");
				if(path.split("/").length>2)
					continue;
				if(!isPresente(path)){
					SerieSubSubsfactory serie=new SerieSubSubsfactory(nome,0, path);
					addSerie(serie);
					salvaInDB(serie);
					serie.setIDDB(serie.getIDDB());
				}
			}
			else if(riga.compareToIgnoreCase("</select>")==0)
				break;
		}
		
		file.close();
		try {
			f_r.close();
			OperazioniFile.deleteFile(Settings.getUserDir()+"response_subs");
		}
		catch (IOException e) {
			ManagerException.registraEccezione(e);
		}
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

	private void caricaSerieDB(){
		String query="SELECT * FROM "+Database.TABLE_SUBSFACTORY+" ORDER BY nome_serie DESC";
		ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
		for(int i=0;i<res.size();i++){
			KVResult<String, Object> r=res.get(i);
			int db=(int) r.getValueByKey("id");
			String nome=(String) r.getValueByKey("nome_serie");
			String path=(String) r.getValueByKey("directory");
			SerieSubSubsfactory s=new SerieSubSubsfactory(nome, db ,path);
			elenco_serie.add(0,s);
		}
	}
	public String toString(){
		return "Subsfactory";
	}
}
