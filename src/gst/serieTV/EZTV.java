package gst.serieTV;

import gst.database.Database;
import gst.database.tda.KVResult;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class EZTV extends ProviderSerieTV {
	private ArrayList<String> baseUrls;
	private String			baseUrl;
	private Settings settings;

	public EZTV() {
		super(ProviderSerieTV.PROVIDER_EZTV);
		baseUrls = new ArrayList<String>();
		baseUrls.add("https://eztv.it");
		
		/*
		baseUrls.add("http://gestioneserietv.altervista.org/proxy.php?url=https://eztv.it");
		baseUrls.add("http://tvshowsmanager.hostei.com/?url=https://eztv.it");
		
		baseUrls.add("http://sitenable.com/surf.php?u=https://eztv.it");
		baseUrls.add("http://freeproxy.io/surf.php?u=https://eztv.it");
		baseUrls.add("http://siteget.net/surf.php?u=https://eztv.it");
		baseUrls.add("http://filesdownloader.com/surf.php?u=https://eztv.it");
		baseUrls.add("http://freeanimesonline.com/surf.php?u=https://eztv.it");
		*/
		settings=Settings.getInstance();
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

		Download downloader = new Download(base_url + "/showlist/", settings.getUserDir() + "file.html");
		System.out.println("path download: "+settings.getUserDir() + "file.html");
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
			f_r = new FileReader(settings.getUserDir() + "file.html");
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
					
					SerieTV toInsert = new SerieTV(getProviderID(), nomeserie, url);
					toInsert.setConclusa(conclusa);
					toInsert.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
					toInsert.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
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
			if(file!=null)
				file.close();
			try {
				if(f_r!=null)
					f_r.close();
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
		ArrayList<SerieTV> list = GestioneSerieTV.getInstance().getSerieFromProvider(getProviderID());
		for(int i=0;i<list.size();i++){
			if(isTempPlaceholder(list.get(i).getNomeSerie())){
				String query = "DELETE FROM "+Database.TABLE_SERIETV+" WHERE id="+list.get(i).getIDDb();
				Database.updateQuery(query);
			}
		}
	}
	public void caricaListaProxy(){
		String urlListProxy = "http://pinoelefante.altervista.org/software/GSTJetty/proxy.txt";
		URLConnection urlConn;
		try {
			urlConn = new URL(urlListProxy).openConnection();
			InputStream is = urlConn.getInputStream();
			String list="";
			int read=0;
			byte[] buffer=new byte[256];
			while((read=is.read(buffer))>0){
				list+=new String(buffer).substring(0, read);
			}
			is.close();
			
			System.out.println(list);
			String[] proxies = list.split("\n");
			for(int i=0;i<proxies.length;i++){
				baseUrls.add(proxies[i]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		cleanUpTemp();
		caricaListaProxy();
		baseUrl = getOnlineUrl();
		System.out.println("Base URL in uso: " + baseUrl);
	}
}