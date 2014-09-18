package gst.sottotitoli.rss;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.sottotitoli.italiansubs.ItalianSubs;

import java.net.MalformedURLException;
import java.net.URL;

public class RSSItemItalianSubs{
	private String nomeserie;
	private int stagione, episodio;
	private boolean hd720p;
	private boolean preair;
	private boolean normale=true;
	private int idserie;
	private int idsub;
	private ItalianSubs itasa = ItalianSubs.getInstance();
	
	public RSSItemItalianSubs(String title, String guid){
		parse(title);
		parseLinks(guid);
	}
	private void parseLinks(String guid){
		try {
			URL orig = new URL(guid);
			if(orig.getQuery()==null)
				return;
			String[] params = orig.getQuery().split("&");
			for(int i=0;i<params.length;i++){
				if(params[i].toLowerCase().startsWith("id")){
					setIDSub(Integer.parseInt(params[i].split("=")[1]));
				}
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	private void parse(String item){
		CaratteristicheFile stat = Naming.parse(item, null);
		stagione=stat.getStagione();
		episodio=stat.getEpisodio();
		hd720p=stat.is720p();
		normale=!stat.is720p();
		
		String nome = item.replace("720p", "")
				.replace("Bluray", "")
				.replace("DVDRip", "")
				.replace("BDRip", "")
				.replace("WEB-DL", "")
				.replace("Preair", "").trim();
		if(nome.contains(" ")){
			nome=nome.substring(0, nome.lastIndexOf(" ")).trim();
		}
		setNomeSerie(nome);
		setIDSerie(itasa.cercaSerie(nome));
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