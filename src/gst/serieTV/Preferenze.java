package gst.serieTV;

import gst.programma.Settings;

public class Preferenze {
	private boolean download_preair;
	private boolean scarica_hd;
	private boolean scarica_tutto;
	
	public static int TUTTO=4,HD=2, PREAIR=1;
	
	public int toValue(){
		int value=0;
		if(download_preair)
			value+=PREAIR;
		
		if(scarica_hd)
			value+=HD;
		
		if(scarica_tutto)
			value+=TUTTO;
		
		return value;
	}
	public Preferenze(int value){
		setFromValue(value);
	}
	public Preferenze(){
		setFromValue(Settings.getInstance().getRegolaDownloadDefault());
	}
	public boolean isPreferisciHD(){
		return scarica_hd;
	}
	public void setPreferisciHD(boolean b){
		scarica_hd=b;
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
		setScaricaTutto(res==1?true:false);
		value=value>>1;
	}
}
