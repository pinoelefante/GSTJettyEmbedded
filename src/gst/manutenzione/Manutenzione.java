package gst.manutenzione;

import gst.programma.ManagerException;
import gst.programma.OperazioniFile;
import gst.programma.Settings;
import gst.tda.db.KVResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import gst.database.Database;;;

public class Manutenzione {
	public static boolean esportaDBinSQL(Connection dbOrigine, String directory) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(directory+File.separator+"gst_db_backup.sql");
			
			ArrayList<KVResult<String, Object>> serietv=Database.selectQuery(dbOrigine,"SELECT * FROM "+Database.TABLE_SERIETV);
			
			//TODO inserire creazione database
			
			for(int i=0;i<serietv.size();i++){
				KVResult<String, Object> row=serietv.get(i);
				String query="INSERT INTO "+Database.TABLE_SERIETV+" (id, url, nome, inserita, conclusa, stop_search, provider, id_itasa, id_subsfactory, id_subspedia, id_tvdb, preferenze_download) VALUES ("
					+(Integer)row.getValueByKey("id")+","
					+"\""+(String)row.getValueByKey("url")+"\","
					+"\""+(String)row.getValueByKey("nome")+"\","
					+(Integer)row.getValueByKey("inserita")+","
					+(Integer)row.getValueByKey("conclusa")+","
					+(Integer)row.getValueByKey("stop_search")+","
					+(Integer)row.getValueByKey("provider")+","
					+(Integer)row.getValueByKey("id_itasa")+","
					+(Integer)row.getValueByKey("id_subsfactory")+","
					+(Integer)row.getValueByKey("id_subspedia")+","
					+(Integer)row.getValueByKey("id_tvdb")+","
					+(Integer)row.getValueByKey("preferenze_download")+")\n";
				fw.append(query);
			}
			
			ArrayList<KVResult<String, Object>> episodi=Database.selectQuery(dbOrigine,"SELECT * FROM "+Database.TABLE_EPISODI);
			for(int i=0;i<episodi.size();i++){
				KVResult<String, Object> row=episodi.get(i);
				String query="INSERT INTO "+Database.TABLE_EPISODI+" (id, id_serie, url, vista, stagione, episodio, tags, preair, sottotitolo, id_tvdb_ep) VALUES("
					+(Integer)row.getValueByKey("id")+","
					+(Integer)row.getValueByKey("id_serie")+","
					+"\""+(String)row.getValueByKey("url")+"\","
					+(Integer)row.getValueByKey("vista")+","
					+(Integer)row.getValueByKey("stagione")+","
					+(Integer)row.getValueByKey("episodio")+","
					+(Integer)row.getValueByKey("tags")+","
					+(Integer)row.getValueByKey("preair")+","
					+(Integer)row.getValueByKey("sottotitolo")+","
					+(Integer)row.getValueByKey("id_tvdb_ep")+")\n";
				fw.append(query);
			}
			
			ArrayList<KVResult<String, Object>> itasa=Database.selectQuery(dbOrigine,"SELECT * FROM "+Database.TABLE_ITASA);
			for(int i=0;i<itasa.size();i++){
				KVResult<String, Object> row=itasa.get(i);
				String query="INSERT INTO "+Database.TABLE_ITASA+" (id_serie, nome_serie) VALUES("
					+(Integer)row.getValueByKey("id_serie")+","
					+"\""+(String)row.getValueByKey("nome_serie")+"\")\n";
				fw.append(query);
			}
			
			ArrayList<KVResult<String, Object>> subsfactory=Database.selectQuery(dbOrigine,"SELECT * FROM "+Database.TABLE_SUBSFACTORY);
			for(int i=0;i<subsfactory.size();i++){
				KVResult<String, Object> row=subsfactory.get(i);
				String query="INSERT INTO "+Database.TABLE_SUBSFACTORY+" (id, nome_serie, directory) VALUES("
					+(Integer)row.getValueByKey("id")+","
					+"\""+(String)row.getValueByKey("nome_serie")+"\","
					+"\""+(String)row.getValueByKey("directory")+"\")\n";
				fw.append(query);
			}
			
			ArrayList<KVResult<String, Object>> logsub=Database.selectQuery(dbOrigine,"SELECT * FROM "+Database.TABLE_LOGSUB);
			for(int i=0;i<logsub.size();i++){
				KVResult<String, Object> row=logsub.get(i);
				String query="INSERT INTO "+Database.TABLE_LOGSUB+" (id, serie, stagione, episodio, id_provider) VALUES("
					+(Integer)row.getValueByKey("id")+","
					+"\""+(String)row.getValueByKey("serie")+"\","
					+(Integer)row.getValueByKey("stagione")+","
					+(Integer)row.getValueByKey("episodio")+","
					+(Integer)row.getValueByKey("id_provider")+")\n";
				fw.append(query);
			}
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			OperazioniFile.deleteFile(directory+File.separator+"gst_db_backup.sql");
			ManagerException.registraEccezione(e);
			return false;
		} 
		finally {
			if(fw!=null)
				try {
					fw.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static boolean importaDBdaSQL(Connection dbOrigine, String pathDB) {
		FileReader fr=null;
		Scanner file=null;
		try {
			fr=new FileReader(pathDB);
			file=new Scanner(fr);
			truncateAll(dbOrigine);
			int error=0;
			while(file.hasNextLine()){
				String query=file.nextLine().trim();
				Object res=Database.eseguiQuery(dbOrigine,query);
				if(res==null || (res instanceof Boolean && (boolean)res==false))
					error++;
			}
			return error==0;
		} 
		catch (Exception e) {

			return false;
		} 
		finally {
			try {
				if(file!=null)
					file.close();
				if(fr!=null)
					fr.close();
			}
			catch(IOException e){}
		}
	}

	public static boolean importaDBdaSQLite(String pathDB, Connection dbDestinazione) {
		if(!OperazioniFile.fileExists(pathDB))
			return false;
		
		Connection dbOrigine=null;
		File dir=null;
		
		try {
			dbOrigine=Database.ConnectToDB(pathDB);
			if(!isGSTValidDB(dbOrigine)){
				System.out.println("Database non valido");
				dbOrigine.close();
				return false;
			}
			String export_path=Settings.getUserDir()+randomName(10)+File.separator;
			dir=new File(export_path);
			dir.mkdirs();
			esportaDBinSQL(dbOrigine, export_path);
			importaDBdaSQL(dbDestinazione, export_path+"gst_db_backup.sql");
			return true;
		} 
		catch (Exception e) {

			return false;
		} 
		finally {
			if(dbOrigine!=null)
				try {
					dbOrigine.close();
				} 
				catch (SQLException e) {}
			if(dir!=null)
				OperazioniFile.DeleteDirectory(dir);
		}
	}
	private static String randomName(int len){
		String letters="abcdefghijklmnopqrstuvz";
		Random rand=new Random();
		String gen="";
		for(int i=0;i<len;i++)
			gen+=letters.charAt(rand.nextInt(letters.length()));
		return gen;
	}
	private static boolean isGSTValidDB(Connection con){
		String[] serie={"id", "url", "nome", "inserita", "conclusa", "stop_search", "provider", "id_itasa", "id_subsfactory", "id_subspedia", "id_tvdb", "preferenze_download"};
		for(int i=0;i<serie.length;i++){
			if(!Database.checkColumn(con, Database.TABLE_SERIETV, serie[i]))
				return false;
		}
		String[] episodi={"id", "id_serie", "url", "vista", "stagione", "episodio", "tags", "preair", "sottotitolo", "id_tvdb_ep"};
		for(int i=0;i<episodi.length;i++){
			if(!Database.checkColumn(con, Database.TABLE_EPISODI, episodi[i]))
				return false;
		}
		String[] itasa={"id_serie", "nome_serie"};
		for(int i=0;i<itasa.length;i++){
			if(!Database.checkColumn(con, Database.TABLE_ITASA, itasa[i]))
				return false;
		}
		String[] subsf={"id", "nome_serie", "directory"};
		for(int i=0;i<subsf.length;i++){
			if(!Database.checkColumn(con, Database.TABLE_SUBSFACTORY, subsf[i]))
				return false;
		}
		String[] logsub={"id", "serie", "stagione", "episodio", "id_provider"};
		for(int i=0;i<logsub.length;i++){
			if(!Database.checkColumn(con, Database.TABLE_LOGSUB, logsub[i]))
				return false;
		}
		return true;
	}
	public static boolean truncate(Connection con, String tableName){
		String query="DELETE FROM "+tableName;
		String query_clean="DELETE FROM sqlite_sequence WHERE name='"+tableName+"'";
		return (Database.updateQuery(con, query) && Database.updateQuery(con,query_clean));
	}
	public static void truncateAll(Connection con){
		truncate(con,Database.TABLE_SERIETV);
		truncate(con,Database.TABLE_EPISODI);
		truncate(con,Database.TABLE_ITASA);
		truncate(con,Database.TABLE_LOGSUB);
		truncate(con,Database.TABLE_SUBSFACTORY);
		truncate(con,Database.TABLE_SUBSPEDIA);
		truncate(con,Database.TABLE_TVDB_EPISODI);
		truncate(con,Database.TABLE_TVDB_SERIE);
		Database.rebuildDB(con);
	}
	public static void generaLauncherManutenzione(){
		String exe=Settings.getEXEName();
		String command="";
		if(exe.toLowerCase().endsWith(".jar")){
			command="\""+System.getProperty("java.home");
			command+=File.separator+"bin"+File.separator+"java\" -jar ";
		}
		command+="\""+exe+"\""+" manutenzione";
		
		try {
			FileWriter fw=new FileWriter(Settings.getCurrentDir()+"manutenzione.bat");
			FileWriter fw2=new FileWriter(Settings.getUserDir()+"manutenzione.bat");
			fw.append(command);
			fw2.append(command);
			fw.close();
			fw2.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
