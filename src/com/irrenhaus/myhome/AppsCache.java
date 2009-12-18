package com.irrenhaus.myhome;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

public class AppsCache implements Runnable {
	private Context context;
	private Vector<ApplicationInfo> applicationInfos;
	
	private Vector<ApplicationLoadingListener> listeners;
	
	private static AppsCache instance = null;
	
	public boolean loadingDone = false;
	
	private Thread myself;
	
	public AppsCache()
	{
		applicationInfos = new Vector<ApplicationInfo>();
		listeners = new Vector<ApplicationLoadingListener>();
	}
	
	public static AppsCache getInstance()
	{
		if(instance == null)
			instance = new AppsCache();
		return instance;
	}
	
	public void setContext(Context context)
	{
		this.context = context;
	}
	
	public ApplicationInfo searchByIntent(Intent intent)
	{
		for(int i = 0; i < getAppCount(); i++)
		{
			if(applicationInfos.get(i).intent.toURI().equals(intent.toURI()))
				return applicationInfos.get(i);
		}
		return null;
	}
	
	public int getAppCount()
	{
		return applicationInfos.size();
	}
	
	public ApplicationInfo getAppInfo(int position)
	{
		return applicationInfos.get(position);
	}
	
	public void addLoadingListener(ApplicationLoadingListener l)
	{
		listeners.add(l);
	}
	
	public synchronized boolean isLoadingDone()
	{
		return loadingDone;
	}
	
	public void start()
	{
		if(myself != null && !isLoadingDone())
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
		loadingDone = false;
		
		if(applicationInfos.size() > 0)
		{
			applicationInfos.clear();
		}
		
		PackageManager pkgMgr = context.getPackageManager();
		
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = pkgMgr.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pkgMgr));

        for(int i = 0; i < apps.size(); i++)
        {
        	ResolveInfo resolve = apps.get(i);
        	ApplicationInfo info = new ApplicationInfo();
        	
        	info.name = ""+resolve.loadLabel(pkgMgr);
        	info.icon = resolve.loadIcon(pkgMgr);
        	Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(resolve.activityInfo.applicationInfo.packageName,
            									  resolve.activityInfo.name));
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	info.intent = intent;
        		
        	Drawable icon = info.icon;

            if (!info.filtered) {
                final Resources resources = context.getResources();
                int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                
                final int iconWidth = icon.getIntrinsicWidth();
                final int iconHeight = icon.getIntrinsicHeight();

                if (icon instanceof PaintDrawable) {
                    PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                }

                if (width > 0 && height > 0 && (width < iconWidth || height < iconHeight)) {
                    final float ratio = (float) iconWidth / iconHeight;

                    if (iconWidth > iconHeight) {
                        height = (int) (width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int) (height * ratio);
                    }

                    final Bitmap.Config c =
                            icon.getOpacity() != PixelFormat.OPAQUE ?
                                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(width, height, c);
                    final Canvas canvas = new Canvas(thumb);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));
                    Rect oldBounds = new Rect();
                    oldBounds.set(icon.getBounds());
                    icon.setBounds(0, 0, width, height);
                    icon.draw(canvas);
                    icon.setBounds(oldBounds);
                    icon = info.icon = new BitmapDrawable(thumb);
                    info.filtered = true;
                }
            }
        	
        	applicationInfos.add(info);
        	
        	for(int k = 0; k < listeners.size(); k++)
        	{
        		listeners.get(k).applicationLoaded(info);
        	}
        }
    	
    	for(int k = 0; k < listeners.size(); k++)
    	{
    		listeners.get(k).loadingDone();
    	}
        
        loadingDone = true;
	}
	
	//////////////////////////////////////////////////////////////////
	
	public class ApplicationInfo
	{
		public String	name = null;
		public Drawable icon = null;
		public Intent	intent = null;
		public boolean	filtered = false;
		
		public boolean equals(ApplicationInfo info)
		{
			return ((name.equals(info.name)) &&
					(intent.toURI().equals(info.intent.toURI())));
		}
	}
}
