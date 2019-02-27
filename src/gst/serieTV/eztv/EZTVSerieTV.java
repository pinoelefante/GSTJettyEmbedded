package gst.serieTV.eztv;

import org.jdom.Element;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serieTV.Identifier;
import gst.serieTV.SerieTV;

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
		Element eztv_id = new Element("eztv_id");
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
}
