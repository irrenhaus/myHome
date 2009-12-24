package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class MyPlacesAdapter extends BaseAdapter {
	private Context 				context = null;

	private Vector<ApplicationInfo> places;
	private Vector<Folder>			folders;
	
	public MyPlacesAdapter(Context context)
	{
		super();
		
		this.context = context;
		places = new Vector<ApplicationInfo>();
		folders = new Vector<Folder>();
	}
	
	public void reload()
	{
		MyHomeDB homeDb = new MyHomeDB(context);
		SQLiteDatabase db = homeDb.getReadableDatabase();
		
		places.clear();
		
		Cursor c = db.query(MyHomeDB.FOLDER_DEFINITION_TABLE, new String[] {Folder.TITLE},
					null, null, null, null, null);
		
		while(c.moveToNext())
		{
			ApplicationInfo info = AppsCache.getInstance().new ApplicationInfo();
			
			info.isFolder = true;
			info.filtered = true;
			info.icon = Folder.getIcon(context);
			info.name = c.getString(c.getColumnIndex(Folder.TITLE));
			
			add(info);
		}
		
		db.close();
		homeDb.close();
	}
	
	public void add(ApplicationInfo info)
	{
		places.add(info);
		folders.add(new Folder(context, info.name));
	}
	
	public Folder getFolder(int position)
	{
		if(position < 0 || position >= folders.size())
			return null;
		
		return folders.get(position);
	}
	
	public Folder getFolder(String name)
	{
		for(int i = 0; i < folders.size(); i++)
		{
			if(folders.get(i).getTitle().equals(name))
				return folders.get(i);
		}
		
		return null;
	}
	
	@Override
	public int getCount() {
		return places.size();
	}

	@Override
	public Object getItem(int position) {
		return places.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null || !(convertView instanceof ShortcutTextView))
			convertView = new ShortcutTextView(context);
		
		final ApplicationInfo info = places.get(position);
		
		((ShortcutTextView)convertView).setText(info.name);
		((ShortcutTextView)convertView).setCompoundDrawablesWithIntrinsicBounds(null,
																		info.icon,
																		null,
																		null);
		((ShortcutTextView)convertView).setLines(2);
		((ShortcutTextView)convertView).setGravity(Gravity.CENTER);
		
		return convertView;
	}
}
