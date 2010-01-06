package com.irrenhaus.myhome;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Settings extends PreferenceActivity {
	private boolean restart = false;
	private boolean reinitToolbar = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.main_preferences);

		ListPreference numDesktops = (ListPreference) findPreference(Config.NUM_DESKTOPS_KEY);
		ListPreference defDesktop = (ListPreference) findPreference(Config.DEFAULT_DESKTOP_NUM_KEY);

		CheckBoxPreference desktopRotation = (CheckBoxPreference)findPreference(Config.DESKTOP_ROTATION_KEY);
		
		CheckBoxPreference callerBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_CALLER_BUTTON);
		CheckBoxPreference contactsBtn = (CheckBoxPreference)findPreference(Config.TOOLBAR_SHOW_CONTACTS_BUTTON);

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
				Config.putBoolean(Config.TOOLBAR_SHOW_CALLER_BUTTON, (Boolean)newValue);
				
				reinitToolbar = true;
				
				return true;
			}
		});
		
		contactsBtn.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Config.putBoolean(Config.TOOLBAR_SHOW_CONTACTS_BUTTON, (Boolean)newValue);
				
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
			
		finish();
	}
}
