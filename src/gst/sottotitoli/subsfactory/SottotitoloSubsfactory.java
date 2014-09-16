package gst.sottotitoli.subsfactory;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;

class SottotitoloSubsfactory {
	protected String nomefile, id_serie;
	protected int season, ep;
	private boolean normale=true, hd720p;
	private String url_download;
	
	public SottotitoloSubsfactory(){}
	public SottotitoloSubsfactory(String nome, String id) {
		nomefile=nome;
		this.id_serie=id;
		parseNome();
	}
	private void parseNome(){
		try{
			CaratteristicheFile stats=Naming.parse(nomefile, null);
			ep=stats.getEpisodio();
			season=stats.getStagione();
		}
		catch(Exception e){
			ep=0;
			season=0;
		}
	}
	public boolean isNormale(){
		return normale;
	}
	public boolean is720p(){
		return hd720p;
	}
	public void setNormale(boolean s){
		normale=s;
	}
	public void set720p(boolean s){
		hd720p=s;
	}
	public int getStagione(){
		return season;
	}
	public int getEpisodio(){
		return ep;
	}
	public String getNomeFile(){
		return nomefile;
	}
	public String getIDSerie(){
		return id_serie;
	}
	public void setUrlDownload(String url){
		url_download=url;
	}
	public String getUrlDownload(){
		return url_download;
	}
	public String toString(){
		return season+"x"+ep+" - "+getUrlDownload();
	}
}