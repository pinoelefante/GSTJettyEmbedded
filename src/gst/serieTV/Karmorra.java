package gst.serieTV;

public class Karmorra extends ProviderSerieTV {

	public Karmorra(int id) {
		super(PROVIDER_KARMORRA);
	}

	@Override
	public String getProviderName() {
		return "Karmorra";
	}

	@Override
	public String getBaseURL() {
		return "http://showrss.karmorra.info";
	}

	@Override
	public void aggiornaElencoSerie() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getProviderID() {
		return PROVIDER_KARMORRA;
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
