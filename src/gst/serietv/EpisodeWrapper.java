package gst.serietv;

import util.Tuple;

public class EpisodeWrapper extends Tuple<Integer,Integer>
{
	public EpisodeWrapper(Integer k, Integer v)
	{
		super(k, v, "season", "episode");
	}
	public Integer getSeason()
	{
		return getElement1();
	}
	public Integer getEpisode()
	{
		return getElement2();
	}
	public void setSeason(Integer v)
	{
		setElement1(v);
	}
	public void setEpisode(Integer v)
	{
		setElement2(v);
	}
}
