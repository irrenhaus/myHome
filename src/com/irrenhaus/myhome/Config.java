package com.irrenhaus.myhome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Config {
	private static final HashMap<String, Object> values = new HashMap<String, Object>();
	private static final HashMap<String, Boolean> persist = new HashMap<String, Boolean>();

	public static final String	DEFAULT_DESKTOP_NUM_KEY = "default_desktop_num";
	public static final String	CURRENT_DESKTOP_NUM_KEY = "current_desktop_num";
	public static final String	NUM_DESKTOPS_KEY = "num_desktops";
	public static final String	TOOLBAR_SHOW_CALLER_BUTTON = "toolbar_show_caller_button";
	public static final String	TOOLBAR_SHOW_CONTACTS_BUTTON = "toolbar_show_contacts_button";

	public static final int		DEFAULT_DESKTOP_NUM		= 1;
	public static final int		DEFAULT_NUM_DESKTOPS	= 3;
	
	private static boolean		config_altered = false;
	public static String 		DESKTOP_ROTATION_KEY = "desktop_rotation";
	
	public static void readConfiguration(Context context)
	{
		// Put in the default values
		values.put(DEFAULT_DESKTOP_NUM_KEY, DEFAULT_DESKTOP_NUM);
		persist.put(DEFAULT_DESKTOP_NUM_KEY, true);
		
		values.put(NUM_DESKTOPS_KEY, DEFAULT_NUM_DESKTOPS);
		persist.put(NUM_DESKTOPS_KEY, true);
		
		values.put(DESKTOP_ROTATION_KEY, true);
		persist.put(DESKTOP_ROTATION_KEY, true);
		
		values.put(TOOLBAR_SHOW_CALLER_BUTTON, true);
		persist.put(TOOLBAR_SHOW_CALLER_BUTTON, true);
		
		values.put(TOOLBAR_SHOW_CONTACTS_BUTTON, true);
		persist.put(TOOLBAR_SHOW_CONTACTS_BUTTON, true);
		
		SharedPreferences prefs = context.getSharedPreferences("config", 0);
		
		Map<String, ?> all = prefs.getAll();
		
		Set<String> keys = all.keySet();
		
		for(String key: keys)
		{
			Log.d("myHome", "Prev value key "+key+": "+values.get(key));
			Log.d("myHome", "Stored value key "+key+": "+all.get(key));
			values.put(key, all.get(key));
			persist.put(key, true);
			Log.d("myHome", "After value key "+key+": "+values.get(key));
		}

		values.put(CURRENT_DESKTOP_NUM_KEY, Config.getInt(DEFAULT_DESKTOP_NUM_KEY));
	}
	
	public static void saveConfiguration(Context context)
	{
		if(!config_altered)
			return;
		
		SharedPreferences prefs = context.getSharedPreferences("config", 0);
		
		SharedPreferences.Editor edit = prefs.edit();
		
		Set<String> keys = values.keySet();
		
		for(String key: keys)
		{
			if(persist.get(key) != null && persist.get(key))
			{
				Object value = values.get(key);
				
				if(value instanceof Boolean)
					edit.putBoolean(key, (Boolean) value);
				else if(value instanceof Integer)
					edit.putInt(key, (Integer)value);
				else if(value instanceof String)
					edit.putString(key, (String)value);
				else if(value instanceof Float)
					edit.putFloat(key, (Float) value);
				else if(value instanceof Long)
					edit.putLong(key, (Long) value);
			}
		}
		
		edit.commit();
	}
	
	public static int getInt(String key)
	{
		Integer v = (Integer)values.get(key);
		
		return v != null ? v : -1;
	}
	
	public static String getString(String key)
	{
		return (String)values.get(key);
	}
	
	public static Boolean getBoolean(String key, Boolean def)
	{
		Boolean v = (Boolean)values.get(key);
		return v != null ? v : def;
	}
	
	public static Float getFloat(String key)
	{
		Float v = (Float)values.get(key);
		return v != null ? v : -1.0f;
	}
	
	public static Long getLong(String key)
	{
		Long v = (Long)values.get(key);
		return v != null ? v : -1;
	}
	
	private static void alterConfigIfPersist(String key)
	{
		Boolean v = persist.get(key);
		
		if(v != null && v == true)
			config_altered = true;
	}
	
	public static void putString(String key, String value)
	{
		values.put(key, value);
		
		alterConfigIfPersist(key);
	}
	
	public static void putInt(String key, Integer value)
	{
		values.put(key, value);
		
		alterConfigIfPersist(key);
	}
	
	public static void putBoolean(String key, Boolean value)
	{
		values.put(key, value);
		
		alterConfigIfPersist(key);
	}
	
	public static void putFloat(String key, Float value)
	{
		values.put(key, value);
		
		alterConfigIfPersist(key);
	}
	
	public static void putLong(String key, Long value)
	{
		values.put(key, value);
		
		alterConfigIfPersist(key);
	}
	
	public static void persistsValue(String key, boolean p)
	{
		persist.put(key, p);
		
		config_altered = true;
	}
}
