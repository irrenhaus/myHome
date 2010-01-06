package com.irrenhaus.myhome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.irrenhaus.myhome.CellLayout.LayoutParams;

public class myHome extends Activity {
	private Workspace					workspace = null;
	private Screen						screen = null;
	private Toolbar						toolbar = null;

	private static final int			PICK_WIDGET = 1;
	public  static final int			ADD_WIDGET = 2;
	private static final int			PICK_ACTION = 3;
	

	public static final int 			DESKTOP_ACTION_ADD_WIDGET = 0;
	public static final int 			DESKTOP_ACTION_SET_WALLPAPER = 1;
	
	public static final int 			DESKTOP_ACTION_ITEM_COUNT = 2;

	private MyHomeDB 					homeDb;

	private SQLiteDatabase 				db;
	private AlertDialog					currentDialog;
	private boolean						databaseChanged = false;
	private BroadcastReceiver wallpaperReceiver;
	
	private static myHome				instance;

	private static final int			MENU_ENTRY_SETTINGS = R.string.menu_entry_settings;
	private static final int			MENU_ENTRY_MYHOME_SETTINGS = R.string.menu_entry_myhome_settings;
	private static final int			MENU_ENTRY_ADD_PLACE = R.string.menu_entry_add_place;
	private static final int			MENU_ENTRY_RESTORE = R.string.menu_entry_restore_desktop;
	private static final int			MENU_ENTRY_RESTART = R.string.menu_entry_restart_desktop;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        instance = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        Config.readConfiguration(this);
        
        processInstanceState(savedInstanceState);
        
        CrashHandler.getInstance().CheckErrorAndSendMail(this);
        
        AppsCache.getInstance().setContext(getApplicationContext());
        WidgetCache.getInstance().init(getApplicationContext());
        
        workspace = (Workspace)findViewById(R.id.workspace);
        workspace.setHome(this);
        
        screen = (Screen)findViewById(R.id.screen);
        
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.initToolbar();

        AppsCache.getInstance().addLoadingListener(workspace);
        
        WallpaperManager mgr = WallpaperManager.getInstance();
        
        mgr.setActivity(this);
        
		if(!AppsCache.getInstance().isLoadingDone())
		{
			Toast.makeText(this, R.string.loading_please_wait, Toast.LENGTH_SHORT).show();
			AppsCache.getInstance().start();
		}
		else
			AppsCache.getInstance().sendLoadingDone();
		
		AppWidgetManager.getInstance(myHome.this);
		
		wallpaperReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				screen.wallpaperChanged();
				Log.d("myHome", "Received :)");
			}
		};
		
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
		
		registerReceiver(wallpaperReceiver, intentFilter);
		
		WallpaperChangerService.setContext(getApplicationContext());
		if(Config.getBoolean(Config.WALLPAPER_CHANGER_ACTIVE_KEY, false))
			startWallpaperChangerService();
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
    	unregisterReceiver(wallpaperReceiver);
    	stopWallpaperChangerService();
    }

	@Override
    public void onStop()
    {
    	super.onStop();
        
        Config.saveConfiguration(this);
    	
    	storeToSDCard();
    }
    
    public static myHome getInstance()
    {
    	return instance;
    }
    
    public boolean isDiamondLayout()
    {
    	return false;
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	Resources res = getResources();
    	
    	Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    			Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    	
    	menu.add(1, MENU_ENTRY_ADD_PLACE, ContextMenu.NONE,
   			 res.getString(MENU_ENTRY_ADD_PLACE)).setOnMenuItemClickListener(
   					 new OnMenuItemClickListener() {
							public boolean onMenuItemClick(MenuItem arg0) {
								AlertDialog.Builder builder =
									new AlertDialog.Builder(myHome.this);
								
								LinearLayout l = new LinearLayout(myHome.this);
								
								l.setLayoutParams(new LinearLayout.LayoutParams(
														LayoutParams.FILL_PARENT,
														LayoutParams.FILL_PARENT));
								
								final EditText edit = new EditText(myHome.this);
								
								edit.setLayoutParams(new LinearLayout.LayoutParams(
														LayoutParams.FILL_PARENT,
														LayoutParams.FILL_PARENT));
								
								l.addView(edit);
								
								builder.setTitle(myHome.this.getResources().
										getString(R.string.dialog_title_add_place));
								builder.setView(l);

								String pos = myHome.this.getResources().getString(
													R.string.dialog_button_add);
								String neg = myHome.this.getResources().getString(
										R.string.dialog_button_cancel);
								
								builder.setPositiveButton(pos,
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										Folder f = new Folder(myHome.this,
																edit.getText().toString());
										storeAddPlace(f);
										
										myHome.this.runOnUiThread(new Runnable() {
											public void run() {
												MyPlacesGrid grid = myHome.this.workspace.
																		getMyPlacesGrid();
												
												((MyPlacesAdapter)grid.getAdapter()).reload();
												((MyPlacesAdapter)grid.getAdapter()).
														notifyDataSetChanged();
											}
										});
										
										dialog.cancel();
									}
								});
								
								builder.setNegativeButton(neg,
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								});
								
								builder.create().show();
								
								return true;
							}
   					 });
    	
    	menu.add(1, MENU_ENTRY_RESTORE, ContextMenu.NONE,
   			 res.getString(MENU_ENTRY_RESTORE)).setOnMenuItemClickListener(
   					 new OnMenuItemClickListener() {
							public boolean onMenuItemClick(MenuItem arg0) {
								AlertDialog.Builder builder =
									new AlertDialog.Builder(myHome.this);
								
								builder.setTitle(myHome.this.getResources().
										getString(R.string.dialog_title_restore_desktop));
								builder.setTitle(myHome.this.getResources().
										getString(R.string.dialog_message_restore_desktop));

								String pos = myHome.this.getResources().getString(
													R.string.dialog_button_ok);
								String neg = myHome.this.getResources().getString(
										R.string.dialog_button_cancel);
								
								builder.setPositiveButton(pos,
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										if(restoreFromSDCard())
											workspace.loadWorkspaceDatabase();
										
										
										dialog.cancel();
									}
								});
								
								builder.setNegativeButton(neg,
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								});
								
								builder.create().show();
								
								return true;
							}
   					 });
    	
    	menu.add(1, MENU_ENTRY_RESTART, ContextMenu.NONE,
      			 res.getString(MENU_ENTRY_RESTART)).setOnMenuItemClickListener(
      					 new OnMenuItemClickListener() {
   							public boolean onMenuItemClick(MenuItem arg0) {
   								myHome.this.finish();
   								return true;
   							}
      					 });

    	menu.add(2, MENU_ENTRY_SETTINGS,
    			ContextMenu.NONE, res.getString(MENU_ENTRY_SETTINGS)).setIntent(intent);

    	menu.add(2, MENU_ENTRY_MYHOME_SETTINGS,
    			ContextMenu.NONE, res.getString(MENU_ENTRY_MYHOME_SETTINGS)).setIntent(
    					new Intent(this, Settings.class));
    	
    	return true;
    }
    
    public void openDatabase()
    {
    	if(homeDb == null)
    		homeDb = new MyHomeDB(this);
    	
    	if(db == null || !db.isOpen())
    		db = homeDb.getWritableDatabase();
    }
    
	@Override
	protected void onResume()
	{
		super.onResume();
    	
		openDatabase();
	}
	
    public void startWidgetPicker()
    {
    	int widgetid = WidgetCache.getInstance().allocateAppWidgetId();
		
		Intent picker = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		picker.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetid);
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, new ArrayList());
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, new ArrayList());
		
		startActivityForResult(picker, PICK_WIDGET);
    }
    
    public void startDesktopActionPicker()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	String items[] = new String[DESKTOP_ACTION_ITEM_COUNT];

    	items[DESKTOP_ACTION_ADD_WIDGET] = getResources().
    										getString(R.string.action_menu_entry_widget);
    	items[DESKTOP_ACTION_SET_WALLPAPER] = getResources().
    										getString(R.string.action_menu_entry_wallpaper);
    	
    	builder.setInverseBackgroundForced(true);
    	builder.setCustomTitle(null);
    	
    	builder.setItems(items,
    			new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch(which)
						{
							case DESKTOP_ACTION_ADD_WIDGET:
							{
				    			startWidgetPicker();
								break;
							}
								
							case DESKTOP_ACTION_SET_WALLPAPER:
							{
				    			Intent intent = new Intent(myHome.this, WallpaperChanger.class);
				    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    			startActivity(intent);
								break;
							}
							
							default:
								Log.d("myHome", "Unknown pos: "+which);
							}
					
						currentDialog.cancel();
						currentDialog = null;
					}
    			});
    	
    	currentDialog = builder.create();
    	currentDialog.show();
    }
    
    @Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
    	runOnUiThread(new Runnable() {
			public void run() {
				if(requestCode == PICK_WIDGET)
				{
					int widgetid = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
					
					if(resultCode != Activity.RESULT_OK)
						WidgetCache.getInstance().deleteAppWidgetId(widgetid);
					else
					{
						WidgetCache.getInstance().widgetReady(widgetid);
						
						workspace.getCurrentDesktop().addAppWidget(true, widgetid);
					}
				}
		    	else if(requestCode == ADD_WIDGET)
		    	{
		    		int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		    		if(resultCode == Activity.RESULT_OK && id != -1)
		    		{
		    			workspace.getCurrentDesktop().completeAddWidget(id);
		    		}
		    		else
		    		{
		    			WidgetCache.getInstance().deleteAppWidgetId(id);
		    		}
		    	}
			}
    	});
	}
    
    private String dumpCursorToSql(Cursor cursor, String table)
    {
    	String ret = "";
    	String[] cols = cursor.getColumnNames();
    	
    	while(cursor.moveToNext())
    	{
    		String query = "INSERT INTO " + table + " (";
    		
    		for(int i = 0; i < cols.length; i++)
    		{
    			query += cols[i];
    			if(i+1 < cols.length)
    			{
    				query += ", ";
    			}
    		}

    		query += ") VALUES (";
    		
    		for(int i = 0; i < cols.length; i++)
    		{
    			String name = cursor.getColumnName(i);
    			if(name.equals(DesktopItem._ID) || name.equals(DesktopItem.TYPE) ||
    					name.equals(DesktopView.DESKTOP_NUMBER))
    				query += ""+cursor.getInt(i);
    			else
    				query += DatabaseUtils.sqlEscapeString(cursor.getString(i));
    			
    			if(i+1 < cols.length)
    				query += ", ";
    		}
    		
    		query += ");";
    		
    		ret += query + "\n";
    	}
    	
    	return ret;
    }
    
    private void storeToSDCard()
    {
    	if(!databaseChanged)
    		return;
    	
    	openDatabase();
    	
    	int count = 0;
    	
    	try
    	{
	    	File sdcard = Environment.getExternalStorageDirectory();
	    	
	    	if(sdcard == null || !sdcard.canWrite()) {
	    		Log.d("myHome", "Cannot write to sdcard (sdcard == null || !sdcard.canWrite())");
	    		return;
	    	}
	    	
	    	File myFolder = new File(sdcard.getAbsolutePath()+"/myHome/");
	    	if(!myFolder.exists() && !myFolder.mkdir()) {
	    		Log.d("myHome", "Cannot write to sdcard (!myFolder.exists() && !myFolder.mkdir())");
	    		return;
	    	}
	    	
	    	File data = new File(myFolder.getAbsolutePath()+"/last_state.data");
	    	if(data.exists())
	    		data.delete();
	    	
	    	if(!data.createNewFile() || !data.canWrite()) {
	    		Log.d("myHome", "Cannot write to sdcard (!data.createNewFile() || !data.canWrite())");
	    		return;
	    	}
	    	
	    	FileWriter fileWriter = new FileWriter(data);
	    	BufferedWriter buf = new BufferedWriter(fileWriter);
	    	
	    	Cursor workspace = db.query(MyHomeDB.WORKSPACE_TABLE,
										null,
										null, null, null, null, null);
	    	
	    	Cursor folder_def = db.query(MyHomeDB.FOLDER_DEFINITION_TABLE,
										null,
										null, null, null, null, null);
	    	
	    	Cursor folder_con = db.query(MyHomeDB.FOLDER_TABLE,
										null,
										null, null, null, null, null);

	    	String workspace_dump = dumpCursorToSql(workspace, MyHomeDB.WORKSPACE_TABLE);
	    	String folder_def_dump = dumpCursorToSql(folder_def, MyHomeDB.FOLDER_DEFINITION_TABLE);
	    	String folder_con_dump = dumpCursorToSql(folder_con, MyHomeDB.FOLDER_TABLE);

	    	Log.d("myHome", workspace_dump);
	    	Log.d("myHome", folder_def_dump);
	    	Log.d("myHome", folder_con_dump);

	    	buf.write(workspace_dump);
	    	buf.write(folder_def_dump);
	    	buf.write(folder_con_dump);
	    	
	    	buf.close();
	    	
	    	workspace.close();
	    	folder_def.close();
	    	folder_con.close();
    	} catch(IOException e) {
    		Log.d("myHome", "Cannot write to sdcard");
    	}
    	
    	databaseChanged = false;
    }
    
    private boolean restoreFromSDCard()
    {
    	openDatabase();
    	
    	try
    	{
	    	File sdcard = Environment.getExternalStorageDirectory();
	    	
	    	if(sdcard == null || !sdcard.canRead())
	    		return false;
	    	
	    	File data = new File(sdcard.getAbsolutePath()+"/myHome/last_state.data");
	    	if(!data.exists() || !data.canRead())
	    		return false;
	    	
	    	FileReader fileReader = new FileReader(data);
	    	BufferedReader buf = new BufferedReader(fileReader);

	    	db.execSQL("DELETE FROM " + MyHomeDB.FOLDER_DEFINITION_TABLE + " WHERE 1");
	    	db.execSQL("DELETE FROM " + MyHomeDB.FOLDER_TABLE + " WHERE 1");
	    	db.execSQL("DELETE FROM " + MyHomeDB.WORKSPACE_TABLE + " WHERE 1");
	    	
	    	while(buf.ready())
	    	{
	    		String query = buf.readLine();
	    		
	    		db.execSQL(query);
	    	}
	    	
	    	buf.close();
    	} catch(IOException e) {
    		Log.d("myHome", "Could not restore desktop.");
    	}
    	
    	return true;
    }
    
    private boolean itemExistsInDatabase(ContentValues values)
    {
    	openDatabase();
    	
    	Cursor c = db.query(MyHomeDB.WORKSPACE_TABLE,
    						new String[] {DesktopItem.TYPE, DesktopItem.INTENT},
    						DesktopItem.TYPE+"=? AND "+DesktopItem.INTENT+"=?",
    						new String[] { String.valueOf(values.getAsInteger(DesktopItem.TYPE)),
    								values.getAsString(DesktopItem.INTENT)},
    						null, null, null);
    	
    	if(c.moveToFirst())
    	{
    		c.close();
    		return true;
    	}
    	
    	c.close();
    	return false;
    }
    
    private boolean folderExistsInDatabase(ContentValues values)
    {
    	openDatabase();
    	
    	Cursor c = db.query(MyHomeDB.FOLDER_DEFINITION_TABLE,
    						new String[] {Folder.TITLE},
    						Folder.TITLE+"=?",
    						new String[] {values.getAsString(Folder.TITLE)},
    						null, null, null);
    	
    	if(c.moveToFirst())
    	{
    		c.close();
    		return true;
    	}
    	
    	c.close();
    	return false;
    }
    
    private boolean itemExistsInPlace(ContentValues values)
    {
    	openDatabase();
    	
    	Cursor c = db.query(MyHomeDB.FOLDER_TABLE,
    						new String[] {Folder.TITLE, Folder.INTENT},
    						Folder.TITLE+"=? AND "+Folder.INTENT+"=?",
    						new String[] {values.getAsString(Folder.TITLE),
    									  values.getAsString(Folder.INTENT)},
    						null, null, null);
    	
    	if(c.moveToFirst())
    	{
    		c.close();
    		return true;
    	}
    	
    	c.close();
    	return false;
    }

    public void storeAddItem(DesktopItem item)
    {
    	openDatabase();
    	
    	ContentValues values = item.makeContentValues();
    	
    	if(!itemExistsInDatabase(values))
    	{
    		db.insert(MyHomeDB.WORKSPACE_TABLE, null, values);
    		databaseChanged = true;
    	}
    }

    public void storeUpdateItem(DesktopItem item)
    {
    	openDatabase();
    	
    	Log.d("myHome", "StoreUpdateItem");
    	
		ContentValues values = item.makeContentValues();
		
		if(db.update(MyHomeDB.WORKSPACE_TABLE, values, DesktopItem.TYPE+"=? AND "+DesktopItem.INTENT+"=?",
				new String[] { String.valueOf(values.getAsInteger(DesktopItem.TYPE)),
								values.getAsString(DesktopItem.INTENT)}) > 0)
			databaseChanged = true;
    }
    
    public void storeRemoveItem(DesktopItem item)
    {
    	openDatabase();
    	
    	Log.d("myHome", "StoreRemoveItem");
    	
    	ContentValues values = item.makeContentValues();
    	
		if(db.delete(MyHomeDB.WORKSPACE_TABLE, DesktopItem.TYPE+"=? AND "+DesktopItem.INTENT+"=?",
				new String[] { String.valueOf(values.getAsInteger(DesktopItem.TYPE)),
								values.getAsString(DesktopItem.INTENT)}) > 0)
			databaseChanged = true;
    }

    public boolean storeAddPlace(Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
    	
    	if(!folderExistsInDatabase(values))
    		db.insert(MyHomeDB.FOLDER_DEFINITION_TABLE, null, values);
    	else
    		return false;
    	
    	databaseChanged = true;
    	
    	return true;
    }

    public void storeUpdatePlace(String oldName, Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
		
		if(db.update(MyHomeDB.FOLDER_DEFINITION_TABLE, values, Folder.TITLE+"=?",
				new String[] {oldName}) > 0)
			databaseChanged = true;
    }
    
    public void storeRemovePlace(Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
    	
		if(db.delete(MyHomeDB.FOLDER_DEFINITION_TABLE, Folder.TITLE+"=?",
				new String[] {folder.getTitle()}) > 0 ||
			db.delete(MyHomeDB.FOLDER_TABLE, Folder.TITLE+"=?",
				new String[] {folder.getTitle()}) > 0 ||
			db.delete(MyHomeDB.WORKSPACE_TABLE, DesktopItem.INTENT+"=?",
				new String[] {folder.getTitle()}) > 0)
			databaseChanged = true;
    }
    
    public void storeAddShortcutToPlace(String intent, Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
    	values.put(Folder.INTENT, intent);
    	
    	if(!itemExistsInPlace(values))
    	{
    		db.insert(MyHomeDB.FOLDER_TABLE, null, values);
			databaseChanged = true;
    	}
    }
    
    public void storeRemoveShortcutFromPlace(String intent, Folder folder)
    {
    	openDatabase();
    	
		if(db.delete(MyHomeDB.FOLDER_TABLE, Folder.TITLE+"=? AND "+Folder.INTENT+"=?",
				new String[] {folder.getTitle(), intent}) > 0)
			databaseChanged = true;
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
		
		db.close();
		homeDb.close();
    }
    
    @Override
    public void onSaveInstanceState(Bundle state)
    {
    	super.onSaveInstanceState(state);
    	
    	state.putInt(Config.CURRENT_DESKTOP_NUM_KEY,
    			Config.getInt(Config.CURRENT_DESKTOP_NUM_KEY));
    }
    
    private void processInstanceState(Bundle state) {
    	if(state == null)
    		return;
    	
    	Integer defDesktop = Config.getInt(Config.DEFAULT_DESKTOP_NUM_KEY);
    	
    	int screen = state.getInt(Config.CURRENT_DESKTOP_NUM_KEY, defDesktop);
    	
    	Config.putInt("current_desktop_num", screen);
	}
    
    @Override
    public void onRestoreInstanceState(Bundle state)
    {
    	super.onRestoreInstanceState(state);
    }

	public void startWidgetConfigure(Intent intent) {
		startActivityForResult(intent, myHome.ADD_WIDGET);
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}

	public Screen getScreen() {
		return screen;
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	public void startWallpaperChangerService() {
		startService(new Intent(getApplicationContext(), WallpaperChangerService.class));
	}

	public void stopWallpaperChangerService() {
		stopService(new Intent(getApplicationContext(), WallpaperChangerService.class));
	}

	public void restartWallpaperChangerService() {
		stopWallpaperChangerService();
		startWallpaperChangerService();
	}
}