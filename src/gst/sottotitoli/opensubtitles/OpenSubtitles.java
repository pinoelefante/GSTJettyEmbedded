package gst.sottotitoli.opensubtitles;

import gst.download.Download;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import util.os.DirectoryManager;
import util.zip.ArchiviZip;

public class OpenSubtitles implements ProviderSottotitoli {
	private static OpenSubtitles instance;
	private OpenSubtitlesAPI api;
	private HashMap<String, String> lingue_disponibili;
	private Timer timer;
	private final static long timer_period = 12 * 60 *1000;
	
	public static OpenSubtitles getInstance(){
		if(instance == null)
			instance = new OpenSubtitles();
		return instance;
	}
	public OpenSubtitles() {
		api = new OpenSubtitlesAPI();
		api.logga();
		
		lingue_disponibili = new HashMap<>();
		lingue_disponibili.put(INGLESE, "eng");
		lingue_disponibili.put(ITALIANO, "ita");
		lingue_disponibili.put(FRANCESE, "fre");
		lingue_disponibili.put(PORTOGHESE, "por");
		lingue_disponibili.put(TEDESCO, "ger");
		lingue_disponibili.put(SPAGNOLO, "spa");
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				api.noOperation();
			}
		}, timer_period, timer_period);
	}
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		if(!hasLanguage(lang))
			return false;
		
		ArrayList<File> files = DirectoryManager.getInstance().cercaFileVideo(serie, ep);
		if(files.size()<=0)
			return false;
		
		boolean down = false;
		for(int i=0;i<files.size();i++){
			try {
				String filehash = OpenSubtitlesHasher.computeHash(files.get(i));
				int filesize = (int) files.get(i).length();
				String filename = files.get(i).getName();
				filename = filename.substring(0, filename.lastIndexOf("."));
				Map<String, String> results = api.searchSubtitles(filehash, filesize, lingue_disponibili.get(lang));
				if(results.isEmpty())
					return false;
				else {
					String url_download = null;
					url_download = results.get(filename)!=null?results.get(filename):results.values().iterator().next();
					String filezip = files.get(i).getParent()+File.separator+serie.getFolderSerie()+"_"+ep.getStagione()+"_"+ep.getEpisodio()+"_"+i+".zip";
					try {
						Download.downloadFromUrl(url_download, filezip);
						ArchiviZip.estrai_tutto(filezip, files.get(i).getParent());
						down = true;
					}
					catch(IOException e){}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(down){
			ep.setSubDownload(!down);
			GestoreSottotitoli.setSottotitoloDownload(ep.getId(), false, lang);
		}
		return down;
	}
	
	private final static ArrayList<SerieSub> elenco_serie = new ArrayList<SerieSub>(1); 
	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		return elenco_serie;
	}

	@Override
	public String getProviderName() {
		return "OpenSubtitles";
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.OPENSUBTITLES;
	}

	@Override
	public void associaSerie(SerieTV s) {}

	@Override
	public void aggiornaElencoSerieOnline() {}

	@Override
	public boolean associa(int idSerie, int idSub) {
		return true;
	}

	@Override
	public boolean disassocia(int idSerie) {
		return true;
	}

	@Override
	public boolean hasLanguage(String lang) {
		return lingue_disponibili.get(lang)!=null;
	}

}
