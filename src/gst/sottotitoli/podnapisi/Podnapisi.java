package gst.sottotitoli.podnapisi;

import gst.serietv.Episodio;
import gst.serietv.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.zip.ArchiviZip;

public class Podnapisi implements ProviderSottotitoli {
	private static Podnapisi instance;
	private Map<String, Integer> langs;
	private PodnapisiAPI api;
	private boolean init;
	
	public static Podnapisi getInstance(){
		if(instance == null)
			instance = new Podnapisi();
		return instance;
	}
	private Podnapisi() {
		api = new PodnapisiAPI();
		
	}
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang) {
		init();
		
		if(!hasLanguage(lang))
			return false;
		
//		ArrayList<File> files = DirectoryManager.getInstance().cercaFileVideo(serie, ep);
//		if(files.size()<=0)
//			return false;
//		
//		boolean down = false;
//		for(int i = 0;i < files.size();i++){
//			if(api.setFilters(langs.get(lang))){
//				CaratteristicheFile stat = Naming.parse(files.get(i).getName(), null);
//				ArrayList<String> subs = api.search(files.get(i), stat.is720p());
//				if(subs.size()>0){
//					String url = api.download(subs.get(0));
//					if(!url.isEmpty()){
//						String zip=files.get(i).getParent()+File.separator+serie.getFolderSerie()+"_"+stat.getStagione()+"_"+stat.getEpisodio()+"_"+i+".zip";
//						try {
//							Download.downloadFromUrl(url, zip);
//							ArchiviZip.estrai_tutto(zip, files.get(i).getParent());
//							down = true;
//						}
//						catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}
		return false;
	}

	@Override
	public ArrayList<SerieSub> getElencoSerie() {
		return new ArrayList<SerieSub>();
	}

	@Override
	public String getProviderName() {
		return "Podnapisi.net";
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.PODNAPISI;
	}

	@Override
	public void associaSerie(SerieTV s) {}

	@Override
	public void aggiornaElencoSerieOnline() {}

	@Override
	public boolean associa(int idSerie, int idSub) {
		return false;
	}

	@Override
	public boolean disassocia(int idSerie) {
		return false;
	}

	@Override
	public boolean hasLanguage(String lang) {
		init();
		if(langs == null) {
			langs = new HashMap<String, Integer>();
			List<Entry<String, Integer>> ls = api.getSupportedLanguages();
			for(Entry<String, Integer> e : ls){
				langs.put(e.getKey(), e.getValue());
			}
		}
		return langs.containsKey(lang);
	}
	private void init(){
		if(!init){
    		api.initiate();
    		// api.authenticate(settings.getPodnapisiUsername(), settings.getPodnapisiPassword());
    		init = true;
		}
	}
}
