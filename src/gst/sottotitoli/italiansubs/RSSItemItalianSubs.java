package gst.sottotitoli.italiansubs;

class RSSItemItalianSubs{
	/**
	 * 
	 */
	private final ItalianSubs italianSubs;
	private String url;
	private String nomeserie;
	private int stagione, episodio;
	private boolean hd720p;
	private boolean preair;
	private boolean normale=true;
	private int idserie;
	private int idsub;
	
	public RSSItemItalianSubs(ItalianSubs italianSubs, String itemname, String url){
		this.italianSubs = italianSubs;
		this.setUrl(url);
		parse(itemname);
	}
	private void parse(String item){
		String nome = item;
		if (nome.contains("720p")) {
			nome = nome.replace("720p", "").trim();
			setNormale(false);
			set720p(true);
		}
		else if (nome.contains("Bluray")) {
			nome = nome.replace("Bluray", "").trim();
			setNormale(false);
		}
		else if (nome.contains("DVDRip")) {
			nome = nome.replace("DVDRip", "").trim();
			setNormale(false);
		}
		else if (nome.contains("BDRip")) {
			nome = nome.replace("BDRip", "").trim();
			setNormale(false);
		}
		else if (nome.contains("WEB-DL")) {
			nome = nome.replace("WEB-DL", "").trim();
			setNormale(false);
		}
		
		if (nome.contains("Preair"))
			nome.replace("Preair", "");
		if(nome.contains(" ")){
			String str_index = nome.substring(nome.lastIndexOf(" ")).trim();
			try {
				if (!str_index.contains("x")) {
					setEpisodio(Integer.parseInt(str_index));
				}
				else {
					setStagione(Integer.parseInt(str_index.substring(0, str_index.indexOf("x"))));
					setEpisodio(Integer.parseInt(str_index.substring(str_index.indexOf("x") + 1)));
				}
			}
			catch (NumberFormatException e) {
				//ManagerException.registraEccezione(e);
				return;
			}
			setNomeSerie(nome.substring(0, nome.indexOf(str_index)).trim().replace("&amp;", "&"));
			setIDSerie(this.italianSubs.cercaSerie(getNomeSerie()));
			setIDSub(Integer.parseInt(getUrl().substring(getUrl().indexOf("&id=")+"&id=".length())));
		}
		else {
			setNomeSerie(nome);
		}
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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