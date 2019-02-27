package gst.infoManager.thetvdb;

import java.util.ArrayList;

import org.jdom.Element;

import gst.serieTV.XMLSerializable;

public class SerieTVDBFull extends SerieTVDB implements XMLSerializable {
	private String giorno_settimana;
	private String ora;
	private int durataEpisodi;
	private String stato_serie;
	private ArrayList<String> poster;
	private ArrayList<String> banners;
	private ArrayList<ActorTVDB> attori;
	private String attoriString;
	private Integer ultimoAggiornamento;
	
	public SerieTVDBFull(int id, String nomeserie, String descrizione, String banner, String inizio, String lang) {
		super(id, nomeserie, descrizione, banner, inizio, lang);
		attori = new ArrayList<ActorTVDB>();
		poster = new ArrayList<String>();
		banners = new ArrayList<String>();
	}
	public SerieTVDBFull(SerieTVDB serie){
		super(serie.getId(), serie.getNomeSerie(), serie.getDescrizione(), serie.getUrlBanner(), serie.getDataInizio(), serie.getLang());
	}

	public String getGiornoSettimana() {
		return giorno_settimana;
	}

	public void setGiornoSettimana(String giorno_settimana) {
		this.giorno_settimana = giorno_settimana;
	}

	public String getOraTrasmissione() {
		return ora;
	}

	public void setOraTrasmissione(String ora) {
		this.ora = ora;
	}

	public int getDurataEpisodi() {
		return durataEpisodi;
	}

	public void setDurataEpisodi(int durataEpisodi) {
		this.durataEpisodi = durataEpisodi;
	}

	public String getStatoSerie() {
		return stato_serie;
	}

	public void setStatoSerie(String stato_serie) {
		this.stato_serie = stato_serie;
	}

	public ArrayList<String> getPoster() {
		return poster;
	}

	public void aggiungiPoster(String poster) {
		this.poster.add(poster);
	}
	public ArrayList<ActorTVDB> getAttori() {
		return attori;
	}
	public void aggiungiAttore(ActorTVDB a) {
		attori.add(a);
	}
	public ArrayList<ActorTVDB> getAttoriString() {
		if(attoriString!=null && attoriString.length()>0){
			String[] attori = attoriString.replace("|", "_").split("_");
			ArrayList<ActorTVDB> attoriSplit = new ArrayList<ActorTVDB>();
			for(int i=0;i<attori.length;i++){
				if(attori[i].trim().length()>0){
					ActorTVDB act = new ActorTVDB(attori[i].trim());
					attoriSplit.add(act);
				}
			}
			return attoriSplit;
		}
		return null;
	}
	public ArrayList<String> getBanners(){
		return banners;
	}
	public void aggiungiBanner(String b){
		banners.add(b);
	}
	public void setAttoriString(String attoriString) {
		this.attoriString = attoriString;
	}
	public Integer getUltimoAggiornamento() {
		return ultimoAggiornamento;
	}
	public void setUltimoAggiornamento(Integer ultimoAggiornamento) {
		this.ultimoAggiornamento = ultimoAggiornamento;
	}
	@Override
	public Element getXml()
	{
		Element root = new Element("tvdb_show");
		Element id_serie = new Element("id_serie");
		id_serie.addContent(getId()+"");
		root.addContent(id_serie);
		Element nome_serie = new Element("nome_serie");
		nome_serie.addContent(getNomeSerie());
		root.addContent(nome_serie);
		Element first_air = new Element("first_air");
		first_air.addContent(getDataInizioITA());
		root.addContent(first_air);
		Element rating = new Element("rating");
		rating.addContent(getRating()+"");
		root.addContent(rating);
		Element network = new Element("network");
		network.addContent(getNetwork());
		root.addContent(network);
		Element air_day = new Element("air_day");
		air_day.addContent(getGiornoSettimana());
		root.addContent(air_day);
		Element air_hour = new Element("air_hour");
		air_hour.addContent(getOraTrasmissione());
		root.addContent(air_hour);
		Element durata = new Element("durata_episodi");
		durata.addContent(getDurataEpisodi()+"");
		root.addContent(durata);
		Element stato_serie = new Element("stato_serie");
		stato_serie.addContent(getStatoSerie());
		root.addContent(stato_serie);
		Element banner_url=new Element("banner_url");
		banner_url.addContent(getUrlBanner());
		root.addContent(banner_url);
		Element descrizione = new Element("descrizione");
		descrizione.addContent(getDescrizione());
		root.addContent(descrizione);
	
		Element generi = new Element("generi");
		for(String genere: getGeneri()){
			Element g = new Element("genere");
			g.addContent(genere);
			generi.addContent(g);
		}
		root.addContent(generi);
		Element attori = new Element("attori");
		ArrayList<ActorTVDB> elenco_attori = getAttori().size()>0 ? getAttori(): getAttoriString();
		if(elenco_attori != null){
    		for(ActorTVDB a:elenco_attori){
    			Element attore = new Element("attore");
    			Element nome = new Element("nome_attore");
    			nome.addContent(a.getNome());
    			Element ruolo = new Element("ruolo_attore");
    			ruolo.addContent(a.getRuolo());
    			Element img_attore=new Element("img_attore");
    			img_attore.addContent(a.getUrlImage());
    			attore.addContent(nome);
    			attore.addContent(ruolo);
    			attore.addContent(img_attore);
    			attori.addContent(attore);
    		}
		}
		root.addContent(attori);
		Element poster_url = new Element("posters");
		for(String p: getPoster()){
			Element poster = new Element("poster");
			poster.addContent(p);
			poster_url.addContent(poster);
		}
		root.addContent(poster_url);
		Element banners = new Element("banners");
		for(String b : getBanners()){
			Element banner = new Element("banner");
			banner.addContent(b);
			banners.addContent(banner);
		}
		root.addContent(banners);
		return root;
	}
}
