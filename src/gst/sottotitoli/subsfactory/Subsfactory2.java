package gst.sottotitoli.subsfactory;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;

import util.UserAgent;
import gst.programma.ManagerException;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.sottotitoli.SerieSubConDirectory;

public class Subsfactory2 implements ProviderSottotitoli{
	private static String URL_ELENCO_SERIE = "http://www.subsfactory.it/archivio/download-category/tutte-le-serie/";
	public static void main(String[] args){
		Subsfactory2 s = new Subsfactory2();
		s.aggiornaElencoSerieOnline();
	}
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProviderName() {
		return "Subsfactory.it";
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSFACTORY;
	}

	@Override
	public void associaSerie(SerieTV s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aggiornaElencoSerieOnline() {
		try {
			org.jsoup.nodes.Document page = Jsoup.connect(URL_ELENCO_SERIE).header("User-Agent", UserAgent.get())
					.timeout(10000)
					.get();
			org.jsoup.nodes.Element listContainer = page.getElementById("download-page").select("ul.download-monitor-subcategories2").get(0);
			org.jsoup.select.Elements series = listContainer.select(".tdleft").select("a");
			
			for(int i=0;i<series.size();i++){
				org.jsoup.nodes.Element s=series.get(i);
				String link = s.attr("href");
				String nome = s.text();
				System.out.println(nome + " " + link);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
		// TODO Auto-generated method stub
		return false;
	}

}
