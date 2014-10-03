package gst.sottotitoli.addic7ed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

public class Addic7ed implements ProviderSottotitoli {
	private final static String URL_SHOWLIST = "http://www.addic7ed.com/ajax_getShows.php";
	private final static String URL_GET_EPISODES = "http://www.addic7ed.com//ajax_loadShow.php?show=<IDSHOW>&season=<SEASON>&langs=|<LANG>|&hd=<HD>&hi=0";
	private Map<String, Integer> lingue_disponibili;
	
	
	public Addic7ed() {
		lingue_disponibili = new HashMap<>();
		lingue_disponibili.put(INGLESE, 1);
		lingue_disponibili.put(ITALIANO, 7);
		lingue_disponibili.put(FRANCESE, 8);
		lingue_disponibili.put(PORTOGHESE, 10);
		lingue_disponibili.put(TEDESCO, 11);
		lingue_disponibili.put(SPAGNOLO, 4);
	}

	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		//TODO
		return null;
	}

	@Override
	public String getProviderName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProviderID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void associaSerie(SerieTV s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void aggiornaElencoSerieOnline() {
		try {
			Document doc = Jsoup.connect(URL_SHOWLIST).get();
			Elements opts = doc.select("#qsShow option");
			for(int i=0;i<opts.size();i++){
				Element opt = opts.get(i);
				try {
					int idSerie = Integer.parseInt(opt.val());
					if(idSerie > 0){
						String nome = opt.text();
						System.out.println(nome+" - "+idSerie);
						
					}
				}
				catch(Exception e){
					
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args){
		Addic7ed a = new Addic7ed();
		a.aggiornaElencoSerieOnline();
	}

	@Override
	public boolean associa(int idSerie, int idSub) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disassocia(int idSerie) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLanguage(String lang) {
		switch(lang){
			case INGLESE:
			case ITALIANO:
			case FRANCESE:
			case PORTOGHESE:
			case SPAGNOLO:
			case TEDESCO:
				return true;
		}
		return false;
	}

}
