package gst.sottotitoli.italiansubs;

import gst.database.Database;
import gst.download.Download;
import gst.naming.CaratteristicheFile;
import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;
import gst.tda.db.KVResult;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Scanner;

import util.zip.ArchiviZip;

public class ItalianSubs implements ProviderSottotitoli{
	private static ItalianSubs itasa;
	public final static String HDTV = "Normale",	
							HD720p = "720p",
							HD1080i = "1080i",
							HD1080p = "1080p",
							WEB_DL = "web-dl",
							DVDRIP = "dvdrip",
							BLUERAY = "bluray", 
							BRRIP = "bdrip";
	
	private ArrayList<RSSItemItalianSubs> feed_rss; 
	private ArrayList<SerieSub> elenco_serie;
	
	private ItasaAPI api;
	
	private GregorianCalendar RSS_UltimoAggiornamento;
	private final long update_time_rss=/*1 minuto*/60000L*15;  //15 minuti
	
	private boolean loggato=false;
	private Settings settings;
	
	public static ItalianSubs getInstance(){
		if(itasa==null)
			itasa = new ItalianSubs();
		return itasa;
	}
	
	private ItalianSubs(){
		settings = Settings.getInstance();
		api = new ItasaAPI();
	}
	private boolean logga(){
		loggato=api.login(!settings.getItasaUsername().isEmpty()?settings.getItasaUsername():"GestioneSerieTV",
				  		  !settings.getItasaPassword().isEmpty()?settings.getItasaPassword():"gestione@90");
		return loggato;
	}
	
	public boolean scaricaSottotitolo(SerieTV serie, Episodio episodio) {
		if(serie.getIDItasa()<=0)
			return false;
		
		if(!loggato)
			logga();
		
		Torrent link = GestioneSerieTV.getInstance().getLinkDownload(episodio.getId());
		if(link==null)
			return false;
		
		String pathFile = null;
		int idSub = api.cercaSottotitolo(serie.getIDItasa(), episodio.getStagione(), episodio.getEpisodio(), getVersione(link));
		String dirDown = settings.getDirectoryDownload()+serie.getFolderSerie();
		if(idSub>0){
			try {
				pathFile=api.download(idSub, dirDown);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			idSub=cercaFeed(serie.getIDItasa(), link);
			if(idSub>0){
				try {
					pathFile=api.download(idSub, dirDown);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(pathFile!=null){
			ArchiviZip.estrai_tutto(pathFile, dirDown);
			return true;
		}
		//TODO modifica statoSub in db in GestioneSottotitoli
		
		return false;
	}
	private String getVersione(Torrent t){
		CaratteristicheFile c = t.getCaratteristiche();
		
		if(c.isDVDRip())
			return DVDRIP;
		if(c.is720p())
			return HD720p;
		
		return HDTV;
	}
	private int cercaFeed(int iditasa, Torrent t){
		if(verificaTempo(update_time_rss, RSS_UltimoAggiornamento)){
			System.out.println("Aggiornando il feed RSS - Italiansubs.net");
			aggiornaFeedRSS();
		}
		for(int i=0;i<feed_rss.size();i++){
			RSSItemItalianSubs rss=feed_rss.get(i);
			if(rss.getIDSerie()==iditasa){
				if(rss.is720p()==t.getCaratteristiche().is720p()){
					if(rss.isNormale()==!t.getCaratteristiche().is720p()){
						if(rss.getStagione()==t.getCaratteristiche().getStagione()){
							if(rss.getEpisodio()==t.getCaratteristiche().getEpisodio()){
								return rss.getIDSub();
							}
						}
					}
				}
			}
		}
		return -1;
	}
	private void aggiornaFeedRSS(){
		RSS_UltimoAggiornamento=new GregorianCalendar();
		feed_rss.clear();
		try {
			Download.downloadFromUrl("http://feeds.feedburner.com/ITASA-Ultimi-Sottotitoli", settings.getUserDir()+"feed_itasa");
			FileReader f_r=new FileReader(settings.getUserDir()+"feed_itasa");
			Scanner file=new Scanner(f_r);
			while(file.hasNextLine()){
				String riga=file.nextLine().trim();
				if(riga.contains("<item>")){
					String linea=file.nextLine().trim();
					String nome="", url="";
					boolean n_done=false, 
							u_done=false;
					while(!linea.contains("</item>")){
						if(linea.contains("<title>")){
							nome=linea.replace("<title>", "").replace("</title>", "").trim();
							n_done=true;
						}
						else if(linea.startsWith("<guid")){
							url=linea.substring(linea.indexOf("\">")+2, linea.indexOf("</guid>")).replace("&amp;", "&");
							u_done=true;
						}
						if(u_done && n_done){
							RSSItemItalianSubs sub=new RSSItemItalianSubs(this, nome, url);
							feed_rss.add(sub);
							u_done=false;
							n_done=false;
						}
						linea=file.nextLine().trim();
					}
				}
			}
			file.close();
			f_r.close();
			OperazioniFile.deleteFile(settings.getUserDir()+"feed_itasa");
		} 
		catch (IOException e) {
			ManagerException.registraEccezione(e);
		}
	}
	
	private boolean isSeriePresente(int id){
		if(elenco_serie.isEmpty())
			return false;
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			if((int)s.getIDDB()==id)
				return true;
		}
		return false;
	}
	
	private boolean addSerie(SerieSub toInsert){
		if(elenco_serie.isEmpty()){
			elenco_serie.add(toInsert);
			return true;
		}
		
		boolean insert=false;
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			int compare=toInsert.getNomeSerie().compareToIgnoreCase(s.getNomeSerie());
			if(compare<0){
				elenco_serie.add(i, toInsert);
				return true;
			}
			else if(compare==0){
				return false;
			}
		}
		if(!insert){
			elenco_serie.add(toInsert);
			return true;
		}
		return false;
	}
	private void salvaInDB(SerieSub serie){
		String query="INSERT INTO "+Database.TABLE_ITASA+" (id_serie, nome_serie) VALUES ("+(int)serie.getIDDB()+", \""+serie.getNomeSerie()+"\")";
		Database.updateQuery(query);
	}
	public void aggiornaElencoSerieOnline(){
		
	}
	
	public static boolean VerificaLogin(String username, String password){
		return itasa.api.verificaLogin(username, password)!=null;
	}
	int cercaSerie(String nome){
		for(int i=0;i<elenco_serie.size();i++){
			SerieSub s=elenco_serie.get(i);
			if(s.getNomeSerie().compareToIgnoreCase(nome)==0)
				return (int)s.getIDDB();
		}
		return -1;
	}
	public String toStringFeed(){
		String str="";
		for(int i=0;i<feed_rss.size();i++)
			str+=feed_rss.get(i).toString()+"\n";
		
		return str;
	}
	public String toString(){
		return "Italiansubs";
	}
	
	public SerieSub getSerieAssociata(SerieTV serie) {
		if(serie.getIDItasa()>0){
			int id=serie.getIDItasa();
			for(int i=0;i<elenco_serie.size();i++){
				SerieSub s=elenco_serie.get(i);
				if(s.getIDDB()==id)
					return s;
			}
		}
		for(int i=0;i<elenco_serie.size();i++)
			if(elenco_serie.get(i).getNomeSerie().compareToIgnoreCase(serie.getNomeSerie())==0)
				return elenco_serie.get(i);
		return null;
	}

	@Override
	public String getProviderName() {
		return "ItalianSubs.net";
	}
	private boolean verificaTempo(long maxdif, GregorianCalendar last){
		if(last==null)
			return true;
		GregorianCalendar adesso=new GregorianCalendar();
		long time_now=adesso.getTimeInMillis();
		adesso=null;
		long time_last=last.getTimeInMillis();
		if((time_now-time_last)>maxdif)
			return true;
		return false;
	}
	
	public ArrayList<SerieSub> getElencoSerie(){
		//TODO modificare
		String query = "SELECT * FROM "+Database.TABLE_ITASA+" ORDER BY nome_serie DESC";
		ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
		elenco_serie = new ArrayList<SerieSub>();
		for(int i=0;i<res.size();i++){
			KVResult<String, Object> r=res.get(i);
			String nome=(String) r.getValueByKey("nome_serie");
			Integer id=(Integer) r.getValueByKey("id_serie");
			SerieSub serie=new SerieSub(nome, id);
			elenco_serie.add(0,serie);
		}
		return elenco_serie;
	}

	@Override
	public SerieSub cercaSerieAssociata(SerieTV serie) {
		//TODO query ricerca
		return null;
	}

}

