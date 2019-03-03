package gst.serietv.showrss;

import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.Torrent;

@DatabaseTable(tableName="showrss_torrent")
public class ShowRSSTorrent extends Torrent
{

	public ShowRSSTorrent() {}

	public ShowRSSTorrent(int showid, int season, int episode, String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(showid, season, episode, url, resolution, proper, repack, preair, source);
	}

}
