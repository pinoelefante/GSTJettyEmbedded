package gst.serietv.eztv;

import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.Torrent;

@DatabaseTable(tableName="eztv_torrent")
public class EZTVTorrent extends Torrent
{
	public EZTVTorrent() {}
	
	public EZTVTorrent(int showId, int season, int episode,String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(showId, season, episode, url, resolution, proper, repack, preair, source);
	}

}
