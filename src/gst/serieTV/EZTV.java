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
		return "https://eztv.yt/";
	}

	@Override
	public void aggiornaElencoSerie() {
		update_in_corso=true;
		System.out.println("EZTV.it - Aggiornando elenco serie tv");

		String response = "";
		try {
			Response resp = api.getShowlist().execute();
			if (!resp.isSuccessful()) {
				throw new RuntimeException(resp.message());
			}
			response = resp.body().toString();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			update_in_corso=false;
			return;
		}

		Scanner file = null;
		ByteArrayInputStream memStream = null;
		int caricate = 0;
		try {
			memStream = new ByteArrayInputStream(response.getBytes(Charset.defaultCharset()));
			file = new Scanner(memStream);

			while (file.hasNextLine()) {
				String linea = file.nextLine().trim();
				if (linea.contains("\"thread_link\"")) {
					String nomeserie = linea.substring(linea.indexOf("class=\"thread_link\">") + "class=\"thread_link\">".length(), linea.indexOf("</a>")).trim();
					String url = linea.substring(linea.indexOf("<a href=\"") + "<a href=\"".length(), linea.indexOf("\" class=\"thread_link\">")).trim();
					url = url.replace(getBaseURL(), "");
					url = url.replace("/shows/", "");
					url = url.substring(0, url.indexOf("/"));
					String nextline = file.nextLine().trim();
					boolean conclusa = false;
					if (nextline.contains("ended"))
						conclusa = true;
					
					if(isTempPlaceholder(nomeserie))
						continue;
					
					SerieTV toInsert = new SerieTV(getProviderID(), nomeserie, url);
					toInsert.setConclusa(conclusa);
					toInsert.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
					toInsert.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
					if(aggiungiSerieADatabase(toInsert, PROVIDER_EZTV)){
						caricate++;
					}
				}
			}
			System.out.println("EZTV - aggiornamento elenco serie tv completo\nCaricate " + caricate + " nuove serie");
		}
		catch (Exception e) {
			ManagerException.registraEccezione(e);
		}
		finally {
			update_in_corso=false;
			if(file!=null)
				file.close();
			try {
				if(memStream!=null)
					memStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				ManagerException.registraEccezione(e);
			}
		}
		OperazioniFile.deleteFile(settings.getUserDir() + "file.html");
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
			
			Download download = new Download(base_url, settings.getUserDir() + serie.getNomeSerie());
			download.avviaDownload();
			download.getDownloadThread().join();

			FileReader fr = new FileReader(settings.getUserDir() + serie.getNomeSerie());
			Scanner file = new Scanner(fr);
			while (file.hasNextLine()) {
				String linea = file.nextLine();
				if (linea.contains("magnet:?xt=urn:btih:")) {
					int inizio = linea.indexOf("magnet:?xt=urn:btih:");
					int fine = linea.indexOf("\" class=\"magnet\"");
					String url_magnet = linea.substring(inizio, fine);
					if (url_magnet.length() > 0) {
						CaratteristicheFile stat = Torrent.parse(url_magnet);
						int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(serie.getIDDb(), stat.getStagione(), stat.getEpisodio());
						ProviderSerieTV.aggiungiLink(episodio_id, stat.value(), url_magnet);
					}
				}
			}
			file.close();
			fr.close();
			OperazioniFile.deleteFile(settings.getUserDir() + serie.getNomeSerie());

			if (serie.isConclusa()) {
				serie.setStopSearch(true);
			}
		}

		catch (InterruptedException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (IOException e) {
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
		Call<ResponseBody> getShowlist();
		@GET("shows/{id}/")
		Call<ResponseBody> getEpisodes(@Path("id") String id);
	}
}
