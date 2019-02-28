package gst.serietv.showrss;

import org.jdom.Element;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.Identifier;
import gst.serietv.SerieTV;

@DatabaseTable(tableName="showrss_serie")
public class ShowRSSSerieTV extends SerieTV implements Identifier<Integer>
{
	@DatabaseField(columnName="id", id=true)
	private int id;
	
	public ShowRSSSerieTV() {}
	public ShowRSSSerieTV(String name, int id)
	{
		super(name);
		setId(id);
	}
	
	@Override
	public Element getXml()
	{
		Element doc = super.getXml();
		Element onlineId = new Element("showrss_id");
		onlineId.addContent(getId()+"");
		doc.addContent(onlineId);
		return doc;
	}
	@Override
	public Integer getId()
	{
		return id;
	}
	@Override
	public void setId(Integer id)
	{
		this.id = id;
	}
}
