package gst.serieTV;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import util.MyCollections;
import util.Tuple;

@SuppressWarnings("rawtypes")
public abstract class AbstractTorrentProviderController<ID, S extends SerieTV & Identifier<ID>, E extends EpisodioTorrent, T extends Torrent> extends AbstractController<S,E,ID>
{
	protected abstract E creaEpisodioTorrent(Tuple<Integer,Integer> tupla, ID serieId);
	protected abstract T creaTorrent(String link, int resolution, boolean proper, boolean repack, boolean preair, String source);
	protected abstract List<T> listOfflineTorrents(E episode);
	protected abstract List<String> getLinks(S serie);
	protected abstract void saveNewEpisode(E onlineEpisodio);
	protected abstract void saveNewTorrentsForOldEpisode(E online, E offline);
	
	@Override
	public SortedSet<E> aggiornaEpisodi(S s)
	{
		List<String> links = getLinks(s);
		SortedSet<E> onlineEpisodes = parseLinks(s, links);
		SortedSet<E> offlineEpisodes = elencoEpisodi(s);
		
		SortedSet<E> newEpisodes = new TreeSet<>(); // usato per ritornare i nuovi episodi
		
		for(E ep : onlineEpisodes)
		{
			if(offlineEpisodes.contains(ep))
			{
				Optional<E> optEp = offlineEpisodes.stream().filter((E offlineEp) -> offlineEp.equals(ep)).findFirst();
				if(optEp.isPresent())
					saveNewTorrentsForOldEpisode(ep, optEp.get());
				else
					System.out.println("Non ho trovato il vecchio episodio!");
				offlineEpisodes.remove(ep);
				newEpisodes.add(ep);
			}
			else
			{
				saveNewEpisode(ep);
			}
		}
		return newEpisodes; // nuovi episodi
	}
	
	@SuppressWarnings("unchecked")
	protected SortedSet<E> parseLinks(S serie, List<String> links)
	{
		Map<Tuple<Integer,Integer>, E> episodeMap = new TreeMap<>();
		for(String link : links)
		{
			Tuple<Integer,Integer> key = NameUtils.getSeasonEpisode(link);
			if(!episodeMap.containsKey(key))
			{
				E episodio = creaEpisodioTorrent(key, serie.getId());
				episodeMap.put(key, episodio);
			}
			
			int resolution = NameUtils.getResolutionFromName(link);
			boolean preair = NameUtils.isPreAir(link);
			boolean proper = NameUtils.isProper(link);
			boolean repack = NameUtils.isRepack(link);
			String source =  NameUtils.getVideoSource(link);
			
			E episodio = episodeMap.get(key);
			Torrent torrent = creaTorrent(link, resolution, proper, repack, preair, source);
			episodio.addLink(torrent);
		}
		return MyCollections.createSortedSetFromMapValues(episodeMap);
	}
	@SuppressWarnings("unchecked")
	protected SortedSet<T> getOnlyNewTorrents(E online, E offline)
	{
		List<T> listOfflineTorrents = listOfflineTorrents(offline);
		SortedSet<T> setOnlineTorrent = MyCollections.createSortedSetFromList(online.getLinksList());
		setOnlineTorrent.removeAll(listOfflineTorrents);
		
		return setOnlineTorrent; 
	}
}
