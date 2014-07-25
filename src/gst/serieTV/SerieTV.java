package gst.serieTV;

import gst.database.Database;
import gst.tda.db.KVResult;
import gst.tda.serietv.ElencoEpisodi;
import gst.tda.serietv.Episodio;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerieTV {
	private String titolo;
	private ProviderSerieTV provider;
	private String url_serie;
	private int id_db, id_itasa=0, id_tvdb=0, id_subspedia=0, id_subsfactory=0;
	private boolean conclusa, stop_search, inserita;
	private Preferenze preferenze_download;
	
	public SerieTV(ProviderSerieTV provider, String nomeserie, String url) {
		this.provider=provider;
		titolo=formattaNome(nomeserie);
		url_serie=url;
	}

	public String getNomeSerie(){
		return titolo;
	}
	// Stato conclusa (ended su eztv)
	public void setConclusa(boolean stato) {
		if(conclusa!=stato){
			conclusa=stato;
		}
	}
	public void setPreferenze(Preferenze p){
		preferenze_download=p;
	}
	public Preferenze getPreferenze(){
		if(preferenze_download==null)
			preferenze_download=new Preferenze();
		return preferenze_download;
	}
	public boolean isConclusa(){
		return conclusa;
	}
	public String toString(){
		return getNomeSerie() + " ("+provider.getProviderName()+")";
	}
	public String getFolderSerie() {
		return getNomeSerie().replace(":", "-").replace("?", "").replace("/", "-").replace("\\", "-").replace("*", "").replace("<", "").replace(">", "").replace("|", "").replace("\"", "").replace(".", "");
	}
	public int getIDDb(){
		return id_db;
	}
	public void setIDDb(int i){
		id_db=i;
	}
	public boolean isInserita(){
		return inserita;
	}
	public void setInserita(boolean s){
		inserita=s;
	}
	public void aggiornaDB(){
		provider.salvaSerieInDB(this);
	}
	public boolean isStopSearch(){
		if(getNumEpisodi()==0){
			stop_search=false;
		}
		return stop_search;
	}
	public void setStopSearch(boolean s, boolean updateDB){
		stop_search=s;
		if(updateDB){
			aggiornaDB();
		}
	}
	public String getUrl(){
		return url_serie;
	}

	public int getIDItasa() {
		return id_itasa;
	}

	public void setIDItasa(int id_itasa) {
		this.id_itasa = id_itasa;
	}

	public int getIDDBSubsfactory() {
		return id_subsfactory;
	}

	public void setIDSubsfactory(int id_subsfactory, boolean updatedb) {
		this.id_subsfactory = id_subsfactory;
		if(id_subsfactory>0){
			String query="SELECT directory FROM "+Database.TABLE_SUBSFACTORY+" WHERE id="+id_subsfactory;
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				String directory=(String) res.get(0).getValueByKey("directory");
				setSubsfactoryDirectory(directory);
			}
		}
		if(updatedb)
			aggiornaDB();
	}

	public int getIDTvdb() {
		return id_tvdb;
	}

	public void setIDTvdb(int id_tvdb) {
		this.id_tvdb = id_tvdb;
	}

	public int getIDSubspedia() {
		return id_subspedia;
	}

	public void setIDSubspedia(int id_subspedia) {
		this.id_subspedia = id_subspedia;
	}
	public int compareTo(SerieTV s2){
		return getUrl().compareToIgnoreCase(s2.getUrl());
		
		//return this.getNomeSerie().trim().toLowerCase().compareTo(s2.getNomeSerie().trim().toLowerCase());
	}
	
	public int getProviderID(){
		return provider.getProviderID();
	}
	public ProviderSerieTV getProvider(){
		return provider;
	}
	public static String formattaNome(String nome){
		String formattato=nome;
		if(formattato.contains(", The")){
			formattato="The "+formattato.replace(", The", "").trim();
		}
		String pattern_anno="\\([0-9]{4}\\)";
		Pattern p_anno=Pattern.compile(pattern_anno);
		Matcher m = p_anno.matcher(formattato);
		if(m.find()){
			formattato=formattato.replace(m.group(), "");
		}
		formattato=formattato.trim();
		return formattato;
	}
	public static String removeNationality(String nome){
		String formattato=nome;
		Pattern pattern=Pattern.compile("\\W[aA-zZ]{2}\\W$");
		Matcher matcher=pattern.matcher(formattato);
		if(matcher.find()){
			formattato=formattato.replace(matcher.group(), "").trim();
		}
		return formattato;
	}
	public static void main(String[] args){
		String nomeserie="Dracula (US)";
		String nomeserie2="(US) Dracula";
		
		System.out.println("Prima stringa: "+nomeserie+"\nFormattata: "+removeNationality(nomeserie));
		System.out.println("Seconda stringa: "+nomeserie2+"\nFormattata: "+removeNationality(nomeserie2));
	}
	public void addEpisodio(Torrent episodio){
		if(episodio.is720p() && episodio.getScaricato()==Torrent.SCARICARE && !getPreferenze().isPreferisciHD())
			episodio.setScaricato(Torrent.IGNORATO, false);
		if(episodio.isPreAir() && episodio.getScaricato()==Torrent.SCARICARE && !getPreferenze().isDownloadPreair())
			episodio.setScaricato(Torrent.IGNORATO, false);
		episodi.aggiungiLink(episodio);
		if(episodio.isSottotitolo())
			GestioneSerieTV.getSubManager().aggiungiEpisodio(episodio);
	}
	public void addEpisodioDB(Torrent episodio){
		//System.out.println(getNomeSerie()+" "+episodio.getStagione()+"x"+episodio.getEpisodio());
		if(episodio.is720p() && episodio.getScaricato()==Torrent.SCARICARE && !getPreferenze().isPreferisciHD())
			episodio.setScaricato(Torrent.IGNORATO, false);
		if(episodio.isPreAir() && episodio.getScaricato()==Torrent.SCARICARE && !getPreferenze().isDownloadPreair())
			episodio.setScaricato(Torrent.IGNORATO, false);
		episodi.aggiungiLinkDB(episodio);
		if(episodio.isSottotitolo())
			GestioneSerieTV.getSubManager().aggiungiEpisodio(episodio);
	}
	public void aggiornaEpisodiOnline(){
		provider.caricaEpisodiOnline(this);
	}
	public int getNumEpisodi(){
		return episodi.size();
	}
	public Episodio getEpisodio(int i){ // index in elencoepisodi
		return episodi.get(i);
	}
	private String SubsfactoryOnlineDirectory="";
	
	public String getSubsfactoryDirectory(){
		if(SubsfactoryOnlineDirectory.isEmpty()){
			if(getIDDBSubsfactory()>0){
				String query="SELECT directory FROM "+Database.TABLE_SUBSFACTORY+" WHERE id="+getIDDBSubsfactory();
				ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
				if(res.size()==1){
					SubsfactoryOnlineDirectory=(String) res.get(0).getValueByKey("directory");
				}
			}
		}
		return SubsfactoryOnlineDirectory;
	}

	public void setSubsfactoryDirectory(String id) {
		SubsfactoryOnlineDirectory=id;
	}
	public boolean rimuoviSerie(){
		setStopSearch(false, false);
		return getProvider().rimuoviSerieDaDB(this);
	}
}
