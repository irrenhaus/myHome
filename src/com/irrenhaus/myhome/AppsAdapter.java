package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class AppsAdapter extends BaseAdapter {
	private Context 	context = null;
	private AppsFilter	filter = null;
	
	private Vector<ApplicationInfo> apps;
	
	private AppsCache cache = AppsCache.getInstance();
	
	public AppsAdapter(Context context, AppsFilter filter)
	{
		super();
		
		this.context = context;
		this.filter = filter;
		apps = new Vector<ApplicationInfo>();
	}
	
	public void setFilter(AppsFilter filter)
	{
		this.filter = filter;
	}
	
	public void reload()
	{
		apps.clear();
		if(filter != null)
			filter.init();
		
		for(int i = 0; i < cache.getAppCount(); i++)
		{
			ApplicationInfo info = cache.getAppInfo(i);
			
			if(filter == null || filter.filterApplicationInfo(info))
				apps.add(info);
		}
		
		if(filter != null)
			filter.done();
	}
	
	public void add(ApplicationInfo info)
	{
		apps.add(info);
	}
	
	public int getCount() {
		return apps.size();
	}

	public Object getItem(int position) {
		return apps.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null || !(convertView instanceof ShortcutTextView))
			convertView = new ShortcutTextView(context);
		
		final ApplicationInfo info = apps.get(position);
		
		((ShortcutTextView)convertView).setText(info.name);
		((ShortcutTextView)convertView).setCompoundDrawables(null,
																		info.icon,
																		null,
																		null);
		((ShortcutTextView)convertView).setLines(2);
		((ShortcutTextView)convertView).setGravity(Gravity.CENTER);
		
		convertView.setDrawingCacheEnabled(true);
		
		return convertView;
	}
}
