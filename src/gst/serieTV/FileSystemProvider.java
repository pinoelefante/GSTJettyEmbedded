package gst.serieTV;

import gst.programma.Settings;

public class FileSystemProvider extends ProviderSerieTV {

	public FileSystemProvider(int id) {
		super(id);
	}

	@Override
	public String getProviderName() {
		return "Filesystem";
	}

	@Override
	public String getBaseURL() {
		return null;
	}

	@Override
	public void aggiornaElencoSerie() {
		
	}

	@Override
	public int getProviderID() {
		return PROVIDER_FILESYSTEM;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

}
