package com.irrenhaus.myhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class WallpaperManager_5 extends WallpaperManager implements Runnable {

	@Override
	public Bitmap getWallpaper(int w, int h) {
		return null;
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public void selectWallpaperDrawable(Drawable u) {
		
	}

	@Override
	public void selectWallpaperPath(String path) {
		
	}

	@Override
	public void selectWallpaperResource(int res) {
		
	}

	@Override
	public void set() {
		
	}

	@Override
	public void setActivity(Activity activity) {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public boolean wallpaperChanged() {
		return false;
	}

	@Override
	public int getSDKVersion() {
		return 5;
	}

	@Override
	public String getServiceName() {
		return VersionAbstraction.WALLPAPER_MANAGER;
	}

	public void run() {
		
	}

	public void drawWallpaper(Canvas canvas) {
	}

}
