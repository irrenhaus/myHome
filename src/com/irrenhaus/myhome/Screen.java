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
	//private ImageView desktopOverview = null;
	private Context	context;
	
	public Screen(Context context) {
		super(context);
		
		this.context = context;
		
		init();
	}
	
	public Screen(Context context, AttributeSet set) {
		super(context, set);
	
		this.context = context;
		
		init();
	}
	
	public void init()
	{
		//desktopOverview = (ImageView) this.findViewById(R.id.desktopOverview);
	}
	
	@Override
    public void dispatchDraw(Canvas canvas)
    {
    	WallpaperManager mgr = WallpaperManager.getInstance();
    	Bitmap bmp = Utilities.centerToFit(mgr.getWallpaper(), getWidth(), getHeight(), context);
    	
    	if(bmp != null)
    	{
    		canvas.drawBitmap(bmp, 0, 0, new Paint());
    	}
    	
    	for(int i = 0; i < getChildCount(); i++)
    		drawChild(canvas, getChildAt(i), getDrawingTime());
    }
}
