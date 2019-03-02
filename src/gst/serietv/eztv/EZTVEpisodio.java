package gst.serietv.eztv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serietv.EpisodioTorrent;

@DatabaseTable(tableName="eztv_episodio")
public class EZTVEpisodio extends EpisodioTorrent<EZTVTorrent>
{
	@DatabaseField(columnName="id", generatedId=true)
	private int episodeId;
	@DatabaseField(columnName="serieId")
	private int serieId;
	public EZTVEpisodio() {
		super(0,0);
	}
	public EZTVEpisodio(int stagione, int episodio, int serieId)
	{
		super(stagione, episodio);
		setSerieId(serieId);
	}

	public int getEpisodeId()
	{
		return episodeId;
	}

	public void setEpisodeId(int episodeId)
	{
		this.episodeId = episodeId;
	}

	public int getSerieId()
	{
		return serieId;
	}

	public void setSerieId(int serieId)
	{
		this.serieId = serieId;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = super.getJson();
		obj.put("epId", getEpisodeId());
		obj.put("showId", getSerieId());
		return obj;
	}
	@Override
	public Element getXml()
	{
		Element doc = super.getXml();
		Element epId = new Element("epId");
		epId.addContent(getEpisodeId()+"");
		Element showId = new Element("showId");
		showId.addContent(getSerieId()+"");
		doc.addContent(epId);
		doc.addContent(showId);
		return doc;
	}
}
