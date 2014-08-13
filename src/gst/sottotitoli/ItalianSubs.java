package gst.sottotitoli;

import gst.download.Download;
import gst.naming.Renamer;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.tda.db.KVResult;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;

import Database.Database;
//TODO utilizzare serie sub al posto di serie tv
public class ItalianSubs implements ProviderSottotitoli{
	public final static int HDTV = 0,	
							HD720p = 1,  
							WEB_DL = 2,
							DVDRIP = 3,
							BLUERAY = 4, 
							BRRIP = 5, 
							FILM = 6;
	
	private static String APIKEY="87c9d52fba19ba856a883b1d3ddb14dd";
	@SuppressWarnings("unused")
	private static String AUTHCODE="";
	
	private static String API_SHOWLIST="https://api.italiansubs.net/api/rest/shows?apikey="+APIKEY;
	private static String API_SUB_GETID = "https://api.italiansubs.net/api/rest/subtitles/search?q=<QUERY>&show_id=<SHOW_ID>&version=<VERSIONE>&apikey="+APIKEY;
	private static String API_LOGIN="https://api.italiansubs.net/api/rest/users/login?username=<USERNAME>&password=<PASSWORD>&apikey="+APIKEY;
	
	private ArrayList<RSSItem> feed_rss; 
	private ArrayList<SerieSub> elenco_serie;
	
	private WebClient webClient;
	private boolean login_itasa=false;
	private boolean locked=true;
	private Thread LoggerItasa;
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=/*1 minuto*/60000L*15;  //15 minuti
	
	public ItalianSubs(){
		feed_rss=new ArrayList<RSSItem>();
		elenco_serie=new ArrayList<SerieSub>();
		caricaSerieDB();
		aggiornaElencoSerieOnline();
		System.out.println("ITASADB: Sono state caricate "+elenco_serie.size());
		loggaItasa();
	}
	public boolean isLocked(){
		return locked;
	}
	public void attendiUnlock(){
		try {
			LoggerItasa.join();
			locked=false;
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	
	public boolean scaricaSottotitolo(Torrent t) {
		int id_itasa=t.getSerieTV().getIDItasa();
		try {
			if(id_itasa<=0)
				return false;
			int id=cercaIDSottotitoloFromAPI(id_itasa, t.getStagione(), t.getEpisodio(), t.is720p()?HD720p:HDTV);
			scaricaSub(id, Renamer.generaNomeDownload(t), t.getNomeSerieFolder());
			t.setSubDownload(false, true);
			return true;
		}
		catch (ItasaSubNotFound e) {
			//ManagerException.registraEccezione(e);
			int id_s=cercaFeed(id_itasa, t);
			if(id_s<=0)
				return false;
			try {
				scaricaSub(id_s, Renamer.generaNomeDownload(t), t.getNomeSerieFolder());
				t.setSubDownload(false, true);
			}
			catch (FailedLoginException | FailingHttpStatusCodeException | IOException e1) {
				e1.printStackTrace();
				ManagerException.registraEccezione(e);
				return false;
			}
			return true;
		}
		catch (FailedLoginException e) {
			ManagerException.registraEccezione(e);
			return false;
		}
		catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
		
	}
	private int cercaFeed(int iditasa, Torrent t){
		if(verificaTempo(update_time_rss, RSS_UltimoAggiornamento)){
			System.out.println("Aggiornando il feed RSS - Italiansubs.net");
			aggiornaFeedRSS();
		}
		for(int i=0;i<feed_rss.size();i++){
			RSSItem rss=feed_rss.get(i);
			if(rss.getIDSerie()==iditasa){
				if(rss.is720p()==t.is720p()){
					if(rss.isNormale()==!t.is720p()){
						if(rss.getStagione()==t.getStagione()){
							if(rss.getEpisodio()==t.getEpisodio()){
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
		try {
			Download.downloadFromUrl("http://feeds.feedburner.com/ITASA-Ultimi-Sottotitoli", Settings.getUserDir()+"feed_itasa");
			FileReader f_r=new FileReader(Settings.getUserDir()+"feed_itasa");
			Scanner file=new Scanner(f_r);
			while(file.hasNextLine()){
				String riga=file.nextLine().trim();
				if(riga.contains("<item>")){
					String linea=file.nextLine().trim();
					String nome="", url="";
					boolean n_done=false, 
							u_done=false;
					while(!linea.contains("</item>")){
						if(linea.contains("<title>")){
							nome=linea.replace("<title>", "").replace("</title>", "").trim();
							n_done=true;
						}
						else if(linea.startsWith("<guid")){
							//url=linea.replace("<link>", "").replace("</link>", "").trim();
							url=linea.substring(linea.indexOf("\">")+2, linea.indexOf("</guid>")).replace("&amp;", "&");
							u_done=true;
						}
						if(u_done && n_done){
							RSSItem sub=new RSSItem(nome, url);
							feed_rss.add(sub);
							u_done=false;
							n_done=false;
						}
						linea=file.nextLine().trim();
					}
				}
			}
			file.close();
			f_r.close();
			OperazioniFile.deleteFile(Settings.getUserDir()+"feed_itasa");
		} 
		catch (IOException e) {
			ManagerException.registraEccezione(e);
		}
	}
	public static void main(String[] args){
		Database.Connect();
		ItalianSubs itasa=new ItalianSubs();
		try {
			int id=itasa.cercaIDSottotitoloFromAPI(399, 7, 5, ItalianSubs.HDTV);
			System.out.println("ID del sottotitolo: "+id);
		}
		catch (ItasaSubNotFound e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	private int cercaIDSottotitoloFromAPI(int show_id, int serie, int episodio, int tipo) throws ItasaSubNotFound { 
		String query = serie + "x"	+ (episodio < 10 ? "0" + episodio : episodio); 
		String tipo_sub = "Normale";
		switch (tipo) {
			case HDTV:
				tipo_sub = "Normale";
			break;
			case HD720p:
				tipo_sub = "720p";
			break;
		}
		String url_query = API_SUB_GETID.replace("<QUERY>", query).replace("<VERSIONE>", tipo_sub).replace("<SHOW_ID>", show_id	+ "");

		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(url_query);
			
			NodeList countlist=doc.getElementsByTagName("count");
			if(countlist.getLength()==1){
				Element count=(Element) countlist.item(0);
				int num_sub=Integer.parseInt(count.getTextContent());
				if(num_sub>0){
					NodeList idlist=doc.getElementsByTagName("id");
					int id=0;
					for(int i=0;i<idlist.getLength();i++){
						Node value=idlist.item(i);
						if(value instanceof Element){
							Element v=(Element)value;
							int id_v=Integer.parseInt(v.getTextContent());
							if(id_v>id)
								id=id_v;
						}
					}
					return id;
				}
				else {
					throw new ItasaSubNotFound("Sottotitolo non trovato");
				}
			}
			else
				throw new ItasaSubNotFound("Risposta API non valida");
		}
		catch(ParserConfigurationException e){}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		throw new ItasaSubNotFound("Sottotitolo non trovato");
	}
	private boolean isSeriePresente(int id){
		if(elenco_serie.isEmpty())
			return false;
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			if((int)s.getIDDB()==id)
				return true;
		}
		return false;
	}
	public synchronized void caricaElencoSerieOnlineXML() {
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(API_SHOWLIST);
			
			NodeList elenco_shows=doc.getElementsByTagName("show");
			for(int i=0;i<elenco_shows.getLength();i++){
				Node show=elenco_shows.item(i);
				NodeList show_attributi=show.getChildNodes();
				String nome="";
				int id=0;
				for(int j=0;j<show_attributi.getLength();j++){
					Node attr=show_attributi.item(j);
					if(attr instanceof Element){
						Element attributo=(Element)attr;
						switch(attributo.getTagName()){
							case "id":
								id=Integer.parseInt(attributo.getTextContent().trim());
								break;
							case "name":
								nome=attributo.getTextContent().trim();
								break;
						}
					}
				}
				if(!isSeriePresente(id)){
					SerieSub serie=new SerieSub(nome, id);
					addSerie(serie);
					salvaInDB(serie);
				}
			}
		} 
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean addSerie(SerieSub toInsert){
		if(elenco_serie.isEmpty()){
			elenco_serie.add(toInsert);
			return true;
		}
		
		boolean insert=false;
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			int compare=toInsert.getNomeSerie().compareToIgnoreCase(s.getNomeSerie());
			if(compare<0){
				elenco_serie.add(i, toInsert);
				return true;
			}
			else if(compare==0){
				return false;
			}
		}
		if(!insert){
			elenco_serie.add(toInsert);
			return true;
		}
		return false;
	}
	private void salvaInDB(SerieSub serie){
		String query="INSERT INTO "+Database.TABLE_ITASA+" (id_serie, nome_serie) VALUES ("+(int)serie.getIDDB()+", \""+serie.getNomeSerie()+"\")";
		Database.updateQuery(query);
	}
	public void aggiornaElencoSerieOnline(){
		caricaElencoSerieOnlineXML();
	}
	public void aggiornaElencoSerieOnlineFILE(){
		caricaElencoSerieOnlineXML();
		/*
		ArrayList<SerieSub> elenco=new ArrayList<SerieSub>();
		try {
			Download.downloadFromUrl(API_SHOWLIST, Settings.getCurrentDir()+"response_itasa");
			FileReader f_r=new FileReader(Settings.getCurrentDir()+"response_itasa");
			Scanner file=new Scanner(f_r);
			while(file.hasNextLine()){
				String linea=file.nextLine().trim();
				if(!linea.startsWith("<root>"))
					continue;
				linea=linea.substring(linea.indexOf("<show>"));
				do{
					String parse="";
					parse=linea.substring(linea.indexOf("<show>"), linea.indexOf("</show>")+"</show>".length());
					int id=Integer.parseInt(parse.substring(parse.indexOf("<id>")+"<id>".length(), parse.indexOf("</id>")));
					String nome=parse.substring(parse.indexOf("<name>")+"<name>".length(), parse.indexOf("</name>"));
					SerieSub s=new SerieSub(nome, id);
					elenco.add(s);
					linea=linea.substring(parse.length());
					if(linea.startsWith("</shows>"))
						break;
				}while(true);
			}
			file.close();
			f_r.close();
			OperazioniFile.deleteFile(Settings.getCurrentDir()+"response_itasa");
			if(elenco.size()>0){
				elenco_serie.clear();
				elenco_serie.addAll(elenco);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		*/
	}
	public boolean VerificaLogin(String username, String password){
		String url_login=API_LOGIN.replace("<USERNAME>", username).replace("<PASSWORD>", password);
		boolean stato=false;
		
		FileReader f_r=null;
		Scanner file=null;
		try {
			Download.downloadFromUrl(url_login, Settings.getUserDir()+"response_login");
			f_r=new FileReader(Settings.getUserDir()+"response_login");
			file=new Scanner(f_r);
			while(file.hasNextLine()){
				String linea=file.nextLine().trim();
				if(linea.startsWith("<root>")){
					String status=linea.substring(linea.indexOf("<status>"), linea.indexOf("</status>")).trim().replace("<status>", "");
					if(status.compareToIgnoreCase("success")==0){
						AUTHCODE=linea.substring(linea.indexOf("<authcode>"), linea.indexOf("</authcode>")).trim().replace("<authcode>", "").trim();
						stato=true;
					}
					else{
						AUTHCODE="";
						stato=false;
					}
					break;
				}
			}
		}
		catch (IOException e) {	
			ManagerException.registraEccezione(e);
		}
		finally{
			file.close();
			try {
				f_r.close();
			}
			catch (IOException e) {	
				ManagerException.registraEccezione(e);
			}
			OperazioniFile.deleteFile(Settings.getUserDir()+"response_login");
		}
		return stato;
	}
	public void loggaItasa() {
		class LoggerItasa extends Thread{
			public void run(){
				if (webClient == null){
					webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_8);
					webClient.setActiveXNative(false);
					webClient.setAppletEnabled(false);
					webClient.setCssEnabled(false);
					webClient.setJavaScriptEnabled(false);
				}
				
				try {
					webClient.getCookieManager().clearCookies();
					
					final int NORMALE=2, CONDIVISO=1, NOUSER=0;
					int login=NOUSER;
					
					if(!Settings.getItasaUsername().isEmpty() && !Settings.getItasaPassword().isEmpty()){
						if(VerificaLogin(Settings.getItasaUsername(), Settings.getItasaPassword())){ //utente loggato come utente proprietario
							login=NORMALE;
						}
					}
					else if(VerificaLogin("GestioneSerieTV", "gestione@90")){
						login=CONDIVISO;
					}
					else{
						login=NOUSER;
						login_itasa=false;
						return;
					}
					
					HtmlPage page1 = (HtmlPage) webClient.getPage("http://www.italiansubs.net");
					HtmlForm form = page1.getFormByName("login");
					HtmlSubmitInput button = (HtmlSubmitInput) form.getInputByName("Submit");
					HtmlTextInput textField = (HtmlTextInput) form.getInputByName("username");
					HtmlPasswordInput textField2 = (HtmlPasswordInput) form.getInputByName("passwd");
					
					textField.setValueAttribute(login==NORMALE?Settings.getItasaUsername():"GestioneSerieTV");
					textField2.setValueAttribute(login==NORMALE?Settings.getItasaPassword():"gestione@90");
					button.click();
					
					webClient.closeAllWindows();
					webClient.getCache().clear();
					login_itasa=true;
					locked=false;
				}
				catch (FailingHttpStatusCodeException | IOException e) {
					e.printStackTrace();
					login_itasa=false;
					ManagerException.registraEccezione(e);
				}
			}
		}
		LoggerItasa=new LoggerItasa();
		LoggerItasa.start();
	}
	public boolean isLogged(){
		try {
			LoggerItasa.join();
			return login_itasa;
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
		
	}
	private void scaricaSub(int id, String nome, String cartella) throws FailingHttpStatusCodeException, MalformedURLException, IOException, FailedLoginException {
		if(!isLogged())
			throw new FailedLoginException("Utente non loggato");
		if(nome.length()<=0)
			throw new IOException("Nome file destinazione vuoto");
		
		WebClient client=new WebClient(BrowserVersion.INTERNET_EXPLORER_8);
		client.setActiveXNative(false);
		client.setAppletEnabled(false);
		client.setCssEnabled(false);
		client.setJavaScriptEnabled(false);
		
		Set<Cookie> cook=webClient.getCookieManager().getCookies();
		Iterator<Cookie> it_cook=cook.iterator();
		while(it_cook.hasNext()){
			Cookie c=it_cook.next();
			client.getCookieManager().addCookie(c);
		}
		
		HtmlPage esempio = null;
		try {
			esempio = (HtmlPage) client.getPage("http://www.italiansubs.net/index.php?option=com_remository&Itemid=6&func=fileinfo&id="+id);
		}
		catch (FailingHttpStatusCodeException | IOException e1) {
			e1.printStackTrace();
			ManagerException.registraEccezione(e1);
		}

		@SuppressWarnings("rawtypes")
		List list = esempio.getByXPath("//dt[1]/center/a/@href");
		for (int i = 0; i < list.size(); i++) {
			DomNode node = (DomNode) list.get(i);
			String sub = node.getTextContent();
			System.out.println(sub);
			if (!sub.equalsIgnoreCase("http://www.italiansubs.net")) {
				WebResponse wr = client.getPage(sub).getWebResponse();
				
				String fileCompleto = Settings.getDirectoryDownload()+File.separator+nome;
				File cartella_download=new File(Settings.getDirectoryDownload() + File.separator+cartella);
				
				if(!cartella_download.exists()){
					System.out.println("Cartella "+cartella_download.getAbsolutePath()+" non esistente");
					cartella_download.mkdir();
				}
				
				BufferedInputStream in = new BufferedInputStream(wr.getContentAsStream());
				FileOutputStream output = new FileOutputStream(fileCompleto);
				IOUtils.copy(in, output);
				output.close();
				in.close();
		
				OperazioniFile.copyfile(fileCompleto, cartella_download.getAbsolutePath()+File.separator+nome);
				OperazioniFile.deleteFile(fileCompleto);
			}
		}
	}
	private int cercaSerie(String nome){
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			if(s.getNomeSerie().compareToIgnoreCase(nome)==0)
				return (int)s.getIDDB();
		}
		return -1;
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
	class RSSItem{
		private String url;
		private String nomeserie;
		private int stagione, episodio;
		private boolean hd720p;
		private boolean preair;
		private boolean normale=true;
		private int idserie;
		private int idsub;
		
		public RSSItem(String itemname, String url){
			this.setUrl(url);
			parse(itemname);
		}
		private void parse(String item){
			String nome = item;
			if (nome.contains("720p")) {
				nome = nome.replace("720p", "").trim();
				setNormale(false);
				set720p(true);
			}
			else if (nome.contains("Bluray")) {
				nome = nome.replace("Bluray", "").trim();
				setNormale(false);
			}
			else if (nome.contains("DVDRip")) {
				nome = nome.replace("DVDRip", "").trim();
				setNormale(false);
			}
			else if (nome.contains("BDRip")) {
				nome = nome.replace("BDRip", "").trim();
				setNormale(false);
			}
			else if (nome.contains("WEB-DL")) {
				nome = nome.replace("WEB-DL", "").trim();
				setNormale(false);
			}
			
			if (nome.contains("Preair"))
				nome.replace("Preair", "");
			if(nome.contains(" ")){
				String str_index = nome.substring(nome.lastIndexOf(" ")).trim();
				try {
					if (!str_index.contains("x")) {
						setEpisodio(Integer.parseInt(str_index));
					}
					else {
						setStagione(Integer.parseInt(str_index.substring(0, str_index.indexOf("x"))));
						setEpisodio(Integer.parseInt(str_index.substring(str_index.indexOf("x") + 1)));
					}
				}
				catch (NumberFormatException e) {
					//ManagerException.registraEccezione(e);
					return;
				}
				setNomeSerie(nome.substring(0, nome.indexOf(str_index)).trim().replace("&amp;", "&"));
				setIDSerie(cercaSerie(getNomeSerie()));
				setIDSub(Integer.parseInt(getUrl().substring(getUrl().indexOf("&id=")+"&id=".length())));
			}
			else {
				setNomeSerie(nome);
			}
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getNomeSerie() {
			return nomeserie;
		}
		public void setNomeSerie(String nomeserie) {
			this.nomeserie = nomeserie;
		}
		public int getEpisodio() {
			return episodio;
		}
		public void setEpisodio(int episodio) {
			this.episodio = episodio;
		}
		public int getStagione() {
			return stagione;
		}
		public void setStagione(int stagione) {
			this.stagione = stagione;
		}
		public boolean is720p() {
			return hd720p;
		}
		public void set720p(boolean hd720p) {
			this.hd720p = hd720p;
		}
		public boolean isPreAir() {
			return preair;
		}
		public void setPreAir(boolean preair) {
			this.preair = preair;
		}
		public String toString(){
			return nomeserie+" "+getStagione()+"x"+getEpisodio()+(is720p()?" 720p":"")+(isNormale()?" Normale":"");
		}
		public boolean isNormale() {
			return normale;
		}
		public void setNormale(boolean normale) {
			this.normale = normale;
		}
		public int getIDSerie() {
			return idserie;
		}
		public void setIDSerie(int idserie) {
			this.idserie = idserie;
		}
		public int getIDSub() {
			return idsub;
		}
		public void setIDSub(int idsub) {
			this.idsub = idsub;
		}
	}
	@Override
	public SerieSub getSerieAssociata(SerieTV serie) {
		if(serie.getIDItasa()>0){
			int id=serie.getIDItasa();
			for(int i=0;i<elenco_serie.size();i++){
				SerieSub s=elenco_serie.get(i);
				if(s.getIDDB()==id)
					return s;
			}
		}
		for(int i=0;i<elenco_serie.size();i++)
			if(elenco_serie.get(i).getNomeSerie().compareToIgnoreCase(serie.getNomeSerie())==0)
				return elenco_serie.get(i);
		return null;
	}

	@Override
	public boolean cercaSottotitolo(Torrent t) {
		System.out.println("ITASA "+t.toString());
		int id_itasa=t.getSerieTV().getIDItasa();
		if(id_itasa>0){
			int api_search=-1;
			int feed_search=-1;
			try {
				api_search=cercaIDSottotitoloFromAPI(id_itasa, t.getStagione(), t.getEpisodio(), t.is720p()?HD720p:HDTV);
				if(api_search>0){
					System.out.println("ITASA - Sottotitolo trovato tramite API");
					return true;
				}
			}
			catch (ItasaSubNotFound e) {
				System.out.println("ITASA - Sottotitolo non trovato");
				ManagerException.registraEccezione(e);
			}
			feed_search=cercaFeed(id_itasa, t);
			if(feed_search>0){
				System.out.println("ITASA - Sottotitolo trovato nel feed");
				return true;
			}
			else
				System.out.println("ITASA - Sottotitolo non trovato nel feed");
			
			return false;
		}
		else
			return false;
		
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		return elenco_serie;
	}

	@Override
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
	@Override
	public int getProviderID() {
		return GestoreSottotitoli.ITASA;
	}
	private void caricaSerieDB(){
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" ORDER BY nome_serie DESC";
		ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
		for(int i=0;i<res.size();i++){
			KVResult<String, Object> r=res.get(i);
			String nome=(String) r.getValueByKey("nome_serie");
			Integer id=(Integer) r.getValueByKey("id_serie");
			SerieSub serie=new SerieSub(nome, id);
			elenco_serie.add(0,serie);
		}
	}
}

