package gst.serieTV;

import gst.programma.Settings;

public class Preferenze {
	private boolean download_preair;
	private boolean scarica_hd;
	private boolean scarica_tutto;
	private boolean scarica_full_hd;
	private boolean scarica_ultra_hd;
	
	public static int TUTTO=256, ULTRA_HD = 8, FULL_HD = 4,HD=2, PREAIR=1;
	
	public int toValue(){
		int value=0;
		if(download_preair)
			value+=PREAIR;
		
		if(scarica_hd)
			value+=HD;
		
		if(scarica_tutto)
			value+=TUTTO;
		if (scarica_full_hd)
			value += FULL_HD;
		if (scarica_ultra_hd)
			value += ULTRA_HD;
		
		return value;
	}
	public Preferenze(int value){
		setFromValue(value);
	}
	public Preferenze(){
		setFromValue(Settings.getInstance().getRegolaDownloadDefault());
	}
	public String toString() {
		return "Tutto: " + scarica_tutto + "\n" + 
				"PreAir: " + download_preair + "\n" +
				"HD: " + scarica_hd + "\n" +
				"FullHD: " + scarica_full_hd + "\n" +
				"UltraHD: " + scarica_ultra_hd + "\n";
	}
	public boolean isPreferisciHD(){
		return scarica_hd;
	}
	public boolean isPreferisciFullHD(){
		return scarica_full_hd;
	}
	public boolean isPreferisciUltraHD(){
		return scarica_ultra_hd;
	}
	public void setPreferisciHD(boolean b){
		scarica_hd=b;
	}
	public void setPreferisciFullHD(boolean b){
		scarica_full_hd=b;
	}
	public void setPreferisciUltraHD(boolean b){
		scarica_ultra_hd=b;
	}
	public boolean isDownloadPreair(){
		return download_preair;
	}
	public void setDownloadPreair(boolean b){
		download_preair=b;
	}
	public void setScaricaTutto(boolean s){
		scarica_tutto=s;
	}
	public boolean isScaricaTutto(){
		return scarica_tutto;
	}
	public void setFromValue(int value){
		int res=value&1;
		setDownloadPreair(res==1?true:false);
		value=value>>1;
		res=value&1;
		setPreferisciHD(res==1?true:false);
		value=value>>1;
		res=value&1;
		setPreferisciFullHD(res == 1);
		value=value>>1;
		res=value&1;
		setPreferisciUltraHD(res == 1);
		
		value=value>>1;
		value=value>>1;
		value=value>>1;
		value=value>>1;
		value=value>>1;
		res=value&1;
		setScaricaTutto(res==1?true:false);
		value=value>>1;
	}
}
