package gst.sottotitoli.rss;

import gst.naming.CaratteristicheFile;
import gst.naming.Naming;

public class SubspediaRSSItem {
	private String titolo;
	private String link;
	private CaratteristicheFile stats;
	private static final String[] patterns_file={Naming.PATTERN_SxE};
	
	public SubspediaRSSItem(String titolo, String link){
		this.titolo=tryToCutTheTitle(titolo);
		if(this.titolo.isEmpty())
			this.titolo=titolo;
		this.link=link;
		stats=Naming.parse(titolo, patterns_file);
	}
	public int getStagione(){
		return stats.getStagione();
	}
	public int getEpisodio() {
		return stats.getEpisodio();
	}
	public boolean isStessaSerie(String toCompare){
		return this.titolo.toLowerCase().compareTo(toCompare.toLowerCase().trim())==0;
	}
	public String toString(){
		return titolo+"\nStagione: "+getStagione()+" Episodio: "+getEpisodio()+"\nLink: "+getLink()+"\n";
	}
	private String tryToCutTheTitle(String titolo){
		String title="";
		if(titolo.contains(" ")){
			title=titolo.substring(0, titolo.lastIndexOf(" ")).trim();
			if(title.endsWith("-")){
				title=title.substring(0, title.length()-2).trim();
			}
		}
		return title;
	}
	public String getLink(){
		return link;
	}
	public String getTitolo(){
		return titolo;
	}
}