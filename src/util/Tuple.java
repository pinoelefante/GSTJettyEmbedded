package util;

import org.json.simple.JSONObject;

import gst.serietv.JSONSerializable;

public class Tuple<K extends Comparable<K>,V extends Comparable<V>> implements Comparable<Tuple<K,V>>, JSONSerializable
{
	private String element1Name, element2Name;
	private K element1;
	private V element2;
	
	public Tuple(K k, V v)
	{
		this(k,v, "element1", "element2");
	}
	public Tuple(K k, V v, String name1, String name2)
	{
		setElement1(k);
		setElement2(v);
		setElement1Name(name1);
		setElement2Name(name2);
	}
	
	public K getElement1()
	{
		return element1;
	}
	protected void setElement1(K element1)
	{
		this.element1 = element1;
	}
	public V getElement2()
	{
		return element2;
	}
	protected void setElement2(V element2)
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

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJson()
	{
		JSONObject obj = new JSONObject();
		obj.put(getElement1Name(), getElement1());
		obj.put(getElement2Name(), getElement2());
		return obj;
	}

	public String getElement1Name()
	{
		return element1Name;
	}

	public void setElement1Name(String element1Name)
	{
		this.element1Name = element1Name;
	}

	public String getElement2Name()
	{
		return element2Name;
	}

	public void setElement2Name(String element2Name)
	{
		this.element2Name = element2Name;
	}
}
