package gst.serietv.eztv;

import java.util.List;
import java.util.SortedSet;

import gst.database.Database;
import gst.serietv.AbstractTorrentProviderController;
import gst.serietv.NameUtils;
import javafx.util.Pair;
import util.MyCollections;
import util.Tuple;

public class EZTVController extends AbstractTorrentProviderController<Integer, EZTVSerieTV, EZTVEpisodio, EZTVTorrent>
{
	private final static EZTVController instance = new EZTVController();
	private EZTV eztv;
	private final Database db;
	
	public static EZTVController getInstance() {
		return instance;
	}
	
	private EZTVController() {
		eztv = new EZTV();
		db = Database.GetInstance();
		db.CreateDB(EZTVSerieTV.class, EZTVEpisodio.class, EZTVTorrent.class);
	}
	@Override
	public List<EZTVSerieTV> aggiornaSerie()
	{
		List<EZTVSerieTV> allOnlineSeries = eztv.aggiornaElencoSerie();
		allOnlineSeries.forEach((EZTVSerieTV x) -> x.setTitolo(NameUtils.formattaNome(x.getTitolo())));
		List<EZTVSerieTV> allDbSeries = db.SelectAll(EZTVSerieTV.class);
		allOnlineSeries.removeIf((x) -> allDbSeries.stream().anyMatch((dbs)-> dbs.getId() == x.getId()));
	
		db.SaveList(EZTVSerieTV.class, allOnlineSeries);
		
		return MyCollections.CastTo(allOnlineSeries);
	}
	
	@Override
	protected void saveNewEpisode(EZTVEpisodio onlineEpisodio)
	{
		db.SaveItem(onlineEpisodio, new Runnable() {
			public void run()
			{
				onlineEpisodio.getLinksList().forEach((EZTVTorrent t) -> t.setEpisodeId(onlineEpisodio.getEpisodeId()));
				db.SaveList(EZTVTorrent.class, onlineEpisodio.getLinksList());
			}
		});
	}

	@Override
	protected void saveNewTorrentsForOldEpisode(EZTVEpisodio online, EZTVEpisodio offline)
	{
		SortedSet<EZTVTorrent> setOnlineTorrent = getOnlyNewTorrents(online, offline);
		setOnlineTorrent.forEach((EZTVTorrent t) -> t.setEpisodeId(offline.getEpisodeId()));
		db.SaveList(EZTVTorrent.class, MyCollections.createListFromSet(setOnlineTorrent));
	}

	@Override
	public List<EZTVSerieTV> elencoSerie()
	{
		List<EZTVSerieTV> series = db.SelectAll(EZTVSerieTV.class);
		return series;
	}

	@Override
	protected EZTVEpisodio creaEpisodioTorrent(Tuple<Integer, Integer> tupla, Integer serieId)
	{
		return new EZTVEpisodio(tupla.getElement1(), tupla.getElement2(), serieId);
	}

	@Override
	protected EZTVTorrent creaTorrent(String link, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		return new EZTVTorrent(link, resolution, proper, repack, preair, source);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<EZTVEpisodio> elencoEpisodi(EZTVSerieTV s)
	{
		return MyCollections.createSortedSetFromList(db.SelectWhere(EZTVEpisodio.class, new Pair<String,Object>("serieId", s.getId())));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<EZTVTorrent> listOfflineTorrents(EZTVEpisodio episode)
	{
		List<EZTVTorrent> listOfflineTorrents = db.SelectWhere(EZTVTorrent.class, new Pair<String,Object>("episodeId", episode.getEpisodeId()));
		return listOfflineTorrents;
	}
	
	@Override
	protected List<String> getLinks(EZTVSerieTV serie)
	{
		return eztv.caricaLinkTorrents(serie);
	}

	@Override
	public SortedSet<EZTVEpisodio> aggiornaEpisodi(Integer id)
	{
		EZTVSerieTV serie = getSerie(id);
		return aggiornaEpisodi(serie);
	}

	@Override
	public EZTVSerieTV getSerie(Integer id)
	{
		return db.SelectById(EZTVSerieTV.class, id);
	}

	@Override
	public SortedSet<EZTVEpisodio> elencoEpisodi(Integer id)
	{
		EZTVSerieTV serie = getSerie(id);
		return elencoEpisodi(serie);
	}
}
