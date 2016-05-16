package gst.sottotitoli.converter;

public class SrtEntry {
	private String t_inizio, t_fine, testo;
	private int entry;
	
	public SrtEntry(){}

	public String getT_inizio() {
		return t_inizio;
	}

	public void setT_inizio(String t_inizio) {
		this.t_inizio = t_inizio;
	}

	public String getT_fine() {
		return t_fine;
	}

	public void setT_fine(String t_fine) {
		this.t_fine = t_fine;
	}

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		if(this.testo==null || this.testo.isEmpty())
			this.testo=testo;
		else
			this.testo+="\n"+testo;
	}

	public int getEntry() {
		return entry;
	}

	public void setEntry(int entry) {
		this.entry = entry;
	}

}
