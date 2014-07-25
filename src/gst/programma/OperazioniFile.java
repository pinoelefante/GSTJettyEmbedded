package gst.programma;

import gst.serieTV.Torrent;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OperazioniFile {
	public static boolean DeleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] contenuto = dir.list();
			for (int i = 0; i < contenuto.length; i++) {
				boolean success = DeleteDirectory(new File(dir, contenuto[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	public static boolean deleteFile(String nomefile){
		return (new File(nomefile)).delete();
	}

	public static boolean copyfile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println(f2.getAbsolutePath());
			System.out.println("File copied.");
			return true;
		}
		catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
			ManagerException.registraEccezione(ex);
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		return false;
	}
	public static void dumpfileclean(){
		File directory=new File(Settings.getCurrentDir());
		if(directory.isDirectory()){
			String[] contenuto = directory.list();
			for(int i=0;i<contenuto.length;i++){
				if(contenuto[i].startsWith("hs_err_") && contenuto[i].endsWith(".log")){
					deleteFile(contenuto[i]);
				}
				else if(contenuto[i].startsWith("response_sub_"))
					deleteFile(contenuto[i]);
			}
		}
	}
	public static void esploraCartella(String path) throws Exception{
		File dir=new File(path);
		if(dir.isDirectory()){
			Desktop d=Desktop.getDesktop();
			d.open(dir);
		}
		else
			throw new Exception("Il percorso non è una cartella");
	}
	public static void esploraWeb(String url) throws Exception{
		Desktop d=Desktop.getDesktop();
		try {
			d.browse(new URI(url));
		}
		catch (IOException | URISyntaxException e) {
			ManagerException.registraEccezione(e);
			throw e;
		}
	}
	public static void email(String uri){
		Desktop d=Desktop.getDesktop();
		try {
			d.mail(new URI(uri));
		}
		catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	public static boolean fileExists(String path){
		File f=new File(path);
		if(f.exists()){
			if(f.isFile())
				return true;
			else
				return false;
		}
		else
			return false;
	}
	public static String cercavideofile(Torrent t) throws FileNotFoundException{
		String path_download=Settings.getDirectoryDownload()+(Settings.getDirectoryDownload().endsWith(File.separator)?"":File.separator)+t.getNomeSerieFolder();
		File cartella_download=new File(path_download);
		if(!cartella_download.exists())
			throw new FileNotFoundException("La cartella "+path_download+" non esiste");
		if(!cartella_download.isDirectory())
			throw new FileNotFoundException("Il percorso "+path_download+" non è una directory");

		String[] lista=cartella_download.list();
		String puntata_s=t.getEpisodio()<10?"0"+t.getEpisodio():t.getEpisodio()+"";
		String serie_s=t.getStagione()<10?"0"+t.getStagione():t.getStagione()+"";
		for(int i=0;i<lista.length;i++){
			if(!(lista[i].endsWith(".avi")||lista[i].endsWith(".mp4")||lista[i].endsWith(".mkv")))
				continue;
			
			if(lista[i].contains("720p")!=t.is720p())
				continue;
			if(lista[i].contains("REPACK")!=t.isRepack())
				continue;
			
			if(lista[i].contains(t.getNameFromMagnet()))
				return lista[i];
			
			int index_serie=lista[i].indexOf(serie_s);
			if(index_serie<0 && serie_s.startsWith("0")){
				serie_s=t.getStagione()+"";
				index_serie=lista[i].indexOf(serie_s);
			}
			String linea;
			linea=lista[i].substring(index_serie>0?index_serie+serie_s.length():0);
			int index_ep=linea.indexOf(puntata_s);
			if(index_serie>=0 && index_ep>=0){
				return lista[i];	
			}
		}
		throw new FileNotFoundException("File non trovato");
	}
	public static ArrayList<String> ZipDecompress(String input_zip, String cartella_output) throws IOException {
		ArrayList<String> estratti=new ArrayList<String>();
		ZipInputStream input = new ZipInputStream(new FileInputStream(input_zip));
		ZipEntry zipEntry;
		while ((zipEntry = input.getNextEntry()) != null) {
			if(zipEntry.isDirectory())
				continue;
			
			String nome_file=zipEntry.getName();
			//System.out.println(nome_file);
			if(zipEntry.getName().contains(File.separator)){
				nome_file=nome_file.substring(nome_file.lastIndexOf(File.separator)+1);
			}
			if(nome_file.compareToIgnoreCase(".DS_Store")==0)
				continue;
			if(nome_file.compareToIgnoreCase(".Thumbs.db")==0)
				continue;
			
			File dir_extr=new File(cartella_output);
			if(!dir_extr.exists())
				dir_extr.mkdir();
			
			String dir=cartella_output+(cartella_output.endsWith(File.separator)?"":File.separator);
			FileOutputStream fos = new FileOutputStream(dir+nome_file);

			try {
				
				byte[] readBuffer = new byte[4096];
				int bytesIn = 0;
				while ((bytesIn = input.read(readBuffer)) != -1)
					fos.write(readBuffer, 0, bytesIn);
				estratti.add(dir+nome_file);
			}
			finally {
				fos.close();
			}
		}
		input.close();
		return estratti;
	}
	/*
	public static void main(String[] args){
		try {
			ZipDecompress("D:\\SerieTV\\Flashpoint\\Flashpoint_S3_E11_sub.zip", "D:\\SerieTV\\Flashpoint");
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	*/
	public static boolean subExistsFromPartialFilename(String folder, String path){
		File dir=new File(folder);
		if(dir.exists()){
			if(dir.isDirectory()){
				String[] files=dir.list();
				for(int i=0;i<files.length;i++){
					if((new File(files[i]).isDirectory()))
						continue;
					if(!files[i].contains("."))
						continue;
					
					String ext=files[i].substring(files[i].lastIndexOf("."));
					if(ext.compareToIgnoreCase(".avi")==0 || ext.compareToIgnoreCase(".mp4")==0 || ext.compareToIgnoreCase(".mkv")==0)
						continue;
					String match=files[i].substring(0, path.length());
					
					if(match.compareToIgnoreCase(path)==0){
						if(files[i].contains(".")){
							String ext_sub=files[i].substring(files[i].lastIndexOf("."));
							if(ext_sub.compareToIgnoreCase(".srt")==0)
								return true;
							if(ext_sub.compareToIgnoreCase(".ass")==0)
								return true;
							if(ext_sub.compareToIgnoreCase(".sub")==0)
								return true;
							if(ext_sub.compareToIgnoreCase(".ssa")==0)
								return true;
						}
						return false;
					}	
				}
			}
		}
		return false;
	}
}
