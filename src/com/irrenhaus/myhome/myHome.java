package com.irrenhaus.myhome;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class myHome extends Activity {
	private Workspace			workspace = null;
	
	private boolean				initialized = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(initialized)
        {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
            
            setContentView(workspace);
        	
        	return;
        }
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        AppsCache.getInstance().setContext(this);
        
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
        
        initialized = true;
    }
}