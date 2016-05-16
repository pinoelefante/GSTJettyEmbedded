package gst.database.tda;

public class KVItem<K, V> {
	private K key;
	private V value;
	
	public KVItem(K k, V v){
		key=k;
		value=v;
	}
	public K getKey(){
		return key;
	}
	public V getValue(){
		return value;
	}
}
