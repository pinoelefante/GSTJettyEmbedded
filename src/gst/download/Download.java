package gst.download;

import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map.Entry;

import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;

public class Download {
	private long d_corrente, d_finale;
	private String url_download, path_destinazione;
	private Thread download;
	private boolean complete=false, toStart=true, started=false;
	
	public Download(String url, String path){
		this.url_download=url;
		this.path_destinazione=path;
		download=new Downloader();
	}
	public Download(String url, String path, boolean toStart){
		this.url_download=url;
		this.path_destinazione=path;
		this.toStart=toStart;
		if(toStart)
			download=new Downloader();
		else
			complete=true;
	}
	/**
	 * 
	 * @return thread che si occupa dell'effettivo download del file
	 */
	public Thread getDownloadThread(){
		return download;
	}
	/**
	 * 
	 * @return grandezza in byte del file da scaricare/che si sta scaricando 
	 */
	public long getFileSize(){
		return d_finale;
	}
	/**
	 * 
	 * @return byte del file scaricati
	 */
	public long getFileSizeDowloaded(){
		return d_corrente;
	}
	/**
	 * avvia il thread di download
	 */
	public void avviaDownload(){
		if(download!=null)
			download.start();
	}
	/**
	 * interrompe il thread di download
	 */
	public void interrompiDownload(){
		download.interrupt();
		complete=true;
	}
	/**
	 * 
	 * @return l'url del file che si sta scaricando
	 */
	public String getUrlDownload(){
		return url_download;
	}
	/**
	 * 
	 * @return percorso in cui si salver� il file che si sta scaricando
	 */
	public String getPathDownload(){
		return path_destinazione;
	}
	/**
	 * 
	 * @return true se il download � completo
	 */
	public boolean isComplete(){
		return complete;
	}
	/**
	 * 
	 * @return false se il download del file non deve essere avviato
	 */
	public boolean isToStart(){
		return toStart;
	}
	
	/**
	 * La classe (thread) si occupa di scaricare effettivamente il file
	 * @author pino
	 *
	 */
	class Downloader extends Thread {
		private InputStream is = null;
		private FileOutputStream fos = null;
		/**
		 * chiude gli stream e cancella il file
		 */
		public void clean(){
			if(is!=null){
				try {
					is.close();
					is=null;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos!=null){
				try {
					fos.close();
					fos=null;
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			File f=new File(path_destinazione);
			f.delete();
		}
		/**
		 * in caso di interrupt si cancella il file che si stava scaricando
		 */
		public void interrupt(){
			super.interrupt();
			clean();
		}
		/**
		 * Download del file
		 */
		public void run(){
			String userAgent = "GestioneSerieTV/rel."+Settings.getInstance().getVersioneSoftware()+" ("+System.getProperty("os.name")+")";
			URL url = null;
			try {
				url = new URL(url_download);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			try {
				URLConnection urlConn = url.openConnection();
				urlConn.setConnectTimeout(300000);
				urlConn.setReadTimeout(300000);
				if (userAgent != null) {
					urlConn.setRequestProperty("User-Agent", userAgent);
				}
				is = urlConn.getInputStream();
				
				if(path_destinazione.contains(File.separator)){
					String path=path_destinazione.substring(0, path_destinazione.lastIndexOf(File.separator));
					File f=new File(path);
					f.mkdirs();
					if(!OperazioniFile.dirExists(path)){
						clean();
						return;
					}
				}
				
				fos = new FileOutputStream(path_destinazione);
				d_finale=urlConn.getContentLengthLong();

				byte[] buffer = new byte[32768]; //32KB
				int len;
				setStarted(true);
				while ((len = is.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
					d_corrente+=len;
				}
				buffer=null;
			} 
			catch (IOException e) {
				clean();
				e.printStackTrace();
			}
			catch (NullPointerException e){
				clean();
			}
			finally {
				try {
					if (is != null)
						is.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					if (fos != null) {
						try {
							fos.close();
							complete=true;
						} 
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	public static boolean downloadTorrent(SerieTV serie, Torrent torrent) throws DirectoryNotAvailableException{
		String baseDir = DirectoryManager.getInstance().getAvailableDirectory();
		
		String directory = baseDir+serie.getFolderSerie();
		BitTorrentClient client;
		try {
			client = Settings.getInstance().getClientTorrent();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return client.downloadTorrent(torrent, directory);
	}
	
	public static void downloadFromUrl(String url_download, String localFilename) throws IOException{
		Download download=new Download(url_download, localFilename);
		download.avviaDownload();
		try {
			download.getDownloadThread().join();
			if(!download.isComplete())
				throw new IOException("Download non completato");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Il download non � stato completato");
		}
	}
	
	public boolean isStarted() {
		return started;
	}
	private void setStarted(boolean started) {
		this.started = started;
	}
	private static boolean isHttpRaggiungibile(String url_s){
		HttpURLConnection urlConn=null;
		String userAgent = "GestioneSerieTV/rel."+Settings.getInstance().getVersioneSoftware()+" ("+System.getProperty("os.name")+")";
		try {
			URL url=new URL(url_s);
			urlConn=(HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(30000);
			urlConn.setReadTimeout(30000);
			if(userAgent!=null)
				urlConn.setRequestProperty("User-Agent", userAgent);
			int rc=urlConn.getResponseCode();
			System.out.println(rc+" - response code - "+url_s);
			if(rc==200)
				return true;
		}
		catch(IOException e){
			e.printStackTrace();
			return false;
		}
		finally {
			if(urlConn!=null)
				urlConn.disconnect();
		}
		return false;
	}
	public static boolean isRaggiungibile(String url){
		return isHttpRaggiungibile(url);
	}
	public static void downloadCustomHeaders(String url, String dest, ArrayList<Entry<String, String>> headers) throws IOException {
		
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			for(int i=0;headers!=null && i<headers.size();i++){
				Entry<String, String> p = headers.get(i);
				con.setRequestProperty(p.getKey(), p.getValue());
			}
			con.setRequestProperty("User-Agent", "gstJ/"+Settings.getInstance().getVersioneSoftware());
			InputStream is = con.getInputStream();
			
			if(dest.contains(File.separator)){
				String path=dest.substring(0, dest.lastIndexOf(File.separator));
				File f=new File(path);
				f.mkdirs();
			}
			
			FileOutputStream fos = new FileOutputStream(dest);

			byte[] buffer = new byte[32768]; //32KB
			int len;
			while ((len = is.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			buffer=null;
			fos.close();
			is.close();
		

	}
}
