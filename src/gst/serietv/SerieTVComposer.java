package gst.serietv;

import org.jdom.Element;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "serie_tv")
public class SerieTVComposer extends SerieTV implements XMLSerializable
{
	@DatabaseField(columnName = "id", generatedId = true)
	private int	id;
	@DatabaseField(columnName = "eztv")
	private int	eztv;
	@DatabaseField(columnName = "showrss")
	private int	showrss;

	public SerieTVComposer()
	{
	}

	public SerieTVComposer(int eztv, int showrss, String titolo)
	{
		setEztv(eztv);
		setShowrss(showrss);
		setTitolo(titolo);
	}

	public int getEztv()
	{
		return eztv;
	}

	public void setEztv(int eztv)
	{
		this.eztv = eztv;
	}

	public int getShowrss()
	{
		return showrss;
	}

	public void setShowrss(int showrss)
	{
		this.showrss = showrss;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Override
	public Element getXml()
	{
		Element doc = super.getXml();
		Element id = new Element("id");
		id.addContent(getId() + "");
		doc.addContent(id);
		Element eztv = new Element("eztv");
		eztv.addContent(getEztv() + "");
		doc.addContent(eztv);
		Element showrss = new Element("showrss");
		showrss.addContent(getShowrss() + "");
		doc.addContent(showrss);
		return doc;
	}
}