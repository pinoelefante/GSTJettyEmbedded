package gst.database.tda;

import java.util.ArrayList;

public class KVResult<K, V> {
	private ArrayList<KVItem<K,V>> items;
	
	public KVResult (){
		items=new ArrayList<KVItem<K,V>>();
	}
	public void addItem(KVItem<K,V> item){
		items.add(item);
	}
	public V getValueByKey(K key){
		for(int i=0;i<items.size();i++){
			K k=items.get(i).getKey();
			if(k.equals(key)){
				return items.get(i).getValue();
			}
		}
		return null;
	}
	public int size(){
		return items.size();
	}
}
