package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Episodio implements Comparable<Episodio>, XMLSerializable, JSONSerializable
{
	@DatabaseField(columnName="stagione", uniqueCombo=true)
	private int stagione;
	@DatabaseField(columnName="episodio", uniqueCombo=true)
	private int episodio;
	
	public Episodio() { }
	public Episodio(int stagione, int episodio)
	{
		setStagione(stagione);
		setEpisodio(episodio);
	}

	public int getStagione()
	{
		return stagione;
	}

	public void setStagione(int stagione)
	{
		this.stagione = stagione;
	}

	public int getEpisodio()
	{
		return episodio;
	}

	public void setEpisodio(int episodio)
	{
		this.episodio = episodio;
	}

	@Override
	public int compareTo(Episodio o)
	{
		int seasonCompare = Integer.compare(getStagione(), o.getStagione());
		if(seasonCompare == 0)
			return getEpisodio()-o.getEpisodio();
		return seasonCompare;
	}
	@Override
	public String toString()
	{
		return String.format("S%02dE%02d", getStagione(), getEpisodio());
	}
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Episodio))
			return false;
		Episodio ep = (Episodio)obj;
		return compareTo(ep) == 0;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = new JSONObject();
		obj.put("season", getStagione());
		obj.put("episode", getEpisodio());
		return obj;
	}
	@Override
	public Element getXml()
	{
		Element doc = new Element("ep");
		Element show = new Element("show");
		doc.addContent(show);
		Element season = new Element("season");
		season.addContent(getStagione()+"");
		doc.addContent(season);
		Element epNum = new Element("episode");
		epNum.addContent(getEpisodio()+"");
		doc.addContent(epNum);
		return doc;
	}
}
