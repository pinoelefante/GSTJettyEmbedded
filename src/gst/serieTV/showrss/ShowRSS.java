package gst.serieTV.showrss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.httpOperations.HttpOperations;

public class ShowRSS
{
	private static final String BASE_URL = "https://showrss.info";
	
	public List<ShowRSSSerieTV> aggiornaElencoSerie()
	{
		List<ShowRSSSerieTV> lista = new ArrayList<>();
		try
		{
			String pageURL = BASE_URL + "/browse";
			Document doc = HttpOperations.getJSoupDocument(pageURL);
			Elements form = doc.select("select#showselector option");
			
			for (int i = 1; i < form.size(); i++)
			{
				Element opt = form.get(i);
				String val = opt.val();
				String nome = opt.text();
				if (!val.isEmpty() && !nome.isEmpty())
				{
					int onlineId = Integer.parseInt(val);
					ShowRSSSerieTV serie = new ShowRSSSerieTV(nome, onlineId);
					lista.add(serie);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return lista;
	}
	
	public List<String> caricaEpisodiOnline(ShowRSSSerieTV serie)
	{
		List<String> lista = new ArrayList<String>();
		try
		{
			String pageURL = BASE_URL + "/browse/" + serie.getId();
			Document doc = HttpOperations.getJSoupDocument(pageURL);
			Elements magnets = doc.select("a");
			for (int i = 0; i < magnets.size(); i++)
			{
				String url = magnets.get(i).attr("href");
				if (url.startsWith("magnet:"))
					lista.add(url);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return lista;
	}
}
