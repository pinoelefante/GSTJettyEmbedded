package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public abstract class Episodio implements Comparable<Episodio>, XMLSerializable, JSONSerializable
{
	@DatabaseField(columnName="stagione")
	private int stagione;
	@DatabaseField(columnName="episodio")
	private int episodio;
	
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
		if(getStagione() == o.getStagione() && getEpisodio() == o.getEpisodio())
			return 0;
		if(getStagione() < o.getStagione())
			return -1;
		else if(getStagione() > o.getStagione())
			return 1;
		else
		{
			if(getEpisodio()<o.getEpisodio())
				return -1;
			else
				return 1;
		}
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
		Element doc = new Element("episode");
		Element season = new Element("season");
		season.addContent(getStagione()+"");
		doc.addContent(season);
		Element epNum = new Element("epNum");
		epNum.addContent(getEpisodio()+"");
		doc.addContent(epNum);
		return doc;
	}
}
