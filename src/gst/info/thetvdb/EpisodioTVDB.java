package gst.info.thetvdb;

public class EpisodioTVDB {
	private int idSerie, stagione, episodio, idEpisodio;
	private String titolo, image, descrizione, guestStars,data_air, regista, sceneggiatori, lang;
	private float rating;
	private int ultimoAggiornamento;
	
	public EpisodioTVDB(int idSerie, int stagione, int episodio){
		setIdSerie(idSerie);
		setStagione(stagione);
		setEpisodio(episodio);
	}
	
	public void print(){
		System.out.println("IDSerie = "+idSerie);
		System.out.println("IDEpisodio = "+idEpisodio);
		System.out.println("Stagione = "+stagione);
		System.out.println("Episodio = "+episodio);
		System.out.println("Titolo = "+titolo);
		System.out.println("Immagine = "+image);
		System.out.println("Descrizione = "+descrizione);
		System.out.println("Guest Stars = "+guestStars);
		System.out.println("DataAir = "+data_air);
		System.out.println("Regista = "+regista);
		System.out.println("Sceneggiatori = "+sceneggiatori);
		System.out.println("Lingua = "+lang);
		System.out.println("Rating = "+rating);
	}
	
	public int getIdSerie() {
		return idSerie;
	}
	public void setIdSerie(int idSerie) {
		this.idSerie = idSerie;
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
	public int getIdEpisodio() {
		return idEpisodio;
	}
	public void setIdEpisodio(int idEpisodio) {
		this.idEpisodio = idEpisodio;
	}
	public String getTitolo() {
		return titolo;
	}
	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}
	public String getImageURL() {
		return image;
	}
	public void setImageURL(String image) {
		this.image = image;
	}
	public String getDescrizione() {
		if(descrizione==null)
			return "";
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public String[] getGuestStars() {
		if(guestStars==null){
			String[] ret={""};
			return ret;
		}
		return guestStars.split("\\|");
	}
	public String getGuestStarsS(){
		return guestStars==null?"":guestStars;
	}
	public String getGuestStarsFormatted(){
		String[] gss=getGuestStars();
		String gs = "";
		for(int i=0;i<gss.length;i++){
			if(gss[i].isEmpty())
				continue;
			gs+=gss[i];
			if(i<gss.length-1)
				gs+=", ";
		}
		return gs;
	}
	public void setGuestStars(String guestStars) {
		this.guestStars = guestStars;
	}
	public String getDataAir() {
		if(data_air==null)
			return "";
		return data_air;
	}
	public void setDataAir(String data_air) {
		if(data_air==null){
			data_air="";
			return;
		}
		if(data_air.contains("/"))
			data_air.replace("/", "-");
		this.data_air = data_air;
	}
	public void setDataAirFromItalian(String data_air){
		if(data_air==null){
			data_air="";
			return;
		}
		String[] nums = data_air.split("-");
		if(nums.length<3)
			return;
		data_air=nums[2]+"-"+nums[1]+"-"+nums[0];
	}
	public String getDataAirIta(){
		if(data_air==null)
			return "";
		String[] n=data_air.split("-");
		if(n.length<3)
			return data_air;
		return n[2]+"-"+n[1]+"-"+n[0];
	}
	public String[] getRegista() {
		if(regista==null){
			String[] ret={""};
			return ret;
		}
		return regista.split("\\|");
	}
	public String getRegistaS(){
		return regista==null?"":regista;
	}
	public String getRegistaFormatted(){
		String[] rs = getRegista();
		String r = "";
		for(int i=0;i<rs.length;i++){
			if(rs[i].isEmpty())
				continue;
			r+=rs[i];
			if(i<rs.length-1)
				r+=", ";
		}
		return r;
	}
	public void setRegista(String regista) {
		this.regista = regista;
	}
	public String[] getSceneggiatori() {
		if(sceneggiatori==null){
			String[] ret={""};
			return ret;
		}
		return sceneggiatori.split("\\|");
	}
	public String getSceneggiatoriFormatted(){
		String[] rs = getSceneggiatori();
		String r = "";
		for(int i=0;i<rs.length;i++){
			if(rs[i].isEmpty())
				continue;
			r+=rs[i];
			if(i<rs.length-1)
				r+=", ";
		}
		return r;
	}
	public String getSceneggiatoriS(){
		return sceneggiatori==null?"":sceneggiatori;
	}
	public void setSceneggiatori(String sceneggiatori) {
		this.sceneggiatori = sceneggiatori;
	}
	public float getRating() {
		return rating;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public int getUltimoAggiornamento() {
		return ultimoAggiornamento;
	}

	public void setUltimoAggiornamento(int ultimoAggiornamento) {
		this.ultimoAggiornamento = ultimoAggiornamento;
	}
}
