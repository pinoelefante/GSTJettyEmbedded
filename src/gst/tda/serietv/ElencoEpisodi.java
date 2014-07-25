package gst.tda.serietv;

import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;

import java.util.ArrayList;

public class ElencoEpisodi {
	private ArrayList<Episodio> episodi;
	private SerieTV serie;
	
	public ElencoEpisodi(SerieTV s){
		serie=s;
		episodi=new ArrayList<Episodio>();
	}
	public void aggiungiLink(Torrent t){
		Episodio ep=cercaEpisodio(t.getStagione(), t.getEpisodio());
		if(ep==null){
			ep=aggiungiEpisodio(t.getStagione(), t.getEpisodio());
		}
		ep.addLink(t);
	}
	public void aggiungiLinkDB(Torrent t){
		Episodio ep=cercaEpisodio(t.getStagione(), t.getEpisodio());
		if(ep==null){
			ep=aggiungiEpisodio(t.getStagione(), t.getEpisodio());
		}
		ep.addLinkFromDB(t);
	}
	
	private Episodio aggiungiEpisodio(int stagione, int episodio){
		Episodio daInserire=new Episodio(serie, stagione, episodio);
		int i;
		if(size()==0){
			episodi.add(daInserire);
		}
		else {
    		boolean inserita=false;
    		for(i=0;i<episodi.size() &&!inserita;i++){
    			Episodio ep=episodi.get(i);
    			if(daInserire.getStagione()<ep.getStagione()){
    				episodi.add(i, daInserire);
    				inserita=true;
    			}
    			else if(daInserire.getStagione()==ep.getStagione()){
    				if(daInserire.getEpisodio()<ep.getEpisodio()){
    					episodi.add(i, daInserire);
        				inserita=true;
    				}
    			}
    		}
    		if(!inserita){
    			episodi.add(daInserire);
    			//System.out.println("Inserimento all'esterno del for");
    		}
		}
		return daInserire;
	}
	private Episodio cercaEpisodio(int stagione, int episodio){
		for(int i=0;i<episodi.size();i++){
			if(stagione==episodi.get(i).getStagione()){
				if(episodio==episodi.get(i).getEpisodio())
					return episodi.get(i);
			}
		}
		return null;
	}
	public void clean(){
		for(int i=0;i<episodi.size();i++)
			episodi.get(i).cleanAll();
		episodi.clear();
	}
	public int size(){
		return episodi.size();
	}
	public Episodio get(int i){
		if(i<0 || i>=size())
			return null;
		return episodi.get(i);
	}
	public void stampaElenco(){
		for(int i=0;i<episodi.size();i++){
			System.out.println(episodi.get(i));
		}
	}
	public void ottimizzaSpazio(){
		for(int i=0;i<episodi.size();i++){
			episodi.get(i).ottimizzaSpazio();
		}
	}
}
