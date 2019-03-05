package gst.serietv;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import gst.database.Database;

public abstract class AbstractTorrentProviderController<ID, S extends SerieTV & Identifier<ID>, T extends Torrent> extends AbstractController<S,ID>
{
	protected abstract T creaTorrent(ID showId, int season, int episode, String link, int resolution, boolean proper, boolean repack, boolean preair, String source);
	protected abstract List<String> getLinks(S serie);
	protected abstract List<String> getLinks(ID serieId);
	public abstract Set<T> getTorrents(ID showId, int season, int episode);
	public abstract Set<T> getTorrents(ID showId);
	
	protected Database db;
	
	public AbstractTorrentProviderController()
	{
		db = Database.getInstance();
	}
	
	@Override
	public Set<EpisodeWrapper> aggiornaEpisodi(ID id)
	{
		S show = getSerie(id);
		return aggiornaEpisodi(show);
	}
	@Override
	public Set<EpisodeWrapper> aggiornaEpisodi(S s)
	{
		List<String> torrentLinks = getLinks(s);
		Set<T> online = parseLinks(s, torrentLinks);
		Set<T> offline = getTorrents(s.getId());
		
		online.removeAll(offline);
		
		db.SaveCollection(online);
		
		Set<EpisodeWrapper> newEpisodes = getDistinctEpisodesTupleFromSet(online);
		
		
		return newEpisodes;
	}
	
	protected SortedSet<T> parseLinks(S serie, List<String> links)
	{
		SortedSet<T> torrents = new TreeSet<>();
		for(String link : links)
		{
			EpisodeWrapper key = NameUtils.getSeasonEpisode(link);
			int resolution = NameUtils.getResolutionFromName(link);
			boolean preair = NameUtils.isPreAir(link);
			boolean proper = NameUtils.isProper(link);
			boolean repack = NameUtils.isRepack(link);
			String source =  NameUtils.getVideoSource(link);
			
			T torrent = creaTorrent(serie.getId(), key.getStagione(), key.getEpisodio(), link, resolution, proper, repack, preair, source);
			torrents.add(torrent);
		}
		return torrents;
	}
	@Override
	public Set<EpisodeWrapper> elencoEpisodi(ID id)
	{
		Set<T> torrents = getTorrents(id);
		Set<EpisodeWrapper> episodes = getDistinctEpisodesTupleFromSet(torrents);
		return episodes;
	}
	private Set<EpisodeWrapper> getDistinctEpisodesTupleFromSet(Set<T> torrents)
	{
		Set<EpisodeWrapper> episodes = torrents.stream()
				.map((T t) -> new EpisodeWrapper(t.getSeason(), t.getEpisode()))
				.distinct()
				.sorted()
				.collect(Collectors.toSet());
		return episodes;
	}
	@Override
	public Set<EpisodeWrapper> elencoEpisodi(S s)
	{
		return elencoEpisodi(s.getId());
	}
}
