package gst.serietv.eztv;

import util.httpOperations.HttpOperations;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EZTV
{
	private final String BASE_URL = "https://eztv.io/";

	public EZTV()
	{
	}

	public List<EZTVSerieTV> aggiornaElencoSerie()
	{
		StringReader stringReader = null;
		Scanner file = null;
		List<EZTVSerieTV> lista = new ArrayList<>();
		try
		{
			String pageHtml = HttpOperations.GET_withResponse(BASE_URL + "/showlist/");
			stringReader = new StringReader(pageHtml);
			file = new Scanner(stringReader);

			while (file.hasNextLine())
			{
				String linea = file.nextLine().trim();
				if (linea.contains("\"thread_link\""))
				{
					String nomeserie = linea.substring(linea.indexOf("class=\"thread_link\">") + "class=\"thread_link\">".length(), linea.indexOf("</a>")).trim();
					String url = linea.substring(linea.indexOf("<a href=\"") + "<a href=\"".length(), linea.indexOf("\" class=\"thread_link\">")).trim();
					url = url.replace(BASE_URL, "");
					url = url.replace("/shows/", "");
					url = url.substring(0, url.indexOf("/"));
					
					if (isTempPlaceholder(nomeserie))
						continue;

					EZTVSerieTV tvshow = new EZTVSerieTV(nomeserie, Integer.parseInt(url));
					lista.add(tvshow);
				}
			}
			
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}
		finally
		{
			try
			{
				stringReader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				file.close();
			}
			catch (Exception e)
			{

			}
		}
		return lista;
	}

	private String[] temp_patterns = { "^temp[_]{0,1}[\\d]*$", "^t[\\d]*$", "/^temporary_placeholder_[0-9]$/", "test", "z_blank" };
	private boolean isTempPlaceholder(String nome)
	{
		for (int i = 0; i < temp_patterns.length; i++)
		{
			if (nome.replaceAll(temp_patterns[i], "").isEmpty())
				return true;
		}
		return false;
	}

	public List<String> caricaLinkTorrents(EZTVSerieTV serie)
	{
		return caricaLinkTorrents(serie.getId());
	}
	public List<String> caricaLinkTorrents(int serieId)
	{
		List<String> links = new ArrayList<>();
		try
		{
			String pageHtml = HttpOperations.GET_withResponse(BASE_URL+"/shows/"+serieId+"/");
			StringReader sr = new StringReader(pageHtml);
			Scanner file = new Scanner(sr);
			while (file.hasNextLine())
			{
				String linea = file.nextLine();
				if (linea.contains("magnet:?xt=urn:btih:"))
				{
					int inizio = linea.indexOf("magnet:?xt=urn:btih:");
					int fine = linea.indexOf("\" class=\"magnet\"");
					String url_magnet = linea.substring(inizio, fine);
					if (url_magnet.length() > 0)
						links.add(url_magnet);
				}
			}
			file.close();
			sr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return links;
	}
}