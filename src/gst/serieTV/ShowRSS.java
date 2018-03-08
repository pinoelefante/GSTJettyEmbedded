package gst.serieTV;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.naming.CaratteristicheFile;
import gst.programma.Settings;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.UserAgent;

public class ShowRSS extends ProviderSerieTV {
	private Settings settings;
	public ShowRSS() {
		super(PROVIDER_SHOWRSS);
		settings = Settings.getInstance();
	}

	@Override
	public String getProviderName() {
		return "showRss.info";
	}

	@Override
	public String getBaseURL() {
		return "http://showrss.info - old";
	}

	@Override
	public void aggiornaElencoSerie() {
		/*
		try {
			Document doc = Jsoup.connect(getBaseURL()+"/?cs=browse")
					.header("User-Agent", UserAgent.get())
					.timeout(10000)
					.get();
			Elements form = doc.select("select#browse_show option");
			int caricate = 0;
			for(int i=0;i<form.size();i++){
				Element opt = form.get(i);
				String val = opt.val();
				String nome = opt.text();
				if(!val.isEmpty() && !nome.isEmpty()){
					SerieTV serie = new SerieTV(getProviderID(), nome, val);
					serie.setConclusa(false);
					serie.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
					serie.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
					if(aggiungiSerieADatabase(serie, PROVIDER_SHOWRSS)){
						serie = getSerieByURL(serie.getUrl(), PROVIDER_SHOWRSS);
						if(associaEztv(serie)){
							nuove_serie.remove(serie);
						}
						else
							caricate++;
					}
				}
			}
			System.out.println("showrss.info: "+caricate+" serie nuove");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	private boolean associaEztv(SerieTV s)
	{
		/*
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE provider="+PROVIDER_EZTV+" AND lower(nome)=\""+s.getNomeSerie().toLowerCase()+"\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()==1){
			KVResult<String, Object> resEztv = res.get(0);
			int ideztv = (int) resEztv.getValueByKey("id");
			String query_associa = "UPDATE "+Database.TABLE_SERIETV+" SET id_karmorra="+s.getIDDb()+" WHERE id="+ideztv;
			return Database.updateQuery(query_associa);
		}
		*/
		return false;
	}
	public int associaToEztv(SerieTV s){
		String query = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE provider="+PROVIDER_EZTV+" AND lower(nome)=\""+s.getNomeSerie().toLowerCase()+"\"";
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		if(res.size()==1){
			KVResult<String, Object> resEztv = res.get(0);
			int ideztv = (int) resEztv.getValueByKey("id");
			String query_associa = "UPDATE "+Database.TABLE_SERIETV+" SET id_karmorra=? WHERE id=?";
			Database.updateQuery(query_associa, s.getIDDb(), ideztv);
			return ideztv;
		}
		return -1;
	}
	
	@Override
	protected ArrayList<SerieTV> getElencoSerieDB() {
		ArrayList<SerieTV> karmorra=new ArrayList<SerieTV>();
		String select = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE provider="+PROVIDER_SHOWRSS;
		ArrayList<KVResult<String, Object>> r = Database.selectQuery(select);
		for(int i=0;i<r.size();i++){
			karmorra.add(parseSerie(r.get(i)));
		}
		String eztv = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE provider="+PROVIDER_EZTV+" AND id_karmorra>0";
		ArrayList<KVResult<String, Object>> r2 = Database.selectQuery(eztv);
		for(int i=0;i<r2.size();i++){
			SerieTV s=parseSerie(r2.get(i));
			for(int j=0;j<karmorra.size();j++){
				if(karmorra.get(j).getIDDb()==s.getIDKarmorra()){
					karmorra.remove(j);
					break;
				}
			}
		}
		return karmorra;
	}
	
	public ArrayList<SerieTV> getElencoSerieCompleto(){
		ArrayList<SerieTV> karmorra=new ArrayList<SerieTV>();
		String select = "SELECT * FROM "+Database.TABLE_SERIETV+" WHERE provider="+PROVIDER_SHOWRSS;
		ArrayList<KVResult<String, Object>> r = Database.selectQuery(select);
		for(int i=0;i<r.size();i++){
			karmorra.add(parseSerie(r.get(i)));
		}
		return karmorra;
	}

	@Override
	public int getProviderID() {
		return PROVIDER_SHOWRSS;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie) {
		/*
		try {
			int idSerie = serie.getIDDb();
			SerieTV k_serie = serie.getIDKarmorra()==0 ? serie : ProviderSerieTV.getSerieByID(serie.getIDKarmorra());
			Document doc = Jsoup.connect(getBaseURL()+"/?cs=browse&show="+k_serie.getUrl())
					.header("User-Agent", UserAgent.get())
					.timeout(10000)
					.get();
			Elements magnets = doc.select("a");
			for(int i=0;i<magnets.size();i++){
				String url = magnets.get(i).attr("href");
				if(url.startsWith("magnet:")){
					CaratteristicheFile stat = Torrent.parse(url);
					int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(idSerie, stat.getStagione(), stat.getEpisodio());
					ProviderSerieTV.aggiungiLink(episodio_id, stat.value(), url);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
}
