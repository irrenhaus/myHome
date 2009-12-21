package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Screen extends LinearLayout {
	private ImageView desktopOverview = null;
	
	public Screen(Context context) {
		super(context);
		
		init();
	}
	
	public Screen(Context context, AttributeSet set) {
		super(context, set);
		
		init();
	}
	
	public void init()
	{
		desktopOverview = (ImageView) this.findViewById(R.id.desktopOverview);
	}
	
	@Override
    public void dispatchDraw(Canvas canvas)
    {
    	WallpaperManager mgr = WallpaperManager.getInstance();
    	Bitmap bmp = mgr.getWallpaper();
    	if(bmp != null)
    	{
    		Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
    		Rect dest = new Rect(0, 0, getWidth(), getHeight());
    		canvas.drawBitmap(bmp, src, dest, new Paint());
    	}
    		
    	
    	for(int i = 0; i < getChildCount(); i++)
    		drawChild(canvas, getChildAt(i), getDrawingTime());
    }
	
	public void desktopChanged(boolean diamond, int num)
	{
		if(desktopOverview == null)
			init();
		
		
	}
}
