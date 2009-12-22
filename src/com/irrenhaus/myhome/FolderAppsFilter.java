package com.irrenhaus.myhome;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class FolderAppsFilter implements AppsFilter {
	private String			folderTitle;

	private String[]		containingIntents;
	
	private MyHomeDB		homeDb;
	private SQLiteDatabase	db;
	
	public FolderAppsFilter(String title, Context context) {
		this.setFolderTitle(title);
		
		homeDb = new MyHomeDB(context);
	}

	@Override
	public boolean filterApplicationInfo(ApplicationInfo info) {
		for(int i = 0; i < containingIntents.length; i++)
		{
			if(containingIntents[i].equals(info.intent.toURI()))
				return true;
		}
		
		return false;
	}

	public void setFolderTitle(String folderTitle) {
		this.folderTitle = folderTitle;
	}

	public String getFolderTitle() {
		return folderTitle;
	}

	@Override
	public void done() {
	}

	@Override
	public void init() {
		db = homeDb.getReadableDatabase();
		
		Cursor c = db.query(MyHomeDB.FOLDER_TABLE, new String[] {Folder.TITLE, Folder.INTENT},
							Folder.TITLE+"=?", new String[] {folderTitle},
							null, null, null);
		containingIntents = new String[c.getCount()];
		
		for(int i = 0; i < c.getCount() && c.moveToNext(); i++)
		{
			containingIntents[i] = c.getString(c.getColumnIndex(Folder.INTENT));
		}
		
		c.close();
		
		db.close();
		homeDb.close();
	}

}
