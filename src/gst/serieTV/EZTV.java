package gst.serieTV;

import gst.database.Database;
import gst.naming.CaratteristicheFile;
import gst.programma.Download;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.tda.db.KVResult;
import gst.tda.serietv.Episodio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EZTV extends ProviderSerieTV {
	private ArrayList<String> baseUrls;
	private String			baseUrl;

	public EZTV() {
		super(ProviderSerieTV.PROVIDER_EZTV);
		//cleanUpTemp();
		baseUrls = new ArrayList<String>();
		baseUrls.add("http://eztv.it");
		baseUrls.add("http://eztv.openinternet.biz");
		baseUrl = getOnlineUrl();
		System.out.println("Base URL in uso: " + baseUrl);
		aggiornaElencoSerie(); //TODO remove
	}

	private String getOnlineUrl() {
		for (int i = 0; i < baseUrls.size(); i++) {
			String url_b = baseUrls.get(i);
			System.out.println("Verificando: " + url_b);
			if (Download.isRaggiungibile(url_b))
				return url_b;
		}
		return baseUrls.get(0);
	}

	public String getProviderName() {
		return "eztv.it";
	}

	public String getBaseURL() {
		return baseUrl;
	}

	@Override
	public void aggiornaElencoSerie() {
		update_in_corso=true;
		System.out.println("EZTV.it - Aggiornando elenco serie tv");
		String base_url = getBaseURL();

		Download downloader = new Download(base_url + "/showlist/", Settings.getUserDir() + "file.html");
		System.out.println("path download: "+Settings.getUserDir() + "file.html");
		downloader.avviaDownload();
		try {
			downloader.getDownloadThread().join();
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
			update_in_corso=false;
			return;
		}
		
		FileReader f_r = null;
		Scanner file = null;
		int caricate = 0;
		try {
			f_r = new FileReader(Settings.getUserDir() + "file.html");
			file = new Scanner(f_r);

			while (file.hasNextLine()) {
				String linea = file.nextLine().trim();
				if (linea.contains("\"thread_link\"")) {
					String nomeserie = linea.substring(linea.indexOf("class=\"thread_link\">") + "class=\"thread_link\">".length(), linea.indexOf("</a>")).trim();
					String url = linea.substring(linea.indexOf("<a href=\"") + "<a href=\"".length(), linea.indexOf("\" class=\"thread_link\">")).trim();
					url = url.replace(base_url, "");
					url = url.replace("/shows/", "");
					url = url.substring(0, url.indexOf("/"));
					String nextline = file.nextLine().trim();
					boolean conclusa = false;
					if (nextline.contains("ended"))
						conclusa = true;
					
					if(isTempPlaceholder(nomeserie))
						continue;
					
					SerieTV toInsert = new SerieTV(this, nomeserie, url);
					toInsert.setConclusa(conclusa);
					
					if(aggiungiSerieADatabase(toInsert)){
						caricate++;
					}
				}
			}
			System.out.println("EZTV - aggiornamento elenco serie tv completo\nCaricate " + caricate + " nuove serie");
		}
		catch (FileNotFoundException e) {
			ManagerException.registraEccezione(e);
		}
		finally {
			update_in_corso=false;
			file.close();
			try {
				f_r.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				ManagerException.registraEccezione(e);
			}
		}
		OperazioniFile.deleteFile(Settings.getUserDir() + "file.html");
	}
	private boolean isTempPlaceholder(String nome){
		switch (nome.toLowerCase()) {
			case "t1":
			case "t2":
			case "t3":
			case "t4":
			case "t5":
			case "t6":
			case "t7":
			case "t8":
			case "t9":
			case "temp1":
			case "temp2":
			case "temp3":
			case "temp4":
			case "temp5":
			case "temp6":
			case "temp7":
			case "temp8":
			case "temp9":
			case "temporary_tlaceholder":
			case "temporary_tlaceholder_2":
			case "temp 01":
			case "temp 02":
			case "temp 03":
			case "temp 04":
			case "temp_01":
			case "temp_02":
			case "temp_03":
			case "temp_04":
			case "temp01":
			case "temp02":
			case "temp03":
			case "temp04":
				return true;
		}
		return false;
	}

	@Override
	public ArrayList<Episodio> nuoviEpisodi(SerieTV serie) {
		ArrayList<Episodio> res = new ArrayList<Episodio>();
		for (int i = 0; i < serie.getNumEpisodi(); i++) {
			Episodio e = serie.getEpisodio(i);
			if (!e.isScaricato()) {
				res.add(e);
			}
		}
		res.trimToSize();
		return res;
	}

	@Override
	protected void salvaSerieInDB(SerieTV s) {
		if (s.getIDDb() == 0) {
			String query = "INSERT INTO " + Database.TABLE_SERIETV + " (nome, url, conclusa, stop_search, provider, id_itasa, id_subsfactory, id_subspedia, id_tvdb, preferenze_download) VALUES (" + "\"" + s.getNomeSerie() + "\", " + "\"" + s.getUrl() + "\"," + (s.isConclusa() ? 1 : 0) + "," + (s.isStopSearch() ? 1 : 0) + "," + getProviderID() + "," + s.getIDItasa() + "," + s.getIDDBSubsfactory() + "," + s.getIDSubspedia() + "," + s.getIDTvdb() + "," + s.getPreferenze().toValue() + ")";
			Database.updateQuery(query);

			String query_id = "SELECT id FROM " + Database.TABLE_SERIETV + " WHERE url=\"" + s.getUrl() + "\"";
			ArrayList<KVResult<String, Object>> res = Database.selectQuery(query_id);
			if (res.size() == 1) {
				KVResult<String, Object> row = res.get(0);
				int id_db = (int) row.getValueByKey("id");
				s.setIDDb(id_db);
			}
		}
		else {
			String query = "UPDATE " + Database.TABLE_SERIETV + " SET " + "nome=" + "\"" + s.getNomeSerie() + "\"" + ", url=" + "\"" + s.getUrl() + "\"" + ", conclusa=" + (s.isConclusa() ? 1 : 0) + ", stop_search=" + (s.isStopSearch() ? 1 : 0) + ", id_itasa=" + s.getIDItasa() + ", id_subsfactory=" + s.getIDDBSubsfactory() + ", id_subspedia=" + s.getIDSubspedia() + ", id_tvdb=" + s.getIDTvdb() + ", preferenze_download=" + s.getPreferenze().toValue() + " WHERE id=" + s.getIDDb();
			Database.updateQuery(query);
		}

	}

	@Override
	public void caricaEpisodiDB(SerieTV serie) {
		String query = "SELECT * FROM " + Database.TABLE_EPISODI + " WHERE id_serie=" + serie.getIDDb();
		ArrayList<KVResult<String, Object>> res = Database.selectQuery(query);
		for (int i = 0; i < res.size(); i++) {
			KVResult<String, Object> r = res.get(i);
			// (id,
			// id_serie,url,vista,stagione,episodio,tags,preair,sottotitolo,id_tvdb_ep)
			int id = (int) r.getValueByKey("id");
			String url = (String) r.getValueByKey("url");
			int stato = (int) r.getValueByKey("vista");
			int stagione = (int) r.getValueByKey("stagione");
			int episodio = (int) r.getValueByKey("episodio");
			int tags = (int) r.getValueByKey("tags");
			boolean preair = ((int) r.getValueByKey("preair")) == 1 ? true : false;
			boolean sub = ((int) r.getValueByKey("sottotitolo")) == 1 ? true : false;
			int id_tvdb = (int) r.getValueByKey("id_tvdb_ep");
			CaratteristicheFile stat = new CaratteristicheFile();
			stat.setStatsFromValue(tags);
			Torrent t = new Torrent(serie, url, stato, stat);
			t.setStagione(stagione);
			t.setEpisodio(episodio);
			t.setIDDB(id);
			t.setPreair(preair);
			t.setSubDownload(sub);
			t.setIDTVDB(id_tvdb);
			serie.addEpisodioDB(t);
			// System.out.println(t);
		}
	}

	@Override
	protected void salvaEpisodioInDB(Torrent t) {
		if (t.getIDDB() == 0) {
			String query_iddb = "SELECT id FROM " + Database.TABLE_EPISODI + " WHERE url=\"" + t.getUrl() + "\"";
			ArrayList<KVResult<String, Object>> res = Database.selectQuery(query_iddb);
			if (res.size() == 1) {
				int id = (int) res.get(0).getValueByKey("id");
				t.setIDDB(id);
			}
		}
		if (t.getIDDB() == 0) {
			String query = "INSERT INTO " + Database.TABLE_EPISODI + " (id_serie,url,vista,stagione,episodio,tags,preair,sottotitolo,id_tvdb_ep) VALUES (" + t.getSerieTV().getIDDb() + "," + "\"" + t.getUrl() + "\"," + t.getScaricato() + "," + t.getStagione() + "," + t.getEpisodio() + "," + t.getStats().value() + "," + (t.isPreAir() ? 1 : 0) + "," + (t.isSottotitolo() ? 1 : 0) + "," + t.getIDTVDB() + ")";
			// System.out.println(query);
			Database.updateQuery(query);
		}
		else {
			String query = "UPDATE " + Database.TABLE_EPISODI + " SET id_serie=" + t.getSerieTV().getIDDb() + ", url=\"" + t.getUrl() + "\", vista=" + t.getScaricato() + ", stagione=" + t.getStagione() + "," + "episodio=" + t.getEpisodio() + ", tags=" + t.getStats().value() + ", preair=" + (t.isPreAir() ? 1 : 0) + ", sottotitolo=" + (t.isSottotitolo() ? 1 : 0) + ", id_tvdb_ep=" + t.getIDTVDB() + " " + "WHERE id=" + t.getIDDB();
			// System.out.println(query);
			Database.updateQuery(query);
		}
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
			
			Download download = new Download(base_url, Settings.getUserDir() + serie.getNomeSerie());
			download.avviaDownload();
			download.getDownloadThread().join();

			FileReader fr = new FileReader(Settings.getUserDir() + serie.getNomeSerie());
			Scanner file = new Scanner(fr);
			while (file.hasNextLine()) {
				String linea = file.nextLine();
				if (linea.contains("magnet:?xt=urn:btih:")) {
					int inizio = linea.indexOf("magnet:?xt=urn:btih:");
					int fine = linea.indexOf("\" class=\"magnet\"");
					String url_magnet = linea.substring(inizio, fine);
					// System.out.println(url_magnet);
					if (url_magnet.length() > 0) {
						Torrent t = new Torrent(serie, url_magnet, Torrent.SCARICARE);
						t.parseMagnet();
						serie.addEpisodio(t);
					}
				}
			}
			file.close();
			fr.close();
			OperazioniFile.deleteFile(Settings.getUserDir() + serie.getNomeSerie());
			// }

			if (serie.isConclusa()) {
				serie.setStopSearch(true);
			}
		}

		catch (InterruptedException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}

	private void cleanUpTemp() {
		String[] query = { "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T1\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T2\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T3\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T4\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T5\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T6\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T7\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T8\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"T9\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp1\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp2\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp3\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp4\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp5\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp6\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp7\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp8\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp9\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temporary_Placeholder\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temporary_Placeholder_2\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp01\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp02\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp03\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp04\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp 01\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp 02\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp 03\"", "DELETE FROM " + Database.TABLE_SERIETV + " WHERE nome=\"Temp 04\"" };
		for (int j = 0; j < query.length; j++)
			Database.updateQuery(query[j]);
	}
}