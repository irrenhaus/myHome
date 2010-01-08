package com.irrenhaus.myhome;

import java.io.File;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.threefiftynice.android.preference.ListPreferenceMultiSelect;

public class Settings extends PreferenceActivity {
	private boolean restart = false;
	private boolean reinitToolbar = false;
	private boolean restartWallpaperChanger = false;
	private Vector<String> wallpaperEntries;
	private Vector<String> wallpaperValues;
	protected Boolean startWallpaperChanger = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.main_preferences);

		ListPreference numDesktops = (ListPreference) findPreference(Config.NUM_DESKTOPS_KEY);
		ListPreference defDesktop = (ListPreference) findPreference(Config.DEFAULT_DESKTOP_NUM_KEY);

		CheckBoxPreference desktopRotation = (CheckBoxPreference)findPreference(Config.DESKTOP_ROTATION_KEY);

		CheckBoxPreference callerBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_CALLER_BUTTON_KEY);
		CheckBoxPreference contactsBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_CONTACTS_BUTTON_KEY);
		CheckBoxPreference switcherBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_DESKTOP_SWITCHER_BUTTON_KEY);
		CheckBoxPreference gestureBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_GESTURE_BUTTON_KEY);

		CheckBoxPreference changerActive = (CheckBoxPreference)findPreference(Config.WALLPAPER_CHANGER_ACTIVE_KEY);
		CheckBoxPreference changerSetOnStart = (CheckBoxPreference)findPreference(Config.WALLPAPER_CHANGER_SET_ON_START_KEY);
		ListPreference changerDuration = (ListPreference)findPreference(Config.WALLPAPER_CHANGER_DURATION_KEY);
		final ListPreferenceMultiSelect changerBackgrounds = (ListPreferenceMultiSelect)findPreference(Config.WALLPAPER_CHANGER_BACKGROUNDS_KEY);
		
		fillWallpaperChangerValues(changerBackgrounds);
		
		/*if(Config.getInt(Config.NUM_DESKTOPS_KEY) != -1)
			numDesktops.setValueIndex(Config.getInt(Config.NUM_DESKTOPS_KEY) - 3);
		if(Config.getInt(Config.DEFAULT_DESKTOP_NUM_KEY) != -1)
			defDesktop.setValueIndex(Config.getInt(Config.DEFAULT_DESKTOP_NUM_KEY));*/
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.dialog_restart_title);
		builder.setMessage(R.string.dialog_restart_message);
		
		builder.setPositiveButton(R.string.dialog_button_ok, new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.cancel();
			}
		});
		
		final Dialog restartDialog = builder.create();
		
		numDesktops.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Integer val = Integer.parseInt((String)newValue);

				Config.putInt(Config.NUM_DESKTOPS_KEY, (Integer)val);
				
				restartDialog.show();
				restart = true;
				
				return true;
			}
		});
		
		defDesktop.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Integer num = (Integer)Integer.parseInt((String)newValue);
				if(num > Config.getInt(Config.NUM_DESKTOPS_KEY))
					num = Config.getInt(Config.NUM_DESKTOPS_KEY) - 1;
				
				Config.putInt(Config.DEFAULT_DESKTOP_NUM_KEY, (Integer)num);
				
				restartDialog.show();
				restart = true;
				
				return true;
			}
		});
		
		callerBtn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.TOOLBAR_SHOW_CALLER_BUTTON_KEY, (Boolean)newValue);
				
				reinitToolbar = true;
				
				return true;
			}
		});
		
		contactsBtn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.TOOLBAR_SHOW_CONTACTS_BUTTON_KEY, (Boolean)newValue);
				
				reinitToolbar = true;
				
				return true;
			}
		});
		
		switcherBtn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.TOOLBAR_SHOW_DESKTOP_SWITCHER_BUTTON_KEY, (Boolean)newValue);
				
				reinitToolbar = true;
				
				return true;
			}
		});
		
		gestureBtn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.TOOLBAR_SHOW_GESTURE_BUTTON_KEY, (Boolean)newValue);
				
				reinitToolbar = true;
				
				return true;
			}
		});
		
		desktopRotation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.DESKTOP_ROTATION_KEY, (Boolean)newValue);
				
				return true;
			}
		});
		
		changerActive.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.WALLPAPER_CHANGER_ACTIVE_KEY, (Boolean)newValue);
				
				startWallpaperChanger = (Boolean)newValue;
				
				return true;
			}
		});
		
		changerSetOnStart.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.WALLPAPER_CHANGER_SET_ON_START_KEY, (Boolean)newValue);
				
				return true;
			}
		});
		
		changerDuration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putString(Config.WALLPAPER_CHANGER_DURATION_KEY, (String)newValue);
				
				restartWallpaperChanger = true;
				
				return true;
			}
		});
		
		changerBackgrounds.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
				Config.putString(Config.WALLPAPER_CHANGER_BACKGROUNDS_KEY, ((StringBuffer)newValue).toString());
				
				restartWallpaperChanger = true;
				
				return true;
			}
		});
		
		Preference gestures = (Preference)findPreference("prefs_gestures");
		gestures.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference arg0) {
				startActivity(new Intent(Settings.this, GestureSettings.class));
				return true;
			}
		});
	}
	
	private void fillWallpaperChangerValues(ListPreferenceMultiSelect changerBackgrounds) {
		if(wallpaperEntries == null)
			wallpaperEntries = new Vector<String>();
		else
			wallpaperEntries.clear();
		
		if(wallpaperValues == null)
			wallpaperValues = new Vector<String>();
		else
			wallpaperValues.clear();
		
		loadImages();

		changerBackgrounds.setEntries(wallpaperEntries.toArray(new String[0]));
		changerBackgrounds.setEntryValues(wallpaperValues.toArray(new String[0]));
	}
	
	public void loadImages()
	{
		File dir = new File("/sdcard/myHome/wallpaper/");
		
		if(!dir.exists())
		{
			return;
		}
		
		walkDir(dir);
	}
	
	private void walkDir(File dir)
	{
		String[] files = dir.list();
		
		for(int i = 0; i < files.length; i++)
		{
			File file = new File(dir.getAbsolutePath()+"/"+files[i]);
			
			if(file.isDirectory())
				walkDir(file);
			else
			{
				String name = file.getName();
				if(name.endsWith("png") || name.endsWith("jpg") || name.endsWith("jpeg"))
				{
					if(!name.contains("_small"))
					{
						wallpaperEntries.add(file.getName());
						wallpaperValues.add(file.getAbsolutePath());
					}
				}
			}
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		
		if(restart)
			myHome.getInstance().finish();
		else if(reinitToolbar)
		{
			myHome.getInstance().getToolbar().initToolbar();
			myHome.getInstance().getToolbar().invalidate();
		}
		
		if(startWallpaperChanger)
		{
			myHome.getInstance().startWallpaperChangerService();
		}
		else
		{
			myHome.getInstance().stopWallpaperChangerService();
		}
		
		if(restartWallpaperChanger)
		{
			myHome.getInstance().restartWallpaperChangerService();
		}
			
		Config.saveConfiguration(myHome.getInstance());
		
		finish();
	}
}
