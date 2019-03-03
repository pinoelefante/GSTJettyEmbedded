package gst.serietv.showrss;

import java.util.List;
import java.util.Set;

import gst.serietv.AbstractTorrentProviderController;
import javafx.util.Pair;
import util.MyCollections;

public class ShowRSSController extends AbstractTorrentProviderController<Integer, ShowRSSSerieTV, ShowRSSTorrent>
{
	private final static ShowRSSController instance = new ShowRSSController();
	private ShowRSS showRss;
	private ShowRSSController()
	{
		showRss = new ShowRSS();
		db.CreateDB(ShowRSSSerieTV.class, ShowRSSTorrent.class);
	}
	public static ShowRSSController getInstance() {
		return instance;
	}

	@Override
	protected ShowRSSTorrent creaTorrent(Integer showId, int season, int episode, String link, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		return new ShowRSSTorrent(showId, season, episode, link, resolution, proper, repack, preair, source);
	}

	@Override
	protected List<String> getLinks(ShowRSSSerieTV serie)
	{
		return showRss.caricaEpisodiOnline(serie);
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
		
		db.SaveCollection(allOnlineSeries);
		
		return MyCollections.CastTo(allOnlineSeries);
	}

	@Override
	public List<ShowRSSSerieTV> elencoSerie()
	{
		return db.SelectAll(ShowRSSSerieTV.class);
	}

	@Override
	public ShowRSSSerieTV getSerie(Integer id)
	{
		return db.SelectById(ShowRSSSerieTV.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<ShowRSSTorrent> getTorrents(Integer showId, int season, int episode)
	{
		List<ShowRSSTorrent> t = db.SelectWhere(ShowRSSTorrent.class, new Pair<>("showId", showId), new Pair<>("season",season), new Pair<>("episode",episode));
		return MyCollections.createSortedSetFromList(t);
	}
	@Override
	protected List<String> getLinks(Integer serieId)
	{
		return showRss.caricaEpisodiOnline(serieId);
	}
	@SuppressWarnings("unchecked")
	@Override
	public Set<ShowRSSTorrent> getTorrents(Integer showId)
	{
		List<ShowRSSTorrent> t = db.SelectWhere(ShowRSSTorrent.class, new Pair<>("showId", showId));
		return MyCollections.createSortedSetFromList(t);
	}
}
