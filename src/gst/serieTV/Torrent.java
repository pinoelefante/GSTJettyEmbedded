package gst.serieTV;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;

public class Torrent implements Comparable {
	 
	private String	url;
	private CaratteristicheFile prop_torrent;
	private int id, idEpisodio;
	
	public Torrent(String link, int idDB, int idEpisodio) {
		setUrl(link);
		setId(idDB);
		setIdEpisodio(idEpisodio);
		prop_torrent=Torrent.parse(getUrl());
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public CaratteristicheFile getCaratteristiche() {
		return prop_torrent;
	}
	public void setCaratteristiche(CaratteristicheFile prop_torrent) {
		this.prop_torrent = prop_torrent;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdEpisodio() {
		return idEpisodio;
	}
	public void setIdEpisodio(int idEpisodio) {
		this.idEpisodio = idEpisodio;
	}
	public static CaratteristicheFile parse(String url) {
		String[] patt=new String[]{
				Naming.PATTERN_SnEn,
				Naming.PATTERN_SxE,
				Naming.PATTERN_Part_dotnofn,
				Naming.PATTERN_nofn,
				Naming.PATTERN_DATA,
				Naming.PATTERN_EP_ALL_NUM
		};
		CaratteristicheFile stat = null;
		if(isMagnet(url))
			stat=Naming.parse(getNameFromMagnet(url), patt);
		else
			stat=Naming.parse(url, patt);
		return stat;
	}
	private static String getNameFromMagnet(String url) {
		String[] pts = url.split("&");
		for(int i=0;i<pts.length;i++){
			String[] kv = pts[i].split("=");
			if(kv[0].compareToIgnoreCase("dn")==0)
				return kv[1];
		}
		return url;
	}
	private static boolean isMagnet(String link){
		return link.toLowerCase().startsWith("magnet");
	}
	public CaratteristicheFile getStats(){
		return prop_torrent;
	}
	public void setStats(int val){
		prop_torrent.setStatsFromValue(val);
	}

	@Override
	public int compareTo(Object o)
	{
		Torrent other = (Torrent) o;
		if (this.url.equals(other.getUrl()))
			return 0;
		return -this.getStats().compareTo(other.getStats());
	}
}
