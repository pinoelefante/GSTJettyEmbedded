package gst.sottotitoli.converter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

public class Srt2WebVtt {
	public static void convert(String pathSrt) throws Exception{
		ArrayList<SrtEntry> srtEntry = readFile(pathSrt);
		salvaFile(srtEntry, pathSrt.substring(0, pathSrt.lastIndexOf("."))+".vtt");
	}
	private static ArrayList<SrtEntry> readFile(String path) throws Exception{
		ArrayList<SrtEntry> entries = new ArrayList<>();
		Scanner file = new Scanner(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		String buffer = "";
		while(file.hasNextLine()){
			int read = 0;
			SrtEntry entry = new SrtEntry();
			do {
				buffer = file.nextLine().trim();
				System.out.println(buffer);
				if(buffer.length()>0){
					switch(read){
						case 0:{
							entry.setEntry(Integer.parseInt(buffer));
							break;
						}
						case 1:{
							String[] tempi = buffer.split("-->");
							entry.setT_inizio(tempi[0].trim());
							entry.setT_fine(tempi[1].trim());
							break;
						}
						case 3:
						case 2:{
							entry.setTesto(buffer);
							break;
						}
					}
					read++;
				}
				else
					break;
			}
			while(read<=3);
			if(read>0)
				entries.add(entry);
		}
		file.close();
		return entries;
	}
	private static void salvaFile(ArrayList<SrtEntry> list, String filename) throws Exception{
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
		out.append("WEBVTT\n\n");
		for(int i=0;i<list.size();i++){
			SrtEntry e = list.get(i);
			out.append(e.getEntry()+"\n"+e.getT_inizio().replace(",", ".")+" --> "+e.getT_fine().replace(",", ".")+"\n"+e.getTesto()+"\n\n");
		}
		out.flush();
		out.close();
	}
	public static void main(String[] args) throws Exception{
		convert("D:\\SerieTV\\Helix\\Helix.S01E03.HDTV.x264-2HD.1.srt");
	}
}
