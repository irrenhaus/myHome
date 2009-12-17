package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class Workspace extends LinearLayout
					   implements DragController, ApplicationLoadingListener {
	private DesktopView 		desktopView = null;
	private AppsGrid			allAppsGrid = null;
	
	private boolean				appsGridOpened = false;
	
	private ListView 			myPlacesList = null;
	private MyPlacesAdapter		myPlacesAdapter = null;
	
	private boolean				dragInProgress = false;
	private View				dragView = null;
	private Object				dragInfo = null;
	private DragSource			dragSource = null;
	
	private myHome				home = null;

	public Workspace(Context context) {
		super(context);
	}

	public Workspace(Context context, AttributeSet set) {
		super(context, set);
	}
	
	public void setHome(myHome home)
	{
		this.home = home;
		
		//myPlacesList = (ListView)findViewById(R.id.myPlacesList);
        
        myPlacesAdapter = new MyPlacesAdapter(home);
        //myPlacesList.setAdapter(myPlacesAdapter);
        
        desktopView = new DesktopView(home);
        desktopView.setDragController(this);
        addView(desktopView);
        
        allAppsGrid = new AppsGrid(home);
        allAppsGrid.setDragController(this);
        allAppsGrid.setAdapter(new AppsAdapter(home, null));
        
        if(!AppsCache.getInstance().isLoadingDone())
        {
        	AppsCache.getInstance().start();
        }
        else
        {
        	((AppsAdapter)allAppsGrid.getAdapter()).reload();
        	((AppsAdapter)allAppsGrid.getAdapter()).notifyDataSetChanged();
        }
	}

	public void openAllAppsGrid()
    {
    	CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
    	((DesktopView)getChildAt(0)).addView(allAppsGrid, -1, lp);
    	appsGridOpened = true;
    }
    
    public void closeAllAppsGrid()
    {
    	((DesktopView)getChildAt(0)).removeView(allAppsGrid);
    	appsGridOpened = false;
    }
    
    @Override
	public void onDragBegin(DragSource src, View view, Object info) {
		
		closeAllAppsGrid();
		
		dragInProgress = true;
		
		dragSource = src;
		
		dragView = view;
		dragInfo = info;
		
		Point eventPoint = new Point();
		int[] loc = new int[2];
		dragView.getLocationInWindow(loc);
		
		eventPoint.x = loc[0];
		eventPoint.y = loc[1];
		
		desktopView.onIncomingDrag(dragView, dragInfo);
		dragSource.onDrag(dragView, dragInfo);
		desktopView.onDragMovement(dragView, dragInfo, eventPoint);
	}

	@Override
	public void onDragEnd() {
		dragInProgress = false;
		
		desktopView.onDrop(dragView, dragInfo);
		
		dragSource.onDrop(dragView, dragInfo);
	}
	
	@Override
	public void onDragMotionEvent(MotionEvent event)
	{
		onTouchEvent(event);
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		switch(event.getAction())
		{
			case MotionEvent.ACTION_MOVE:
				if(dragInProgress) {
					Point eventPoint = new Point();
					eventPoint.x = (int) event.getX();
					eventPoint.y = (int) event.getY();
					
					desktopView.onDragMovement(dragView, dragInfo, eventPoint);
				}
				break;
			
			case MotionEvent.ACTION_UP:
				if(dragInProgress) {
					onDragEnd();
				}
				break;
		}
		
		return true;
	}

	@Override
	public void applicationLoaded(final ApplicationInfo info) {
		home.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AppsAdapter adapter = (AppsAdapter)allAppsGrid.getAdapter();
				adapter.add(info);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
    @Override
    public void dispatchDraw(Canvas canvas)
    {
    	WallpaperManager mgr = WallpaperManager.getInstance();
    	if(mgr.getWallpaper() != null)
    		canvas.drawBitmap(mgr.getWallpaper(), 0, 0, new Paint());
    	
    	drawChild(canvas, getChildAt(0), getDrawingTime());
    }

	public boolean isAppsGridOpened() {
		return appsGridOpened;
	}
}
