package com.irrenhaus.myhome;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class WallpaperManager implements Runnable {
	private Activity				activity = null;
	
	private boolean					done = false;
	
	private boolean					resource = false;
	private int						resourceId = -1;
	
	private Thread					myself = null;
	
	private Bitmap					wallpaper = null;
	
	private static WallpaperManager	instance = null;
	
	public WallpaperManager()
	{
		
	}
	
	public static WallpaperManager getInstance()
	{
		if(instance == null)
			instance = new WallpaperManager();
		return instance;
	}
	
	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}
	
	public void selectWallpaperResource(int res)
	{
		resource = true;
		resourceId = res;
	}
	
	public synchronized boolean isDone()
	{
		return done;
	}
	
	public void get()
	{
		wallpaper = ((BitmapDrawable)activity.getWallpaper()).getBitmap();
	}
	
	public Bitmap getWallpaper()
	{
		return wallpaper;
	}
	
	public void set()
	{
		if(myself != null && !isDone())
		{
			myself.stop();
		}
		
		myself = new Thread(this);
		myself.start();
	}
	
	public void stop()
	{
		if(myself != null && myself.isAlive() && !myself.isInterrupted())
		{
			myself.interrupt();
		}
	}
	
	@Override
	public synchronized void run()
	{
		Bitmap wallpaper = null;
		
		if(resource)
		{
			Drawable res = activity.getResources().getDrawable(resourceId);
			
			int width = res.getMinimumWidth();
			int height = res.getMinimumHeight();
			
			if(res instanceof BitmapDrawable)
				wallpaper = ((BitmapDrawable)res).getBitmap();
			else
			{
				done = true;
				return;
			}
			
			try {
				activity.setWallpaper(wallpaper);
			} catch (IOException e) {
				done = true;
				e.printStackTrace();
			}
		}
		
		done = true;
	}
}

