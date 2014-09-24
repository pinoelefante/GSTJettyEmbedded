package gst.services;

import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;

import java.util.ArrayList;
import java.util.TimerTask;

public class TaskAssociaSerie extends TimerTask {
    
	@Override
	public void run() {
		System.out.println("Task - Associa serie");
		ArrayList<SerieTV> st=GestioneSerieTV.getInstance().getElencoSeriePreferite();
		for(int i=0;i<st.size();i++){
			SerieTV s=st.get(i);
			GestoreSottotitoli.getInstance().associaSerie(s);
		}
	}

}
