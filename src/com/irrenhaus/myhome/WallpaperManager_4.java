package com.irrenhaus.myhome;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class WallpaperManager_4 extends WallpaperManager implements Runnable {
	private Activity					activity = null;
	
	private boolean						done = false;
	
	private boolean						resource = false;
	private int							resourceId = -1;
	private Drawable					drawable = null;
	
	private Thread						myself = null;
	
	private Bitmap						wallpaper = null;

	private boolean 					wallpaperChanged;

	private Bitmap 						wallpaperBmp;
	private Rect						wallpaperSrcRect;
	private RectF						wallpaperDstRect;

	private Paint						defaultPaint = new Paint();

	public int getSDKVersion() {
		return 4;
	}

	public String getServiceName() {
		return VersionAbstraction.WALLPAPER_MANAGER;
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
	
	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}
	
	public void selectWallpaperResource(int res)
	{
		resource = true;
		resourceId = res;
		set();
	}
	
	public void selectWallpaperDrawable(Drawable u)
	{
		resource = false;
		drawable = u;
		set();
	}
	
	public void selectWallpaperPath(String path)
	{
		resource = false;
		drawable = Drawable.createFromPath(path);
		set();
	}
	
	private Bitmap getWallpaper(int w, int h)
	{
		if(wallpaper == null || wallpaper.isRecycled())
			wallpaper = Utilities.centerToFit(((BitmapDrawable)activity.getWallpaper()).getBitmap(),
				w, h, activity);
		
		return wallpaper;
	}
	
	private void set()
	{
		if(myself != null && !done)
		{
			myself.stop();
		}
		
		myself = new Thread(this);
		myself.start();
	}
	
	protected void stop()
	{
		if(myself != null && myself.isAlive() && !myself.isInterrupted())
		{
			myself.interrupt();
		}
	}
	
	public synchronized void run()
	{
		Bitmap wallpaper = null;
		
		if(resource)
		{
			Drawable res = activity.getResources().getDrawable(resourceId);
			
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
			if(drawable != null)
			{
				try {
					activity.setWallpaper(((BitmapDrawable)drawable).getBitmap());
				} catch (IOException e) {
					done = true;
					e.printStackTrace();
				}
				drawable = null;
			}
		}
		
		wallpaperChanged = true;
		
		if(this.wallpaper != null)
			this.wallpaper.recycle();
		
		activity.sendBroadcast(new Intent(Intent.ACTION_WALLPAPER_CHANGED));
		
		done = true;
	}

	public void drawWallpaper(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
    	wallpaperDstRect = new RectF(0, 0, width, height);
    	wallpaperSrcRect = new Rect(0, 0, width, height);
    	
		if(wallpaperChanged || wallpaperBmp == null || wallpaperBmp.isRecycled())
		{
			wallpaperBmp = getWallpaper(width, height);
		}
			
    	if(wallpaperBmp != null && !wallpaperBmp.isRecycled())
    	{
    		int count = Config.getInt(Config.NUM_DESKTOPS_KEY) + 1;
    		
    		int wallpaperWidth = wallpaperBmp.getWidth();
    		int scrollX = myHome.getInstance().getWorkspace().getScrollX();
    		
    		float offset = wallpaperWidth > width ? (count * width - wallpaperWidth) /
    				(count * (float) width) : 1.0f;
    		
    		float x = scrollX * offset * -1;

    		if (x + wallpaperWidth < width) {
    			x = width - wallpaperWidth;
    		}
    		
    		if(scrollX < 0)
    			x = 0;

    		wallpaperSrcRect.left = (int) (x * -1);
    		wallpaperSrcRect.top = (height- wallpaperBmp.getHeight()) / 2 * -1;
    		wallpaperSrcRect.right = wallpaperSrcRect.left + width;
    		wallpaperSrcRect.bottom = wallpaperSrcRect.top + height;
    		
    		canvas.drawBitmap(wallpaperBmp, wallpaperSrcRect, wallpaperDstRect, defaultPaint);
    	}
	}
}

