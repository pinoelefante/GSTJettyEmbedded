package gst.info.thetvdb;

import java.util.ArrayList;

public class SerieTVDB {
	private int id;
	private String nome, url_banner, descrizione;
	private int i_anno, i_mese, i_giorno; 
	private ArrayList<String> generi;
	private float rating;
	private String network;
	private String lang;
	
	public SerieTVDB(int id, String nomeserie, String descrizione, String banner, String inizio, String lang){
		this.setId(id);
		this.setNomeSerie(nomeserie);
		this.setDescrizione(descrizione);
		this.setUrlBanner(banner);
		this.setDataInizio(inizio);
		this.setLang(lang);
	}
	
	private void setDataInizio(String inizio) {
		if(inizio.length()>0){
    		String[] comp=inizio.split("-");
    		i_anno=Integer.parseInt(comp[0]);
    		i_mese=Integer.parseInt(comp[1]);
    		i_giorno=Integer.parseInt(comp[2]);
		}
	}
	public int getAnnoInizio(){
		return i_anno;
	}
	public String getDataInizioITA(){
		return i_giorno+"-"+i_mese+"-"+i_anno;
	}
	public String getDataInizio(){
		return i_anno+"-"+i_mese+"-"+i_giorno;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNomeSerie() {
		return nome;
	}

	public void setNomeSerie(String nome) {
		this.nome = nome;
	}

	public String getUrlBanner() {
		return url_banner;
	}

	public void setUrlBanner(String url_banner) {
		this.url_banner = url_banner;;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public String toString(){
		return "ID: "+getId()+"\nTitolo: "+getNomeSerie()+"\nData inizio: "+getDataInizio()+"\nBanner: "+getUrlBanner()+"\nDescrizione: "+getDescrizione();
	}
	public void setGeneri(String textContent) {
		String[] generi=textContent.replace("|", " ").split(" ");
		if(this.generi==null)
			this.generi=new ArrayList<String>(2);
		for(int i=0;i<generi.length;i++){
			if(generi[i].length()>0)
				this.generi.add(generi[i]);
		}
		
	}
	public void setRating(String textContent) {
		textContent.replace(",", ".");
		try {
			rating=Float.parseFloat(textContent);
		}
		catch(NumberFormatException e){
			rating=0.0f;
		}
	}
	public void setNetwork(String textContent) {
		network=textContent;
	}
	public String getNetwork(){
		return network;
	}
	public float getRating(){
		return rating;
	}
	public ArrayList<String> getGeneri(){
		return generi;
	}
	public String getGeneriString(){
		String g = "";
		for(int i=0;i<generi.size();i++){
			g+=generi.get(i);
			if(i<generi.size()-1)
				g+="|";
		}
		return g;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
}
