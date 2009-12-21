package com.irrenhaus.myhome;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.irrenhaus.myhome.CellLayout.LayoutParams;

public class myHome extends Activity {
	private Workspace				workspace = null;
	
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        instance = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        AppsCache.getInstance().setContext(getApplicationContext());
        
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

        AppsCache.getInstance().addLoadingListener(workspace);
        
        WallpaperManager mgr = WallpaperManager.getInstance();
        
        mgr.setActivity(this);
        mgr.get();
        
        if(mgr.getWallpaper() == null)
        {
	        mgr.selectWallpaperResource(R.drawable.wallpaper);
	        mgr.set();
        }
        
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
    
    private boolean existsInDatabase(ContentValues values)
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

    public void storeAddItem(DesktopItem item)
    {
    	openDatabase();
    	
    	Log.d("myHome", "StoreAddItem");
    	
    	ContentValues values = item.makeContentValues();
    	
    	if(!existsInDatabase(values))
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
}