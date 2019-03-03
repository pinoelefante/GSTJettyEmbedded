package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

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
	@DatabaseField(columnName = "favourite")
	private boolean favourite;
	@DatabaseField(columnName = "lastUpdate", defaultValue="0")
	private long lastUpdate;
	@DatabaseField(columnName = "resolution", defaultValue="720")
	private int favouriteResolution;

	public SerieTVComposer() { }

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

	public boolean isFavourite()
	{
		return favourite;
	}

	public void setFavourite(boolean favourite)
	{
		this.favourite = favourite;
	}

	public long getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public int getFavouriteResolution()
	{
		return favouriteResolution;
	}

	public void setFavouriteResolution(int favouriteResolution)
	{
		this.favouriteResolution = favouriteResolution;
	}

	@Override
	public Element getXml()
	{
		Element doc = super.getXml();
		Element id = new Element("id");
		id.addContent(getId() + "");
		doc.addContent(id);
		/*
		Element eztv = new Element("eztv");
		eztv.addContent(getEztv() + "");
		doc.addContent(eztv);
		Element showrss = new Element("showrss");
		showrss.addContent(getShowrss() + "");
		doc.addContent(showrss);
		*/
		Element fav = new Element("fav");
		fav.addContent(isFavourite()+"");
		doc.addContent(fav);
		return doc;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = super.getJson();
		obj.put("id", getId());
		obj.put("fav", isFavourite());
		// obj.put("eztv", getEztv());
		// obj.put("showrss", getShowrss());
		return obj;
	}
}