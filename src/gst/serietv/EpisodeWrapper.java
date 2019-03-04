package gst.serietv;

import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="episode")
public class EpisodeWrapper extends Episodio
{
	@DatabaseField(columnName="id", generatedId=true)
	private int id;
	@DatabaseField(columnName="showId", uniqueCombo=true)
	private int showId;
	@DatabaseField(columnName="status")
	private EpisodeStatusEnum status = EpisodeStatusEnum.DA_SCARICARE;
	@DatabaseField(columnName="viewPercent")
	private int viewPercent = 0;
	
	public EpisodeWrapper() { }
	
	public EpisodeWrapper(int stagione, int episodio)
	{
		super(stagione, episodio);
	}
	public EpisodeWrapper(int showId, int stagione, int episodio)
	{
		super(stagione, episodio);
		setShowId(showId);
	}

	public int getShowId()
	{
		return showId;
	}

	public void setShowId(int showId)
	{
		this.showId = showId;
	}

	public EpisodeStatusEnum getStatus()
	{
		return status;
	}

	public void setStatus(EpisodeStatusEnum status)
	{
		this.status = status;
	}

	public int getPercent()
	{
		return viewPercent;
	}

	public void setPercent(int percent)
	{
		this.viewPercent = percent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject o = super.getJson();
		o.put("status", getStatus().ordinal());
		return o;
	}
}
