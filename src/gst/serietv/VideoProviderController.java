package gst.serietv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import gst.database.Database;
import gst.download.UTorrent;
import gst.serietv.eztv.EZTVController;
import gst.serietv.eztv.EZTVSerieTV;
import gst.serietv.showrss.ShowRSSController;
import gst.serietv.showrss.ShowRSSSerieTV;
import javafx.util.Pair;
import util.MyCollections;

public class VideoProviderController
{
	private static VideoProviderController instance = new VideoProviderController();
	private List<AbstractController<?,?>> controllers;
	private Database db;

	private VideoProviderController()
	{
		db = Database.GetInstance();
		db.CreateDB(SerieTVComposer.class);
		controllers = new ArrayList<>(2);
		controllers.add(EZTVController.getInstance());
		controllers.add(ShowRSSController.getInstance());
	}
	public static VideoProviderController getInstance() {
		return instance;
	}
	@SuppressWarnings("unchecked")
	public List<SerieTV> getElencoSerieTV()
	{
		return MyCollections.CastTo(db.SelectWhereOrder(SerieTVComposer.class,
				null,
				new Pair[]{new Pair<String,String>("titolo", "ASC")}));
	}

	@SuppressWarnings("unchecked")
	public List<SerieTV> aggiornaSerie()
	{
		long startUpdate = System.currentTimeMillis();
		List<SerieTV> newShows = new ArrayList<>();
		for (AbstractController<?,?> c : controllers)
		{
			List<SerieTV> serie = (List<SerieTV>) c.aggiornaSerie();
			newShows.addAll(serie);
		}
		System.out.println("Tempo aggiornamento serie: "+ (System.currentTimeMillis() - startUpdate));
		return MyCollections.CastTo(merge(newShows));
	}

	private List<SerieTVComposer> merge(List<SerieTV> list)
	{
		Map<String, SerieTVComposer> map = getSerieTVMap();
		List<SerieTVComposer> newShows = new ArrayList<>();
		List<SerieTVComposer> updated = new ArrayList<>();
		for (SerieTV s : list)
		{
			String key = s.getTitolo().toLowerCase();
			if (map.containsKey(key))
			{
				SerieTVComposer composer = map.get(key);
				if(!hasId(composer, s))
				{
    				associateComposer(composer, s);
    				updated.add(composer);
				}
			}
			else
			{
				SerieTVComposer composer = createComposer(s);
				map.put(key, composer);
				newShows.add(composer);
			}
		}
		db.SaveCollection(updated);
		db.SaveCollection(newShows);
		return newShows;
	}

	private void associateComposer(SerieTVComposer composer, SerieTV serie)
	{
		if (serie instanceof EZTVSerieTV)
			composer.setEztv(((EZTVSerieTV) serie).getId());
		else if (serie instanceof ShowRSSSerieTV)
			composer.setShowrss(((ShowRSSSerieTV) serie).getId());
	}

	private SerieTVComposer createComposer(SerieTV serie)
	{
		SerieTVComposer composer = new SerieTVComposer(0, 0, serie.getTitolo());
		associateComposer(composer, serie);
		return composer;
	}

	private boolean hasId(SerieTVComposer composer, SerieTV serie)
	{
		if (serie instanceof EZTVSerieTV)
			return composer.getEztv() > 0;
		else if (serie instanceof ShowRSSSerieTV)
			return composer.getShowrss() > 0;
		return false;
	}

	private Map<String, SerieTVComposer> getSerieTVMap()
	{
		List<SerieTVComposer> list = db.SelectAll(SerieTVComposer.class);
		Map<String, SerieTVComposer> map = new TreeMap<>();
		list.forEach((SerieTVComposer s) -> map.put(s.getTitolo().toLowerCase(), s));
		return map;
	}
	
	public boolean rename(int composerId, String newTitle)
	{
		SerieTVComposer composer = getComposer(composerId);
		composer.setTitolo(newTitle);
		return db.SaveItem(composer).isComplete();
	}
	
	public boolean associateComposers(int composerFirst, int composerSecond)
	{
		SerieTVComposer composer1 = getComposer(composerFirst);
		SerieTVComposer composer2 = getComposer(composerSecond);
		
		if(composer1.getEztv() == 0)
			composer1.setEztv(composer2.getEztv());
		else if(composer1.getShowrss() == 0)
			composer1.setShowrss(composer2.getShowrss());
		if(db.SaveItem(composer1).isComplete())
		{
			db.DeleteItem(composer2);
			return true;
		}
		return false;
	}
	
	private SerieTVComposer getComposer(int id)
	{
		return db.SelectById(SerieTVComposer.class, id);
	}
	
	public Map<SerieTV, Set<EpisodeWrapper>> aggiornaEpisodi(int composerId)
	{
		Map<SerieTV, Set<EpisodeWrapper>> map = new TreeMap<>();
		SerieTVComposer composer = getComposer(composerId);
		Set<EpisodeWrapper> episodi = new TreeSet<>();
		for(AbstractController<?,?> controller : controllers)
		{
			if(controller instanceof EZTVController)
				episodi.addAll(((EZTVController)controller).aggiornaEpisodi(composer.getEztv()));
			else if(controller instanceof ShowRSSController)
				episodi.addAll(((ShowRSSController)controller).aggiornaEpisodi(composer.getShowrss()));
		}
		map.put(composer, episodi);
		aggiornaTempoAggiornamento(composer);
		return map;
	}
	
	public Map<SerieTV, Set<EpisodeWrapper>> aggiornaEpisodi()
	{
		List<SerieTV> listaSerie = getFavouriteList();
		Map<SerieTV, Set<EpisodeWrapper>> map = new TreeMap<>();
		for(SerieTV serie : listaSerie)
			map.putAll(aggiornaEpisodi(((SerieTVComposer)serie).getId()));
		return map;
	}
	
	public Map<SerieTV, Set<EpisodeWrapper>> getElencoEpisodi()
	{
		Map<SerieTV, Set<EpisodeWrapper>> map = new TreeMap<>();
		List<SerieTV> serie = getFavouriteList();
		for(SerieTV s : serie)
		{
			SerieTVComposer composer = (SerieTVComposer)s;
			map.put(s, getElencoEpisodi(composer.getId()));
		}
		return map;
	}
	@SuppressWarnings("rawtypes")
	public SortedSet<EpisodeWrapper> getElencoEpisodi(int composerId)
	{
		SerieTVComposer composer = getComposer(composerId);
		SortedSet<EpisodeWrapper> episodi = new TreeSet<>();
		for(AbstractController controller : controllers)
		{
			if(controller instanceof EZTVController)
				episodi.addAll(((EZTVController)controller).elencoEpisodi(composer.getEztv()));
			else if(controller instanceof ShowRSSController)
				episodi.addAll(((ShowRSSController)controller).elencoEpisodi(composer.getShowrss()));
		}
		return episodi;
	}
	public boolean setFavourite(int showId, boolean status)
	{
		SerieTVComposer composer = getComposer(showId);
		composer.setFavourite(status);
		return db.SaveItem(composer).isComplete();
	}
	@SuppressWarnings("unchecked")
	public List<SerieTV> getFavouriteList()
	{
		return MyCollections.CastTo(db.SelectWhereOrder(SerieTVComposer.class,
				new Pair[] { new Pair<String, Boolean>("favourite", true)},
				new Pair[] { new Pair<String, String>("titolo", "ASC")}));
	}
	
	public Set<Torrent> getTorrentsForEpisode(int showId, int season, int episode)
	{
		Set<Torrent> torrents = new TreeSet<>();
		SerieTVComposer composer = getComposer(showId);
		for(AbstractController<?,?> c : controllers)
		{
			if(c instanceof EZTVController)
				torrents.addAll(((EZTVController) c).getTorrents(composer.getEztv(), season, episode));
			else if(c instanceof ShowRSSController)
				torrents.addAll(((ShowRSSController) c).getTorrents(composer.getShowrss(), season, episode));
		}
		return torrents;
	}
	public boolean changeFavouriteResolution(int id, int resolution)
	{
		SerieTVComposer composer = getComposer(id);
		composer.setFavouriteResolution(resolution);
		return db.SaveItem(composer).isComplete();
	}
	private void aggiornaTempoAggiornamento(SerieTVComposer comp)
	{
		comp.setLastUpdate(System.currentTimeMillis());
		db.SaveItem(comp);
	}
	public Torrent downloadEpisode(int showId, int season, int episode)
	{
		SerieTVComposer composer = getComposer(showId);
		Set<Torrent> torrents = getTorrentsForEpisode(showId, season, episode);
		if(torrents.isEmpty())
			return null;
		List<Torrent> t = torrents.stream().filter((Torrent x) -> x.getResolution() == composer.getFavouriteResolution()).collect(Collectors.toList());
		Torrent download = !t.isEmpty() ? t.get(0) : torrents.iterator().next();
		String path = "D:\\Torrent\\SerieTV";
		UTorrent.getInstance().downloadCLI(download, path);
		System.out.println(download);
		return download;
	}
}
