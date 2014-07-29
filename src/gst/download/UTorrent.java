package gst.download;

import gst.serieTV.Torrent;

public class UTorrent implements BitTorrentClient{

	@Override
	public boolean haveWebAPI() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWebAPIEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String auth(String username, String pass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setDirectoryDownload(String dir) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean downloadTorrent(Torrent t, String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
