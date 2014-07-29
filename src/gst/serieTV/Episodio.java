package gst.serieTV;

import java.util.ArrayList;

public class Episodio {
	public final static int SCARICARE=0, SCARICATO=1, VISTO=2, RIMOSSO=3, IGNORATO=4;
	private int episodio, stagione, id, serie, stato, idTVDB;
	private boolean sub_down;
	 
	private ArrayList<Torrent> links;
	
	public Episodio(int stagione, int episodio){
		this.stagione=stagione;
		this.episodio=episodio;
		links = new ArrayList<Torrent>();
	}
	public int getStagione(){
		return stagione;
	}
	public int getEpisodio(){
		return episodio;
	}
	public ArrayList<Torrent> getLinks(){
		return links;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSerie() {
		return serie;
	}
	public void setSerie(int serie) {
		this.serie = serie;
	}
	public int getStatoVisualizzazione() {
		return stato;
	}
	public void setStatoVisualizzazione(int stato) {
		this.stato = stato;
	}
	public int getIdTvDB() {
		return idTVDB;
	}
	public void setIdTvDB(int idTVDB) {
		this.idTVDB = idTVDB;
	}
	public boolean isSubDownload() {
		return sub_down;
	}
	public void setSubDownload(boolean sub_down) {
		this.sub_down = sub_down;
	}
	public void aggiungiLink(Torrent t){
		boolean inserito = false;
		for(int i=0;i<links.size() && !inserito;i++){
			if(t.getStats().value()>links.get(i).getStats().value()){
				links.add(i, t);
				inserito = true;
			}
		}
		if(!inserito)
			links.add(t);
	}
}
