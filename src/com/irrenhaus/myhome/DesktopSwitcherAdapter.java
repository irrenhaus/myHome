package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

public class DesktopSwitcherAdapter extends BaseAdapter {
	private Vector<Bitmap>	desktops;
	private Context			context;
	
	private Workspace		workspace;
	private int				desktopCount;
	
	private final Paint		defaultPaint = new Paint();
	
	public DesktopSwitcherAdapter(Context context)
	{
		super();
		
		this.context = context;
		desktops = new Vector<Bitmap>();
	}
	
	@Override
	public int getCount() {
		return desktops.size();
	}

	@Override
	public Object getItem(int arg0) {
		return desktops.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
		ImageButton btn = null;
		
		btn = new ImageButton(context);
		btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		btn.setImageBitmap(desktops.get(pos));
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				workspace.closeDesktopSwitcher(new Runnable() {
					public void run() {
						workspace.gotoDesktop(pos);
					}
				});
			}
		});
		
		return btn;
	}
	
	public void init(Workspace w, int d, int width, int height, int cols)
	{
		workspace = w;
		desktopCount = d;
		
		if(desktops == null)
			desktops = new Vector<Bitmap>();
		else
			desktops.clear();
		
		for(int i = 0; i < desktopCount; i++)
		{
			DesktopView desktop = workspace.getDesktop(i);
			
			Bitmap cache = Bitmap.createBitmap(desktop.getWidth(), desktop.getHeight(), Bitmap.Config.ARGB_8888);
			
			Canvas cacheCanvas = new Canvas(cache);
			desktop.draw(cacheCanvas);
			
			int cacheWidth = cache.getWidth();
			int cacheHeight = cache.getHeight();
			float mod = ((float)cacheHeight) / ((float)cacheWidth);
			
			Log.d("myHome", "cw: "+cacheWidth+", ch: "+cacheHeight+", mod: "+mod);
			
			int bmpWidth = cacheWidth / cols;
			int bmpHeight = (int)(bmpWidth * mod);
			
			Bitmap bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, cache.getConfig());
			Canvas canvas = new Canvas(bmp);
			canvas.drawBitmap(cache, new Rect(0, 0, cacheWidth, cacheHeight),
					new Rect(0, 0, bmpWidth, bmpHeight), defaultPaint);
			
			cacheCanvas = null;
			cache.recycle();
			cache = null;
			
			desktops.add(bmp);
		}
	}

	public void deinit() {
		for(int i = 0; i < desktopCount; i++)
		{
			desktops.get(i).recycle();
		}
		desktops.clear();
	}

}
