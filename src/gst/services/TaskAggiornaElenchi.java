package gst.services;

import gst.sottotitoli.GestoreSottotitoli;

import java.util.TimerTask;

public class TaskAggiornaElenchi extends TimerTask {

	@Override
	public void run() {
		System.out.println("Aggiornamento elenchi");
		GestoreSottotitoli.getInstance().aggiornaElenco(GestoreSottotitoli.ITASA);
		GestoreSottotitoli.getInstance().aggiornaElenco(GestoreSottotitoli.SUBSFACTORY);
		GestoreSottotitoli.getInstance().aggiornaElenco(GestoreSottotitoli.SUBSPEDIA);
	}

}
