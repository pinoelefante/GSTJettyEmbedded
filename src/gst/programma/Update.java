package gst.programma;

import gst.database.Database;
import gst.manutenzione.Manutenzione;
import gst.naming.CaratteristicheFile;
import gst.serieTV.SerieTV;
import gst.tda.db.KVResult;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;

public class Update {
	public static void start() {
		if(Settings.isNewUpdate() || Settings.getVersioneSoftware()>Settings.getLastVersion()){
			switch(Settings.getLastVersion()){
				case 0:
				case 102:
					System.out.println("Aggiornamento 102-103");
					update_102_to_103();
				case 103: 
					System.out.println("Aggiornamento 103-104");
					update_103_to_104();
				case 107:
					System.out.println("Aggiornamento 107-108");
					update_107_to_108();
				case 110:
					System.out.println("Aggiornamento 110-111");
					update_110_to_111();
				case 117:
					System.out.println("Aggiornamento 117-118");
					update_117_to_118();
				case 118:
					System.out.println("Aggiornamento 118-119");
					update_118_to_119();
				default:
					Settings.setLastVersion(Settings.getVersioneSoftware());
					Settings.setNewUpdate(false);
					Settings.salvaSettings();
			}
		}
	}
	private static void update_118_to_119() {
		Manutenzione.generaLauncherManutenzione();
	}
	private static void update_117_to_118(){
		String query="SELECT * FROM "+Database.TABLE_SETTINGS;
		ArrayList<KVResult<String, Object>> opzioni=Database.selectQuery(query);
		for(int i=0;i<opzioni.size();i++){
			KVResult<String, Object> res=opzioni.get(i);
			
			String download_path=(String) res.getValueByKey("download_path");
			if(download_path==null || download_path.compareTo("null")==0)
				download_path=Settings.getUserDir()+"Download";
			Settings.setDirectoryDownload(download_path);
			
			String utorrent_path=(String) res.getValueByKey("utorrent");
			if(utorrent_path==null || utorrent_path.compareTo("null")==0)
				utorrent_path="";
			Settings.setClientPath(utorrent_path);
			
			String vlc_path=(String) res.getValueByKey("vlc");
			if(vlc_path==null || vlc_path.compareTo("null")==0)
				vlc_path="";
			Settings.setVLCPath(vlc_path);
			
			String user_itasa=(String) res.getValueByKey("itasa_user");
			if(user_itasa==null || user_itasa.compareTo("null")==0)
				user_itasa="";
			Settings.setItasaUsername(user_itasa);
			
			String pass_itasa=(String) res.getValueByKey("itasa_pass");
			if(pass_itasa==null || pass_itasa.compareTo("null")==0)
				pass_itasa="";
			Settings.setItasaPassword(pass_itasa);
			
			String client_id=(String) res.getValueByKey("client_id");
			if(client_id==null || client_id.compareTo("null")==0)
				client_id="";
			Settings.setClientID(client_id);
			
			Settings.setTrayOnIcon(((int) res.getValueByKey("tray_on_icon"))==1?true:false);
			Settings.setStartHidden(((int) res.getValueByKey("start_hidden"))==1?true:false);
			Settings.setAskOnClose(((int) res.getValueByKey("ask_on_close"))==1?true:false);
			Settings.setAlwaysOnTop(((int) res.getValueByKey("always_on_top"))==1?true:false);
			Settings.setAutostart(((int) res.getValueByKey("autostart"))==1?true:false);
            Settings.setDownloadAutomatico(((int) res.getValueByKey("download_auto"))==1?true:false);
            Settings.setMinRicerca((int) res.getValueByKey("min_download_auto"));
            Settings.setNewUpdate(((int) res.getValueByKey("new_update"))==1?true:false);
            Settings.setLastVersion((int) res.getValueByKey("last_version"));
            Settings.setRicercaSottotitoli(((int) res.getValueByKey("download_sottotitoli"))==1?true:false);
            Settings.setVLCAutoload(((int) res.getValueByKey("vlc_autoload"))==1?true:false);
            Settings.setExtenalVLC(((int) res.getValueByKey("external_vlc"))==1?true:false);
            Settings. setEnableITASA(((int) res.getValueByKey("itasa"))==1?true:false);
            Settings.setLettoreNascondiViste(((int) res.getValueByKey("hide_viste"))==1?true:false);
            Settings.setLettoreNascondiIgnore(((int) res.getValueByKey("hide_ignorate"))==1?true:false);
            Settings.setLettoreNascondiRimosso(((int) res.getValueByKey("hide_rimosse"))==1?true:false);
            Settings.setLettoreOrdine((int) res.getValueByKey("ordine_lettore"));
		}
		Settings.salvaSettings();
	}
	private static void update_110_to_111(){
		if(OperazioniFile.fileExists(Settings.getCurrentDir()+"database2.sqlite") && Database.isFreshNew()){
			Manutenzione.esportaDBinSQL(Database.Connect(), Settings.getCurrentDir());
			Manutenzione.importaDBdaSQLite(Settings.getCurrentDir()+"database2.sqlite", Database.Connect());
			OperazioniFile.deleteFile(Settings.getCurrentDir()+"database2.sqlite");
		}
		/*
		Database.Disconnect();
		if(OperazioniFile.fileExists(Settings.getUserDir()+"database2.sqlite"))
			OperazioniFile.copyfile(Settings.getUserDir()+"database2.sqlite", Settings.getUserDir()+"database2.sqlite.bak");
		if(OperazioniFile.deleteFile(Settings.getUserDir()+"database2.sqlite")){
			System.out.println("database2.sqlite eliminato con successo");
		}
		if(OperazioniFile.copyfile(Settings.getCurrentDir()+"database2.sqlite", Settings.getUserDir()+"database2.sqlite")){
			System.out.println("database2.sqlite copiato con successo");
			Database.Connect();
		}
		else {
			ManagerException.registraEccezione("Si è verificato un errore durante l'aggiornamento del software.\nNon è stato possibile copiare il vecchio database.\nSe questa è una nuova installazione, questo errore può essere ignorato.");
			Database.Connect();
		}
		*/
	}
	private static void update_107_to_108(){
		Settings.setEnableITASA(true);
		String query="UPDATE "+Database.TABLE_SETTINGS+" SET itasa=1";
		Database.updateQuery(query);
	}
	private static void update_103_to_104() {
		String[] query={
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T1\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T2\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T3\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T4\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T5\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T6\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T7\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T8\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"T9\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp1\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp2\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp3\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp4\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp5\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp6\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp7\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp8\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp9\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temporary_Placeholder\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temporary_Placeholder_2\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp01\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp02\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp03\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp04\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp 01\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp 02\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp 03\"",
				"DELETE FROM "+Database.TABLE_SERIETV+" WHERE nome=\"Temp 04\""
			};
			for(int j=0;j<query.length;j++)
				Database.updateQuery(query[j]);
	}
	private static void update_102_to_103(){
		String NOMEDB=Settings.getCurrentDir()+"database.db";
		if(OperazioniFile.fileExists(NOMEDB) && Database.isFreshNew()){
			try {
				SQLiteConfig conf=new SQLiteConfig();
				conf.enableRecursiveTriggers(true);
				conf.enforceForeignKeys(true);
				conf.setSynchronous(SynchronousMode.OFF);
				Connection con = DriverManager.getConnection("jdbc:sqlite:"+NOMEDB, conf.toProperties());
				
				String query_settings="SELECT * FROM settings";
				ArrayList<KVResult<String, Object>> settings=Database.selectQuery(con, query_settings);
				if(settings!=null){
					String dir_down=(String) settings.get(0).getValueByKey("dir_download");
					Settings.setDirectoryDownload(dir_down);
					String dir_utorr=(String) settings.get(0).getValueByKey("dir_client");
					Settings.setClientPath(dir_utorr);
					String dir_vlc=(String) settings.get(0).getValueByKey("dir_vlc");
					Settings.setVLCPath(dir_vlc);
					int tray_on_icon=(int) settings.get(0).getValueByKey("tray_on_icon");
					Settings.setTrayOnIcon(tray_on_icon==1?true:false);
					int starthidden=(int) settings.get(0).getValueByKey("start_hidden");
					Settings.setStartHidden(starthidden==1?true:false);
					int ask_close=(int) settings.get(0).getValueByKey("ask_on_close");
					Settings.setAskOnClose(ask_close==1?true:false);
					int alwaysontop=(int) settings.get(0).getValueByKey("always_on_top");
					Settings.setAlwaysOnTop(alwaysontop==1?true:false);
					int startwin=(int) settings.get(0).getValueByKey("start_win");
					Settings.setAutostart(startwin==1?true:false);
					int ricerca_auto=(int) settings.get(0).getValueByKey("ricerca_auto");
					Settings.setDownloadAutomatico(ricerca_auto==1?true:false);
					int min_ricerca=(int) settings.get(0).getValueByKey("min_ricerca");
					Settings.setMinRicerca(min_ricerca);
					int last_ver=(int) settings.get(0).getValueByKey("last_version");
					Settings.setLastVersion(last_ver);
					int ricerca_sub=(int) settings.get(0).getValueByKey("ricerca_sub");
					Settings.setRicercaSottotitoli(ricerca_sub==1?true:false);
					String useritasa=(String) settings.get(0).getValueByKey("itasa_id");
					Settings.setItasaUsername(useritasa);
					String passitasa=(String) settings.get(0).getValueByKey("itasa_pass");
					Settings.setItasaPassword(passitasa);
					Settings.salvaSettings();
				}
				
				String query_serie="SELECT * FROM serie";
				ArrayList<KVResult<String, Object>> series=Database.selectQuery(con, query_serie);
				if(series!=null){
					for(int i=0;i<series.size();i++){
						KVResult<String, Object> r=series.get(i);
						int id_db=(int) r.getValueByKey("id");
						String nome=(String) r.getValueByKey("nome");
						String url=(String) r.getValueByKey("url");
						url=url.replace("/shows/", "");
						url=url.substring(0,url.indexOf("/"));
						int stato=(int) r.getValueByKey("stato");
						int inserita=(int) r.getValueByKey("inserita");
						String nome_formattato=SerieTV.formattaNome(nome);
						String query_insert_serie="INSERT INTO "+Database.TABLE_SERIETV +" (id,url, nome, inserita, conclusa, stop_search, provider) VALUES("+
								id_db+
								",\""+url+"\""+
								",\""+nome_formattato+"\""+
								","+inserita+
								","+stato+
								","+0+
								","+1+")";
						Database.updateQuery(query_insert_serie);
						
						if(nome.compareToIgnoreCase(nome_formattato)!=0){
							String base_dir_download=Settings.getDirectoryDownload();
							if(base_dir_download!=null && !base_dir_download.isEmpty()){
								File old_dir=new File(Settings.getDirectoryDownload()+File.separator+nome);
								if(old_dir.exists() && old_dir.isDirectory()){
									if(old_dir.renameTo(new File(Settings.getDirectoryDownload()+File.separator+nome_formattato))){
										System.out.println(old_dir+"- Cartella rinominata correttamente");
									}
									else
										System.out.println(old_dir+"- errore durante rinominazione in: "+nome_formattato);
								}	
							}
						}
						if(inserita==1){
	    					String query_episodi="SELECT * FROM torrent WHERE id_serie="+id_db;
	    					ArrayList<KVResult<String, Object>> episodes=Database.selectQuery(con, query_episodi);
	    					if(episodes!=null){
	    						for(int j=0;j<episodes.size();j++){
	    							KVResult<String, Object> rt=episodes.get(j);
	    							String magnet=(String) rt.getValueByKey("magnet");
	    							int vista=(int) rt.getValueByKey("vista");
	    							int stagione=(int) rt.getValueByKey("serie");
	    							int episodio=(int) rt.getValueByKey("episodio");
	    							int hd=(int) rt.getValueByKey("HD720p");
	    							int repack=(int) rt.getValueByKey("repack");
	    							int preair=(int) rt.getValueByKey("preair");
	    							int proper=(int) rt.getValueByKey("proper");
	    							int sottotitolo=(int) rt.getValueByKey("sottotitolo");
	    							
	    							String query_insert_episodio="INSERT INTO "+Database.TABLE_EPISODI+" (id_serie, url, vista, stagione, episodio, tags, preair, sottotitolo) VALUES ("+
	    								id_db+
	    								",\""+magnet+"\""+
	    								","+vista+
	    								","+stagione+
	    								","+episodio+
	    								","+CaratteristicheFile.valueFromStat(hd==1?true:false, repack==1?true:false, proper==1?true:false)+
	    								","+preair+
	    								","+sottotitolo+")";
	    							Database.updateQuery(query_insert_episodio);
	    						}
	    					}
						}
					}
				}
				//TODO cercare doppioni serie tv
				con.close();
				OperazioniFile.deleteFile(NOMEDB);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
