package gst.infoManager.thetvdb;

import gst.programma.ManagerException;
import gst.programma.Settings;
import gst.serieTV.SerieTV;

import java.io.IOException;
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
	private String						   defaultLang			= "en";
	private final String FANART="fanart", POSTER="poster";

	public ArrayList<Entry<String, String>> lingueDisponibili;

	public static TheTVDB getInstance() {
		if (instance == null)
			instance = new TheTVDB();
		return instance;
	}

	private TheTVDB() {
		defaultLang = Settings.getInstance().getTVDBPreferredLang();
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
		try {
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(API_MIRROR_PATH);

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

		try {
			Mirror mirror = getXMLMirror();
			if (mirror == null)
				return null;

			String API_PATH = API_CERCA_SERIE_LINGUA.replace("<seriesname>", serietv.getNomeSerie()).replace("<mirrorpath>", mirror.getUrl()).replace("<language>", defaultLang);

			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(API_PATH);

			NodeList elementi = doc.getElementsByTagName("Series");
			if (elementi.getLength() == 0) {
				API_PATH = API_CERCA_SERIE_LINGUA.replace("<seriesname>", SerieTV.removeNationality(serietv.getNomeSerie())).replace("<mirrorpath>", mirror.getUrl()).replace("<language>", defaultLang);
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
				SerieTVDB newSerie = new SerieTVDB(id, nome_serie, descrizione, getBannerURL(banner_path), data_inizio, lang);
				addSerieToList(serie_trovate, newSerie);
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
		filtraRisultati(serie_trovate, serietv.getNomeSerie());
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

	public SerieTVDBFull getSerie(int idSerie) {
		Mirror xmlMirror = getXMLMirror();
		if (xmlMirror == null)
			return null;
		String apiCall = API_GET_SERIE_INFO.replace("<mirrorpath>", xmlMirror.getUrl()).replace("<idserie>", idSerie + "").replace("<language>", defaultLang);

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
			doc = domparser.parse(apiCall);
		}
		catch (SAXException | IOException e) {
			e.printStackTrace();
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
					case "language":
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
		getImmagini(newSerie);
		return newSerie;
	}
	
	public void getAttori(SerieTVDBFull serie){
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
		try {
			doc = domparser.parse(apiCall);
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
	}

	public String getBannerURL(String path) {
		Mirror mirror = getBannerMirror();
		if (mirror != null) {
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
		try {
			doc = domparser.parse(apiCall);
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
}
