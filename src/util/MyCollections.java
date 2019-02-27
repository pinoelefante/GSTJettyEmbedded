package util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class MyCollections
{
	public static <K,V> SortedSet<V> createSortedSetFromMapValues(Map<K,V> map)
	{
		SortedSet<V> set = new TreeSet<V>();
		map.forEach((K k, V v) -> set.add(v));
		return set;
	}
	public static <V> SortedSet<V> createSortedSetFromList(List<V> list)
	{
		SortedSet<V> set = new TreeSet<V>();
		list.forEach((V v) -> set.add(v));
		return set;
	}
	@SuppressWarnings("unchecked")
	public static <T,V> List<T> CastTo(List<V> list)
    {
        List<T> newList = new ArrayList<>();
        list.stream().forEach((V x) -> newList.add((T)x));
        return newList;
    }
	public static <V> List<V> createListFromSet(Set<V> set)
	{
		List<V> list = new ArrayList<V>();
		set.forEach((V v) -> list.add(v));
		return list;
	}
	public static <Key,Value> Map<Key,Value> createMapFromListUsingMethod(List<Value> list, String methodName)
	{
		Map<Key,Value> map = new TreeMap<>();
		if(list.isEmpty())
			return map;
		Value v = list.get(0);
		try
		{
			Method methodToSearch = v.getClass().getMethod(methodName);
			for(Value x : list)
			{
				Key k = (Key)methodToSearch.invoke(x);	
				map.put(k, x);
			}
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return map;
	}
	public static <V> Set<V> mergeSets(Set<V>...sets)
	{
		Set<V> all = new TreeSet<V>();
		for(Set<V> s : sets)
			all.addAll(s);
		return all;
	}
}
