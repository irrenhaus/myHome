package com.irrenhaus.myhome;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
		String intentUri = intent.toURI();
		for(int i = 0; i < getAppCount(); i++)
		{
			if(applicationInfos.get(i).intentUri.equals(intentUri))
				return applicationInfos.get(i);
		}
		return null;
	}
	
	public ApplicationInfo searchByName(String name)
	{
		for(int i = 0; i < getAppCount(); i++)
		{
			if(applicationInfos.get(i).name.equals(name))
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
	
	public void sendLoadingDone()
	{
		for(int k = 0; k < listeners.size(); k++)
    	{
    		listeners.get(k).loadingDone();
    	}
	}
	
	public synchronized void run()
	{
		PackageManager pkgMgr = context.getPackageManager();
		
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = pkgMgr.queryIntentActivities(mainIntent, 0);
        
		loadingDone = false;
		
		if(applicationInfos.size() > 0)
		{
			applicationInfos.clear();
		}
		
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pkgMgr));

        for(int i = 0; i < apps.size(); i++)
        {
        	ResolveInfo resolve = apps.get(i);
        	ApplicationInfo info = new ApplicationInfo();
        	
        	info.name = ""+resolve.loadLabel(pkgMgr);
        	Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(resolve.activityInfo.applicationInfo.packageName,
            									  resolve.activityInfo.name));
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	info.intent = intent;
        	info.intentUri = intent.toURI();

            info.icon = Utilities.createIconThumbnail(resolve.loadIcon(pkgMgr), context);
        	
        	applicationInfos.add(info);
        	
        	for(int k = 0; k < listeners.size(); k++)
        	{
        		listeners.get(k).applicationLoaded(info);
        	}
        }
    	
    	sendLoadingDone();
        
        loadingDone = true;
	}

	public String resolveNameByIntent(Intent intent) {
		String name = null;
		
		if(intent != null)
		{
			PackageManager pkgMgr = context.getPackageManager();
			final List<ResolveInfo> apps = pkgMgr.queryIntentActivities(intent, 0);
			
			if(apps.size() > 0)
			{
				ResolveInfo resolve = apps.get(0);
				name = (String)resolve.loadLabel(pkgMgr);
			}
		}
		
		return name;
	}

	public ApplicationInfo resolveByIntent(Intent i) {
		ApplicationInfo info = null;
		
		if(i != null)
		{
			PackageManager pkgMgr = context.getPackageManager();
			final List<ResolveInfo> apps = pkgMgr.queryIntentActivities(i, 0);
			
			if(apps.size() > 0)
			{
				ResolveInfo resolve = apps.get(0);
				
	        	Intent intent = new Intent(Intent.ACTION_MAIN);
	            intent.addCategory(Intent.CATEGORY_LAUNCHER);
	            intent.setComponent(new ComponentName(resolve.activityInfo.applicationInfo.packageName,
	            									  resolve.activityInfo.name));
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

	        	info = searchByIntent(intent);
			}
		}
		
		return info;
	}
	
	public Dialog createSelectShortcutDialog(final SelectShortcutListener listener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(myHome.getInstance());
		
		builder.setNegativeButton(R.string.dialog_button_cancel, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.onSelected(null);
				dialog.cancel();
			}
		});
		
		final ApplicationShortcutAdapter adapter = new ApplicationShortcutAdapter(myHome.getInstance());
		
		builder.setSingleChoiceItems(adapter, -1,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.onSelected(((ApplicationInfo)adapter.getItem(which)).intent);
				dialog.cancel();
			}
		});
		
		return builder.create();
	}
	
	//////////////////////////////////////////////////////////////////
	
	private class ApplicationShortcutAdapter extends BaseAdapter {
		private Context					context;
		private AppsCache				cache;
		
		public ApplicationShortcutAdapter(Context context)
		{
			this.context = context;
			this.cache = AppsCache.getInstance();
		}
		
		public int getCount() {
			return cache.getAppCount();
		}

		public Object getItem(int arg0) {
			return cache.getAppInfo(arg0);
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ApplicationInfo info = cache.getAppInfo(arg0);
			
			if(arg1 != null && arg1 instanceof TextView)
			{
				TextView item = (TextView)arg1;
				item.setText(info.name);
				item.setCompoundDrawables(info.icon, null, null, null);
				item.setTextColor(Color.BLACK);
			}
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView item = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
			item.setText(info.name);
			item.setCompoundDrawables(info.icon, null, null, null);
			item.setTextColor(Color.BLACK);
			
			return item;
		}
		
	}
	
	public class ApplicationInfo
	{
		public String	name = null;
		public Drawable icon = null;
		public Intent	intent = null;
		public String	intentUri = null;
		public boolean	filtered = false;
		public boolean	isFolder = false;
		
		public ApplicationInfo()
		{
			name = null;
			icon = null;
			intent = null;
			filtered = false;
			isFolder = false;
		}
		
		public boolean equals(ApplicationInfo info)
		{
			return ((name.equals(info.name)) &&
					((intent != null && info.intent != null &&
							(intentUri.equals(info.intentUri))) ||
					  (intent == null && info.intent == null)) &&
					(isFolder == info.isFolder));
		}
	}
	
	///////////////////////////////////////////////
	
	public interface SelectShortcutListener {
		public void onSelected(Intent intent);
	}
}
