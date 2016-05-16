package gst.sottotitoli;

public class SerieSub {
	private String nomeserie;
	private int ID;
	
	public SerieSub(String nome, int iddb) {
		setNomeSerie(nome);
		setIDDB(iddb);
	}
	public String getNomeSerie() {
		return nomeserie;
	}
	public void setNomeSerie(String nomeserie) {
		this.nomeserie = nomeserie;
	}
	public int getIDDB() {
		return ID;
	}
	public void setIDDB(int id) {
		ID = id;
	}
	public String toString(){
		return getNomeSerie();
	}
}
