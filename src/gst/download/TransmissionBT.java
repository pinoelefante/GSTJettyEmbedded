package gst.download;

import gst.serieTV.Torrent;

public class TransmissionBT implements BitTorrentClient {
	private String address="http://host:9091/transmission/rpc";
	
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
