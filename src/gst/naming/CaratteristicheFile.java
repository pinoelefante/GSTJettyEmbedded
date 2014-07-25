package gst.naming;

public class CaratteristicheFile {
	private int stagione, episodio;
	private boolean hd720, repack, proper, dvdrip;
	
	public CaratteristicheFile() {}
	
	public int getStagione() {
		return stagione;
	}
	public void setStagione(int stagione) {
		this.stagione = stagione;
	}
	public int getEpisodio() {
		return episodio;
	}
	public void setEpisodio(int episodio) {
		this.episodio = episodio;
	}
	public boolean is720p() {
		return hd720;
	}
	public void set720p(boolean hd720) {
		this.hd720 = hd720;
	}
	public boolean isRepack() {
		return repack;
	}
	public void setRepack(boolean repack) {
		this.repack = repack;
	}
	public boolean isProper() {
		return proper;
	}
	public void setProper(boolean proper) {
		this.proper = proper;
	}
	public String toString(){
		return "Season: "+stagione+" Episode: "+episodio+"\n720p: "+hd720+"\nrepack: "+repack+"\nproper: "+proper;
	}
	private final static int HD=4, REPACK=2, PROPER=1;
	public int compareStats(CaratteristicheFile s){
		int val_this=value(this);
		int val_comp=value(s);
		if(val_this>val_comp)
			return 1;
		else if(val_this<val_comp)
			return -1;
		else
			return 0;
	}
	public int value(CaratteristicheFile s){
		int val=0;
		if(s.is720p())
			val+=HD;
		if(s.isRepack())
			val+=REPACK;
		if(s.isProper())
			val+=PROPER;
		return val;
	}
	public static int valueFromStat(boolean hd, boolean repack, boolean proper){
		int val=0;
		if(hd)
			val+=HD;
		if(repack)
			val+=REPACK;
		if(proper)
			val+=PROPER;
		return val;
	}
	public int value(){
		return value(this);
	}
	public boolean isDVDRip() {
		return dvdrip;
	}
	public void setDVDRip(boolean dvdrip) {
		this.dvdrip = dvdrip;
	}
	public void setStatsFromValue(int v){
		int val=v&1;
		//System.out.println("Value: "+v+" AND: "+val);
		setProper(val==1?true:false);
		v=v>>1;
		val=v&1;
		//System.out.println("Value: "+v+" AND: "+val);
		setRepack(val==1?true:false);
		v=v>>1;
		val=v&1;
		//System.out.println("Value: "+v+" AND: "+val);
		set720p(val==1?true:false);
	}
	public static void main(String[] args){
		CaratteristicheFile f=new CaratteristicheFile();
		f.setStatsFromValue(7);
		System.out.println("\n\n"+f);
	}
}
