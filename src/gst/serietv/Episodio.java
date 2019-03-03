package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public class Episodio<ID> implements Comparable<Episodio<ID>>, XMLSerializable, JSONSerializable
{
	@DatabaseField(columnName="showId", id=true)
	private ID showId;
	@DatabaseField(columnName="stagione", id=true)
	private int stagione;
	@DatabaseField(columnName="episodio", id=true)
	private int episodio;
	
	public Episodio(ID showId, int stagione, int episodio)
	{
		setShowId(showId);
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
	public ID getShowId()
	{
		return showId;
	}

	public void setShowId(ID showId)
	{
		this.showId = showId;
	}

	@Override
	public int compareTo(Episodio<ID> o)
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
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Episodio))
			return false;
		Episodio<ID> ep = (Episodio<ID>)obj;
		return compareTo(ep) == 0;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = new JSONObject();
		obj.put("show", getShowId());
		obj.put("season", getStagione());
		obj.put("episode", getEpisodio());
		return obj;
	}
	@Override
	public Element getXml()
	{
		Element doc = new Element("episode");
		Element show = new Element("show");
		show.addContent(getShowId()+"");
		doc.addContent(show);
		Element season = new Element("season");
		season.addContent(getStagione()+"");
		doc.addContent(season);
		Element epNum = new Element("epNum");
		epNum.addContent(getEpisodio()+"");
		doc.addContent(epNum);
		return doc;
	}
}
