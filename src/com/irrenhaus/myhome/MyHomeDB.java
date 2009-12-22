package com.irrenhaus.myhome;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHomeDB extends SQLiteOpenHelper {
	public static final int		MYHOME_DB_VERSION = 4;
	
	public static final String 	WORKSPACE_TABLE = "workspace_data";

	public static final String	FOLDER_TABLE = "folder_data";

	public static final String	FOLDER_DEFINITION_TABLE = "folder_definition";
	
	public MyHomeDB(Context context) {
		super(context, "myhome.db", null, MYHOME_DB_VERSION);
		
	}
	
	public void finalize()
	{
		close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + WORKSPACE_TABLE + " (" +
				   DesktopItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				   DesktopView.DESKTOP_NUMBER + " INTEGER," +
				   DesktopItem.INTENT + " TEXT," +
				   DesktopItem.LAYOUT_PARAMS + " TEXT," +
				   DesktopItem.TYPE + " INTEGER" +
				   ");");
		
		db.execSQL("CREATE TABLE " + FOLDER_DEFINITION_TABLE + " (" +
				   Folder.TITLE + " TEXT" +
				   ");");
		
		db.execSQL("CREATE TABLE " + FOLDER_TABLE + " (" +
				   Folder.TITLE + " TEXT," +
				   Folder.INTENT + " TEXT" +
				   ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("CREATE TABLE " + FOLDER_DEFINITION_TABLE + " (" +
				   Folder.TITLE + " TEXT" +
				   ");");
		
		db.execSQL("CREATE TABLE " + FOLDER_TABLE + " (" +
				   Folder.TITLE + " TEXT" +
				   Folder.INTENT + " TEXT" +
				   ");");
	}
	
	public static String layoutParams2String(CellLayout.LayoutParams params)
	{
		String ret = "";
		
		ret = "cellX: " + params.cellX + " cellY: " + params.cellY +
			  " spanX: " + params.cellHSpan + " spanY: " + params.cellVSpan;
		
		return ret;
	}

	public static CellLayout.LayoutParams string2LayoutParams(String in)
	{
		Pattern pat = Pattern.compile("cellX: ([0-9]*) cellY: ([0-9]*) spanX: ([0-9]*) spanY: ([0-9]*)");
		Matcher mat = pat.matcher(in);
		
		if(mat.matches())
		{
			int cx = Integer.parseInt(mat.group(1));
			int cy = Integer.parseInt(mat.group(2));
			int sx = Integer.parseInt(mat.group(3));
			int sy = Integer.parseInt(mat.group(4));
			
			return new CellLayout.LayoutParams(cx, cy, sx, sy);
		}
		else
			return null;
	}
}
