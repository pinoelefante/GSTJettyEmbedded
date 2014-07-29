package gst.download;

import gst.serieTV.Torrent;

public interface BitTorrentClient {
	public boolean haveWebAPI();
	public boolean isWebAPIEnabled();
	public String auth(String username, String pass);
	public boolean setDirectoryDownload(String dir);
	public boolean downloadTorrent(Torrent t, String path);
}
