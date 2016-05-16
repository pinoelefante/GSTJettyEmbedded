package gst.naming;

import gst.programma.ManagerException;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Naming {
	/*
	 * Series X YofZ
	 * Stagione 0 Completa / itasa - stagione completa
	 * StagioneX
	 */
	public static Hashtable<String, Pattern> patterns;
	private static String[] default_patterns;
	public static void init(){
		patterns=new Hashtable<String, Pattern>();
		patterns.put(PATTERN_SnEn, Pattern.compile(PATTERN_SnEn));
		patterns.put(PATTERN_SxE, Pattern.compile(PATTERN_SxE));
		patterns.put(PATTERN_Sn, Pattern.compile(PATTERN_Sn));
		patterns.put(PATTERN_nofn, Pattern.compile(PATTERN_nofn));
		patterns.put(PATTERN_Part_dotnofn, Pattern.compile(PATTERN_Part_dotnofn));
		patterns.put("PATTERN_ANNO", Pattern.compile("2[0-9]{3}"));
		patterns.put(PATTERN_DATA, Pattern.compile(PATTERN_DATA));
		patterns.put(PATTERN_EP_ALL_NUM, Pattern.compile(PATTERN_EP_ALL_NUM));
		patterns.put(PATTERN_Sx_Ex, Pattern.compile(PATTERN_Sx_Ex));
		//aggiungere qui i nuovi pattern
		
		default_patterns=new String[]{
				PATTERN_SnEn,
				PATTERN_SxE,
				PATTERN_Sx_Ex,
				PATTERN_Sn,
				PATTERN_nofn,
				PATTERN_Part_dotnofn,
				PATTERN_DATA,
				PATTERN_EP_ALL_NUM
		};
	}
	public static final String 
			PATTERN_SnEn="[Ss][0-9]{1,}[.-]{0,1}[Ee][0-9]{1,}",					//S00E00  s00e00
			PATTERN_SxE="[0-9]{1,}[x|.][0-9]{1,}",								//0[.x]0
			PATTERN_Sn="[Ss][0-9]{1,}",											//s00
			PATTERN_nofn="[0-9]{1,}of[0-9]{1,}",								//00of00
			PATTERN_Part_dotnofn="[Pp][Aa][Rr][Tt][\\S][\\d]",  				//part[._]0
			PATTERN_DATA="2[0-9]{3}[._\\s+][0-9]{1,2}[._\\s+][0-9]{1,2}",		//2xxx[._\s+]xx[._\s+]xx
			PATTERN_EP_ALL_NUM = "[.]{1}[0-9]{3}[.]{1}",						//S01E01 -> 101
			PATTERN_Sx_Ex = "[Ss][0-9]{1,2}-[Ee][0-9]{1,2}";					//S1-E25
			//aggiungere qui i nuovi pattern

	public static void main(String[] args){
		System.out.println(parse("blackish.110.hdtv-lol", null)+"\n");
		//System.out.println(parse("Discovery.Ch.River.Monsters.Series.3.10of10.The.Lost.Reels.Part.2.DVDrip.x264.AACmp4-MVGroup", null));
	}
	public static CaratteristicheFile parse(String toParse, String[] pattern){
		if(patterns==null)
			init();
		CaratteristicheFile stat=new CaratteristicheFile();
		if(toParse.toLowerCase().contains("720p"))
			stat.set720p(true);
		if(toParse.toUpperCase().contains("PROPER"))
			stat.setProper(true);
		if(toParse.toUpperCase().contains("REPACK"))
			stat.setRepack(true);
		if(toParse.toUpperCase().contains("DVDRIP"))
			stat.setDVDRip(true);
		if(toParse.toLowerCase().contains("preair"))
			stat.setPreair(true);
		
		Pattern p=patterns.get(PATTERN_DATA);
		Matcher m = p.matcher(toParse);
		if(!m.find()){
			/*Rimuove l'anno dalla stringa*/
    		Pattern p_anno=patterns.get("PATTERN_ANNO");
    		Matcher m_anno=p_anno.matcher(toParse);
    		if(m_anno.find()){
    			toParse=toParse.replace(m_anno.group(), "");
    		}
		}
		
		parse(toParse, (pattern==null?default_patterns:pattern), 0, stat);
		return stat;
	}
	private static void parse(String toParse, String[] pattern, int current_pattern, CaratteristicheFile stats){
		if(pattern.length==0)
			return;
		else if(current_pattern>=pattern.length)
			return;
		
		Pattern p=patterns.get(pattern[current_pattern]);
		Matcher m = p.matcher(toParse);
		if(!m.find()){
			parse(toParse, pattern, current_pattern+1, stats);
			return;
		}
		
		String splitted=m.group();
		String[] dati=null; 
		switch(pattern[current_pattern]){
			case PATTERN_SnEn:
				splitted=splitted.replace("S", "").replace("s", "").replace("E", "_").replace("e", "_").replace(".", "").replace("-", "");
				dati=splitted.split("_");
				break;
			case PATTERN_SxE:
				splitted=splitted.replace(".", "x");
				dati=splitted.split("x");
				break;
			case PATTERN_Sx_Ex:
				splitted=splitted.replace("-", "x").toLowerCase().replace("s", "").replace("e", "");
				dati=splitted.split("x");
				break;
			case PATTERN_Sn:
				dati=new String[2];
				splitted=splitted.replace("s", "").replace("S", "").trim();
				dati[0]=splitted;
				dati[1]="0";
				break;
			case PATTERN_nofn:
				dati=new String[2];
				dati[0]="1";
				dati[1]=splitted.trim().split("of")[0];
				break;
			case PATTERN_Part_dotnofn:
				dati=new String[2];
				dati[0]="1";
				dati[1]=splitted.substring(5);
				break;
			case PATTERN_DATA:{
				dati = new String[2];
				String[] ns = splitted.replaceAll("[._\\s+]","_").split("_");
				if(ns.length==3){
					dati[0] = ns[0];
					dati[1]=ns[1]+ns[2];
				}
				else {
					dati[0] = "0";
					dati[1] = "0";
				}
				break;
			}
			case PATTERN_EP_ALL_NUM: {
				splitted = splitted.replace(".", "");
				dati = new String[2];
				dati[0]=splitted.substring(0, splitted.length()/2);
				dati[1]=splitted.substring(splitted.length()/2);
				break;
			}
			/*
			case 4:
				//[Ss]eries\\S[0-9]{1,}of[0-9]{1,}
				splitted=splitted.toLowerCase();
				break;
			case 5:
				break;
			case 6:
				break;
			case 7:
				break;
			*/
			default:
				stats.setStagione(0);
				stats.setEpisodio(0);
				return;
		}
		
		try {
			//System.out.println("dati[0]="+dati[0]);
			stats.setStagione(Integer.parseInt(dati[0]));
		}
		catch(NumberFormatException | NullPointerException e){
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			stats.setStagione(0);
		}
		try {
			//System.out.println("dati[1]="+dati[1]);
			stats.setEpisodio(Integer.parseInt(dati[1]));
		}
		catch(NumberFormatException | NullPointerException e){
			e.printStackTrace();
			ManagerException.registraEccezione(e);
			stats.setEpisodio(0);
		}
	}
	public static String rimuoviAnnoInParentesi(String s){
		return s.replaceAll("[(0-9)]{6}", "").trim();
	}
}
