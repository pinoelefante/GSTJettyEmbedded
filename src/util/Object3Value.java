package util;

public class Object3Value<K, V, T> {
	private K k; 
	private V v;
	private T t;
	
	public Object3Value(K q,V w,T e) {
		k=q;
		v=w;
		t=e;
	}
	public K getK(){
		return k;
	}
	public V getV(){
		return v;
	}
	public T getT(){
		return t;
	}
}
