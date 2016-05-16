package gst.infoManager.thetvdb;

public class Mirror {
	private int id, mask;
	private String url;
	private boolean isXML, isBanner, isZip;
	
	public Mirror(int id, String url, int mask){
		this.setId(id);
		this.setUrl(url);
		this.setMask(mask);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
		retrieveMasks(mask);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	private void retrieveMasks(int mask){
		int m=mask&1;
		isXML=(m==1?true:false);
		mask=mask>>1;
		m=mask&1;
		isBanner=(m==1?true:false);
		mask=mask>>1;
		m=mask&1;
		isZip=(m==1?true:false);
	}
	public boolean isXML(){
		return isXML;
	}
	public boolean isBanner(){
		return isBanner;
	}
	public boolean isZip(){
		return isZip;
	}
	public String toString(){
		return "ID: "+id+"\nMirror-path: "+url+"\nTypemask: "+mask+"\n"+
				"XML:"+isXML+"\nBanner:"+isBanner+"\nZip:"+isZip+"\n";
	}
}
