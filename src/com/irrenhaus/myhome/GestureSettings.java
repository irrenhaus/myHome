package com.irrenhaus.myhome;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class GestureSettings extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.gesture_preferences);

		SeekBar minScore = (SeekBar)findViewById(R.id.minScoreValue);
		final TextView valueView = (TextView)findViewById(R.id.scoreValueView);
		ListView gesturesList = (ListView)findViewById(R.id.gesturesList);
		
		final GesturesAdapter adapter = new GesturesAdapter(this);
		
		int defProgress = (int) (Config.getFloat(Config.GESTURE_MIN_SCORE_KEY) * 10);
		valueView.setText(""+((float)defProgress / 10.0f));
		
		minScore.setProgress(defProgress);
		minScore.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				float newValue = ((float)arg1 / 10.0f);
				valueView.setText("" + newValue);
				Config.putFloat(Config.GESTURE_MIN_SCORE_KEY, newValue);
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				
			}

			public void onStopTrackingTouch(SeekBar arg0) {
				
			}
		});
		
		gesturesList.setAdapter(adapter);
		
		gesturesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, final int pos, long id) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GestureSettings.this);
				
				builder.setTitle(R.string.dialog_remove_gesture_title);
				
				String msg = getResources().getString(R.string.dialog_remove_gesture_message);
				msg += " "+((TextView)v).getText();
				
				builder.setMessage(msg);
				
				builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						adapter.remove(pos);
						dialog.cancel();
					}
				});
				
				builder.create().show();
			}
		});
	}
	
	private class GesturesAdapter extends BaseAdapter {
		private Context					context;
		private Vector<Object>			gestureData;
		private Vector<Gesture>			gestures;
		private Vector<BitmapDrawable>	gestureIcons;
		private int						iconSize;
		
		public GesturesAdapter(Context context)
		{
			this.context = context;

			gestureData = new Vector<Object>();
			gestures = new Vector<Gesture>();
			gestureIcons = new Vector<BitmapDrawable>();
			
			iconSize = (int)context.getResources().getDimension(android.R.dimen.app_icon_size);
			
			load();
		}
		
		synchronized private void add(Object data, Gesture gesture, BitmapDrawable icon)
		{
			gestureData.add(data);
			gestures.add(gesture);
			gestureIcons.add(icon);
		}
		
		synchronized private void removeData(int pos)
		{
			gestureData.remove(pos);
			gestures.remove(pos);
			gestureIcons.get(pos).getBitmap().recycle();
			gestureIcons.remove(pos);
		}
		
		synchronized private void remove(int pos)
		{
			Log.d("myHome", "Position: "+pos+", Size: "+getCount());
			
			String name = GestureView.getGestureName(pos);
			GestureView.removeGesture(name, gestures.get(pos));
			
			removeData(pos);
			
			notifyDataSetChanged();
		}
		
		synchronized public void load()
		{
			new Thread(new Runnable() {
				public void run() {
					try {
						int count = GestureView.getGestureCount();
						
						for(int i = 0; i < count; i++)
						{
							String name = GestureView.getGestureName(i);
							Gesture gesture = GestureView.getGesture(i);
							Bitmap icon = gesture.toBitmap(iconSize, iconSize, 5,
											Color.argb(255, 255, 255, 0));
							BitmapDrawable drawable = new BitmapDrawable(context.getResources(), icon);
							drawable.setBounds(0, 0, iconSize, iconSize);
							
							Object data = GestureView.getGestureData(Intent.parseUri(name, 0),
											context);
							
							add(data, gesture, drawable);
							
							GestureSettings.this.runOnUiThread(new Runnable() {
								public void run() {
									notifyDataSetChanged();
								}
							});
						}
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		synchronized public int getCount() {
			return gestures.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
		
		synchronized private Object getData(int pos)
		{
			return gestureData.get(pos);
		}
		
		synchronized private BitmapDrawable getIcon(int location)
		{
			return gestureIcons.get(location);
		}
		
		synchronized private Gesture getGesture(int location)
		{
			return gestures.get(location);
		}

		synchronized public View getView(int position, View convertView, ViewGroup parent) {
			if(position > gestures.size() || position > gestureData.size() ||
				position > gestureIcons.size())
				return null;
			
			TextView view = null;
			if(convertView != null && convertView instanceof TextView)
				view = (TextView) convertView;
			else
				view = new TextView(context);
			
			Drawable right = null;
			String text = "";
			Object data = getData(position);
			if(data instanceof HashMap)
			{
				Bitmap icon = (Bitmap)((HashMap<String, Object>)data).get("icon");
				right = new BitmapDrawable(icon);
				right.setBounds(0, 0, iconSize, iconSize);
				text = (String) ((HashMap<String, Object>)data).get("name");
			}
			else if(data instanceof ApplicationInfo)
			{
				right = ((ApplicationInfo)data).icon;
				text = ((ApplicationInfo)data).name;
			}
			
			view.setCompoundDrawables(getIcon(position), null, right, null);
			view.setText(text);
			view.setGravity(Gravity.CENTER);
			
			view.setTag(position);
			
			return view;
		}
		
	}
}
