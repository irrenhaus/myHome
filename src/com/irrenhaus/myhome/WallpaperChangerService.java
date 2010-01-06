package com.irrenhaus.myhome;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.threefiftynice.android.preference.ListPreferenceMultiSelect;

public class WallpaperChangerService extends Service {
	private Timer		timer;
	private int			duration;
	private String[]	wallpapers;
	
	private static Context	context;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		if(context == null)
			return;
		
		String encodedWallpapers = Config.getString(Config.WALLPAPER_CHANGER_BACKGROUNDS_KEY);
		wallpapers = ListPreferenceMultiSelect.parseStoredValue(encodedWallpapers);
		
		String d = Config.getString(Config.WALLPAPER_CHANGER_DURATION_KEY);
		
		if(d.endsWith("m"))
		{
			d = d.replace("m", "");
			duration = Integer.parseInt(d) * 60 * 1000;
		} else if(d.endsWith("h"))
		{
			d = d.replace("h", "");
			duration = Integer.parseInt(d) * 60 * 60 * 1000;
		}
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				changeWallpaper();
			}
		}, 0, duration);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	public void changeWallpaper()
	{
		int which = ((int) (Math.rint(Math.random())) % wallpapers.length);
		
		WallpaperManager.getInstance().selectWallpaperPath(wallpapers[which]);
		WallpaperManager.getInstance().set();
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		WallpaperChangerService.context = context;
	}

}
