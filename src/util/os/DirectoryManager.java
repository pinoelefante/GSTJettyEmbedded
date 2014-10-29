package util.os;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;

import java.io.File;
import java.util.ArrayList;

public class DirectoryManager {
	private static DirectoryManager instance;
	private Settings settings;
	
	public static DirectoryManager getInstance(){
		if(instance==null)
			instance=new DirectoryManager();
		return instance;
	}
	private DirectoryManager(){
		settings = Settings.getInstance();
	}
	public boolean isMainDirAvailable(){
		String path = settings.getDirectoryDownload();
		File dir = new File(path);
		return (dir.exists() && getMegabytes(dir.getFreeSpace())>=settings.getMinFreeSpace());
	}
	public boolean isAlternativeDirAvailable(){
		String path = settings.getDirectoryDownload2();
		File dir = new File(path);
		return (dir.exists() && getMegabytes(dir.getFreeSpace())>=settings.getMinFreeSpace());
	}
	
	private final static String[] estensioniVideoValide = {".avi",".mp4",".mkv"};
	private final static String[] estensioniSottotitoliValide = {".srt",".ass"};
	
	public ArrayList<File> cercaFileVideo(SerieTV serie, Episodio ep){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		String altDir=Settings.getInstance().getDirectoryDownload2()+serie.getFolderSerie();
		
		if(isMainDirAvailable()){
    		File dir = new File(pathBase);
    		cercaFile(dir, fileTrovati, ep, estensioniVideoValide);
		}
		if(isAlternativeDirAvailable()){
			File dir = new File(altDir);
    		cercaFile(dir, fileTrovati, ep, estensioniVideoValide);
		}
		return fileTrovati;
	}
	public ArrayList<File> cercaFileSottotitoli(SerieTV serie, Episodio ep){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		String altDir=Settings.getInstance().getDirectoryDownload2()+serie.getFolderSerie();
		
		if(isMainDirAvailable()){
    		File dir = new File(pathBase);
    		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		}
		if(isAlternativeDirAvailable()){
			File dir = new File(altDir);
    		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		}
		return fileTrovati;
	}
	public ArrayList<File> cercaFileSottotitoli(SerieTV serie, Episodio ep, String videoName){
		ArrayList<File> fileTrovati = new ArrayList<File>();
		String pathBase=Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie();
		String altDir=Settings.getInstance().getDirectoryDownload2()+serie.getFolderSerie();
		if(isMainDirAvailable()){
    		File dir = new File(pathBase);
    		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		}
		if(isAlternativeDirAvailable()){
    		File dir = new File(altDir);
    		cercaFile(dir, fileTrovati, ep, estensioniSottotitoliValide);
		}
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
	public String getAvailableDirectory() throws DirectoryNotAvailableException{
		if(isMainDirAvailable())
			return settings.getDirectoryDownload();
		if(isAlternativeDirAvailable())
			return settings.getDirectoryDownload2();
		throw new DirectoryNotAvailableException("directory non trovata o spazio non sufficiente");
	}
	public static void main(String[] args){
		File f = new File("C:\\SerieTV");
		System.out.println("Spazione libero: " +getMegabytes(f.getFreeSpace()));
	}
	private static int getMegabytes(long bytes){
		return (int) (bytes/(1048576));
	}
}
