package gst.infoManager.thetvdb;

import gst.database.Database;
import gst.serieTV.SerieTV;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TheTVDB {
	private static TheTVDB				   instance;

	private static String				   APIKEY				 = "294AFD865CEB421D";
	private static String				   API_MIRROR_PATH		= "http://thetvdb.com/api/" + APIKEY + "/mirrors.xml";

	private static ArrayList<Mirror>	   list_mirrors;

	private static String				   API_IMAGE			  = "<mirror_path>/banners/<path_image>";
	private static String				   API_CERCA_SERIE_LINGUA = "<mirrorpath>/api/GetSeries.php?seriesname=<seriesname>&language=<language>";
	private static String				   API_GET_SERIE_INFO	 = "<mirrorpath>/api/" + APIKEY + "/series/<idserie>/<language>.xml";
	private static String				   API_GET_ATTORI_SERIE	 = "<mirrorpath>/api/" + APIKEY + "/series/<idserie>/actors.xml";
	private static String				   API_GET_IMAGES	 = "<mirrorpath>/api/" + APIKEY + "/series/<idserie>/banners.xml";
	private static String				   API_GET_INFO_EPISODIO = "<mirrorpath>/api/"+APIKEY+"/series/<idserie>/default/<season>/<episode>/<language>.xml";
	private String						   defaultLang			= "en";
	private final String FANART="fanart", POSTER="poster";

	public ArrayList<Entry<String, String>> lingueDisponibili;

	public static TheTVDB getInstance() {
		if (instance == null)
			instance = new TheTVDB();
		return instance;
	}

	private TheTVDB() {
		// defaultLang = Settings.getInstance().getTVDBPreferredLang();
		initLanguage();
		caricaMirrors();
	}

	private void initLanguage() {
		if (lingueDisponibili == null)
			lingueDisponibili = new ArrayList<Map.Entry<String, String>>();
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Chinese", "zh"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Deutsch", "de"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("English", "en"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Espanol", "es"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Français", "fr"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Italiano", "it"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Japanese", "ja"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Portuguese", "pt"));
		lingueDisponibili.add(new AbstractMap.SimpleEntry<>("Russian", "ru"));
	}

	public void caricaMirrors() {
		if (list_mirrors == null)
			list_mirrors = new ArrayList<Mirror>(2);
		HttpURLConnection conn = null;
		try {
			conn = getConnection(API_MIRROR_PATH);
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(conn.getInputStream());

			NodeList elementi = doc.getElementsByTagName("Mirror");
			for (int i = 0; i < elementi.getLength(); i++) {
				Node mirror = elementi.item(i);
				NodeList attributi = mirror.getChildNodes();
				String mirror_path = "";
				int id = 0, mask = 0;
				for (int j = 0; j < attributi.getLength(); j++) {
					Node attributo = attributi.item(j);
					if (attributo instanceof Element) {
						Element attr = (Element) attributo;
						switch (attr.getTagName()) {
							case "id":
								id = Integer.parseInt(attr.getTextContent());
								break;
							case "mirrorpath":
								mirror_path = attr.getTextContent();
								break;
							case "typemask":
								mask = Integer.parseInt(attr.getTextContent());
								break;
						}
					}
				}
				Mirror newMirror = new Mirror(id, mirror_path, mask);
				list_mirrors.add(newMirror);
			}
		}
		catch (IOException | ParserConfigurationException | SAXException  e) {
			e.printStackTrace();
		}
	}

	public void stampaMirrors() {
		for (int i = 0; i < list_mirrors.size(); i++)
			System.out.println(list_mirrors.get(i));
	}

	public Mirror getXMLMirror() {
		if (list_mirrors != null) {
			for (int i = 0; i < list_mirrors.size(); i++) {
				Mirror mirror = list_mirrors.get(i);
				if (mirror.isXML())
					return mirror;
			}
		}
		return null;
	}

	public Mirror getBannerMirror() {
		if (list_mirrors != null) {
			for (int i = 0; i < list_mirrors.size(); i++) {
				Mirror mirror = list_mirrors.get(i);
				if (mirror.isBanner())
					return mirror;
			}
		}
		return null;
	}

	public Mirror getZipMirror() {
		if (list_mirrors != null) {
			for (int i = 0; i < list_mirrors.size(); i++) {
				Mirror mirror = list_mirrors.get(i);
				if (mirror.isZip())
					return mirror;
			}
		}
		return null;

	}

	public ArrayList<SerieTVDB> cercaSerie(SerieTV serietv) {
		ArrayList<SerieTVDB> serie_trovate = new ArrayList<SerieTVDB>(1);
		HttpURLConnection conn = null;
		try {
			Mirror mirror = getXMLMirror();
			if (mirror == null)
				return null;

			String API_PATH = API_CERCA_SERIE_LINGUA.replace("<seriesname>", serietv.getTitolo()).replace("<mirrorpath>", mirror.getUrl()).replace("<language>", defaultLang);
			conn = getConnection(API_PATH);
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(conn.getInputStream());

			NodeList elementi = doc.getElementsByTagName("Series");
			if (elementi.getLength() == 0) {
				API_PATH = API_CERCA_SERIE_LINGUA.replace("<seriesname>", SerieTV.removeNationality(serietv.getTitolo())).replace("<mirrorpath>", mirror.getUrl()).replace("<language>", defaultLang);
				doc = domparser.parse(API_PATH);
				elementi = doc.getElementsByTagName("Series");
				if (elementi.getLength() == 0)
					return serie_trovate;
			}
			for (int i = 0; i < elementi.getLength(); i++) {
				Node serie = elementi.item(i);
				NodeList attributi = serie.getChildNodes();
				String banner_path = "", descrizione = "", data_inizio = "", nome_serie = "", lang = "";
				int id = 0;
				for (int j = 0; j < attributi.getLength(); j++) {
					Node attributo = attributi.item(j);
					if (attributo instanceof Element) {
						Element attr = (Element) attributo;
						switch (attr.getTagName()) {
							case "id":
							case "seriesid":
								id = Integer.parseInt(attr.getTextContent());
								break;
							case "banner":
								banner_path = attr.getTextContent();
								break;
							case "Overview":
								descrizione = attr.getTextContent();
								break;
							case "FirstAired":
								data_inizio = attr.getTextContent();
								break;
							case "SeriesName":
								nome_serie = attr.getTextContent();
								break;
							case "language":
								lang = attr.getTextContent();
								break;
						}
					}
				}
				SerieTVDB newSerie = new SerieTVDB(id, Naming.rimuoviAnnoInParentesi(nome_serie), descrizione, getBannerURL(banner_path), data_inizio, lang);
				addSerieToList(serie_trovate, newSerie);
			}
		}
		catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		filtraRisultati(serie_trovate, serietv.getTitolo());
		return serie_trovate;
	}
	private void filtraRisultati(ArrayList<SerieTVDB> list, String nomeSerie ){
		ArrayList<SerieTVDB> resOK = new ArrayList<>(2);
		for(int i=0;i<list.size();i++){
			if(list.get(i).getNomeSerie().compareToIgnoreCase(nomeSerie)==0){
				resOK.add(list.get(i));
			}
		}
		if(resOK.size()>0){
			list.clear();
			list.addAll(resOK);
		}
	}
	private void addSerieToList(ArrayList<SerieTVDB> list, SerieTVDB serie){
		boolean trovata = false;
		for(int i=0;i<list.size() && !trovata;i++){
			if(list.get(i).getId()==serie.getId()){
				if(list.get(i).getLang().compareToIgnoreCase(defaultLang)==0){
					trovata=true;
				}
				else {
					list.remove(i);
					list.add(serie);
					trovata = true;
				}
			}
		}
		if(!trovata){
			list.add(serie);
		}
	}
	private final static int PERIODO_AGGIORNAMENTO_SERIE = 2592000; //30 giorni
	public SerieTVDBFull getSerie(int idSerie, boolean forceUpdate, boolean skipImage) {
		SerieTVDBFull serieDB = caricaSerie(idSerie, skipImage);
		if(serieDB!=null){
			if((serieDB.getUltimoAggiornamento()+PERIODO_AGGIORNAMENTO_SERIE) > (System.currentTimeMillis()/1000)){
				return serieDB;
			}
		}
		else 
			System.out.println("Serie non trovata nel database");
		System.out.println("Carico la serie online");
		Mirror xmlMirror = getXMLMirror();
		if (xmlMirror == null)
			return null;
		String apiCall = API_GET_SERIE_INFO.replace("<mirrorpath>", xmlMirror.getUrl()).replace("<idserie>", idSerie + "").replace("<language>", defaultLang);
		HttpURLConnection conn = null;
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser;
		try {
			domparser = dbfactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		Document doc;
		try {
			conn = getConnection(apiCall);
			doc = domparser.parse(conn.getInputStream());
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
			if(forceUpdate && serieDB!=null)
				return serieDB;
			else
				return null;
		}
		
		NodeList elementi = doc.getElementsByTagName("Series");
		if (elementi.getLength() != 1)
			return null;

		Node serie = elementi.item(0);
		NodeList attributi = serie.getChildNodes();
		String banner_path = "", descrizione = "", data_inizio = "", nome_serie = "", lang = "";
		String attori ="", giornoTrasmissione="", oraTrasmissione="", generi="", network="", durateEpisodi="", rating="", status="";
		for (int j = 0; j < attributi.getLength(); j++) {
			Node attributo = attributi.item(j);
			if (attributo instanceof Element) {
				Element attr = (Element) attributo;
				switch (attr.getTagName()) {
					case "banner":
						banner_path = attr.getTextContent();
						break;
					case "Overview":
						descrizione = attr.getTextContent();
						break;
					case "FirstAired":
						data_inizio = attr.getTextContent();
						break;
					case "SeriesName":
						nome_serie = attr.getTextContent();
						break;
					case "Language":
						lang = attr.getTextContent();
						break;
					case "Actors":
						attori = attr.getTextContent();
						break;
					case "Airs_DayOfWeek":
						giornoTrasmissione = attr.getTextContent();
						break;
					case "Airs_Time":
						oraTrasmissione = attr.getTextContent();
						break;
					case "Genre":
						generi = attr.getTextContent();
						break;
					case "Network":
						network = attr.getTextContent();
						break;
					case "Rating":
						rating = attr.getTextContent();
						break;
					case "Runtime":
						durateEpisodi = attr.getTextContent();
						break;
					case "Status":
						status = attr.getTextContent();
						break;
				}
			}
		}
		SerieTVDBFull newSerie = new SerieTVDBFull(idSerie, nome_serie, descrizione, getBannerURL(banner_path), data_inizio, lang);
		newSerie.setDurataEpisodi(Integer.parseInt(durateEpisodi));
		newSerie.setGiornoSettimana(giornoTrasmissione);
		newSerie.setOraTrasmissione(oraTrasmissione);
		newSerie.setGeneri(generi);
		newSerie.setNetwork(network);
		newSerie.setRating(rating);
		newSerie.setStatoSerie(status);
		newSerie.setAttoriString(attori);
		getAttori(newSerie);
		if(!skipImage){
    		getImmagini(newSerie);
		}
		if(serieDB!=null)
			aggiornaSerie(newSerie);
		else
			salvaSerie(newSerie);
		return newSerie;
	}
	
	public void getAttori(SerieTVDBFull serie){
		System.out.println("Carico gli attori da TVDB");
		Mirror xmlMirror = getXMLMirror();
		if(xmlMirror==null)
			return;
		String apiCall = API_GET_ATTORI_SERIE.replace("<mirrorpath>", xmlMirror.getUrl()).replace("<idserie>", serie.getId()+"");
		
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser;
		try {
			domparser = dbfactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		Document doc;
		HttpURLConnection conn = null;
		try {
			conn = getConnection(apiCall);
			doc = domparser.parse(conn.getInputStream());
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		NodeList attori = doc.getElementsByTagName("Actor");
		for(int i=0;i<attori.getLength();i++){
			Node attore = attori.item(i);
			NodeList attributi = attore.getChildNodes();
			String image="", nome="", ruolo="";
			for (int j = 0; j < attributi.getLength(); j++) {
				Node attributo = attributi.item(j);
				if (attributo instanceof Element) {
					Element attr = (Element) attributo;
					switch (attr.getTagName()) {
						case "Image":
							image = attr.getTextContent();
							break;
						case "Role":
							ruolo = attr.getTextContent();
							break;
						case "Name":
							nome = attr.getTextContent();
							break;
					}
				}
			}
			ActorTVDB act = new ActorTVDB(nome, ruolo);
			act.setUrlImage(getBannerURL(image));
			serie.aggiungiAttore(act);
		}
		salvaAttori(serie);
	}
	private void salvaAttori(SerieTVDBFull serie){
		System.out.println("Salvo gli attori nel DB");
		String queryD = "DELETE FROM "+Database.TABLE_TVDB_ATTORI+" WHERE idSerie=?";
		Database.updateQuery(queryD, serie.getId());
		
		for(ActorTVDB a:serie.getAttori()){
			String q = "INSERT INTO "+Database.TABLE_TVDB_ATTORI+" (idSerie,attore,ruolo,image) VALUES (?,?,?,?)";
			Database.updateQuery(q,serie.getId(),a.getNome(), a.getRuolo(), a.getUrlImage());
		}
	}
	private void loadAttori(SerieTVDBFull serie){
		System.out.println("Carico gli attori da DB");
		String query = "SELECT * FROM "+Database.TABLE_TVDB_ATTORI+" WHERE idSerie="+serie.getId();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for(int i=0;i<res.size();i++)
			serie.aggiungiAttore(parseAttore(res.get(i)));
	}
	private ActorTVDB parseAttore(KVResult<String, Object> r){
		String nome = (String) r.getValueByKey("attore");
		String ruolo = (String) r.getValueByKey("ruolo");
		String img = (String) r.getValueByKey("image");
		ActorTVDB a = new ActorTVDB(nome, ruolo);
		a.setUrlImage(img);
		return a;
	}
	public String getBannerURL(String path) {
		Mirror mirror = getBannerMirror();
		if (mirror != null) {
			if(path==null || path.isEmpty())
				return "";
			String url_API = API_IMAGE.replace("<mirror_path>", mirror.getUrl()).replace("<path_image>", path);
			return url_API;
		}
		return path;
	}
	public void getImmagini(SerieTVDBFull s) {
		Mirror mirr = getBannerMirror();
		if(mirr==null)
			return;
		String apiCall = API_GET_IMAGES.replace("<mirrorpath>", mirr.getUrl()).replace("<idserie>", ""+s.getId());
		
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser;
		try {
			domparser = dbfactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		Document doc;
		HttpURLConnection conn = null;
		try {
			conn = getConnection(apiCall);
			doc = domparser.parse(conn.getInputStream());
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		NodeList banners = doc.getElementsByTagName("Banner");
		for(int i=0;i<banners.getLength();i++){
			Node attore = banners.item(i);
			NodeList attributi = attore.getChildNodes();
			String image="", ruolo="";
			for (int j = 0; j < attributi.getLength(); j++) {
				Node attributo = attributi.item(j);
				if (attributo instanceof Element) {
					Element attr = (Element) attributo;
					switch (attr.getTagName()) {
						case "BannerPath":
							image = attr.getTextContent();
							break;
						case "BannerType":
							ruolo = attr.getTextContent();
							break;
					}
				}
			}
			if(ruolo.compareToIgnoreCase(FANART)==0 || ruolo.compareToIgnoreCase(POSTER)==0){
				s.aggiungiPoster(getBannerURL(image));
			}
			else {
				s.aggiungiBanner(getBannerURL(image));
			}
		}
	}
	private void salvaSerie(SerieTVDBFull serie){
		String query = "INSERT INTO "+Database.TABLE_TVDB_SERIE + 
				"(id,nomeSerie,rating,generi,network,inizio,giorno_settimana,ora_trasmissione,durata_episodi,stato,descrizione,descrizione_lang,banner,ultimo_aggiornamento)"+
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Database.updateQuery(query, serie.getId(),serie.getNomeSerie(),serie.getRating(),serie.getGeneriString(),serie.getNetwork(),serie.getDataInizioITA(),serie.getGiornoSettimana(),serie.getOraTrasmissione(),serie.getDurataEpisodi(),serie.getStatoSerie(),serie.getDescrizione(),(serie.getLang().isEmpty()?defaultLang:serie.getLang()),serie.getUrlBanner(),(System.currentTimeMillis()/1000));
	}
	private SerieTVDBFull caricaSerie(int id, boolean skipImage){
		String query = "SELECT * FROM "+Database.TABLE_TVDB_SERIE+" WHERE id="+id;
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res==null || res.size()==0 || res.size()>1)
			return null;
		else {
			SerieTVDBFull serie = parseSerie(res.get(0));
			if(System.currentTimeMillis()/1000 < serie.getUltimoAggiornamento()+PERIODO_AGGIORNAMENTO_SERIE)
				loadAttori(serie);
			else
				getAttori(serie);
			if(!skipImage){
				getImmagini(serie);
			}
			return serie;
		}
	}
	private SerieTVDBFull parseSerie(KVResult<String, Object> r){
		int id = (int) r.getValueByKey("id");
		String nome = (String) r.getValueByKey("nomeSerie");
		String generi = (String) r.getValueByKey("generi");
		float rating = Float.parseFloat(r.getValueByKey("rating")+"");
		String network = (String) r.getValueByKey("network");
		String inizio = (String) r.getValueByKey("inizio");
		String giorno_settimana = (String) r.getValueByKey("giorno_settimana");
		String ora_trasmissione = (String) r.getValueByKey("ora_trasmissione");
		int durata_episodio = (int) r.getValueByKey("durata_episodi");
		String stato = (String) r.getValueByKey("stato");
		String descrizione = (String) r.getValueByKey("descrizione");
		String descrizione_lang = (String) r.getValueByKey("descrizione_lang");
		String bannerURL = (String) r.getValueByKey("banner");
		Integer ultimoAggiornamento = (Integer) r.getValueByKey("ultimo_aggiornamento");
		
		SerieTVDBFull serie = new SerieTVDBFull(id, nome, descrizione, bannerURL, inizio, descrizione_lang.isEmpty()?defaultLang:descrizione_lang);
		serie.setNetwork(network);
		serie.setGiornoSettimana(giorno_settimana);
		serie.setOraTrasmissione(ora_trasmissione);
		serie.setDurataEpisodi(durata_episodio);
		serie.setStatoSerie(stato);
		serie.setUltimoAggiornamento(ultimoAggiornamento);
		serie.setGeneri(generi);
		serie.setRating(rating+"");
		
		return serie;
	}
	private void aggiornaSerie(SerieTVDBFull serie){
		String query = "UPDATE "+Database.TABLE_TVDB_SERIE +" SET rating=?, generi=?, network=?, inizio=?, giorno_settimana=?,"+
				"ora_trasmissione=?, durata_episodi=?,stato=?, descrizione=?, descrizione_lang=?,"+
				"banner=?, ultimo_aggiornamento=? WHERE id=?";
		Database.updateQuery(query, serie.getRating(), serie.getGeneriString(), serie.getNetwork(), serie.getDataInizio(), serie.getGiornoSettimana(), serie.getOraTrasmissione(), serie.getDurataEpisodi(),
				serie.getStatoSerie(), serie.getDescrizione(), serie.getLang(), serie.getUrlBanner(), (System.currentTimeMillis()/1000), serie.getId());
	}
	public EpisodioTVDB getEpisodio(int idSerie, int st, int ep){
		EpisodioTVDB eDB = caricaEpisodio(idSerie, st, ep);
		if(eDB!=null){
			if(eDB.getUltimoAggiornamento()+PERIODO_AGGIORNAMENTO_SERIE > System.currentTimeMillis()/1000)
				return eDB;
		}
		Mirror xmlMirror = getXMLMirror();
		if (xmlMirror == null)
			return null;
		
		String apiCall=API_GET_INFO_EPISODIO.replace("<mirrorpath>", xmlMirror.getUrl())
				.replace("<idserie>", idSerie+"")
				.replace("<season>", ""+st)
				.replace("<episode>", ""+ep)
				.replace("<language>", defaultLang);
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser;
		try {
			domparser = dbfactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		Document doc;
		HttpURLConnection conn = null;
		try {
			conn = getConnection(apiCall);
			doc = domparser.parse(conn.getInputStream());
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
		NodeList elementi = doc.getElementsByTagName("Episode");
		if (elementi.getLength() != 1)
			return null;

		Node episodio = elementi.item(0);
		NodeList attributi = episodio.getChildNodes();
		EpisodioTVDB episodioTVDB = new EpisodioTVDB(idSerie, st, ep);
		episodioTVDB.setLang(defaultLang);
		for (int j = 0; j < attributi.getLength(); j++) {
			Node attributo = attributi.item(j);
			if (attributo instanceof Element) {
				Element attr = (Element) attributo;
				switch (attr.getTagName()) {
					case "id":
						episodioTVDB.setIdEpisodio(Integer.parseInt(attr.getTextContent()));
						break;
					case "EpisodeName":
						episodioTVDB.setTitolo(attr.getTextContent());
						break;
					case "FirstAired":
						episodioTVDB.setDataAir(attr.getTextContent());
						break;
					case "GuestStars":
						episodioTVDB.setGuestStars(attr.getTextContent());
						break;
					case "Director":
						episodioTVDB.setRegista(attr.getTextContent());
						break;
					case "Writer":
						episodioTVDB.setSceneggiatori(attr.getTextContent());
						break;
					case "Overview":
						episodioTVDB.setDescrizione(attr.getTextContent());
						break;
					case "filename":
						episodioTVDB.setImageURL(getBannerURL(attr.getTextContent()));
						break;
					case "Rating":
						try {
							episodioTVDB.setRating(Float.parseFloat(attr.getTextContent()));
						}
						catch(Exception e){
							episodioTVDB.setRating(0f);
						}
						break;
					case "IMDB_ID":
						break;
				}
			}
		}
		if(eDB!=null)
			aggiornaEpisodio(episodioTVDB);
		else
			salvaEpisodio(episodioTVDB);
		return episodioTVDB;
	}
	private void salvaEpisodio(EpisodioTVDB e){
		String query = "INSERT INTO "+Database.TABLE_TVDB_EPISODI+ " (id,idSerie,stagione,episodio,titolo,immagine,descrizione,guestStars,data_air,regista,sceneggiatori,lang,rating,ultimoAggiornamento) "+
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Database.updateQuery(query, e.getIdEpisodio(),e.getIdSerie(),e.getStagione(),e.getEpisodio(),e.getTitolo(),e.getImageURL(),e.getDescrizione(),e.getGuestStarsS(),e.getDataAir(),e.getRegistaS(),e.getSceneggiatoriS(),e.getLang(),e.getRating(),(System.currentTimeMillis()/1000));
	}
	@SuppressWarnings("unused")
	private EpisodioTVDB caricaEpisodio(int idEpisodio){
		String query = "SELECT * FROM "+Database.TABLE_TVDB_EPISODI+" WHERE id="+idEpisodio;
		ArrayList<KVResult<String, Object>> r = Database.selectQuery(query);
		if(r==null || r.size()!=1)
			return null;
		return parseEpisodio(r.get(0));
	}
	private EpisodioTVDB caricaEpisodio(int idSerie, int s, int e){
		String query = "SELECT * FROM "+Database.TABLE_TVDB_EPISODI+" WHERE idSerie="+idSerie+" AND stagione="+s+" AND episodio="+e;
		ArrayList<KVResult<String, Object>> r = Database.selectQuery(query);
		if(r==null || r.size()!=1)
			return null;
		return parseEpisodio(r.get(0));
	}
	private EpisodioTVDB parseEpisodio(KVResult<String, Object> r){
		int idEp = (int) r.getValueByKey("id");
		int idSerie = (int) r.getValueByKey("idSerie");
		int stagione = (int) r.getValueByKey("stagione");
		int episodio = (int) r.getValueByKey("episodio");
		String titolo = (String) r.getValueByKey("titolo");
		String immagine = (String) r.getValueByKey("immagine");
		String descrizione = (String) r.getValueByKey("descrizione");
		String guestStars = (String) r.getValueByKey("guestStars");
		String data_air = (String) r.getValueByKey("data_air");
		String regista = (String) r.getValueByKey("regista");
		String sceneggiatori = (String) r.getValueByKey("sceneggiatori");
		String lang = (String) r.getValueByKey("lang");
		float rating = Float.parseFloat(r.getValueByKey("rating")+"");
		int ultimoAggiornamento = (int) r.getValueByKey("ultimoAggiornamento");
		EpisodioTVDB e = new EpisodioTVDB(idSerie, stagione, episodio);
		e.setIdEpisodio(idEp);
		e.setTitolo(titolo);
		e.setImageURL(immagine);
		e.setDescrizione(descrizione);
		e.setGuestStars(guestStars);
		e.setDataAir(data_air);
		e.setRegista(regista);
		e.setSceneggiatori(sceneggiatori);
		e.setLang(lang);
		e.setRating(rating);
		e.setUltimoAggiornamento(ultimoAggiornamento);
		return e;
	}
	private void aggiornaEpisodio(EpisodioTVDB e){
		String query = "UPDATE "+Database.TABLE_TVDB_EPISODI+" SET titolo=?, descrizione=?, immagine=?,guestStars=?,data_air=?,regista=?,sceneggiatori=?,lang=?,rating=?,ultimoAggiornamento=? WHERE id=?";
		Database.updateQuery(query, e.getTitolo(), e.getDescrizione(), e.getImageURL(), e.getGuestStarsS(), e.getDataAir(), e.getRegistaS(), e.getSceneggiatoriS(), e.getLang(), e.getRating(), (System.currentTimeMillis()/1000), e.getIdEpisodio());
	}
	private HttpURLConnection getConnection(String url) throws IOException {
		try {
			URL uri = new URL(url.replace(" ", "%20"));
			HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
			connection.setRequestProperty("User-Agent", UserAgent.get());
			connection.setReadTimeout(10000);
			return connection;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
