package com.irrenhaus.myhome;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class GestureView extends LinearLayout
						 implements OnGesturePerformedListener {
	public static final String			LIBRARY_FILE = "/sdcard/myHome/gesture_library.dat";
	
	private Runnable					doOnAnimationEnd = null;
	private GestureOverlayView			gestureView;
	
	private static GestureLibrary		library;
	private static boolean				isLoaded = false;
	private static Gesture				pendingGesture = null;
	
	public GestureView(Context context) {
		super(context);
		init();
	}

	public GestureView(Context context, AttributeSet set) {
		super(context, set);
		init();
	}
	
	public void init()
	{
		gestureView = new GestureOverlayView(getContext());
		
		gestureView.addOnGesturePerformedListener(this);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
																		 LayoutParams.FILL_PARENT);
		params.weight = 1.0f;
		
		gestureView.setLayoutParams(params);
		
		addView(gestureView);
		
		setBackgroundResource(R.drawable.tray_bg);
	}
	
	public static void loadLibrary()
	{
		File libFile = new File(LIBRARY_FILE);
		if(library == null)
			library = GestureLibraries.fromFile(libFile);
		
		isLoaded = library.load();
	}
	
	public static int getGestureCount()
	{
		return library.getGestureEntries().size();
	}
	
	public static String getGestureName(int pos)
	{
		return (String) library.getGestureEntries().toArray()[pos];
	}
	
	public static Gesture getGesture(int pos)
	{
		return library.getGestures((String) library.getGestureEntries().toArray()[pos]).get(0);
	}
	
	public static Object getGestureData(Intent intent, Context context)
	{
		String action = intent.getAction();
		
		if(action.equals(Intent.ACTION_CALL) || action.equals(Intent.ACTION_SENDTO))
		{
			HashMap<String, Object> data = new HashMap<String, Object>();
			
			String numberStr = "";
			
			if(action.equals(Intent.ACTION_SENDTO))
				numberStr = intent.getData().toString().replaceAll("smsto:", "");
			else
				numberStr = intent.getData().toString().replaceAll("tel:", "");
			
			data.put("number", numberStr);
			
			String who = "";
			long id = 0;
			
			who = getNameForNumber(numberStr, context);
			id = getIdForNumber(numberStr, context);
			
			data.put("name", who);
			
			Uri iconUri = ContentUris.withAppendedId(People.CONTENT_URI, id);
			
			data.put("icon", People.loadContactPhoto(context, iconUri, R.drawable.icon, null));
			
			return data;
		} else if(action.equals(Intent.ACTION_MAIN)) {
			ApplicationInfo info = AppsCache.getInstance().resolveByIntent(intent);
			
			if(info != null)
			{
				return info;
			}

			String name = AppsCache.getInstance().resolveNameByIntent(intent);
			
			if(name != null)
			{
				info = AppsCache.getInstance().new ApplicationInfo();
				info.filtered = true;
				info.name = name;
				info.icon = context.getResources().getDrawable(R.drawable.icon);
				
				return info;
			}
		}
		
		return null;
	}
	

	
	private static String getNameForNumber(String number, Context context)
	{
		String[] projection = new String[] {
                People._ID,
                People.NAME,
             };
		
		String who = "";
		
		ContentResolver resolver = context.getContentResolver();
		Cursor c = resolver.query(Contacts.Phones.CONTENT_URI, projection,
		          Contacts.PhonesColumns.NUMBER+"=?", new String[] {number}, null);
		
		if(c != null && c.moveToFirst())
		{
			who = c.getString(c.getColumnIndex(People.NAME));
			
			c.close();
		}
		
		return who;
	}
	
	private static long getIdForNumber(String number, Context context)
	{
		String[] projection = new String[] {
                People._ID,
                Phones.PERSON_ID
             };
		
		long id =-1;
		
		ContentResolver resolver = context.getContentResolver();
		Cursor c = resolver.query(Contacts.Phones.CONTENT_URI, projection,
		          Contacts.PhonesColumns.NUMBER+"=?", new String[] {number}, null);
		
		if(c != null && c.moveToFirst())
		{
			id = c.getLong(c.getColumnIndex(Phones.PERSON_ID));
			
			c.close();
		}
		
		return id;
	}
	
	public static void addNewGesture(Gesture gesture)
	{
		pendingGesture = gesture;
		
		myHome.getInstance().getWorkspace().closeAllOpenFor(new Runnable() {
			public void run() {
				myHome.getInstance().startGestureShortcutPicker();
			}
		});
	}
	
	public static void removeGesture(String name, Gesture gesture)
	{
		if(library == null)
			return;
		
		library.removeGesture(name, gesture);
		
		library.save();
	}
	
	public static void saveNewGesture(String entryName, Gesture gesture, Context context)
	{
		if(library == null)
			return;
		
		library.addGesture(entryName, gesture);
		
		library.save();
		
		if(context != null)
			performGestureAction(entryName, context);
	}
	
	public static void savePendingGesture(String entryName, Context context)
	{
		if(library == null)
			return;
		
		library.addGesture(entryName, pendingGesture);
		
		library.save();
		
		if(context != null)
			performGestureAction(entryName, context);
	}
	
	private static void showIntentAction(Intent intent, Context context)
	{
		String action = intent.getAction();
		Log.d("myHome", "Action: "+action);
		
		String text = null;
		
		if(action.equals(Intent.ACTION_CALL))
		{
			String numberStr = intent.getData().toString().replaceAll("tel:", "");
			
			String who = getNameForNumber(numberStr, context);
			
			text = context.getResources().getString(R.string.gesture_action_call);
			text += " "+who;
			
			Log.d("myHome", "Gesture: "+text);
		} else if(action.equals(Intent.ACTION_MAIN)) {
			String name = AppsCache.getInstance().resolveNameByIntent(intent);
			
			if(name != null)
			{
				text = context.getResources().getString(R.string.gesture_action_main);
				text += " "+name;
				
				Log.d("myHome", "Gesture: "+text);
			}
		} else if(action.equals(Intent.ACTION_SENDTO)) {
			String numberStr = intent.getData().toString().replaceAll("smsto:", "");
			
			String who = getNameForNumber(numberStr, context);
			
			text = context.getResources().getString(R.string.gesture_action_sendto);
			text += " "+who;
			
			Log.d("myHome", "Gesture: "+text);
		}
		
		if(text != null)
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	private static void performGestureAction(String name, final Context context)
	{
		try {
			final Intent intent = Intent.parseUri(name, Intent.FLAG_ACTIVITY_NEW_TASK);
			
			showIntentAction(intent, context);
			
			myHome.getInstance().getWorkspace().closeAllOpenFor(new Runnable() {
				public void run() {
						context.startActivity(intent);
				}
			});
		} catch (URISyntaxException e) {
			Toast.makeText(context, R.string.gesture_unknown_action, Toast.LENGTH_SHORT);
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		float minScore = Config.getFloat(Config.GESTURE_MIN_SCORE_KEY);
		
		if(library != null && isLoaded)
		{
			ArrayList<Prediction> predictions = library.recognize(gesture);
			
			if(predictions.size() > 0)
			{
				Prediction cur = predictions.get(0);
				
				if(cur.score > minScore)
				{
					Log.d("myHome", "Selected gesture with score "+cur.score);
					performGestureAction(cur.name, getContext());
				}
				else
				{
					addNewGesture(gesture);
				}
			}
			else
			{
				addNewGesture(gesture);
			}
		}
		else if(library != null)
		{
			Log.d("myHome", "No gestures, adding new.");
			addNewGesture(gesture);
		}
		else
			Log.d("myHome", "Library == null!");
	}
	
	@Override
	public void onAnimationEnd()
	{
		super.onAnimationEnd();
		
		if(doOnAnimationEnd != null)
		{
			doOnAnimationEnd.run();
			doOnAnimationEnd = null;
		}
	}

	public Runnable getDoOnAnimationEnd() {
		return doOnAnimationEnd;
	}

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		this.doOnAnimationEnd = doOnAnimationEnd;
	}

}
