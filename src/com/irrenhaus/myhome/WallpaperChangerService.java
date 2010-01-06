package com.irrenhaus.myhome;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.threefiftynice.android.preference.ListPreferenceMultiSelect;

public class WallpaperChangerService extends Service {
	private Timer				timer;
	private int					duration;
	private String[]			wallpapers;
	private BroadcastReceiver	receiver;
	private static int			lastWallpaper = -1;
	
	private static Context	context;
	
	public static final String SWITCH_WALLPAPER_INTENT = "com.irrenhaus.myhome.switch_wallpaper";

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
		
		int delay = duration;
		if(Config.getBoolean(Config.WALLPAPER_CHANGER_SET_ON_START_KEY, true))
			delay = 0;
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				changeWallpaper();
			}
		}, delay, duration);
		
		receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				changeWallpaper();
			}
		};
		
		context.registerReceiver(receiver, new IntentFilter(SWITCH_WALLPAPER_INTENT));
		
		Log.d("myHome", "WallpaperChangerService started with duration: "+Config.getString(Config.WALLPAPER_CHANGER_DURATION_KEY)+"("+duration+") and delay: "+delay);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		context.unregisterReceiver(receiver);
	}
	
	public void changeWallpaper()
	{
		int which = lastWallpaper + 1;
		
		if(lastWallpaper == -1)
			which = ((int) (Math.rint(Math.random())) % (wallpapers.length));
		
		if(which >= wallpapers.length)
			which = 0;
		
		Log.d("myHome", "WallpaperChangerService: Switching wallpaper to "+(which+1)+" out of "+wallpapers.length);
		
		WallpaperManager.getInstance().selectWallpaperPath(wallpapers[which]);
		WallpaperManager.getInstance().set();
		
		lastWallpaper = which;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		WallpaperChangerService.context = context;
	}

}
