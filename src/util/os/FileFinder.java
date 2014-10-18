package util.os;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;

import java.io.File;
import java.util.ArrayList;

public class FileFinder {
	private static FileFinder sing;
	private final static String[] estensioniVideoValide = {".avi",".mp4",".mkv"};
	private final static String[] estensioniSottotitoliValide = {".srt",".ass"};
	
	public static FileFinder getInstance(){
		if(sing==null)
			sing=new FileFinder();
		return sing;
	}
	public ArrayList<File> cercaFileVideo(SerieTV serie, Episodio ep){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		//System.out.println(pathBase);
		File dir = new File(pathBase);
		cercaFile(dir, fileTrovati, ep, estensioniVideoValide);
		return fileTrovati;
	}
	public ArrayList<File> cercaFileSottotitoli(SerieTV serie, Episodio ep){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		File dir = new File(pathBase);
		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		return fileTrovati;
	}
	public ArrayList<File> cercaFileSottotitoli(SerieTV serie, Episodio ep, String videoName){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		File dir = new File(pathBase);
		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		videoName=videoName.substring(0, videoName.lastIndexOf("."));
		for(int i=0;i<fileTrovati.size();){
			if(fileTrovati.get(i).getName().toLowerCase().startsWith(videoName.toLowerCase()))
				i++;
			else
				fileTrovati.remove(i);
		}
		return fileTrovati;
	}
	
	private void cercaFile(File dir, ArrayList<File> trovati,Episodio ep,  String[] estensioni){
		visitDir(dir, trovati, ep, estensioni);
	}
	private void visitDir(File dir, ArrayList<File> fileTrovati, Episodio ep, String[] estensioniValide){
		if(dir==null || !dir.isDirectory())
			return;
		File[] files=dir.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory())
				visitDir(files[i], fileTrovati, ep, estensioniValide);
			else {
				CaratteristicheFile stat=stats(files[i].getName());
				if(stat.getStagione()==ep.getStagione() && stat.getEpisodio()==ep.getEpisodio() && isFileExtensionValid(files[i].getName(), estensioniValide)){
					fileTrovati.add(files[i]);
				}
			}
		}
	}
	private boolean isFileExtensionValid(String filename, String[] estensioni){
		for(int i=0;i<estensioni.length;i++){
			if(filename.toLowerCase().endsWith(estensioni[i].toLowerCase()))
				return true;
		}
		return false;
	}
	private CaratteristicheFile stats(String file){
		String[] patt=new String[]{
				Naming.PATTERN_SnEn,
				Naming.PATTERN_SxE,
				Naming.PATTERN_Part_dotnofn,
				Naming.PATTERN_nofn
		};
		return Naming.parse(file, patt);
	}
}
