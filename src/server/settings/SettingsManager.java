package server.settings;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

import gst.serietv.JSONSerializable;
import util.Serialization;

public class SettingsManager implements JSONSerializable
{
	private Map<String,Object> options;
	private SettingsManager() {
		options = new TreeMap<>();
	}
	private static class SingletonHelper{
        private static final SettingsManager INSTANCE = new SettingsManager();
    }
	public static SettingsManager getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSetting(String key, T defaultValue)
	{
		if(options.containsKey(key))
			return (T)options.get(key);
		return defaultValue;
	}
	public <T> void setSetting(String key, T value)
	{
		options.put(key, value);
	}
	public boolean loadSettings()
	{
		try
		{
			Map<String,Object> s = Serialization.deserialize("settings.dat");
			setSettings(s);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public boolean saveSettings()
	{
		return Serialization.serialize(options, "settings.dat");
	}
	public void setSettings(Map<String, Object> o)
	{
		options = o;
	}

	@Override
	public JSONObject getJson()
	{
		return new JSONObject(options);
	}
}
