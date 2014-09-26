package gst.programma;

class Dipendenza{
	private String url, nome, hash;

	public Dipendenza(String url,String nome, String h) {
    	setUrl(url);
    	setNome(nome);
    	setHash(h);
    }
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}