package com.irrenhaus.myhome;

import java.util.List;
import java.util.Vector;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class TaskAdapter extends BaseAdapter {
	private Context 				context;

	private Vector<ApplicationInfo> runningApps;
	
	private LayoutInflater			inflater;

	public TaskAdapter(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		runningApps = new Vector<ApplicationInfo>();
	}

	public void load() {
		runningApps.clear();
		
		ActivityManager mgr = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();

		for (RunningAppProcessInfo process : processes) {
			ApplicationInfo info = AppsCache.getInstance().searchByPackageName(
					process.processName);

			if (info != null) {
				runningApps.add(info);
			}
		}

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return runningApps.size();
	}

	@Override
	public Object getItem(int position) {
		return runningApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		ApplicationInfo info;

		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
			
			holder.info = info = runningApps.get(position);
		} else {
			holder = (ViewHolder) convertView.getTag();
			info = holder.info;
		}
		
		holder.text.setText(info.name);
		holder.text.setCompoundDrawables(info.icon, null, null, null);

		return convertView;
	}

	static public class ViewHolder {
		TextView 		text;
		ApplicationInfo	info;
	}

}
