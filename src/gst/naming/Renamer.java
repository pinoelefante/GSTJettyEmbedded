package gst.naming;

import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Torrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class Renamer {
	public Renamer(){}
	
	public static String generaNomeDownload(Torrent t){
		if(t!=null){
			return t.getNomeSerie().replace(" ", "_")+"_S"+t.getStagione()+"_E"+t.getEpisodio()+"_sub"+".zip";
		}
		throw new InvalidParameterException("E' stato passato un parametro non valido");
	}
	public static boolean rinominaSottotitolo(Torrent t){
		String nome_file="";
		try {
			nome_file=OperazioniFile.cercavideofile(t);
			if(nome_file.substring(nome_file.lastIndexOf(".")).compareToIgnoreCase(".avi")==0){
				nome_file=nome_file.substring(0, nome_file.lastIndexOf("."));
			}
			else if(nome_file.substring(nome_file.lastIndexOf(".")).compareToIgnoreCase(".mp4")==0){
				nome_file=nome_file.substring(0, nome_file.lastIndexOf("."));
			}
			else if(nome_file.substring(nome_file.lastIndexOf(".")).compareToIgnoreCase(".mkv")==0){
				nome_file=nome_file.substring(0, nome_file.lastIndexOf("."));
			}
		} 
		catch (FileNotFoundException e) {
			ManagerException.registraEccezione(e);
			System.out.println("rinominaSottotitolo(Torrent t): "+e.getMessage()+".\nSi tenterà di rinominare il sottotitolo con il nome del torrent");
			try {
				String zip_file=Settings.getDirectoryDownload()+t.getNomeSerieFolder()+File.separator+generaNomeDownload(t);
				String dir_dest=Settings.getDirectoryDownload()+t.getNomeSerieFolder()+File.separator;
				
				ArrayList<String> files=OperazioniFile.ZipDecompress(zip_file, dir_dest);
				if(files.size()>0){
					OperazioniFile.deleteFile(zip_file);
					for(int i=0;i<files.size();i++){
						File f=new File(files.get(i));
						String estensione="";
						if(files.get(i).contains("."))
							estensione=files.get(i).substring(files.get(i).lastIndexOf("."));
						if(OperazioniFile.copyfile(f.getAbsolutePath(), dir_dest+t.getNameFromMagnet()+"."+(i+1)+estensione))
							OperazioniFile.deleteFile(files.get(i));
					}
				}
			}
			catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("rinominaSottotitolo(Torrent t): "+e.getMessage()+".\n");
				ManagerException.registraEccezione(e);
				return false;
			}
			return true;
		}
		
		try {
			String zip_file=Settings.getDirectoryDownload()+t.getNomeSerieFolder()+File.separator+generaNomeDownload(t);
			String dir_dest=Settings.getDirectoryDownload()+t.getNomeSerieFolder()+File.separator;
			
			ArrayList<String> files=OperazioniFile.ZipDecompress(zip_file, dir_dest);
			if(files.size()>0){
				OperazioniFile.deleteFile(zip_file);
				for(int i=0;i<files.size();i++){
					File f=new File(files.get(i));
					String estensione="";
					if(files.get(i).contains("."))
						estensione=files.get(i).substring(files.get(i).lastIndexOf("."));
					if(OperazioniFile.copyfile(f.getAbsolutePath(), dir_dest+nome_file+"."+(i+1)+estensione))
						OperazioniFile.deleteFile(files.get(i));
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
		return true;
	}
	
}
