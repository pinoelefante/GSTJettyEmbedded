package gst.serietv;

import com.j256.ormlite.field.DatabaseField;

public abstract class Torrent implements Comparable<Torrent>
{
	@DatabaseField(columnName="episodeId")
	private int episodeId;
	@DatabaseField(columnName="url", id=true)
	private String url;
	@DatabaseField(columnName="resolution")
	private int resolution;
	@DatabaseField(columnName="proper")
	private boolean proper;
	@DatabaseField(columnName="repack")
	private boolean repack;
	@DatabaseField(columnName="preair")
	private boolean preair;
	@DatabaseField(columnName="videoSource")
	private String source;
	
	public Torrent() { }
	public Torrent(String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		this(0, url, resolution, proper, repack, preair, source);
	}
	public Torrent(int episodeId, String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		setEpisodeId(episodeId);
		setUrl(url);
		setResolution(resolution);
		setProper(proper);
		setRepack(repack);
		setPreair(preair);
		setSource(source);
	}
	
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public int getResolution()
	{
		return resolution;
	}
	public void setResolution(int resolution)
	{
		this.resolution = resolution;
	}
	public boolean isRepack()
	{
		return repack;
	}
	public void setRepack(boolean repack)
	{
		this.repack = repack;
	}
	public boolean isPreair()
	{
		return preair;
	}
	public void setPreair(boolean preair)
	{
		this.preair = preair;
	}
	public int getEpisodeId()
	{
		return episodeId;
	}
	public void setEpisodeId(int episodeId)
	{
		this.episodeId = episodeId;
	}
	public boolean isProper()
	{
		return proper;
	}
	public void setProper(boolean proper)
	{
		this.proper = proper;
	}
	public String getSource()
	{
		return source;
	}
	public void setSource(String ripSource)
	{
		this.source = ripSource;
	}
	@Override
	public String toString()
	{
		return String.format("%dp Source: %s Preair: %b Proper: %b Repack: %b", getResolution(), getSource(), isPreair(), isProper(), isRepack());
	}
	@Override
	public int compareTo(Torrent o)
	{
		return this.getUrl().compareTo(o.getUrl());
	}
}
