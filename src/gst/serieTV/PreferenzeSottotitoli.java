package gst.serieTV;

public class PreferenzeSottotitoli {
	private String preferenze;
	
	public PreferenzeSottotitoli(String pref){
		preferenze = pref;
	}
	public String[] getPreferenze(){
		return preferenze.split("\\|");
	}
	public void addPreferenza(String lang){
		if(!hasLanguage(lang)){
			preferenze+="|"+lang;
		}
	}
	public boolean hasLanguage(String lang){
		String[] list = getPreferenze();
		boolean found = false;
		for(int i=0;i<list.length && !found;i++){
			if(list[i].compareToIgnoreCase(lang)==0)
				found=true;
		}
		return found;
	}
	public String getPreferenzeU() {
		return preferenze;
	}
}
