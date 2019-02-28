package gst.sottotitoli;

import java.util.ArrayList;

import gst.serietv.Episodio;
import gst.serietv.SerieTV;

public abstract interface ProviderSottotitoli {
	public final static String ITALIANO="it", 
			INGLESE="en",
			FRANCESE="fr",
			SPAGNOLO="es",
			PORTOGHESE="pr",
			TEDESCO="de";
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang);
	public ArrayList<SerieSub> getElencoSerie();
	public String getProviderName();
	public int getProviderID();
	public void associaSerie(SerieTV s);
	public void aggiornaElencoSerieOnline();
	public boolean associa(int idSerie, int idSub);
	public boolean disassocia(int idSerie);
	public boolean hasLanguage(String lang);
}
