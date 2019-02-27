package gst.serieTV.eztv;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gst.serieTV.EpisodioTorrent;

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
	
}
