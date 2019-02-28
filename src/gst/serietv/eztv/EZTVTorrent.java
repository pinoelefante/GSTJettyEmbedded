package gst.serietv.eztv;

import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.Torrent;

@DatabaseTable(tableName="eztv_torrent")
public class EZTVTorrent extends Torrent
{
	public EZTVTorrent() {}
	
	public EZTVTorrent(int episodeId, String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(episodeId, url, resolution, proper, repack, preair, source);
	}
	public EZTVTorrent(String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		super(0, url, resolution, proper, repack, preair, source);
	}

}
