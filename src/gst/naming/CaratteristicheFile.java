package gst.naming;

public class CaratteristicheFile implements Comparable{
	private int stagione, episodio;
	private boolean hd2160, hd1080, hd720, repack, proper, dvdrip, preair;
	
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
	public void set1080p(boolean b) {
		this.hd1080 = b;
	}
	public boolean is1080p() {
		return hd1080;
	}
	public void set2160p(boolean b) {
		this.hd2160 = b;
	}
	public boolean is2160p() {
		return hd2160;
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
		return "Season: "+stagione+" Episode: "+episodio+"\n720p: "+hd720+"\n1080p: " + hd1080 + "\n2160p: " + hd2160 +"\nrepack: "+repack+"\nproper: "+proper;
	}
	private final static int ULTRA_HD = 16, FULL_HD = 8, HD=4, REPACK=2, PROPER=1;
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
		if (s.is2160p())
			val+= ULTRA_HD;
		if (s.is1080p())
			val+=FULL_HD;
		if(s.is720p())
			val+=HD;
		if(s.isRepack())
			val+=REPACK;
		if(s.isProper())
			val+=PROPER;
		return val;
	}
	public static int valueFromStat(boolean uhd, boolean fhd, boolean hd, boolean repack, boolean proper){
		int val=0;
		if (uhd)
			val+=ULTRA_HD;
		if (fhd)
			val+=FULL_HD;
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
		v=v>>1;
		val=v&1;
		set1080p(val == 1);
		v=v>>1;
		val=v&1;
		set2160p(val == 1);
	}
	public static void main(String[] args){
		CaratteristicheFile f=new CaratteristicheFile();
		f.setStatsFromValue(19);
		System.out.println(f);
	}

	public boolean isPreair() {
		return preair;
	}

	public void setPreair(boolean preair) {
		this.preair = preair;
	}

	@Override
	public int compareTo(Object o)
	{
		CaratteristicheFile other = (CaratteristicheFile) o;
		return this.value() - other.value();
	}
}
