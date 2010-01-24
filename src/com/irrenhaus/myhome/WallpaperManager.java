package com.irrenhaus.myhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public abstract class WallpaperManager extends AbstractionService {
	public abstract void setActivity(Activity activity);

	public abstract void selectWallpaperResource(int res);

	public abstract void selectWallpaperDrawable(Drawable u);

	public abstract void selectWallpaperPath(String path);

	public abstract boolean isDone();

	public abstract Bitmap getWallpaper(int w, int h);
	
	public abstract void drawWallpaper(Canvas canvas);

	public abstract void set();

	public abstract void stop();

	public abstract boolean wallpaperChanged();
}
