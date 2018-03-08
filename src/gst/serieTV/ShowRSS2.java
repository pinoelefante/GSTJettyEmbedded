package gst.serieTV;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.naming.CaratteristicheFile;
import gst.programma.Settings;
import util.UserAgent;

public class ShowRSS2 extends ProviderSerieTV
{
	private Settings settings;

	public ShowRSS2()
	{
		super(PROVIDER_SHOWRSS2);
		settings = Settings.getInstance();
	}

	@Override
	public String getProviderName()
	{
		return "showRss.info";
	}

	@Override
	public String getBaseURL()
	{
		return "http://showrss.info";
	}
	
	@Override
	public void aggiornaElencoSerie()
	{
		try
		{
			Document doc = Jsoup.connect(getBaseURL() + "/browse").header("User-Agent", UserAgent.get()).timeout(10000).get();
			Elements form = doc.select("select#showselector option");
			int caricate = 0;
			for (int i = 1; i < form.size(); i++)
			{
				Element opt = form.get(i);
				String val = opt.val();
				String nome = opt.text();
				if (!val.isEmpty() && !nome.isEmpty())
				{
					SerieTV serie = new SerieTV(getProviderID(), nome, val);
					serie.setConclusa(false);
					serie.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
					serie.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
					if (aggiungiSerieADatabase(serie, PROVIDER_SHOWRSS2))
					{
						serie = getSerieByURL(serie.getUrl(), PROVIDER_SHOWRSS2);
						if (associaEztv(serie)){
							boolean rem = nuove_serie.removeIf(x -> x.getProviderID() == PROVIDER_SHOWRSS2 && x.getUrl().compareTo(val)==0);
							System.out.println("Removed: "+rem);
						}
							
						else
							caricate++;
					}
				}
			}
			System.out.println("showrss.info new: " + caricate + " serie nuove");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private boolean associaEztv(SerieTV s)
	{
		String query = "SELECT * FROM " + Database.TABLE_SERIETV + " WHERE provider=" + PROVIDER_EZTV + " AND lower(nome)=\"" + s.getNomeSerie().toLowerCase() + "\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if (res.size() == 1)
		{
			KVResult<String, Object> resEztv = res.get(0);
			int ideztv = (int) resEztv.getValueByKey("id");
			String query_associa = "UPDATE " + Database.TABLE_SERIETV + " SET id_showrss_new=? WHERE id=?";
			return Database.updateQuery(query_associa, s.getIDDb(), ideztv);
		}
		return false;
	}

	@Override
	public int getProviderID()
	{
		return PROVIDER_SHOWRSS2;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie)
	{
		try
		{
			int idSerie = serie.getIDDb();
			SerieTV k_serie = serie.GetIdShowRss() == 0 ? serie : ProviderSerieTV.getSerieByID(serie.GetIdShowRss());
			Document doc = Jsoup.connect(getBaseURL() + "/browse/" + k_serie.getUrl()).header("User-Agent", UserAgent.get()).timeout(10000).get();
			Elements magnets = doc.select("a");
			for (int i = 0; i < magnets.size(); i++)
			{
				String url = magnets.get(i).attr("href");
				if (url.startsWith("magnet:"))
				{
					CaratteristicheFile stat = Torrent.parse(url);
					int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(idSerie, stat.getStagione(), stat.getEpisodio());
					ProviderSerieTV.aggiungiLink(episodio_id, stat.value(), url);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected ArrayList<SerieTV> getElencoSerieDB()
	{
		ArrayList<SerieTV> showrss = new ArrayList<SerieTV>();
		String select = "SELECT * FROM " + Database.TABLE_SERIETV + " WHERE provider=" + PROVIDER_SHOWRSS2;
		ArrayList<KVResult<String, Object>> r = Database.selectQuery(select);
		for (int i = 0; i < r.size(); i++)
		{
			showrss.add(parseSerie(r.get(i)));
		}
		String eztv = "SELECT * FROM " + Database.TABLE_SERIETV + " WHERE provider=" + PROVIDER_EZTV + " AND id_showrss_new>0";
		ArrayList<KVResult<String, Object>> r2 = Database.selectQuery(eztv);
		for (int i = 0; i < r2.size(); i++)
		{
			SerieTV s = parseSerie(r2.get(i));
			for (int j = 0; j < showrss.size(); j++)
			{
				if (showrss.get(j).getIDDb() == s.GetIdShowRss())
				{
					showrss.remove(j);
					break;
				}
			}
		}
		return showrss;
	}
	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

}
