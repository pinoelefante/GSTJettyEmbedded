package gst.serieTV.showrss;

import com.j256.ormlite.table.DatabaseTable;

import gst.serieTV.Torrent;

@DatabaseTable(tableName="showrss_torrent")
public class ShowRSSTorrent extends Torrent
{

	public ShowRSSTorrent()
	{
	}

	public ShowRSSTorrent(String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(url, resolution, proper, repack, preair, source);
	}

	public ShowRSSTorrent(int episodeId, String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(episodeId, url, resolution, proper, repack, preair, source);
	}

}
