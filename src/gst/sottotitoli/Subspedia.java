package gst.sottotitoli;

import gst.naming.Renamer;
import gst.programma.Download;
import gst.programma.ManagerException;
import gst.programma.Settings;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Subspedia implements ProviderSottotitoli {
	private final String URLFeedRSS="http://subspedia.weebly.com/1/feed";
	private long time_update=(1000*60)* 20L/*minuti*/;
	private long last_update=0L;
	private static ArrayList<SubspediaRSSItem> rss;
	
	public Subspedia(){
		rss=new ArrayList<SubspediaRSSItem>();
	}
	
	public boolean scaricaSottotitolo(Torrent t) {
		String link=cercaSottotitolo(t, false);
		if(link==null)
			return false;
		else {
			link=link.replace(" ", "%20");
			if(scaricaSub(link, Renamer.generaNomeDownload(t), t.getNomeSerieFolder())){
				t.setSubDownload(false, true);
				return true;
			}
			return false;
		}
	}
	private boolean scaricaSub(String url, String nome, String folder){
		String dir_s=Settings.getDirectoryDownload()+(Settings.getDirectoryDownload().endsWith(File.pathSeparator)?folder:(File.separator+folder));
		String destinazione=dir_s+File.separator+nome;
		try {
			Download.downloadFromUrl(url, destinazione);
			
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			return false;
		}
	}
	public SerieSub getSerieAssociata(SerieTV serie) {return null;}
	public boolean cercaSottotitolo(Torrent t) {
		scaricaFeed();
		for(int i=0;i<rss.size();i++){
			SubspediaRSSItem item=rss.get(i);
			if(item.getTitolo().compareToIgnoreCase(t.getNomeSerie())==0){
				if(item.getStagione()==t.getStagione()){
					if(item.getEpisodio()==t.getEpisodio())
						return true;
				}
			}
		}
		return false;
	}
	private String cercaSottotitolo(Torrent t,boolean b) {
		scaricaFeed();
		for(int i=0;i<rss.size();i++){
			SubspediaRSSItem item=rss.get(i);
			if(item.getTitolo().compareToIgnoreCase(t.getNomeSerie())==0){
				if(item.getStagione()==t.getStagione()){
					if(item.getEpisodio()==t.getEpisodio())
						return item.getLink();
				}
			}
		}
		return null;
	}
	public ArrayList<SerieSub> getElencoSerie() {return null;}
	public String getProviderName() {
		return "Subspedia";
	}
	public void aggiornaElencoSerieOnline() {}
	
	private void scaricaFeed() {
		/* Aggiorna il feed RSS ogni time_update minuti */
		if(System.currentTimeMillis()-last_update<time_update)
			return;
		
		last_update=System.currentTimeMillis();
		try {
			//Download.downloadFromUrl(URLFeedRSS, Settings.getCurrentDir()+"feed_subspedia");
			rss.clear();
			
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			dbfactory.setNamespaceAware(true);
			DocumentBuilder domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(URLFeedRSS);
			
			NodeList elementi=doc.getElementsByTagName("item");
			for(int i=0;i<elementi.getLength();i++){
				Node item=elementi.item(i);
				NodeList attributi=item.getChildNodes();
				String titolo="", link="";
				for(int j=0;j<attributi.getLength();j++){
					Node attributo=attributi.item(j);
					if(attributo instanceof Element){
						Element attr=(Element)attributo;
						switch(attr.getTagName()){
							case "title":
								titolo=attr.getTextContent();
								//System.out.println(titolo);
								break;
							case "content:encoded":
								if(attr.getTextContent().contains("a href")){
									if(attr.getTextContent().contains(".zip")){
										link=attr.getTextContent().substring(attr.getTextContent().indexOf("a href")+"a href".length()+2, attr.getTextContent().indexOf(".zip")+".zip".length());
										link=link.replace("http://www.weebly.com", "");
									}
								}
								break;
						}
					}
				}
				rss.add(new SubspediaRSSItem(titolo, link));
			}
			//OperazioniFile.deleteFile(Settings.getCurrentDir()+"feed_subspedia");
		} 
		catch (IOException e) {	
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		catch (SAXException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	
	private void stampaFeed(){
		for(int i=0;i<rss.size();i++){
			System.out.println(rss.get(i));
		}
	}

	@Override
	public int getProviderID() {
		return GestoreSottotitoli.SUBSPEDIA;
	}
	public static void main(String[] args){
		Subspedia sp=new Subspedia();
		sp.scaricaFeed();
		sp.stampaFeed();
	}

}
