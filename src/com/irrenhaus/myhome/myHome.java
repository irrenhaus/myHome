package com.irrenhaus.myhome;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.irrenhaus.myhome.CellLayout.LayoutParams;

public class myHome extends Activity {
	private Workspace				workspace = null;
	private Screen					screen = null;

	private static AppWidgetHost	appWidgetHost = null;
	private static final int		appWidgetHostID = 1337;

	private static final int		PICK_WIDGET = 10;

	public  static final int		ADD_WIDGET = 20;
	
	private	AppWidgetHostView		hostViewTmp;
	private AppWidgetProviderInfo	providerInfoTmp;
	private CellLayout.LayoutParams paramsTmp;

	private MyHomeDB 				homeDb;

	private SQLiteDatabase 			db;
	
	private static myHome			instance;

	private static final int		MENU_ENTRY_SETTINGS = R.string.menu_entry_settings;
	private static final int		MENU_ENTRY_ADD_PLACE = R.string.menu_entry_add_place;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        instance = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        
        AppsCache.getInstance().setContext(getApplicationContext());
        
        screen = (Screen)findViewById(R.id.screen);
        
        workspace = (Workspace)findViewById(R.id.workspace);
        workspace.setHome(this);
        
        Button appsGridButton = (Button)findViewById(R.id.openAllAppsGridButton);
        appsGridButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(workspace.isAppsGridOpened())
					workspace.closeAllAppsGrid();
				else
					workspace.openAllAppsGrid();
			}
        });
        
        Button myPlacesButton = (Button)findViewById(R.id.openMyPlacesButton);
        myPlacesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(workspace.isMyPlacesOpened())
					workspace.closeMyPlaces();
				else
					workspace.openMyPlaces();
			}
        });

        AppsCache.getInstance().addLoadingListener(workspace);
        
        WallpaperManager mgr = WallpaperManager.getInstance();
        
        mgr.setActivity(this);
        mgr.get();
        
        myHome.setAppWidgetHost(new AppWidgetHost(this, myHome.appWidgetHostID));
        
		if(!AppsCache.getInstance().isLoadingDone())
			AppsCache.getInstance().start();
		else
			AppsCache.getInstance().sendLoadingDone();
    }
    
    public static myHome getInstance()
    {
    	return instance;
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	Resources res = getResources();
    	
    	Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
    			Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    	
    	menu.add(1, MENU_ENTRY_SETTINGS,
    			ContextMenu.NONE, res.getString(MENU_ENTRY_SETTINGS)).setIntent(intent);
    	
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
    	Log.d("myHome", "onResume. DB opened: "+db.isOpen());
	}
	
    public void startWidgetPicker()
    {
    	int widgetid = myHome.getAppWidgetHost().allocateAppWidgetId();
		
		Intent picker = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		picker.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetid);
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, new ArrayList());
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, new ArrayList());
		
		startActivityForResult(picker, PICK_WIDGET);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
    	if(requestCode == PICK_WIDGET)
		{
			int widgetid = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			
			if(resultCode != Activity.RESULT_OK)
				myHome.getAppWidgetHost().deleteAppWidgetId(widgetid);
			else
			{
				AppWidgetProviderInfo appWidget = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetid);
				
				AppWidgetHostView view = myHome.getAppWidgetHost().createView(this, widgetid,
											appWidget);
				view.setAppWidget(widgetid, appWidget);
				
				((DesktopView)workspace.getChildAt(0)).addAppWidget(view, appWidget, widgetid);
				
				getAppWidgetHost().startListening();
			}
		}
    	else if(requestCode == ADD_WIDGET)
    	{
    		int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
    		if(resultCode == Activity.RESULT_OK && id != -1)
    		{
    			workspace.getCurrentDesktop().completeAddWidget(hostViewTmp, providerInfoTmp,
    						id, paramsTmp);
    		}
    		else
    		{
    			getAppWidgetHost().deleteAppWidgetId(id);
    		}
    	}
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
    	
    	Log.d("myHome", "StoreAddItem");
    	
    	ContentValues values = item.makeContentValues();
    	
    	if(!itemExistsInDatabase(values))
    		db.insert(MyHomeDB.WORKSPACE_TABLE, null, values);
    }

    public void storeUpdateItem(DesktopItem item)
    {
    	openDatabase();
    	
    	Log.d("myHome", "StoreUpdateItem");
    	
		ContentValues values = item.makeContentValues();
		
		db.update(MyHomeDB.WORKSPACE_TABLE, values, DesktopItem.TYPE+"=? AND "+DesktopItem.INTENT+"=?",
				new String[] { String.valueOf(values.getAsInteger(DesktopItem.TYPE)),
								values.getAsString(DesktopItem.INTENT)});
    }
    
    public void storeRemoveItem(DesktopItem item)
    {
    	openDatabase();
    	
    	Log.d("myHome", "StoreRemoveItem");
    	
    	ContentValues values = item.makeContentValues();
    	
		db.delete(MyHomeDB.WORKSPACE_TABLE, DesktopItem.TYPE+"=? AND "+DesktopItem.INTENT+"=?",
				new String[] { String.valueOf(values.getAsInteger(DesktopItem.TYPE)),
								values.getAsString(DesktopItem.INTENT)});
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
    	
    	return true;
    }

    public void storeUpdatePlace(String oldName, Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
		
		db.update(MyHomeDB.FOLDER_DEFINITION_TABLE, values, Folder.TITLE+"=?",
				new String[] {oldName});
    }
    
    public void storeRemovePlace(Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
    	
		db.delete(MyHomeDB.FOLDER_DEFINITION_TABLE, Folder.TITLE+"=?",
				new String[] {folder.getTitle()});
    }
    
    public void storeAddShortcutToPlace(String intent, Folder folder)
    {
    	openDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(Folder.TITLE, folder.getTitle());
    	values.put(Folder.INTENT, intent);
    	
    	if(!itemExistsInPlace(values))
    		db.insert(MyHomeDB.FOLDER_TABLE, null, values);
    }
    
    public void storeRemoveShortcutFromPlace(String intent, Folder folder)
    {
    	openDatabase();
    	
		db.delete(MyHomeDB.FOLDER_TABLE, Folder.TITLE+"=? AND "+Folder.INTENT+"=?",
				new String[] {folder.getTitle(), intent});
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
		
		db.close();
		homeDb.close();
    }

	public static AppWidgetHost getAppWidgetHost() {
		return appWidgetHost;
	}

	public static void setAppWidgetHost(AppWidgetHost appWidgetHost) {
		myHome.appWidgetHost = appWidgetHost;
	}

	public void startWidgetConfigure(Intent intent, AppWidgetHostView view,
			AppWidgetProviderInfo info, LayoutParams params) {
		hostViewTmp = view;
		providerInfoTmp = info;
		paramsTmp = params;
		
		startActivityForResult(intent, myHome.ADD_WIDGET);
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void desktopChanged(boolean diamond, int num)
	{
		screen.desktopChanged(diamond, num);
	}
}