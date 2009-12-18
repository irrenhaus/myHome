package com.irrenhaus.myhome;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class myHome extends Activity {
	private Workspace			workspace = null;
	
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
    				
    				String params = MyHomeDB.layoutParams2String(item.getLayoutParams());
    				int type = item.getType();
    				int desktopnum = i;
    				String intent = item.getLaunchIntent().toURI();
    				
    				ContentValues values = new ContentValues();
    				values.put(DesktopItem.INTENT, intent);
    				values.put(DesktopItem.LAYOUT_PARAMS, params);
    				values.put(DesktopItem.TYPE, type);
    				values.put(DesktopView.DESKTOP_NUMBER, desktopnum);
    				
    				db.insert(MyHomeDB.WORKSPACE_TABLE, null, values);
    			}
    		}
    	}
		
		db.close();
    }
}