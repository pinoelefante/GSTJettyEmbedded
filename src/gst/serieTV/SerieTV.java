package gst.serieTV;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerieTV {
	private String titolo;
	private int provider;
	private String url_serie;
	private int id_db, id_itasa=0, id_tvdb=0, id_subspedia=0, id_subsfactory=0,id_addic7ed=0, id_karmorra = 0, id_showrss_new = 0;
	private boolean conclusa, stop_search, no_select;
	private Preferenze preferenze_download;
	private PreferenzeSottotitoli pref_subs;
	
	public SerieTV(int provider, String nomeserie, String url) {
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
		return getNomeSerie() + " ("+ProviderSerieTV.getProviderNameByID(provider)+")";
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
	public boolean isStopSearch(){
		return stop_search;
	}
	public void setStopSearch(boolean s){
		stop_search=s;
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

	public void setIDSubsfactory(int id_subsfactory) {
		this.id_subsfactory = id_subsfactory;
		/*
		if(id_subsfactory>0){
			String query="SELECT directory FROM "+Database.TABLE_SUBSFACTORY+" WHERE id="+id_subsfactory;
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				String directory=(String) res.get(0).getValueByKey("directory");
				setSubsfactoryDirectory(directory);
			}
		}
		*/
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
	}
	
	public int getProviderID(){
		return provider;
	}
	public static String formattaNome(String nome){
		String formattato=nome;
		if(formattato.contains(", The")){
			formattato="The "+formattato.replace(", The", "").trim();
		}
		if(formattato.contains(", A")){
			formattato="A "+formattato.replace(", A", "").trim();
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
	private int id_openSubtitles;
	
	public void setIDOpenSubtitles(int id_opensub) {
		id_openSubtitles = id_opensub;
		
	}
	public int getIDOpenSubtitles(){
		return id_openSubtitles;
	}

	public void setProvider(int id_provider) {
		provider = id_provider;
	}
	public void setPreferenzeSottotitoli(PreferenzeSottotitoli f){
		pref_subs=f;
	}
	public PreferenzeSottotitoli getPreferenzeSottotitoli(){
		return pref_subs;
	}

	public void setEscludiSelezionaTutto(boolean escludiSelezionaTutto) {
		no_select=escludiSelezionaTutto;	
	}
	public boolean isEscludiSelezione(){
		return no_select;
	}
	public void setIDAddic7ed(int i){
		id_addic7ed = i;
	}
	public int getIDAddic7ed() {
		return id_addic7ed;
	}

	public int getIDKarmorra() {
		return id_karmorra;
	}

	public void setIDKarmorra(int id_karmorra) {
		this.id_karmorra = id_karmorra;
	}
	public int GetIdShowRss(){
		return id_showrss_new;
	}
	public void SetIdShowRss(int id){
		id_showrss_new = id;
	}
}
