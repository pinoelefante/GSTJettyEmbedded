package gst.sottotitoli.rss;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;

import java.net.MalformedURLException;
import java.net.URL;

public class RSSItemSubsfactory {
	private String titolo, descrizione, url, url_download;
	private String ID="";
	private int stagione=0, episodio=0;
	private boolean HD720p=false, Normale=true;
	public RSSItemSubsfactory(String t, String d, String u){
		setTitolo(t);
		setDescrizione(d);
		setUrl(u.replace("%20", " ").replace("%2F", "/").replace("action=view", "action=downloadfile"));
		parse();
	}
	private void parse(){
		try {
			URL url = new URL(getUrl());
			String[] par = url.getQuery().split("&");
			for(int i=0;i<par.length;i++){
				String[] kv = par[i].split("=");
				switch(kv[0]){
					case "directory":
						setID(kv[1]);
						break;
					case "filename": {
						CaratteristicheFile stat = Naming.parse(kv[1], null);
						setStagione(stat.getStagione());
						setEpisodio(stat.getEpisodio());
						set720p(stat.is720p());
						setNormale(!stat.is720p());
						break;
					}
				}
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if(descrizione.contains("normale") || descrizione.contains("Normale"))
			setNormale(true);
		if(descrizione.contains("720p"))
			set720p(true);
	}
	public boolean isValid(){
		return (getStagione()!=0 && getEpisodio()!=0);
	}
	public String getTitolo() {
		return titolo;
	}
	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}
	public void setUrlDownload(String url){
		url_download=url;
	}
	public String getUrlDownload(){
		return url_download;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public int getStagione() {
		return stagione;
	}
	public void setStagione(int stagione) {
		this.stagione = stagione;
	}
	public int getEpisodio() {
		return episodio;
	}
	public void setEpisodio(int episodio) {
		this.episodio = episodio;
	}
	public boolean is720p() {
		return HD720p;
	}
	public void set720p(boolean hD720p) {
		HD720p = hD720p;
	}
	public boolean isNormale() {
		return Normale;
	}
	public void setNormale(boolean normale) {
		Normale = normale;
	}
	public String toString(){
		return getStagione()+" "+getEpisodio()+" "+isNormale()+" "+is720p();
	}
}