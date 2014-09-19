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
	
	@Override
	public boolean scaricaSottotitolo(SerieTV serie, Episodio ep) {
		ArrayList<File> subs = FileFinder.getInstance().cercaFileSottotitoli(serie, ep);
		if(subs.size()==0)
			return false;
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
						String newFile = video.getParent()+File.separator+video.getName().substring(0, video.getName().lastIndexOf("."))+(subCount==0?"":subCount)+".srt";
						File f = new File(newFile);
						if(f.exists()){
							subCount++;
						}
						else{
							sub.renameTo(new File(newFile));
							renamed=true;
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
}