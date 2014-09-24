package gst.sottotitoli;

import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;

import java.util.ArrayList;

public abstract interface ProviderSottotitoli {
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep);
	public ArrayList<SerieSub> getElencoSerie();
	public String getProviderName();
	public int getProviderID();
	public void associaSerie(SerieTV s);
	public void aggiornaElencoSerieOnline();
	public boolean associa(int idSerie, int idSub);
	public boolean disassocia(int idSerie);
}
