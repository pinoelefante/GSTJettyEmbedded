package util;

import java.io.Serializable;

public class Tuple<K extends Comparable<K>,V extends Comparable<V>> implements Comparable<Tuple<K,V>>, Serializable
{
	private static final long serialVersionUID = 1394606827764440296L;
	private K element1;
	private V element2;
	
	public Tuple(K k, V v)
	{
		setElement1(k);
		setElement2(v);
	}
	
	public K getElement1()
	{
		return element1;
	}
	public void setElement1(K element1)
	{
		this.element1 = element1;
	}
	public V getElement2()
	{
		return element2;
	}
	public void setElement2(V element2)
	{
		this.element2 = element2;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Tuple<?,?>))
			return false;
		try
		{
    		@SuppressWarnings("unchecked")
			Tuple<K,V> objCasted = (Tuple<K,V>)obj;
    		return this.compareTo(objCasted) == 0;
		}
		catch(ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public int compareTo(Tuple<K, V> arg0)
	{
		int kCompare = getElement1().compareTo(arg0.getElement1()); 
		if(kCompare == 0)
			return getElement2().compareTo(arg0.getElement2());
		return kCompare;
	}
}
