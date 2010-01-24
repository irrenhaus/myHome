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
	
	public abstract void drawWallpaper(Canvas canvas);
}
