package gst.sottotitoli;

import gst.database.Database;
import gst.database.tda.KVResult;

import java.util.ArrayList;

public class SerieSubConDirectory extends SerieSub {
	private String directory;
	
	public SerieSubConDirectory(String nome, int iddb, String directory) {
		super(nome, iddb);
		this.directory=directory;
	}
	public SerieSubConDirectory(int id){
		super("",id);
	}
	public int getIDDB() {
		if(super.getIDDB()<=0){
			String query="SELECT id FROM "+Database.TABLE_SUBSFACTORY+" WHERE directory=\""+getDirectory()+"\"";
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				setIDDB(((int)res.get(0).getValueByKey("id")));
			}
		}
		return super.getIDDB();
	}
	public String getDirectory(){
		if(directory==null || directory.isEmpty()){
			String query="SELECT directory FROM "+Database.TABLE_SUBSFACTORY+" WHERE id="+getIDDB();
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				setDirectory(((String)res.get(0).getValueByKey("directory")));
			}
		}
		return directory;
	}
	public void setDirectory(String dir){
		directory=dir;
	}
	@Override
	public String getNomeSerie() {
		if(super.getNomeSerie().isEmpty()){
			String query = "SELECT nome FROM "+Database.TABLE_SUBSFACTORY+" WHERE id="+getIDDB();
			ArrayList<KVResult<String, Object>> res=Database.selectQuery(query);
			if(res.size()==1){
				super.setNomeSerie((String)(res.get(0).getValueByKey("nome")));
			}
		}
		return super.getNomeSerie();
	}
}
