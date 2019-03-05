package gst.serietv.eztv;

import java.util.List;
import java.util.Set;

import gst.serietv.AbstractTorrentProviderController;
import gst.serietv.NameUtils;
import javafx.util.Pair;
import util.MyCollections;

public class EZTVController extends AbstractTorrentProviderController<Integer, EZTVSerieTV, EZTVTorrent>
{
	private EZTV eztv;
	private static class SingletonHelper{
        private static final EZTVController INSTANCE = new EZTVController();
    }
	
	public static EZTVController getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	private EZTVController() {
		eztv = new EZTV();
		db.CreateDB(EZTVSerieTV.class, EZTVTorrent.class);
	}
	
	@Override
	public List<EZTVSerieTV> aggiornaSerie()
	{
		List<EZTVSerieTV> allOnlineSeries = eztv.aggiornaElencoSerie();
		allOnlineSeries.forEach((EZTVSerieTV x) -> x.setTitolo(NameUtils.formattaNome(x.getTitolo())));
		List<EZTVSerieTV> allDbSeries = elencoSerie();
		allOnlineSeries.removeIf((x) -> allDbSeries.stream().anyMatch((dbs)-> dbs.getId() == x.getId()));
	
		db.SaveCollection(allOnlineSeries);
		
		return MyCollections.CastTo(allOnlineSeries);
	}
	
	@Override
	public List<EZTVSerieTV> elencoSerie()
	{
		List<EZTVSerieTV> series = db.SelectAll(EZTVSerieTV.class);
		return series;
	}
	
	@Override
	protected EZTVTorrent creaTorrent(Integer showId, int season, int episode, String link, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		return new EZTVTorrent(showId, season, episode, link, resolution, proper, repack, preair, source);
	}
	
	@Override
	protected List<String> getLinks(Integer serieId)
	{
		return eztv.caricaLinkTorrents(serieId);
	}
	
	@Override
	protected List<String> getLinks(EZTVSerieTV serie)
	{
		return eztv.caricaLinkTorrents(serie);
	}
	
	@Override
	public EZTVSerieTV getSerie(Integer id)
	{
		return db.SelectById(EZTVSerieTV.class, id);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public Set<EZTVTorrent> getTorrents(Integer showId, int season, int episode)
	{
		List<EZTVTorrent> listOfflineTorrents = db.SelectWhere(EZTVTorrent.class, new Pair<>("showId", showId), new Pair<>("season", season), new Pair<>("episode", episode));
		return MyCollections.createSortedSetFromList(listOfflineTorrents);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<EZTVTorrent> getTorrents(Integer showId)
	{
		List<EZTVTorrent> listOfflineTorrents = db.SelectWhere(EZTVTorrent.class, new Pair<>("showId", showId));
		return MyCollections.createSortedSetFromList(listOfflineTorrents);
	}
}
