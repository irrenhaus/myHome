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
	
	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public Object getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null || !(convertView instanceof TextView))
			convertView = new TextView(context);
		
		final ApplicationInfo info = apps.get(position);
		
		((TextView)convertView).setText(info.name);
		((TextView)convertView).setCompoundDrawablesWithIntrinsicBounds(null,
																		info.icon,
																		null,
																		null);
		((TextView)convertView).setLines(2);
		((TextView)convertView).setGravity(Gravity.CENTER);
		
		return convertView;
	}
}
