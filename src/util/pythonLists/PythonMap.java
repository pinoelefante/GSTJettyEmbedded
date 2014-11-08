package util.pythonLists;

import java.util.HashMap;
import java.util.Map;

public class PythonMap {
	public static Map<String,String> parseMap(String l){
		HashMap<String, String> map = new HashMap<String, String>();
	
		String l2 = l.replace("{", "").replace("}", "");
		String[] values = l2.split(",");
		for(int i=0;i<values.length;i++){
			String[] kv = values[i].split("=");
			map.put(kv[0].replace("'", "").trim(),kv[1].replace("'", "").trim());
		}
		return map;
	}
}
