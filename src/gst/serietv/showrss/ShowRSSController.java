package gst.serietv.showrss;

import java.util.List;
import java.util.SortedSet;

import gst.database.Database;
import gst.serietv.AbstractTorrentProviderController;
import javafx.util.Pair;
import util.MyCollections;
import util.Tuple;

public class ShowRSSController extends AbstractTorrentProviderController<Integer, ShowRSSSerieTV, ShowRSSEpisodio, ShowRSSTorrent>
{
	private final static ShowRSSController instance = new ShowRSSController();
	private Database db;
	private ShowRSS showRss;
	private ShowRSSController()
	{
		showRss = new ShowRSS();
		db = Database.GetInstance();
		db.CreateDB(ShowRSSSerieTV.class, ShowRSSEpisodio.class, ShowRSSTorrent.class);
	}
	public static ShowRSSController getInstance() {
		return instance;
	}
	@Override
	protected ShowRSSEpisodio creaEpisodioTorrent(Tuple<Integer, Integer> tupla, Integer serieId)
	{
		return new ShowRSSEpisodio(tupla.getElement1(), tupla.getElement2(), serieId);
	}

	@Override
	protected ShowRSSTorrent creaTorrent(String link, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		return new ShowRSSTorrent(link, resolution, proper, repack, preair, source);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<ShowRSSTorrent> listOfflineTorrents(ShowRSSEpisodio episode)
	{
		return db.SelectWhere(ShowRSSTorrent.class, new Pair<>("episodeId", episode.getEpisodioId()));
	}

	@Override
	protected List<String> getLinks(ShowRSSSerieTV serie)
	{
		return showRss.caricaEpisodiOnline(serie);
	}

	@Override
	protected void saveNewEpisode(ShowRSSEpisodio onlineEpisodio)
	{
		db.SaveItem(onlineEpisodio, new Runnable() {
			public void run()
			{
				onlineEpisodio.getLinksList().forEach((ShowRSSTorrent t) -> t.setEpisodeId(onlineEpisodio.getEpisodioId()));
				db.SaveList(ShowRSSTorrent.class, onlineEpisodio.getLinksList());
			}
		});
		
	}

	@Override
	protected void saveNewTorrentsForOldEpisode(ShowRSSEpisodio online, ShowRSSEpisodio offline)
	{
		SortedSet<ShowRSSTorrent> setOnlineTorrent = getOnlyNewTorrents(online, offline);
		setOnlineTorrent.forEach((ShowRSSTorrent t) -> t.setEpisodeId(offline.getEpisodioId()));
		db.SaveList(ShowRSSTorrent.class, MyCollections.createListFromSet(setOnlineTorrent));
		
	}

	@Override
	public List<ShowRSSSerieTV> aggiornaSerie()
	{
		List<ShowRSSSerieTV> allOnlineSeries = showRss.aggiornaElencoSerie();
		
		List<ShowRSSSerieTV> allDbSeries = db.SelectAll(ShowRSSSerieTV.class);
		if(allDbSeries!=null) {
			allOnlineSeries.removeIf((x) -> allDbSeries.stream().anyMatch((dbs)-> dbs.getId() == x.getId()));
		}
		else
			System.out.println("Cant load showrss series from db");
		
		db.SaveList(ShowRSSSerieTV.class, allOnlineSeries);
		
		return MyCollections.CastTo(allOnlineSeries);
	}

	@Override
	public List<ShowRSSSerieTV> elencoSerie()
	{
		return db.SelectAll(ShowRSSSerieTV.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<ShowRSSEpisodio> elencoEpisodi(ShowRSSSerieTV s)
	{
		return MyCollections.createSortedSetFromList(db.SelectWhere(ShowRSSEpisodio.class, new Pair<String,Object>("serieId", s.getId())));
	}
	public static void main(String[] args)
	{
		ShowRSSController c = new ShowRSSController();
		c.aggiornaSerie();
	}
	@Override
	public SortedSet<ShowRSSEpisodio> aggiornaEpisodi(Integer id)
	{
		ShowRSSSerieTV serie = getSerie(id);
		return aggiornaEpisodi(serie);
	}
	@Override
	public ShowRSSSerieTV getSerie(Integer id)
	{
		return db.SelectById(ShowRSSSerieTV.class, id);
	}
	@Override
	public SortedSet<ShowRSSEpisodio> elencoEpisodi(Integer id)
	{
		ShowRSSSerieTV serie = getSerie(id);
		return elencoEpisodi(serie);
	}
}
