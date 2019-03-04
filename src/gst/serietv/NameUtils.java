package gst.serietv;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameUtils
{
	private static List<Map.Entry<Integer, Pattern>> patterns	  = new ArrayList<>();
	public static final String PATTERN_SnEn = "[Ss][0-9]{1,}[.-]{0,1}[Ee][0-9]{1,}",	// S00E00																																														// s00e00
			PATTERN_SxE = "[0-9]{1,}[x|.][0-9]{1,}",									// 0[.x]0
			PATTERN_Sn = "[Ss][0-9]{1,}",												// s00
			PATTERN_nofn = "[0-9]{1,}of[0-9]{1,}",										// 00of00
			PATTERN_Part_dotnofn = "[Pp][Aa][Rr][Tt][\\S][\\d]",						// part[._]0
			PATTERN_DATA = "2[0-9]{3}[._\\s+][0-9]{1,2}[._\\s+][0-9]{1,2}",				// 2xxx[._\s+]xx[._\s+]xx
			PATTERN_EP_ALL_NUM = "[.]{1}[0-9]{3}[.]{1}",								// S01E01																																																							// 101
			PATTERN_Sx_Ex = "[Ss][0-9]{1,2}-[Ee][0-9]{1,2}";							// S1-E25
	static
	{
		patterns.add(new AbstractMap.SimpleEntry<>(0, Pattern.compile(PATTERN_SnEn)));
		patterns.add(new AbstractMap.SimpleEntry<>(1, Pattern.compile(PATTERN_SxE)));
		patterns.add(new AbstractMap.SimpleEntry<>(2, Pattern.compile(PATTERN_Sx_Ex)));
		patterns.add(new AbstractMap.SimpleEntry<>(3, Pattern.compile(PATTERN_Sn)));
		patterns.add(new AbstractMap.SimpleEntry<>(4, Pattern.compile(PATTERN_nofn)));
		patterns.add(new AbstractMap.SimpleEntry<>(5, Pattern.compile(PATTERN_Part_dotnofn)));
		patterns.add(new AbstractMap.SimpleEntry<>(6, Pattern.compile(PATTERN_DATA)));
		patterns.add(new AbstractMap.SimpleEntry<>(7, Pattern.compile(PATTERN_EP_ALL_NUM)));
	}

	public static String formattaNome(String nome)
	{
		String formattato = nome;
		if (formattato.endsWith(", The"))
		{
			formattato = "The " + formattato.replace(", The", "").trim();
		}
		if (formattato.contains(", A"))
		{
			formattato = "A " + formattato.replace(", A", "").trim();
		}
		/*
		String pattern_anno = "\\([0-9]{4}\\)";
		Pattern p_anno = Pattern.compile(pattern_anno);
		Matcher m = p_anno.matcher(formattato);
		if (m.find())
		{
			formattato = formattato.replace(m.group(), "");
		}
		*/
		formattato = formattato.trim();
		return formattato;
	}

	public static String removeNationality(String nome)
	{
		String formattato = nome;
		Pattern pattern = Pattern.compile("\\W[aA-zZ]{2}\\W$");
		Matcher matcher = pattern.matcher(formattato);
		if (matcher.find())
		{
			formattato = formattato.replace(matcher.group(), "").trim();
		}
		return formattato;
	}

	public static int getResolutionFromName(String name)
	{
		String nameLow = name.toLowerCase();
		if (nameLow.contains("2160p") || nameLow.contains("2160i") || name.contains("4k"))
			return 2160;
		if (nameLow.contains("1080p") || nameLow.contains("1080i"))
			return 1080;
		if (nameLow.contains("720p") || nameLow.contains("720i"))
			return 720;
		return 480;
	}

	public static boolean isProper(String name)
	{
		return name.toLowerCase().contains("proper");
	}

	public static boolean isPreAir(String name)
	{
		return name.toLowerCase().contains("preair");
	}

	public static boolean isRepack(String name)
	{
		return name.toLowerCase().contains("repack");
	}
	public static String getVideoSource(String name)
	{
		String nLow = name.toLowerCase();
		if(nLow.contains("dvdrip") || nLow.contains("dvd-rip") || nLow.contains("dvd rip") || nLow.contains("dvd.rip") || nLow.contains("dvd"))
			return "DVD";
		if(nLow.contains("bdrip") || nLow.contains("bluray") || nLow.contains("blueray") || nLow.contains("bd-rip"))
			return "Blu-ray";
		if(nLow.contains("webrip") || nLow.contains("web"))
			return "WEB";
		return "HDTV";
	}
	public static<T> EpisodeWrapper getSeasonEpisode(String name)
	{
		return parseEpisodeName(name, 0);
	}
	
	private static<T> EpisodeWrapper parseEpisodeName(String toParse, int startFrom)
	{
		EpisodeWrapper myTuple = new EpisodeWrapper(0, 0);
		if(startFrom >= patterns.size())
			return myTuple;
		int currentPattern;
		Matcher matcher = null;
		for (currentPattern = startFrom; currentPattern < patterns.size(); currentPattern++)
		{

			Pattern p = patterns.get(currentPattern).getValue();
			matcher = p.matcher(toParse);
			if (matcher.find())
				break;
		}

		String splitted = matcher.group();
		String[] dati = null;
		switch (patterns.get(currentPattern).getKey())
		{
			case 0:
				splitted = splitted.replace("S", "").replace("s", "").replace("E", "_").replace("e", "_").replace(".", "").replace("-", "");
				dati = splitted.split("_");
				break;
			case 1:
				splitted = splitted.replace(".", "x");
				dati = splitted.split("x");
				break;
			case 2:
				splitted = splitted.replace("-", "x").toLowerCase().replace("s", "").replace("e", "");
				dati = splitted.split("x");
				break;
			case 3:
				dati = new String[2];
				splitted = splitted.replace("s", "").replace("S", "").trim();
				dati[0] = splitted;
				dati[1] = "0";
				break;
			case 4:
				dati = new String[2];
				dati[0] = "1";
				dati[1] = splitted.trim().split("of")[0];
				break;
			case 5:
				dati = new String[2];
				dati[0] = "1";
				dati[1] = splitted.substring(5);
				break;
			case 6:
			{
				dati = new String[2];
				String[] ns = splitted.replaceAll("[._\\s+]", "_").split("_");
				if (ns.length == 3)
				{
					dati[0] = ns[0];
					dati[1] = ns[1] + ns[2];
				}
				else
				{
					dati[0] = "0";
					dati[1] = "0";
				}
				break;
			}
			case 7:
			{
				splitted = splitted.replace(".", "");
				dati = new String[2];
				dati[0] = splitted.substring(0, splitted.length() / 2);
				dati[1] = splitted.substring(splitted.length() / 2);
				break;
			}
		}

		try
		{
			myTuple.setStagione(Integer.parseInt(dati[0]));
			myTuple.setEpisodio(Integer.parseInt(dati[1]));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return myTuple;
	}
}
