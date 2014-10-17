package gst.sottotitoli.localhost;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.player.FileFinder;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

import java.io.File;
import java.util.ArrayList;

public class LocalSubs implements ProviderSottotitoli{
	private static LocalSubs instance;
	
	public static LocalSubs getInstance(){
		if(instance==null){
			instance=new LocalSubs();
		}
		return instance;
	}
	private LocalSubs(){}
	
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang){
		return scaricaSottotitolo(serie, ep, lang, false);
	}
	
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep, String lang, boolean unique) {
		ArrayList<File> subs = FileFinder.getInstance().cercaFileSottotitoli(serie, ep);
		if(subs.size()==0)
			return false;
		
		//Controlla che il sottotitolo non sia associato ad un'altra lingua
		String[] langs=serie.getPreferenzeSottotitoli().getPreferenze();
		for(int i=0;i<subs.size();){
			File f = subs.get(i);
			boolean skip = false;
			for(int j=0;j<langs.length;j++){
    			String l = langs[j];
    			if(l.compareTo(lang)==0)
    				continue;
    			if(f.getName().toLowerCase().endsWith((l+".srt").toLowerCase())){
    				skip = true;
    				break;
    			}
			}
			if(skip){
				subs.remove(i);
			}
			else
				i++;
		}
		if(subs.size()==0)
			return false;
		//Fine controllo
		
		ArrayList<File> videos = FileFinder.getInstance().cercaFileVideo(serie, ep);
		boolean renamed=false;
		for(int i=0;i<videos.size();i++){
			File video = videos.get(i);
			CaratteristicheFile videoStat = Naming.parse(video.getName(), null);
			int subCount = 0;
			for(int j=0;j<subs.size();j++){
				File sub = subs.get(j);
				CaratteristicheFile subStats=Naming.parse(sub.getName(), null);
				if(videoStat.is720p()==subStats.is720p()){	
					do{
						String newFile = video.getParent()+File.separator+video.getName().substring(0, video.getName().lastIndexOf("."))+(subCount==0?"":"."+subCount)+(unique?"":"."+lang)+".srt";
						File f = new File(newFile);
						if(f.exists()){
							subCount++;
						}
						else{
							sub.renameTo(new File(newFile));
							renamed=true;
							GestoreSottotitoli.setSottotitoloDownload(ep.getId(), false, lang);
							break;
						}
					}
					while(true);
				}
			}
		}
		return renamed;
	}
	public ArrayList<SerieSub> getElencoSerie() {return null;}
	public String getProviderName() { return "Offline";}
	public void associaSerie(SerieTV s) {}
	public void aggiornaElencoSerieOnline() {}
	@Override
	public int getProviderID() {
		return GestoreSottotitoli.LOCALE;
	}
	@Override
	public boolean associa(int idSerie, int idSub) {return false;}
	@Override
	public boolean disassocia(int idSerie) {return false;}
	@Override
	public boolean hasLanguage(String lang) {
		// TODO Auto-generated method stub
		return false;
	}
}