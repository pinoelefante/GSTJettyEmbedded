package gst.serieTV;

import java.util.ArrayList;

public class Merger {
	public static ArrayList<SerieTV> mergeListsSerieTV(ArrayList<ArrayList<SerieTV>> liste){
		verificaListe(liste);
		ArrayList<SerieTV> unita = new ArrayList<SerieTV>();
		while(!liste.isEmpty()){
			int min = getMinSerie(liste);
			unita.add(liste.get(min).get(0));
			removeMin(liste, min);
		}
		return unita;
	}
	private static int getMinSerie(ArrayList<ArrayList<SerieTV>> liste){
		int min=0;
		for(int i=0;i<liste.size();i++){
			if(liste.get(i).get(0).getNomeSerie().compareToIgnoreCase(liste.get(min).get(0).getNomeSerie())<0){
				min = i;
			}
		}
		return min;
	}
	private static void removeMin(ArrayList<ArrayList<SerieTV>> liste, int indexList){
		liste.get(indexList).remove(0);
		if(liste.get(indexList).isEmpty())
			liste.remove(liste.get(indexList));
	}
	private static void verificaListe(ArrayList<ArrayList<SerieTV>> liste){
		if(liste==null || liste.isEmpty())
			return;
		for(int i=0;i<liste.size();){
			if(liste.get(i).isEmpty())
				liste.remove(i);
			else
				i++;
		}
	}
}
