package gst.serieTV;

import java.util.ArrayList;

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
	public void removePreferenza(String lang){
		String[] p = getPreferenze();
		String newPrefs = "";
		for(int i=0;i<p.length;i++){
			if(p[i].compareTo(lang)==0)
				continue;
			else{
				newPrefs += p[i];
				if(i<p.length-1)
					newPrefs+="|";
			}
		}
		preferenze = newPrefs;
	}
	public ArrayList<String> getNewLangs(String langs){
		String[] lingue = langs.split("\\|");
		ArrayList<String> nuove=new ArrayList<String>();
		for(int i=0;i<lingue.length;i++){
			if(!hasLanguage(lingue[i])){
				nuove.add(lingue[i]);
			}
		}
		return nuove;
	}
	public ArrayList<String> getRemovedLangs(String langs){
		String[] current = getPreferenze();
		String[] newLangs = langs.split("\\|");
		ArrayList<String> rimosse = new ArrayList<String>();
		for(int i=0;i<current.length;i++){
			String c_lang = current[i];
			boolean found = false;
			for(int j=0;!found && j<newLangs.length;j++){
				if(newLangs[j].compareTo(c_lang)==0){
					found = true;
				}
			}
			if(!found)
				rimosse.add(c_lang);
		}
		return rimosse;
	}
}
