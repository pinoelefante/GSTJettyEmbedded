package gst.sottotitoli.subsfactory;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;

class SottotitoloSubsfactory {
	protected String nomefile;
	private CaratteristicheFile stats;
	private String url_download;
	
	public SottotitoloSubsfactory(){}
	public SottotitoloSubsfactory(String nome) {
		nomefile=nome;
		stats = Naming.parse(nomefile, null);
	}
	public boolean is720p(){
		return stats.is720p();
	}
	public boolean isNormale(){
		return !is720p();
	}
	public int getStagione(){
		return stats.getStagione();
	}
	public int getEpisodio(){
		return stats.getEpisodio();
	}
	public String getNomeFile(){
		return nomefile;
	}
	public void setUrlDownload(String url){
		url_download=url;
	}
	public String getUrlDownload(){
		return url_download;
	}
	public String toString(){
		return getStagione()+"x"+getEpisodio()+" - "+getUrlDownload();
	}
}