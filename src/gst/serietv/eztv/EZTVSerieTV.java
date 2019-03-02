package gst.serietv.eztv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.Identifier;
import gst.serietv.SerieTV;

@DatabaseTable(tableName="eztv_serie")
public class EZTVSerieTV extends SerieTV implements Identifier<Integer>
{
	@DatabaseField(columnName="id", id=true)
	private int id;

	public EZTVSerieTV() {
		super();
	}
	public EZTVSerieTV(String name, int id)
	{
		super(name);
		setId(id);
	}

	
	public Element getXml()
	{
		Element tag = super.getXml();
		Element eztv_id = new Element("id");
		eztv_id.addContent(getId()+"");
		tag.addContent(eztv_id);
		return tag;
	}
	@Override
	public Integer getId()
	{
		return this.id;
	}
	@Override
	public void setId(Integer id)
	{
		this.id = id;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = super.getJson();
		obj.put("id", getId());
		return obj;
	}
}
