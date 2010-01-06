package com.irrenhaus.myhome;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class WallpaperManager implements Runnable {
	private Activity				activity = null;
	
	private boolean					done = false;
	
	private boolean					resource = false;
	private int						resourceId = -1;
	private Drawable				drawable = null;
	
	private Thread					myself = null;
	
	private Bitmap					wallpaper = null;

	private boolean wallpaperChanged;
	
	private static WallpaperManager	instance = null;
	
	public WallpaperManager()
	{
		
	}
	
	protected void finalize()
	{
		wallpaper = null;
		
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
	
	public void selectWallpaperDrawable(Drawable u)
	{
		resource = false;
		drawable = u;
	}
	
	public synchronized boolean isDone()
	{
		return done;
	}
	
	public Bitmap getWallpaper(int w, int h)
	{
		if(wallpaper == null || wallpaper.isRecycled())
			wallpaper = Utilities.centerToFit(((BitmapDrawable)activity.getWallpaper()).getBitmap(),
				w, h, activity);
		
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
		else
		{
			try {
				activity.setWallpaper(((BitmapDrawable)drawable).getBitmap());
			} catch (IOException e) {
				done = true;
				e.printStackTrace();
			}
			drawable = null;
		}
		
		wallpaperChanged = true;
		
		if(this.wallpaper != null)
			this.wallpaper.recycle();
		
		activity.sendBroadcast(new Intent(Intent.ACTION_WALLPAPER_CHANGED));
		
		done = true;
	}

	public boolean wallpaperChanged() {
		return wallpaperChanged;
	}
}

