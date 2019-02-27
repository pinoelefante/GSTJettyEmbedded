package gst.serieTV.showrss;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serieTV.EpisodioTorrent;

@DatabaseTable(tableName="showrss_episodio")
public class ShowRSSEpisodio extends EpisodioTorrent<ShowRSSTorrent>
{
	@DatabaseField(columnName="serieId")
	private int serieId;
	@DatabaseField(columnName="episodioId", generatedId=true)
	private int episodioId;
	
	public ShowRSSEpisodio()
	{
		super(0,0);
	}
	public ShowRSSEpisodio(int stagione, int episodio, int serieId)
	{
		super(stagione, episodio);
		setSerieId(serieId);
	}
	public int getSerieId()
	{
		return serieId;
	}
	public void setSerieId(int serieId)
	{
		this.serieId = serieId;
	}
	public int getEpisodioId()
	{
		return episodioId;
	}
	public void setEpisodioId(int episodioId)
	{
		this.episodioId = episodioId;
	}

}
