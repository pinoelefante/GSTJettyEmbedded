package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;

public class Serialization
{
	public static<T> boolean serialize(T obj, Path filePath)
	{
		String absolutePath = filePath.toAbsolutePath().toString();
		return serialize(obj, absolutePath);
	}
	public static<T> boolean serialize(T obj, String path)
	{
		try(ObjectOutputStream serializer = new ObjectOutputStream(new FileOutputStream(path)))
		{
			serializer.writeObject(obj);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public static<T> T deserialize(String filePath) throws Exception
	{
		try(ObjectInputStream serializer = new ObjectInputStream(new FileInputStream(filePath)))
		{
			return (T)serializer.readObject();
		}
	}
	public static<T> T deserialize(Path filePath) throws Exception
	{
		String absolutePath = filePath.toAbsolutePath().toString();
		return deserialize(absolutePath);
	}
}
