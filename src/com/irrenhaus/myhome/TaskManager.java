package com.irrenhaus.myhome;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;
import com.irrenhaus.myhome.TaskAdapter.ViewHolder;



public class TaskManager extends Slider
						 implements OnItemClickListener {
	private ListView	taskList;
	private TaskAdapter	adapter;
	private Context		context;
	
	public TaskManager(Context context, AttributeSet attr) {
		super(context, attr);
		
		this.context = context;
	}

	public void init()
	{
		if(adapter == null)
		{
			adapter = new TaskAdapter(getContext());
		}
		
		if(taskList == null)
		{
			taskList = (ListView)getContent();
			
			taskList.setAdapter(adapter);
			
			taskList.setCacheColorHint(0);
			
			taskList.setOnItemClickListener(this);
		}
		
		adapter.load();
	}

	@Override
	public void onItemClick(final AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final ApplicationInfo info = ((ViewHolder)arg1.getTag()).info;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.task_mgr_close_task_title);
		String text = context.getResources().getString(R.string.task_mgr_close_task_message);
		builder.setMessage(text + " " + info.name);
		
		builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ActivityManager mgr = (ActivityManager) context
										.getSystemService(Context.ACTIVITY_SERVICE);
				mgr.restartPackage(info.packageName);
				((TaskAdapter)arg0.getAdapter()).load();
				dialog.cancel();
			}
		});
		
		builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		builder.create().show();
	}
}
