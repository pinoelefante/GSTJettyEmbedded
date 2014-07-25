package gst.sottotitoli;

import gst.tda.db.KVResult;

import java.util.ArrayList;

import Database.Database;

public class SerieSubSubsfactory extends SerieSub {
	private boolean directory_search;
	private int id_db;
	private String directory;
	private ArrayList<SottotitoloSubsfactory> subs_subsfactory;
	private boolean folder_loaded=false;
	
	public SerieSubSubsfactory(String nome, int iddb, String directory) {
		super(nome, iddb);
		subs_subsfactory=new ArrayList<SottotitoloSubsfactory>();
		this.directory=directory;
	}
	public boolean isDirectorySearch(){
		return directory_search;
	}
	public void setDirectorySearch(boolean stat){
		directory_search=stat;
	}
	public void setIDDB(int i){
		id_db=i;
	}
	public int getIDDB() {
		if(id_db<=0){
			String query="SELECT id FROM "+Database.TABLE_SUBSFACTORY+" WHERE nome_serie=\""+getNomeSerie()+"\" AND directory=\""+getDirectory()+"\"";
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				setIDDB(((int)res.get(0).getValueByKey("id")));
			}
		}
		return id_db;
	}
	public String getDirectory(){
		return directory;
	}
	public boolean isCartellaOnlineCaricata(){
		return folder_loaded;
	}
	public void setCartellaOnlineCaricata(){
		folder_loaded=true;
	}
	public int getCartellaOnlineSize(){
		return subs_subsfactory.size();
	}
	public SottotitoloSubsfactory getSubFromCartellaOnline(int index){
		if(index>=0 && index<subs_subsfactory.size())
			return subs_subsfactory.get(index);
		return null;
	}
	public void addSub(SottotitoloSubsfactory s){
		subs_subsfactory.add(s);
	}
}
