package gst.programma;

import gst.download.Download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Prerequisiti {
	private static Prerequisiti inst;
	private Settings settings;
	
	public static Prerequisiti getInstance(){
		if(inst==null)
			inst = new Prerequisiti();
		return inst;
	}
	
	private Prerequisiti(){
		settings = Settings.getInstance();
	}
	private static String hashFile(File file, String algorithm) {
	    try (FileInputStream inputStream = new FileInputStream(file)) {
	        MessageDigest digest = MessageDigest.getInstance(algorithm);
	 
	        byte[] bytesBuffer = new byte[1024];
	        int bytesRead = -1;
	 
	        while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
	            digest.update(bytesBuffer, 0, bytesRead);
	        }
	 
	        byte[] hashedBytes = digest.digest();
	 
	        return convertByteArrayToHexString(hashedBytes);
	    } 
	    catch (Exception ex) {
	       ex.printStackTrace();
	    }
	    return null;
	}
	private static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return stringBuffer.toString();
	}
	public String MD5Hash(String path) {
		/*
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			String md5 = DigestUtils.md5Hex(fis);
			fis.close();
			return md5;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		*/
		return hashFile(new File(path), "MD5");
	}
	public void verificaDipendenze(){
		ArrayList<Dipendenza> dip = getListaDipendenze();
		
		for(int i=0;i<dip.size();i++){
			System.out.println("Verifico "+dip.get(i).getNome());
			String pathFile = settings.getCurrentDir()+File.separator+"gstWeb_lib"+File.separator+dip.get(i).getNome();
			String md5 = MD5Hash(pathFile);
			if(md5==null || md5.compareTo(dip.get(i).getHash())!=0){
				int prove=0;
				while(!scaricaDipendenza(dip.get(i)) && prove<3){
					prove++;
				}
			}
		}
	}
	public ArrayList<Dipendenza> getListaDipendenze(){
		ArrayList<Dipendenza> dipendenze = new ArrayList<Dipendenza>();
		
		try {
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse("http://gestioneserietv.altervista.org/release/dependency_"+settings.getVersioneSoftware()+".xml");
			NodeList liblist=doc.getElementsByTagName("lib");
			for(int i=0;i<liblist.getLength();i++){
				Node lib = liblist.item(i);
				NodeList params = lib.getChildNodes();
				String nome = "", url = "", hash = "";
				for(int j=0;j<params.getLength();j++){
					Node p = params.item(j);
					if(p instanceof Element){
						Element e = (Element)p;
						switch(e.getTagName()){
							case "file":
								nome = e.getTextContent();
								break;
							case "url":
								url = e.getTextContent();
								break;
							case "hash":
								hash = e.getTextContent();
								break;
						}
					}
				}
				Dipendenza d = new Dipendenza(url, nome, hash);
				dipendenze.add(d);
			}
		}
		catch(ParserConfigurationException | SAXException | IOException e){ e.printStackTrace(); }
		
		return dipendenze;
	}
	public boolean scaricaDipendenza(Dipendenza d){
		System.out.println("Download "+d.getNome());
		String url = d.getUrl()+d.getNome();
		try {
			Download.downloadFromUrl(url, settings.getCurrentDir()+File.separator+"gstWeb_lib"+File.separator+d.getNome());
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void generaListDipendenze(){
		String dir = "C:\\Users\\pinoelefante\\Desktop\\lib";
		File[] files = new File(dir).listFiles();
		System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		System.out.println("<response>");
		System.out.println("\t<library>");
		for(int i=0;i<files.length;i++){
			System.out.println("\t\t<lib>");
			String md5 = MD5Hash(files[i].getAbsolutePath());
			System.out.println("\t\t\t<file>"+files[i].getName()+"</file>");
			System.out.println("\t\t\t<hash>"+md5+"</hash>");
			System.out.println("\t\t\t<url></url>");
			System.out.println("\t\t</lib>");
		}
		System.out.println("\t</library>");
		System.out.println("<response>");
	}
	public static void main(String[] args){
		Prerequisiti p = getInstance();
		p.verificaDipendenze();
	}
}
