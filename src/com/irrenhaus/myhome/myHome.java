package com.irrenhaus.myhome;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class myHome extends Activity {
	private Workspace				workspace = null;
	
	private static AppWidgetHost	appWidgetHost = null;
	private static final int		appWidgetHostID = 1337;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
    }
	
	@Override
	protected void onResume()
	{
		super.onResume();
        
        AppsCache.getInstance().start();
	}
	
    public void startWidgetPicker()
    {
    	int widgetid = myHome.getAppWidgetHost().allocateAppWidgetId();
		
		Intent picker = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		picker.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetid);
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, new ArrayList());
		picker.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, new ArrayList());
		
		Log.d("AppWidgetID", ""+widgetid);
		
		startActivityForResult(picker, 2);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		{
			int widgetid = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			
			if(resultCode != Activity.RESULT_OK)
				myHome.getAppWidgetHost().deleteAppWidgetId(widgetid);
			else
			{
				AppWidgetProviderInfo appWidget = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetid);
				
				AppWidgetHostView view = myHome.getAppWidgetHost().createView(this, widgetid,
											appWidget);
				
				((DesktopView)workspace.getChildAt(0)).addAppWidget(view, appWidget, widgetid);
				
				getAppWidgetHost().startListening();
			}
		}
	}
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	
    	MyHomeDB homeDb = new MyHomeDB(this);
    	SQLiteDatabase db = homeDb.getWritableDatabase();
		
    	//TODO: Dirty workaround
		db.delete(MyHomeDB.WORKSPACE_TABLE, "1=1", null);
    	
    	for(int i = 0; i < workspace.getDesktopCount(); i++)
    	{
    		DesktopView desktop = workspace.getDesktop(i);
    		
    		for(int d = 0; d < desktop.getChildCount(); d++)
    		{
    			View v = desktop.getChildAt(d);
    			
    			if(v.getTag() instanceof DesktopItem)
    			{
    				DesktopItem item = (DesktopItem) v.getTag();

    				ContentValues values = new ContentValues();
    				
    				if(item.getType() == DesktopItem.APPLICATION_SHORTCUT)
    				{
	    				String params = MyHomeDB.layoutParams2String(item.getLayoutParams());
	    				int type = item.getType();
	    				int desktopnum = i;
	    				String intent = item.getLaunchIntent().toURI();
	    				
	    				values.put(DesktopItem.INTENT, intent);
	    				values.put(DesktopItem.LAYOUT_PARAMS, params);
	    				values.put(DesktopItem.TYPE, type);
	    				values.put(DesktopView.DESKTOP_NUMBER, desktopnum);
    				}
    				else if(item.getType() == DesktopItem.APP_WIDGET)
    				{
    					String params = MyHomeDB.layoutParams2String(item.getLayoutParams());
	    				int type = item.getType();
	    				int desktopnum = i;
	    				String intent = String.valueOf(item.getAppWidgetId());
	    				
	    				values.put(DesktopItem.INTENT, intent);
	    				values.put(DesktopItem.LAYOUT_PARAMS, params);
	    				values.put(DesktopItem.TYPE, type);
	    				values.put(DesktopView.DESKTOP_NUMBER, desktopnum);
    				}
	    			
    				db.insert(MyHomeDB.WORKSPACE_TABLE, null, values);
    			}
    		}
    	}
		
		db.close();
    }

	public static AppWidgetHost getAppWidgetHost() {
		return appWidgetHost;
	}

	public static void setAppWidgetHost(AppWidgetHost appWidgetHost) {
		myHome.appWidgetHost = appWidgetHost;
	}
}