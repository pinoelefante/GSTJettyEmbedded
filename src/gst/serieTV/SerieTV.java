package gst.serieTV;

import org.jdom.Element;

import com.j256.ormlite.field.DatabaseField;

public abstract class SerieTV implements XMLSerializable
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
}
