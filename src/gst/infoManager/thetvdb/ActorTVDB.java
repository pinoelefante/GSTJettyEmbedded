package gst.infoManager.thetvdb;

public class ActorTVDB {
	private String nome, url_file, ruolo;
	
	public ActorTVDB(String nome, String ruolo){
		this.setNome(nome);
		this.setRuolo(ruolo);
	}
	public ActorTVDB(String nome){
		setNome(nome);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUrlImage() {
		return url_file;
	}

	public void setUrlImage(String url_file) {
		this.url_file = url_file;
	}
	public void setRuolo(String ruolo){
		this.ruolo=ruolo;
	}
	public String getRuolo(){
		return ruolo;
	}
}
