package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public abstract class SerieTV implements XMLSerializable, JSONSerializable, Comparable<SerieTV>
{
	@DatabaseField(columnName="titolo")
	private String titolo;

	public SerieTV() {}
	
	public SerieTV(String titolo)
	{
		setTitolo(titolo);
	}
	
	public String getTitolo()
	{
		return titolo;
	}

	public void setTitolo(String titolo)
	{
		this.titolo = titolo;
	}

	@Override
	public Element getXml()
	{
		Element serie_tag=new Element("serie");
		Element nome = new Element("name");
		nome.addContent(getTitolo());
		serie_tag.addContent(nome);
		return serie_tag;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = new JSONObject();
		obj.put("name", getTitolo());
		return obj;
	}
	
	@Override
	public int compareTo(SerieTV o)
	{
		return getTitolo().compareToIgnoreCase(o.getTitolo());
	}
}
