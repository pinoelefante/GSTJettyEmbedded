package gst.database;

import gst.database.tda.KVItem;
import gst.database.tda.KVResult;
import gst.programma.ManagerException;
import gst.programma.Settings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;

public class Database {
	private static Connection con;
	
	public final static String TABLE_PROVIDER="provider";
	public final static String TABLE_SERIETV="serietv";
	public final static String TABLE_EPISODI="episodi";
	public final static String TABLE_ITASA="itasa";
	public final static String TABLE_SUBSFACTORY="subsfactory";
	public final static String TABLE_LOGSUB="logsub";
	public final static String TABLE_SETTINGS="settings";
	public static final String TABLE_SUBSPEDIA = "subspedia";
	public static final String TABLE_TVDB_SERIE = "tvdb_serie";
	public static final String TABLE_TVDB_EPISODI = "tvdb_ep";
	public static final String TABLE_TVDB_ATTORI = "tvdb_attori";
	public static final String TABLE_TORRENT = "torrent";
	public static final String TABLE_PREFERITI = "preferiti";
	public static final String TABLE_SUBDOWN = "list_subdown";
	public static final String TABLE_ADDIC7ED = "addic7ed";
	
	private final static String NOMEDB=Settings.getInstance().getUserDir()+"database3.sqlite";

	public static Connection Connect() {
		if(con!=null)
			return con;
		try {
			Class.forName("org.sqlite.JDBC");
			
			SQLiteConfig conf=new SQLiteConfig();
			conf.enableRecursiveTriggers(true);
			conf.enforceForeignKeys(false);
			conf.setSynchronous(SynchronousMode.OFF);
			
			con = DriverManager.getConnection("jdbc:sqlite:"+NOMEDB, conf.toProperties());
			con.setAutoCommit(true);
			creaDB();
			
			checkIntegrita();
			return con;
		} 
		catch (Exception e) {
			System.out.println("Connessione Fallita");
			System.out.println(e.getMessage());
			ManagerException.registraEccezione(e);
			JOptionPane.showMessageDialog(null, "Errore fatale: impossibile connettersi al database");
			System.exit(0);
		}
		return null;
	}
	public static Connection ConnectToDB(String path) throws Exception{
		Class.forName("org.sqlite.JDBC");
			
		SQLiteConfig conf=new SQLiteConfig();
		conf.enableRecursiveTriggers(true);
		conf.enforceForeignKeys(true);
		conf.setSynchronous(SynchronousMode.OFF);
			
		Connection con = DriverManager.getConnection("jdbc:sqlite:"+path, conf.toProperties());
		System.out.println("Connessione OK");
		checkIntegrita();
		return con;
		
	}
	
	public static void Disconnect(){
		try {
			if(con!=null){
				con.close();
				con=null;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	public static void creaDB(){
		try {
			if(con==null || con.isClosed())
				Connect();

			Statement stat = con.createStatement();
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_PROVIDER+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"nome TEXT NOT NULL"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_ITASA+" (" +
					"id INTEGER PRIMARY KEY,"+ 
					"nome TEXT NOT NULL)");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_ADDIC7ED+" (" +
					"id INTEGER PRIMARY KEY,"+ 
					"nome TEXT NOT NULL)");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_SUBSFACTORY +" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "directory TEXT NOT NULL,"
					+ "nome TEXT NOT NULL"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_SUBSPEDIA+" (" 
					+ "id INTEGER PRIMARY KEY,"
					+ "nome TEXT NOT NULL"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_TVDB_SERIE+" (" +
					"id INTEGER PRIMARY KEY," +
					"nomeSerie TEXT," +
					"rating FLOAT," +
					"generi TEXT,"+
					"network TEXT,"+
					"inizio TEXT,"+
					"giorno_settimana TEXT, "+
					"ora_trasmissione TEXT,"+
					"durata_episodi INTEGER,"+
					"stato TEXT,"+
					"descrizione TEXT,"+
					"descrizione_lang TEXT,"+
					"ultimo_aggiornamento INTEGER,"+
					"banner TEXT"+
					")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_TVDB_EPISODI+" (" +
					"id INTEGER PRIMARY KEY," +
					"idSerie INTEGER," +
					"episodio INTEGER," +
					"stagione INTEGER," +
					"titolo TEXT," +
					"immagine TEXT," +
					"descrizione TEXT," +
					"guestStars TEXT," +
					"data_air TEXT," +
					"regista TEXT," +
					"sceneggiatori TEXT," +
					"lang TEXT," +
					"rating FLOAT," +
					"ultimoAggiornamento INTEGER," +
					"FOREIGN KEY(idSerie) REFERENCES tvdb_serie(id)"+
					")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_TVDB_ATTORI+" (" +
					"idSerie INTEGER," +
					"attore TEXT," +
					"ruolo TEXT," +
					"image TEXT," +
					"FOREIGN KEY(idSerie) REFERENCES tvdb_serie(id)"+
					")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_SERIETV+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
					"url TEXT NOT NULL," +
					"nome TEXT NOT NULL, "+
					"provider INTEGER,"+
					"conclusa INTEGER DEFAULT 0,"+
					"stop_search INTEGER DEFAULT 0,"+
					"id_itasa INTEGER DEFAULT 0,"+
					"id_subsfactory INTEGER DEFAULT 0,"+
					"id_subspedia INTEGER DEFAULT 0,"+
					"id_opensubtitles INTEGER DEFAULT 0,"+
					"id_addic7ed INTEGER DEFAULT 0," +
					"id_tvdb INTEGER DEFAULT 0,"+
					"id_karmorra INTEGER DEFAULT 0," +
					"preferenze_download INTEGER DEFAULT 0," +
					"preferenze_sottotitoli TEXT,"+
					"escludi_seleziona_tutto INTEGER DEFAULT 0," +
					"FOREIGN KEY(provider) REFERENCES provider(id),"+
					"FOREIGN KEY(id_itasa) REFERENCES itasa(id),"+
					"FOREIGN KEY(id_subsfactory) REFERENCES subsfactory(id),"+
					"FOREIGN KEY(id_subspedia) REFERENCES subspedia(id),"+ 
					"FOREIGN KEY(id_addic7ed) REFERENCES addic7ed(id),"+
					"FOREIGN KEY(id_tvdb) REFERENCES tvdb_serie(id),"+
					"FOREIGN KEY(id_karmorra) REFERENCES serietv(id)"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_PREFERITI+" (" +
					"id_serie INTEGER PRIMARY KEY"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_EPISODI+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "serie INTEGER,"
					+ "stagione INTEGER,"
					+ "episodio INTEGER,"
					+ "stato_visualizzazione INTEGER DEFAULT 0,"+
					  "sottotitolo INTEGER DEFAULT 0,"+
					  "id_tvdb INTEGER DEFAULT 0,"
					+ "FOREIGN KEY(serie) REFERENCES serietv(id),"
					+ "FOREIGN KEY(id_tvdb) REFERENCES tvdb_ep(id)"
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_TORRENT+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+"episodio INTEGER,"+
					 "qualita INTEGER,"+
					"url TEXT,"
					+ "FOREIGN KEY(episodio) REFERENCES episodi(id)"
					+ ")");
					
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_LOGSUB+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "episodio INTEGER NOT NULL,"
					+ "provider INTEGER NOT NULL,"
					+ "lingua TEXT,"
					+ "FOREIGN KEY(episodio) REFERENCES episodi(id)"
					+ ""
					+ ")");
			
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS "+TABLE_SUBDOWN+" (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "episodio INTEGER NOT NULL,"
					+ "lingua TEXT NOT NULL,"
					+ "FOREIGN KEY(episodio) REFERENCES episodi(id)"
					+ ""
					+ ")");
			
			stat.close();
			
			init();
		}
		catch (SQLException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}
	private static void init(){
		if(selectQuery("SELECT * FROM provider WHERE id=1").size()==0)
			updateQuery("INSERT INTO provider (id, nome) VALUES (1,'eztv.it')");
		if(selectQuery("SELECT * FROM provider WHERE id=2").size()==0)
			updateQuery("INSERT INTO provider (id, nome) VALUES (2,'Karmorra')");
	}
	private static void checkIntegrita(){}
	
	public static boolean drop(String table){
		String query="DROP TABLE IF EXISTS "+table;
		try {
			Statement st=con.createStatement();
			st.executeUpdate(query);
			st.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		return false;
	}
	public static ArrayList<String> getTableColumns(String tableName) { 
		ArrayList<String> columns = new ArrayList<String>();
		try {
			String cmd = "pragma table_info(" + tableName + ");"; 
			Statement st=con.createStatement();
			ResultSet cur=st.executeQuery(cmd);
			
			while (cur.next()) {
				String nome=cur.getString(2);
				columns.add(nome);
			}
			cur.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		return columns;
	}
	public static ArrayList<String> getTableColumns(Connection con,String tableName) { 
		ArrayList<String> columns = new ArrayList<String>();
		try {
			String cmd = "pragma table_info(" + tableName + ");"; 
			Statement st=con.createStatement();
			ResultSet cur=st.executeQuery(cmd);
			
			while (cur.next()) {
				String nome=cur.getString(2);
				columns.add(nome);
			}
			cur.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
		return columns;
	}
	@SuppressWarnings("unused")
	private static boolean checkColumn(String table, String field){
		ArrayList<String> columns=getTableColumns(table);
		for(int i=0;i<columns.size();i++){
			if(columns.get(i).compareTo(field)==0){
				//ManagerException.registraEccezione(new Exception("Campo '"+field+"' già presente nella tabella '"+table+"'"));
				return true;
			}
		}
		//System.out.println(field+ " non presente");
		return false;
	}
	public static boolean checkColumn(Connection con, String table, String field){
		ArrayList<String> columns=getTableColumns(con,table);
		for(int i=0;i<columns.size();i++){
			if(columns.get(i).compareTo(field)==0){
				//ManagerException.registraEccezione(new Exception("Campo '"+field+"' già presente nella tabella '"+table+"'"));
				return true;
			}
		}
		System.out.println(field+ " non presente");
		return false;
	}
	public static boolean alter_aggiungicampo(String table, String campo, String tipo, String default_v){
		ArrayList<String> columns=getTableColumns(table);
		for(int i=0;i<columns.size();i++){
			if(columns.get(i).compareTo(campo)==0){
				ManagerException.registraEccezione(new Exception("Campo '"+campo+"' già presente nella tabella '"+table+"'"));
				return false;
			}
		}
		
		String query="ALTER TABLE "+table+" ADD COLUMN "+campo+" "+tipo+(default_v.isEmpty()?"":(" DEFAULT "+default_v));
		try {
			Statement st=con.createStatement();
			st.executeUpdate(query);
			st.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			e.printStackTrace();
		}
		return false;
	}
	public static boolean isEmptyTable(String tableName){
		return rowCount(tableName)==0;
	}
	public static int rowCount(String table){
		String query="SELECT COUNT(*) FROM "+table;
		try {
			Statement stat=con.createStatement();
			ResultSet rs=stat.executeQuery(query);
			int count=0;
			while(rs.next()){
				count=rs.getInt(1);
			}
			rs.close();
			stat.close();
			return count;
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
			ManagerException.registraEccezione(e);
		}
		return -1;
	}
	public static ArrayList<KVResult<String,Object>> selectQuery(String query){
		//System.out.println(query);
		ArrayList<KVResult<String, Object>> result=new ArrayList<KVResult<String, Object>>();
		try {
			
			Statement stat=con.createStatement();
			ResultSet rs=stat.executeQuery(query);
			ResultSetMetaData meta=rs.getMetaData();
			while(rs.next()){
				KVResult<String, Object> res=new KVResult<String, Object>();
				for(int i=1;i<=meta.getColumnCount();i++){
					String key=meta.getColumnName(i);
					Object value=rs.getObject(i);
					res.addItem(new KVItem<String, Object>(key, value));
				}
				result.add(res);
			}
			rs.close();
			stat.close();
			return result;
		}
		catch(Exception e){
			System.out.println(query);
			System.out.println(e.getMessage());
			ManagerException.registraEccezione(e);
			return result;
		}
	}
	public static boolean updateQuery(String query){
		//System.out.println(query);
		int ins_ok=0;
		try {
			Statement stat=con.createStatement();
			ins_ok=stat.executeUpdate(query);
		}
		catch (SQLException e) {
			System.out.println("INSERT "+e.getMessage());
			System.out.println(query);
			e.printStackTrace();
			ManagerException.registraEccezione(query);
			ManagerException.registraEccezione(e);
		}
		return (ins_ok==0?false:true);
	}
	public static boolean updateQuery(Connection con,String query){
		//System.out.println(query);
		int ins_ok=0;
		try {
			Statement stat=con.createStatement();
			ins_ok=stat.executeUpdate(query);
		}
		catch (SQLException e) {
			System.out.println("INSERT "+e.getMessage());
			System.out.println(query);
			e.printStackTrace();
			ManagerException.registraEccezione(query);
			ManagerException.registraEccezione(e);
		}
		return (ins_ok==0?false:true);
	}
	public static void rebuildDB(){
		System.out.println("ottimizzazione db in corso...");
		Statement st;
		try {
			st = con.createStatement();
			st.execute("VACUUM");
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void rebuildDB(Connection con){
		System.out.println("ottimizzazione db in corso...");
		Statement st;
		try {
			st = con.createStatement();
			st.execute("VACUUM");
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static ArrayList<KVResult<String,Object>> selectQuery(Connection connection, String query){
		ArrayList<KVResult<String, Object>> result=new ArrayList<KVResult<String, Object>>();
		try {
			Statement stat=connection.createStatement();
			ResultSet rs=stat.executeQuery(query);
			ResultSetMetaData meta=rs.getMetaData();
			while(rs.next()){
				KVResult<String, Object> res=new KVResult<String, Object>();
				for(int i=1;i<=meta.getColumnCount();i++){
					String key=meta.getColumnName(i);
					Object value=rs.getObject(i);
					res.addItem(new KVItem<String, Object>(key, value));
				}
				result.add(res);
			}
			rs.close();
			stat.close();
			return result;
		}
		catch(SQLException e){
			System.out.println(e.getMessage());
			ManagerException.registraEccezione(e);
			return null;
		}
	}
	public static Object eseguiQuery(Connection conn,String query){
		if(query==null || query.isEmpty())
			return true;
		query=query.trim();
		if(query.startsWith("INSERT") || query.startsWith("DELETE") || query.startsWith("UPDATE") || query.startsWith("ALTER")){
			return updateQuery(conn,query);
		}
		else if(query.startsWith("SELECT")){
			return selectQuery(conn,query);
		}
		else {
			return null;
		}
	}
}
