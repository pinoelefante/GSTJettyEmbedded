package util;

public class Object4Value<K, V, T, Z> {
	private K k; 
	private V v;
	private T t;
	private Z z;
	
	public Object4Value(K q,V w,T e, Z r) {
		k=q;
		v=w;
		t=e;
		z=r;
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
	public Z getZ(){
		return z;
	}
}
