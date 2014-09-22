package gst.infoManager.thetvdb;

import java.util.ArrayList;

public class SerieTVDBFull extends SerieTVDB {
	private String giorno_settimana;
	private String ora;
	private int durataEpisodi;
	private String stato_serie;
	private ArrayList<String> poster;
	private ArrayList<String> banners;
	private ArrayList<ActorTVDB> attori;
	private String attoriString;
	
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
}
