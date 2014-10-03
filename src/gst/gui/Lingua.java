package gst.gui;

public class Lingua {
	private String text, val;
	public Lingua(String k, String v){
		text=k;
		val=v;
	}
	public String toString(){
		return text;
	}
	public String getValue(){
		return val;
	}
}
