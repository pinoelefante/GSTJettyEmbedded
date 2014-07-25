package gst.sottotitoli;

class RSSItemSubsfactory {
	private String titolo, descrizione, url, url_download;
	private String ID="";
	private int stagione=0, episodio=0;
	private boolean HD720p=false, Normale=true;
	public RSSItemSubsfactory(String t, String d, String u){
		setTitolo(t);
		setDescrizione(d);
		setUrl(u);
		parse();
	}
	private void parse(){
		String id=getUrl().substring(getUrl().indexOf("directory=")+"directory=".length(), getUrl().indexOf("&filename")).replace("%2F", "/").replace("%20", " ");
		String url_d=getUrl().replace("action=view", "action=downloadfile");
		String filename=getUrl().substring(getUrl().indexOf("filename")+"filename=".length());
		setID(id);
		setUrlDownload(url_d);
		String[] r=filename.split("[sS0-9]{3}[eE][0-9]{2}");
		if(r.length==2){
			String analyze=filename.substring(r[0].length(), filename.indexOf(r[1]));
			String[] res=analyze.replace("s", "").replace("S", "").replace("e", " ").replace("E", " ").split(" ");
			setStagione(Integer.parseInt(res[0]));
			setEpisodio(Integer.parseInt(res[1]));
		}
		else
			return;
		if(descrizione.contains("normale") || descrizione.contains("Normale"))
			setNormale(true);
		if(descrizione.contains("720p"))
			set720p(true);
		if(descrizione.toLowerCase().contains("WEB-DL".toLowerCase()))
			setNormale(false);
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