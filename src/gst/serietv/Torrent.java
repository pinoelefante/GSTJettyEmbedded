package gst.serietv;

import org.jdom.Element;
import org.json.simple.JSONObject;

import com.j256.ormlite.field.DatabaseField;

public abstract class Torrent implements Comparable<Torrent>, XMLSerializable, JSONSerializable
{
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
	@DatabaseField(columnName="season")
	private int season;
	@DatabaseField(columnName="episode")
	private int episode;
	@DatabaseField(columnName="showId")
	private int showId;
	
	private final static int PROPER = 4, REPACK = 2, PREAIR = 1;
	
	public Torrent() { }
	public Torrent(int showId, int season, int episode, String url, int resolution, boolean proper, boolean repack, boolean preair, String source)
	{
		setShowId(showId);
		setSeason(season);
		setEpisode(episode);
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
	
	public int getSeason()
	{
		return season;
	}
	public void setSeason(int season)
	{
		this.season = season;
	}
	public int getEpisode()
	{
		return episode;
	}
	public void setEpisode(int episode)
	{
		this.episode = episode;
	}
	public int getShowId()
	{
		return showId;
	}
	public void setShowId(int showId)
	{
		this.showId = showId;
	}
	@Override
	public String toString()
	{
		return String.format("%dp Source: %s Preair: %b Proper: %b Repack: %b", getResolution(), getSource(), isPreair(), isProper(), isRepack());
	}
	@Override
	public int compareTo(Torrent o)
	{
		if(getResolution() == o.getResolution())
		{
			int maskDiff = o.getMask()-getMask(); 
			if(maskDiff == 0)
				return getUrl().compareTo(o.getUrl());
			return maskDiff;
		}
		return o.getResolution()-getResolution();
	}
	@Override
	public boolean equals(Object obj)
	{
		return getUrl().compareTo(((Torrent)obj).getUrl()) == 0;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = new JSONObject();
		obj.put("url", getUrl());
		obj.put("showId", getShowId());
		obj.put("season", getSeason());
		obj.put("episode", getEpisode());
		obj.put("resolution", getResolution());
		obj.put("proper", isProper());
		obj.put("repack", isRepack());
		obj.put("preair", isPreair());
		obj.put("source", getSource());
		return obj;
	}
	@Override
	public Element getXml()
	{
		Element torrent = new Element("torrent");
		Element url = new Element("url");
		url.addContent(getUrl());
		Element resolution = new Element("resolution");
		resolution.addContent(getResolution()+"");
		Element proper = new Element("proper");
		proper.addContent(isProper()+"");
		Element repack = new Element("repack");
		repack.addContent(isRepack()+"");
		Element preair = new Element("preair");
		preair.addContent(isPreair()+"");
		Element source = new Element("source");
		source.addContent(getSource());
		Element showId = new Element("showId");
		source.addContent(getShowId()+"");
		Element season = new Element("season");
		source.addContent(getSeason()+"");
		Element episode = new Element("episode");
		source.addContent(getEpisode()+"");
		torrent.addContent(url);
		torrent.addContent(showId);
		torrent.addContent(season);
		torrent.addContent(episode);
		torrent.addContent(resolution);
		torrent.addContent(proper);
		torrent.addContent(repack);
		torrent.addContent(preair);
		torrent.addContent(source);
		return torrent;
	}
	private int getMask()
	{
		int mask = (isProper() ? PROPER : 0) + (isRepack() ? REPACK : 0) + (isPreair() ? PREAIR : 0);
		return mask;
	}
}
