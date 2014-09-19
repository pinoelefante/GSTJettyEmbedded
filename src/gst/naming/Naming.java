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
		patterns.put("PATTERN_ANNO", Pattern.compile("[0-9]{4}"));
		//aggiungere qui i nuovi pattern
		
		default_patterns=new String[]{
				PATTERN_SnEn,
				PATTERN_Sn,
				PATTERN_SxE,
				PATTERN_nofn,
				PATTERN_Part_dotnofn
		};
	}
	public static final String 
			PATTERN_SnEn="[Ss][0-9]{1,}[.]{0,1}[Ee][0-9]{1,}",					//S00E00  s00e00
			PATTERN_SxE="[0-9]{1,}[x|.][0-9]{1,}",								//0[.x]0
			PATTERN_Sn="[Ss][0-9]{1,}",											//s00
			PATTERN_nofn="[0-9]{1,}of[0-9]{1,}",								//00of00
			PATTERN_Part_dotnofn="[Pp][Aa][Rr][Tt][\\S][\\d]";					//part[._]0
			//aggiungere qui i nuovi pattern

	public static void main(String[] args){
		System.out.println(parse("magnet:?xt=urn:btih:MIVWZORJG6VY6T3ZBENWJIZ2MIDJ6M5S&dn=The.Hour.UK.2011.2x06.HDTV.x264-FoV&tr=udp://tracker.openbittorrent.com:80&tr=udp://tracker.publicbt.com:80&tr=udp://tracker.istole.it:80", null)+"\n");
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
		
		/*Rimuove l'anno dalla stringa*/
		Pattern p_anno=patterns.get("PATTERN_ANNO");
		Matcher m_anno=p_anno.matcher(toParse);
		if(m_anno.find()){
			toParse=toParse.replace(m_anno.group(), "");
		}
		
		parse(toParse, (pattern==null?default_patterns:pattern), 0, stat);
		return stat;
	}
	/*
	 	-PATTERN_SnEn="[Ss][0-9]{1,}[Ee][0-9]{1,}",							//S00E00  s00e00
		-PATTERN_SxE="[0-9]{1,}[x|.][0-9]{1,}",								//0[.x]0
		-PATTERN_Sn="[Ss][0-9]{1,}",										//s00
		-PATTERN_nofn="[0-9]{1,}of[0-9]{1,}",								//00of00
		-PATTERN_Part_dotnofn="[Pp][Aa][Rr][Tt][\\S][\\d]";					//part[._]0
	 */
	private static void parse(String toParse, String[] pattern, int current_pattern, CaratteristicheFile stats){
		if(pattern.length==0)
			return;
		else if(current_pattern>=pattern.length)
			return;
		
		Pattern p=patterns.get(pattern[current_pattern]);
		Matcher m = p.matcher(toParse);
		if(m.find()){
			//System.out.println("Match trovato con pattern: "+p.pattern()+"\nEstratto: "+m.group());
		}
		else {
			parse(toParse, pattern, current_pattern+1, stats);
			return;
		}
		
		String splitted=m.group();
		String[] dati=null; 
		switch(pattern[current_pattern]){
			case PATTERN_SnEn:
				splitted=splitted.replace("S", "").replace("s", "").replace("E", "_").replace("e", "_").replace(".", "");
				dati=splitted.split("_");
				break;
			case PATTERN_SxE:
				splitted=splitted.replace(".", "x");
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
	
}
