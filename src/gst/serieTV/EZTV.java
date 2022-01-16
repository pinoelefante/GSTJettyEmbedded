package gst.serieTV;

import gst.database.Database;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import util.UserAgent;
import util.httpOperations.HttpOperations;

public class EZTV extends ProviderSerieTV {
	private EZTVApi api;
	private Settings settings;
	public EZTV() {
		super(ProviderSerieTV.PROVIDER_EZTV);

		settings=Settings.getInstance();

		OkHttpClient client = new OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS)
				.connectTimeout(30, TimeUnit.SECONDS)
				.followRedirects(true)
				.followSslRedirects(true)
				.readTimeout(30, TimeUnit.SECONDS)
				.build();
		Retrofit retrofit = new Retrofit.Builder().baseUrl(getBaseURL())
				.client(client)
				.build();
		api = retrofit.create(EZTVApi.class);
	}

	public String getProviderName() {
		return "eztv.it";
	}

	public String getBaseURL() {
		return "https://eztv.re/";
	}

	@Override
	public void aggiornaElencoSerie() {
		update_in_corso=true;
		System.out.println("EZTV.re - Aggiornando elenco serie tv");
		AtomicInteger caricate = new AtomicInteger(0);
		try
		{
			var document = Jsoup.parse(new URL(getBaseURL() + "showlist/"), 30000);
			var links = document.tagName("a").getElementsByAttributeValue("class", "thread_link");
			links.forEach(l -> {
				var url = l.attr("href").replace(getBaseURL(), "").replace("/shows/", "");
				url = url.substring(0,  url.indexOf("/"));
				var nomeserie = l.text().trim();
				if (isTempPlaceholder(nomeserie))
					return;
				SerieTV toInsert = new SerieTV(getProviderID(), nomeserie, url);
				toInsert.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
				toInsert.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
				if(aggiungiSerieADatabase(toInsert, PROVIDER_EZTV)){
					caricate.addAndGet(1);
				}
			});
		}
		catch (IOException e2)
		{
			ManagerException.registraEccezione(e2);
			e2.printStackTrace();
		}
		finally {
			update_in_corso = false;
			System.out.println("EZTV - aggiornamento elenco serie tv completo\nCaricate " + caricate.get() + " nuove serie");
		}
	}
	private String[] temp_patterns = {"^temp[_]{0,1}[\\d]*$", "^t[\\d]*$", "/^temporary_placeholder_[0-9]$/", "test", "z_blank"};
	public boolean isTempPlaceholder(String nome){
		for(int i=0;i<temp_patterns.length;i++){
			if(nome.replaceAll(temp_patterns[i], "").isEmpty())
				return true;
		}
		return false;
	}

	@Override
	public int getProviderID() {
		return PROVIDER_EZTV;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie) {
		if (serie.isStopSearch())
			return;
		System.out.println("Aggiornando i link di: " + serie.getNomeSerie());

		try {
			String base_url = getBaseURL();
			base_url += "/shows/" + serie.getUrl() + "/";
			
			var htmlDocument = Jsoup.parse(new URL(base_url), 30000);
			var magnets = htmlDocument.getElementsByAttributeValueStarting("href", "magnet:?xt=urn:btih:");
			magnets.forEach(m -> {
				var link = m.attr("href");
				if (link != null && link.length() > 0) {
					CaratteristicheFile stat = Torrent.parse(link);
					int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(serie.getIDDb(), stat.getStagione(), stat.getEpisodio());
					ProviderSerieTV.aggiungiLink(episodio_id, stat.value(), link);
				}
			});
			serie.setConclusa(isEnded(serie));
			if (serie.isConclusa()) {
				serie.setStopSearch(true);
				setConclusa(serie);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}

	private void cleanUpTemp() {
		ArrayList<SerieTV> list = GestioneSerieTV.getInstance().getSerieFromProvider(getProviderID());
		for(int i=0;i<list.size();i++){
			if(isTempPlaceholder(list.get(i).getNomeSerie())){
				String query = "DELETE FROM "+Database.TABLE_SERIETV+" WHERE id=?";
				Database.updateQuery(query,list.get(i).getIDDb());
			}
		}
	}

	@Override
	public void init() {
		cleanUpTemp();
	}


	interface EZTVApi {
		@GET("showlist/")
		Call<String> getShowlist();
		@GET("shows/{id}/")
		Call<String> getEpisodes(@Path("id") String id);
	}
}
