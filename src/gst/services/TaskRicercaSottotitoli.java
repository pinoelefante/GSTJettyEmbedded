package gst.services;

import gst.serieTV.Episodio;
import gst.sottotitoli.GestoreSottotitoli;

import java.util.ArrayList;
import java.util.TimerTask;

public class TaskRicercaSottotitoli extends TimerTask {

	@Override
	public void run() {
		System.out.println("Task - Ricerca sottotitoli");
		ArrayList<Episodio> episodi = GestoreSottotitoli.getInstance().getSottotitoliDaScaricare();
		for(int i=0;i<episodi.size();i++){
			GestoreSottotitoli.getInstance().scaricaSottotitolo(episodi.get(i));
		}
		episodi.clear();
		episodi=null;
	}

}
