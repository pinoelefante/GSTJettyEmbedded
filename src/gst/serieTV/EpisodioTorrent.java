package gst.serieTV;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class EpisodioTorrent<T extends Torrent> extends Episodio
{
	private SortedSet<T> links;
	public EpisodioTorrent(int stagione, int episodio)
	{
		super(stagione, episodio);
		links = new TreeSet<>();
	}
	public void addLink(T torrent)
	{
		links.add(torrent);
	}
	public void addLink(List<T> torrents)
	{
		torrents.forEach((T t) -> addLink(t));
	}
	public List<T> getLinksList()
	{
		List<T> list = new ArrayList<>();
		links.forEach((T x) -> list.add(x));
		return list;
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(512);
		builder.append(super.toString());
		builder.append("\n");
		builder.append("Links: "+links.size()+"\n");
		for(Torrent t : links)
		{
			builder.append("\t");
			builder.append(t.toString());
			builder.append("\n");
		}
		return builder.toString();
	}
}
