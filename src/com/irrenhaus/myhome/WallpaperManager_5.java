package com.irrenhaus.myhome;

import java.io.IOException;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

public class WallpaperManager_5 extends com.irrenhaus.myhome.WallpaperManager {
	private Activity contextActivity;
	private boolean  initialized = false;
	private int numDesktops;
	private WallpaperManager manager;

	@Override
	public void selectWallpaperDrawable(Drawable u) {
		try {
			manager.setBitmap(((BitmapDrawable)u).getBitmap());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectWallpaperPath(String path) {
		Drawable drawable = Drawable.createFromPath(path);
		if(drawable instanceof BitmapDrawable)
		{
			try {
				manager.setBitmap(((BitmapDrawable)drawable).getBitmap());
			} catch (IOException e) {
				e.printStackTrace();
			}
			((BitmapDrawable)drawable).getBitmap().recycle();
		}
	}

	@Override
	public void selectWallpaperResource(int res) {
		try {
			manager.setResource(res);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setActivity(Activity activity) {
		contextActivity = activity;
	}

	@Override
	public int getSDKVersion() {
		return 5;
	}

	@Override
	public String getServiceName() {
		return VersionAbstraction.WALLPAPER_MANAGER;
	}

	public void drawWallpaper(Canvas canvas) {
		if(!initialized)
		{
			manager = WallpaperManager.getInstance(myHome.getInstance());
			numDesktops = Config.getInt(Config.NUM_DESKTOPS_KEY);
			//float xStep = numDesktops - 1;
			//manager.setWallpaperOffsetSteps(1.0f / xStep, 0);
		}
		
		int scroll = myHome.getInstance().getWorkspace().getScrollX();
		int width = numDesktops * canvas.getWidth();
		float xOffset = (1.0f / (width - canvas.getWidth())) * scroll;
		if(xOffset > 1.0f)
			xOffset = 1.0f;
		
		IBinder token = myHome.getInstance().getWorkspace().getWindowToken();
		
		if(token != null)
			manager.setWallpaperOffsets(token, xOffset, 0.0f);
	}

}
