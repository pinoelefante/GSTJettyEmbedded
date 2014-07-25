package gst.infoManager;

public class ActorTVDB {
	private String nome, url_file, ruolo;
	private int id;
	
	public ActorTVDB(int id, String nome, String url){
		this.setId(id);
		this.setNome(nome);
		this.setUrlFile(url);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUrlFile() {
		return url_file;
	}

	public void setUrlFile(String url_file) {
		this.url_file = url_file;
	}
	public void setRuolo(String ruolo){
		this.ruolo=ruolo;
	}
	public String getRuolo(){
		return ruolo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
