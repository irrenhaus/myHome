package com.irrenhaus.myhome;

import java.util.Vector;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.irrenhaus.myhome.CellLayout.LayoutParams;

public class WidgetCache {
	private static WidgetCache				instance;
	
	private AppWidgetManager				manager;
	private MyHomeAppWidgetHost				host = null;
	private static final int				appWidgetHostID = 1337;
	
	private Vector<AppWidgetProviderInfo>	widgetProviderInfos;
	private Vector<MyHomeAppWidgetHostView>	widgetViews;
	private Vector<Integer>					widgetIds;
	
	private Context							context;
	
	public WidgetCache()
	{
		widgetProviderInfos = new Vector<AppWidgetProviderInfo>();
		widgetViews = new Vector<MyHomeAppWidgetHostView>();
		widgetIds = new Vector<Integer>();
	}
	
	public void init(Context c)
	{
		if(manager == null)
			manager = AppWidgetManager.getInstance(c);
		
		if(host == null)
		{
			host = new MyHomeAppWidgetHost(c, appWidgetHostID);
			host.startListening();
		}
        
		if(context == null)
			context = c;
		
		int size = widgetIds.size();
		
		for(int i = 0; i < size; i++)
		{
			MyHomeAppWidgetHostView v = widgetViews.get(i);
			if(v.getParent() != null)
				((DesktopView)v.getParent()).removeView(v);
		}
		
		widgetViews.clear();
		
		for(int i = 0; i < size; i++)
		{
			MyHomeAppWidgetHostView v = (MyHomeAppWidgetHostView)
								host.createView(context, widgetIds.get(i),
										widgetProviderInfos.get(i));
			widgetViews.add(v);
		}
	}
	
	public static WidgetCache getInstance()
	{
		if(instance == null)
			instance = new WidgetCache();
		
		return instance;
	}
	
	public int getWidgetPosForId(int id)
	{
		int size = widgetIds.size();
		
		for(int i = 0; i < size; i++)
		{
			int widget = widgetIds.get(i);
			if(widget == id)
				return i;
		}
		
		return -1;
	}

	public void deleteAppWidgetId(int widgetid) {
		host.deleteAppWidgetId(widgetid);
	}

	public void widgetReady(int widgetid) {
		for(int id: widgetIds)
		{
			if(id == widgetid)
				return;
		}
		
		MyHomeAppWidgetHostView view = null;
		AppWidgetProviderInfo info = null;
		
		widgetIds.add(widgetid);
		
		info = manager.getAppWidgetInfo(widgetid);
		
		view = (MyHomeAppWidgetHostView) host.createView(context, widgetid, info);
		
		widgetProviderInfos.add(info);
		widgetViews.add(view);
	}

	public MyHomeAppWidgetHostView getAppWidgetView(int pos) {
		return widgetViews.get(pos);
	}

	public AppWidgetProviderInfo getAppWidgetInfo(int pos) {
		return widgetProviderInfos.get(pos);
	}

	public boolean startWidgetConfigure(int pos) {
		AppWidgetProviderInfo info = getAppWidgetInfo(pos);
		int id = widgetIds.get(pos);
		
		
		if(info.configure == null)
			return false;
		
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		intent.setComponent(info.configure);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		
		myHome.getInstance().startWidgetConfigure(intent);
		
		return true;
	}

	public void setLayoutParams(int pos, LayoutParams params) {
		getAppWidgetView(pos).setLayoutParams(params);
	}

	public int allocateAppWidgetId() {
		return host.allocateAppWidgetId();
	}
}
