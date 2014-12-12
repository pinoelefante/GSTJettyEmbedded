package gst.serieTV;

import gst.database.Database;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EZTV extends ProviderSerieTV {
	private ArrayList<String> baseUrls;
	private String			baseUrl;
	private Settings settings;

	public EZTV() {
		super(ProviderSerieTV.PROVIDER_EZTV);
		baseUrls = new ArrayList<String>();
		baseUrls.add("http://gestioneserietv.altervista.org/proxy_v2/proxy.php?url=https://eztv.it");
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
			if (Download.isRaggiungibile(url_b)){
				lastVerificaRaggiungibile = System.currentTimeMillis();
				return url_b;
			}
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
		if(!isRaggiungibile())
			return;
		
		if(getBaseURL().startsWith("http://gestioneserietv.altervista.org/proxy_v2/proxy.php?url=")){
			proxyShowlist(getBaseURL()+"/showlist/");
			return;
		}
		//VIENE ESEGUITO SOLO SE NON VIENE UTILIZZATO IL PROXY
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
					if(aggiungiSerieADatabase(toInsert, PROVIDER_EZTV)){
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

	@Override
	public int getProviderID() {
		return PROVIDER_EZTV;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie) {
		if(!isRaggiungibile())
			return;
		
		if (serie.isStopSearch())
			return;
		System.out.println("Aggiornando i link di: " + serie.getNomeSerie());
		
		if(getBaseURL().startsWith("http://gestioneserietv.altervista.org/proxy_v2/proxy.php?url=")){
			proxyEpisodiSerie(getBaseURL()+"/shows/" + serie.getUrl() + "/", serie.getIDDb());
			return;
		}
		//VIENE ESEGUITO SOLO SE NON VIENE UTILIZZATO IL PROXY
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
				String query = "DELETE FROM "+Database.TABLE_SERIETV+" WHERE id="+list.get(i).getIDDb();
				Database.updateQuery(query);
			}
		}
	}
	public void caricaListaProxy(){
		String urlListProxy = "http://gestioneserietv.altervista.org/proxyList.txt";
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
	private long lastVerificaRaggiungibile;
	private boolean raggiungibile;
	private boolean isRaggiungibile(){
		if(System.currentTimeMillis() > lastVerificaRaggiungibile+120000){ //controllo dopo due minuti
			for(int i=0;i<baseUrls.size();i++){
				if(Download.isRaggiungibile(baseUrls.get(i))){
					raggiungibile = true;
					baseUrl = baseUrls.get(i);
					break;
				}
				else
					raggiungibile = false;
			}
			lastVerificaRaggiungibile = System.currentTimeMillis();
		}
		return raggiungibile;
	}
	private void proxyShowlist(String url){
		int nuove = 0;
		try {
			Document d = Jsoup.connect(url).get();
			//System.out.println(d.text());
			ArrayList<String> series=proxyShowListParsed(d.text());
			for(int i=0;i<series.size();i=i+2){
				String nome = series.get(i);
				if(i+1>=series.size())
					break;
				String u = series.get(i+1);
				u = u.replace("/shows/", "");
				u = u.substring(0, u.indexOf("/"));
				if(!nome.isEmpty() && !u.isEmpty()){
					SerieTV toInsert = new SerieTV(PROVIDER_EZTV, nome, u);
					toInsert.setPreferenze(new Preferenze(settings.getRegolaDownloadDefault()));
					toInsert.setPreferenzeSottotitoli(new PreferenzeSottotitoli(settings.getLingua()));
					if(aggiungiSerieADatabase(toInsert, PROVIDER_EZTV)){
						nuove++;
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sono state trovate "+nuove+" nuove serie tv");
	}
	private ArrayList<String> proxyShowListParsed(String text) {
		ArrayList<String> s = new ArrayList<String>();
		Scanner scanner = new Scanner(text);
		String nome="", url="";
		while(scanner.hasNext()){
			String tmp = scanner.next();
			if(tmp.startsWith("/")){
				url=tmp;
				if(!nome.trim().isEmpty() && !url.isEmpty()){
    				s.add(nome.trim());
    				s.add(url);
				}
				nome="";
				url="";
			}
			else {
				nome+=" "+tmp;
			}
		}
		scanner.close();
		return s;
	}
	private void proxyEpisodiSerie(String url, int idSerie){
		try {
			Document d = Jsoup.connect(url).get();
			String[] magnets = proxyParseMagnets(d.text());
			for(int i=0;i<magnets.length;i++){
				if(!magnets[i].isEmpty()){
					System.out.println(magnets[i]);
					CaratteristicheFile stat = Torrent.parse(magnets[i]);
	    			int episodio_id = ProviderSerieTV.aggiungiEpisodioSerie(idSerie, stat.getStagione(), stat.getEpisodio());
	    			ProviderSerieTV.aggiungiLink(episodio_id, stat.value(), magnets[i]);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String[] proxyParseMagnets(String text){
		String[] magnet = text.split("magnet:");
		for(int i=0;i<magnet.length;i++){
			if(!magnet[i].isEmpty())
				magnet[i]="magnet:"+magnet[i];
		}
		return magnet;
	}
}