package gst.programma;

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
	public static boolean dirExists(String path){
		File f=new File(path);
		if(f.exists()){
			if(f.isDirectory())
				return true;
			else
				return false;
		}
		else
			return false;
	}
}
