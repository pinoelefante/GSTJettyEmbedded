package util.pythonLists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PythonList {
	public static List<String> parseSimpleList(String l){
		ArrayList<String> list = new ArrayList<String>();
		if(!l.startsWith("[") || l.endsWith("]"))
			return list;
		
		String l2 = l.replace("[", "").replace("]", "");
		String[] values = l2.split(",");
		for(int i=0;i<values.length;i++){
			list.add(values[i].trim());
		}
		return list;
	}
	public static List<Map<String, String>> parseListItemMap(String l){
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if(!l.startsWith("[") || l.endsWith("]"))
			return list;
		
		String l2 = l.replace("[", "").replace("]", "");
		String[] values = l2.split(",");
		for(int i=0;i<values.length;i++){
			Map<String, String> m = PythonMap.parseMap(values[i].trim());
			list.add(m);
		}
		return list;
	}
}
