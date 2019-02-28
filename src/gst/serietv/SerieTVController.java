package gst.serietv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import gst.database.Database;
import gst.serietv.eztv.EZTVController;
import gst.serietv.eztv.EZTVSerieTV;
import gst.serietv.showrss.ShowRSSController;
import gst.serietv.showrss.ShowRSSSerieTV;
import javafx.util.Pair;
import util.MyCollections;

public class SerieTVController
{
	private static SerieTVController instance = new SerieTVController();
	private List<AbstractController<?,?,?>> controllers;
	private Database db;

	private SerieTVController()
	{
		db = Database.GetInstance();
		db.CreateDB(SerieTVComposer.class);
		controllers = new ArrayList<>(2);
		controllers.add(EZTVController.getInstance());
		controllers.add(ShowRSSController.getInstance());
	}
	public static SerieTVController getInstance() {
		return instance;
	}
	@SuppressWarnings("unchecked")
	public List<SerieTV> getElencoSerieTV()
	{
		return MyCollections.CastTo(db.SelectWhereOrder(SerieTVComposer.class, null, new Pair[]{new Pair<String,String>("titolo", "ASC")}));
	}

	@SuppressWarnings("unchecked")
	public List<SerieTVComposer> aggiornaSerie()
	{
		long startUpdate = System.currentTimeMillis();
		List<SerieTV> newShows = new ArrayList<>();
		for (AbstractController<?,?,?> c : controllers)
		{
			List<SerieTV> serie = (List<SerieTV>) c.aggiornaSerie();
			newShows.addAll(serie);
		}
		System.out.println("Tempo aggiornamento serie: "+ (System.currentTimeMillis() - startUpdate));
		return merge(newShows);
	}

	private List<SerieTVComposer> merge(List<SerieTV> list)
	{
		long startTime = System.currentTimeMillis();
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
		db.SaveList(SerieTVComposer.class, updated);
		db.SaveList(SerieTVComposer.class, newShows);
		System.out.println("Merge time: "+(System.currentTimeMillis() - startTime));
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
	public void rename(int composerId, String newTitle)
	{
		SerieTVComposer composer = getComposer(composerId);
		composer.setTitolo(newTitle);
		db.SaveItem(composer);
	}
	public void associateComposers(int composerFirst, int composerSecond)
	{
		SerieTVComposer composer1 = getComposer(composerFirst);
		SerieTVComposer composer2 = getComposer(composerSecond);
		
		if(composer1.getEztv() == 0)
			composer1.setEztv(composer2.getEztv());
		else if(composer1.getShowrss() == 0)
			composer1.setShowrss(composer2.getShowrss());
		
		db.DeleteItem(composer2);
		db.SaveItem(composer1);
	}
	private SerieTVComposer getComposer(int id)
	{
		return db.SelectById(SerieTVComposer.class, id);
	}
	public void aggiornaEpisodi(int composerId)
	{
		SerieTVComposer composer = getComposer(composerId);
		for(AbstractController<?,?,?> controller : controllers)
		{
			if(controller instanceof EZTVController)
				((EZTVController)controller).aggiornaEpisodi(composer.getEztv());
			else if(controller instanceof ShowRSSController)
				((ShowRSSController)controller).aggiornaEpisodi(composer.getShowrss());
		}
	}
	@SuppressWarnings("rawtypes")
	public SortedSet<Episodio> getElencoEpisodi(int composerId)
	{
		SerieTVComposer composer = getComposer(composerId);
		SortedSet<Episodio> episodi = new TreeSet<Episodio>();
		for(AbstractController controller : controllers)
		{
			if(controller instanceof EZTVController)
				episodi.addAll(((EZTVController)controller).elencoEpisodi(composer.getEztv()));
			else if(controller instanceof ShowRSSController)
				episodi.addAll(((ShowRSSController)controller).elencoEpisodi(composer.getShowrss()));
		}
		return episodi;
	}
	public static void main(String[] args)
	{
		SerieTVController c = new SerieTVController();
		List<SerieTV> serie = c.getElencoSerieTV();
		for(SerieTV s : serie)
			System.out.println(s.getTitolo());
	}
	
	// get links for episode
	
	
}
