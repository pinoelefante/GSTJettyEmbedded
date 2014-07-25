package gst.tda.serietv;

import gst.programma.Settings;
import gst.serieTV.SerieTV;
import gst.serieTV.Torrent;

import java.util.ArrayList;

public class Episodio {
	private int episodio, stagione;
	private SerieTV serietv;
	
	public final static int INDEX_HD=1, INDEX_PRE=2, INDEX_SD=3; 
	private ArrayList<Torrent> ep_hd, ep_normali, ep_preair;
	
	public Episodio(SerieTV s, int stagione, int episodio){
		serietv=s;
		this.stagione=stagione;
		this.episodio=episodio;
		ep_hd=new ArrayList<Torrent>(1);
		ep_normali=new ArrayList<Torrent>(1);
		ep_preair=new ArrayList<Torrent>(1);
	}
	public int getStagione(){
		return stagione;
	}
	public int getEpisodio(){
		return episodio;
	}
	public SerieTV getSerieTV(){
		return serietv;
	}
	
	public void addLink(Torrent link){		
		if(link.isPreAir()){
			if(addLinkToList(ep_preair, link)){
				link.updateTorrentInDB();
			}
		}
		else if(link.is720p()){
			if(addLinkToList(ep_hd, link)){
				link.updateTorrentInDB();
			}
		}
		else {
			if(addLinkToList(ep_normali, link)){
				link.updateTorrentInDB();
			}
		}
		checkStatus(link);
	}
	public void addLinkFromDB(Torrent link){
		if(link.isPreAir()){
			addLinkToList(ep_preair, link);
		}
		else if(link.is720p()){
			addLinkToList(ep_hd, link);
		}
		else {
			addLinkToList(ep_normali, link);
		}
		checkStatus(link);
	}
	private boolean addLinkToList(ArrayList<Torrent> elenco, Torrent link){
		if(elenco.size()==0){
			elenco.add(link);
			return true;
		}
		else {
			boolean inserito=false;
			for(int i=0;i<elenco.size();i++){
				Torrent t=elenco.get(i);
				if(t.isMagnetLink() && link.isMagnetLink()){
					String HashNew=Torrent.getMagnetHash(link.getUrl());
					if(t.compareHash(HashNew)){
						String trackers_old=Torrent.getMagnetTrackers(t.getUrl());
						String trackers_new=Torrent.getMagnetTrackers(link.getUrl());
						if(trackers_old.compareToIgnoreCase(trackers_new)!=0){
							t.magnetAppendTrackers(trackers_new);
							t.updateTorrentInDB();
						}
						return false;
					}
				}
				if(t.getUrl().compareTo(link.getUrl())==0)
					return false;
				else {
					if(link.getStats().compareStats(t.getStats())>0){
						elenco.add(i, link);
						return true;
					}
				}
			}
			if(!inserito){
				elenco.add(link);
			}
			return true;
		}
	}
	public boolean isRimosso(){
		ArrayList<Torrent> tutti=new ArrayList<Torrent>();
		if(serietv.getPreferenze().isScaricaTutto()){
			if(getLinkHD()!=null)
				tutti.add(getLinkHD());
			if(getLinkNormale()!=null)
				tutti.add(getLinkNormale());
			if(getLinkPreair()!=null)
				tutti.add(getLinkPreair());
		}
		else {
			if(serietv.getPreferenze().isPreferisciHD()){
				if(getLinkHD()!=null)
					tutti.add(getLinkHD());
			}
			if(getLinkNormale()!=null)
				tutti.add(getLinkNormale());
			if(serietv.getPreferenze().isDownloadPreair()){
				if(getLinkPreair()!=null)
					tutti.add(getLinkPreair());
			}
		}
		if(tutti.isEmpty())
			return true;
		for(int i=0;i<tutti.size();i++){
			Torrent t=tutti.get(i);
			switch(t.getScaricato()){
				case Torrent.SCARICARE:
				case Torrent.SCARICATO:
				case Torrent.VISTO:
				case Torrent.IGNORATO:
					return false;
			}
		}
		return true;
	}
	public boolean isIgnorato(){
		ArrayList<Torrent> tutti=new ArrayList<Torrent>();
		if(serietv.getPreferenze().isPreferisciHD()){
			if(getLinkHD()!=null)
				tutti.add(getLinkHD());
		}
		if(getLinkNormale()!=null)
			tutti.add(getLinkNormale());
		if(serietv.getPreferenze().isDownloadPreair()){
			if(getLinkPreair()!=null)
				tutti.add(getLinkPreair());
		}
	
		if(tutti.isEmpty())
			return true;
		for(int i=0;i<tutti.size();i++){
			Torrent t=tutti.get(i);
			switch(t.getScaricato()){
				case Torrent.SCARICARE:
				case Torrent.SCARICATO:
				case Torrent.VISTO:
				case Torrent.RIMOSSO:
					return false;
			}
		}
		return true;
	}
	public boolean isScaricato(){
		ArrayList<Torrent> tutti=new ArrayList<Torrent>();
		boolean scarica_tutto=serietv.getPreferenze().isScaricaTutto();
		if(serietv.getPreferenze().isScaricaTutto()){
			if(getLinkHD()!=null)
				tutti.add(getLinkHD());
			if(getLinkNormale()!=null)
				tutti.add(getLinkNormale());
			if(getLinkPreair()!=null)
				tutti.add(getLinkPreair());
		}
		else {
			if(serietv.getPreferenze().isPreferisciHD()){
				if(getLinkHD()!=null)
					tutti.add(getLinkHD());
			}
			if(getLinkNormale()!=null)
				tutti.add(getLinkNormale());
			if(serietv.getPreferenze().isDownloadPreair())
				if(getLinkPreair()!=null)
					tutti.add(getLinkPreair());
		}
		if(tutti.isEmpty())
			return true;
		
		int count=0;
		for(int i=0;i<tutti.size();i++){
			Torrent t=tutti.get(i);
			switch(t.getScaricato()){
				case Torrent.SCARICATO:
				case Torrent.VISTO:
				case Torrent.RIMOSSO:
				case Torrent.IGNORATO:
					if(scarica_tutto)
						count++;
					else
						return true;
			}
		}
		if(scarica_tutto){
			if(count==tutti.size())
				return true;
			else
				return false;
		}
		else
			return false;
	}
	public void scaricaLink(Torrent link){
		ArrayList<Torrent> tutti=new ArrayList<Torrent>();
		tutti.addAll(ep_hd);
		tutti.addAll(ep_normali);
		tutti.addAll(ep_preair);
		
		link.setScaricato(Torrent.SCARICATO, true);
		if(Settings.isRicercaSottotitoli())
			link.setSubDownload(true, true);
		
		for(int i=0;i<tutti.size();i++){
			Torrent t=tutti.get(i);
			if(t!=link){
				switch(t.getScaricato()){
					case Torrent.RIMOSSO:
					case Torrent.VISTO:
					case Torrent.SCARICATO:
						break;
					default:
						t.setScaricato(Torrent.IGNORATO, false);
				}
			}
		}
	}
	public Torrent getLinkHD(){
		if(ep_hd.size()>0)
			return ep_hd.get(0);
		return null;
	}
	public Torrent getLinkNormale(){
		if(ep_normali.size()>0)
			return ep_normali.get(0);
		return null;
	}
	public Torrent getLinkPreair(){
		if(ep_preair.size()>0)
			return ep_preair.get(0);
		return null;
	}
	public void cleanAll(){
		ep_hd.clear();
		ep_normali.clear();
		ep_preair.clear();
	}
	public String toString(){
		return serietv.getNomeSerie()+" "+getStagione()+"x"+getEpisodio();
	}
	public void ottimizzaSpazio(){
		for(int i=0;i<ep_hd.size();){
			switch(ep_hd.get(i).getScaricato()){
				case Torrent.IGNORATO:
					ep_hd.remove(ep_hd.get(i));
					break;
				default:
					i++;
			}
		}
		for(int i=0;i<ep_normali.size();){
			switch(ep_normali.get(i).getScaricato()){
				case Torrent.IGNORATO:
					ep_normali.remove(ep_hd.get(i));
					break;
				default:
					i++;
			}
		}
		for(int i=0;i<ep_preair.size();){
			switch(ep_preair.get(i).getScaricato()){
				case Torrent.IGNORATO:
					ep_preair.remove(ep_hd.get(i));
					break;
				default:
					i++;
			}
		}
		ep_hd.trimToSize();
		ep_normali.trimToSize();
		ep_preair.trimToSize();
		Runtime.getRuntime().gc();
	}
	public Torrent getLinkLettore(){ 
		if(serietv.getPreferenze().isPreferisciHD()){
			for(int i=0;i<ep_hd.size();i++){
				switch(ep_hd.get(i).getScaricato()){
					case Torrent.SCARICATO:
					case Torrent.VISTO:
						return ep_hd.get(i);
				}
			}
		}
		
		for(int i=0;i<ep_normali.size();i++){
			switch(ep_normali.get(i).getScaricato()){
				case Torrent.SCARICATO:
				case Torrent.VISTO:
					return ep_normali.get(i);
			}
		}
		
		if(serietv.getPreferenze().isDownloadPreair()){
			for(int i=0;i<ep_preair.size();i++){
				switch(ep_preair.get(i).getScaricato()){
					case Torrent.SCARICATO:
					case Torrent.VISTO:
						return ep_preair.get(i);
				}
			}
		}

		if(serietv.getPreferenze().isPreferisciHD()){
			if(getLinkHD()!=null)
				return getLinkHD();
		}
		
		if(getLinkNormale()!=null)
			return getLinkNormale();
		
		if(serietv.getPreferenze().isDownloadPreair()){
			if(getLinkPreair()!=null)
				return getLinkPreair();
		}
		
		return null;
	}
	public boolean isVisto(){
		ArrayList<Torrent> tutti=new ArrayList<Torrent>();
		if(serietv.getPreferenze().isPreferisciHD())
			tutti.addAll(ep_hd);
		tutti.addAll(ep_normali);
		if(serietv.getPreferenze().isDownloadPreair())
			tutti.addAll(ep_preair);
		if(tutti.isEmpty())
			return true;
		for(int i=0;i<tutti.size();i++){
			switch(tutti.get(i).getScaricato()){
				case Torrent.VISTO:
					return true;
			}
		}
		return false;
	}
	public void ignoraEpisodio(){
		ArrayList<Torrent> eps=new ArrayList<Torrent>(ep_hd.size()+ep_normali.size()+ep_preair.size());
		eps.addAll(ep_hd);
		eps.addAll(ep_normali);
		eps.addAll(ep_preair);
		for(int i=0;i<eps.size();i++){
			Torrent t=eps.get(i);
			if(t.getScaricato()==Torrent.SCARICARE){
				t.setScaricato(Torrent.IGNORATO, true);
			}
		}
	}
	public void setDownloadableFirst(int elenco, int status_to_change, int which_status){
		switch(elenco){
			case INDEX_HD:{
				Torrent t=getLinkHD();
				if(t!=null){
					if(t.getScaricato()==status_to_change)
						t.setScaricato(which_status, true);
				}
				break;
			}
			case INDEX_SD:{
				Torrent t=getLinkNormale();
				if(t!=null){
					if(t.getScaricato()==status_to_change)
						t.setScaricato(which_status, false);
				}
				break;
			}
			case INDEX_PRE:{
				Torrent t=getLinkPreair();
				if(t!=null){
					if(t.getScaricato()==status_to_change)
						t.setScaricato(which_status, false);
				}
				break;
			}
		}
	}

	public String getNomeSerie(){
		return getSerieTV().getNomeSerie();
	}
	public void checkStatus(Torrent t){
		switch(t.getScaricato()){
			case Torrent.SCARICARE:
			case Torrent.RIMOSSO:
			case Torrent.IGNORATO:
				if(t.getFilePath()!=null)
					t.setScaricato(Torrent.SCARICATO, false);
				break;
			case Torrent.SCARICATO:
			case Torrent.VISTO:
				if(t.getFilePath()==null)
					t.setScaricato(Torrent.RIMOSSO, false);
		}
	}
	public ArrayList<Torrent> getAll(){
		ArrayList<Torrent> r=new ArrayList<Torrent>(ep_hd.size()+ep_normali.size()+ep_preair.size());
		r.addAll(ep_hd);
		r.addAll(ep_normali);
		r.addAll(ep_preair);
		return r;
	}
	
	public void setStatus(int status){
		ArrayList<Torrent> eps=getAll();
		for(int i=0;i<eps.size();i++){
			Torrent t=eps.get(i);
			t.setScaricato(status, true);
		}
	}
	public Torrent getLinkScaricato(){
		ArrayList<Torrent> tutti=new ArrayList<>();
		if(serietv.getPreferenze().isPreferisciHD())
			tutti.addAll(ep_hd);
		tutti.addAll(ep_normali);
		if(serietv.getPreferenze().isDownloadPreair())
			tutti.addAll(ep_preair);
		
		for(int i=0;i<ep_preair.size();i++){
			Torrent t=ep_preair.get(i);
			switch(t.getScaricato()){
				case Torrent.SCARICATO:
				case Torrent.VISTO:
					return t;
			}
		}
		return null;
	}
	public ArrayList<Torrent> getLinkDownload(){
		ArrayList<Torrent> links=new ArrayList<Torrent>(2);
		
		if(serietv.getPreferenze().isScaricaTutto()){
			Torrent t_hd=getLinkHD();
			if(t_hd!=null && t_hd.getScaricato()==Torrent.SCARICARE)
				links.add(t_hd);
			Torrent t_sd=getLinkNormale();
			if(t_sd!=null && t_sd.getScaricato()==Torrent.SCARICARE)
				links.add(t_sd);
			Torrent t_pre=getLinkPreair();
			if(t_pre!=null && t_pre.getScaricato()==Torrent.SCARICARE)
				links.add(t_pre);
			if(links.size()==0)
				return null;
			else
				return links;
		}
		else {
			if(serietv.getPreferenze().isPreferisciHD()){
	    		Torrent t=getLinkHD();
				if(t!=null){
					if(t.getScaricato()==Torrent.SCARICARE){
						links.add(t);
						return links;
					}
				}
			}
			if(ep_normali.size()>0){
				if(getLinkNormale().getScaricato()==Torrent.SCARICARE){
					links.add(getLinkNormale());
					return links;
				}
			}
			if(serietv.getPreferenze().isDownloadPreair()){
				Torrent t=getLinkPreair();
				if(t!=null){
					if(t.getScaricato()==Torrent.SCARICARE){
						links.add(t);
						return links;
					}
				}
			}
		}
		return null;
	}
	
	public ArrayList<Torrent> getScaricare(){
		ArrayList<Torrent> scaricare=new ArrayList<Torrent>(1);
		if(serietv.getPreferenze().isPreferisciHD()){
			Torrent t=getLinkHD();
			if(t!=null)
    			if(t.getScaricato()==Torrent.SCARICARE)
    				scaricare.add(t);
		}
		Torrent t=getLinkNormale();
		if(t!=null)
			if(t.getScaricato()==Torrent.SCARICARE)
				scaricare.add(t);
		if(serietv.getPreferenze().isDownloadPreair()){
			Torrent t1=getLinkPreair();
			if(t1!=null)
				if(t1.getScaricato()==Torrent.SCARICARE)
					scaricare.add(t1);
		}
		scaricare.trimToSize();
		return scaricare;
	}
}
