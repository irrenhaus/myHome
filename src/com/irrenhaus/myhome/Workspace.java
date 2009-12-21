package com.irrenhaus.myhome;

import java.net.URISyntaxException;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class Workspace extends LinearLayout
					   implements DragController, ApplicationLoadingListener,
					   			  OnClickListener, OnLongClickListener,
					   			  OnItemClickListener, OnItemLongClickListener{
	public static final int		NUM_DESKTOPS = 1;
	
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

        desktopView.setOnClickListener(this);
        desktopView.setOnLongClickListener(this);
        
        allAppsGrid = new AppsGrid(home);
        allAppsGrid.setDragController(this);
        allAppsGrid.setAdapter(new AppsAdapter(home, null));
        allAppsGrid.setOnItemClickListener(this);
        allAppsGrid.setOnItemLongClickListener(this);
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
		
		((DesktopView)getChildAt(0)).onIncomingDrag(dragView, dragInfo);
		dragSource.onDrag(dragView, dragInfo);
		((DesktopView)getChildAt(0)).onDragMovement(dragView, dragInfo, eventPoint);
		
		view.setVisibility(GONE);
	}

	@Override
	public void onDragEnd() {
		dragInProgress = false;
		
		dragView.setVisibility(VISIBLE);
		
		dragSource.onDropped(dragView, dragInfo);
		
		((DesktopView)getChildAt(0)).onDrop(dragSource, dragView, dragInfo);
	}
	
	@Override
	public void onDragMotionEvent(MotionEvent event)
	{
		onTouchEvent(event);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if(dragInProgress)
			onTouchEvent(event);
			
		return (dragInProgress || super.dispatchTouchEvent(event));
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
					
					((DesktopView)getChildAt(0)).onDragMovement(dragView, dragInfo, eventPoint);
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
    	Bitmap bmp = mgr.getWallpaper();
    	if(bmp != null)
    	{
    		Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
    		Rect dest = new Rect(0, 0, getWidth(), getHeight());
    		canvas.drawBitmap(bmp, src, dest, new Paint());
    	}
    		
    	
    	drawChild(canvas, getChildAt(0), getDrawingTime());
    }

	public boolean isAppsGridOpened() {
		return appsGridOpened;
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
		onDragBegin((DragSource)parent, view, parent.getAdapter().getItem(position));
			
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		home.startActivity(((ApplicationInfo)parent.getAdapter().getItem(position)).intent);
	}

	public void onClick(View v) {
		Object tag = v.getTag();
		
		if((tag instanceof DesktopItem))
		{
			final DesktopItem item = (DesktopItem) tag;
			if(item.getType() == DesktopItem.APPLICATION_SHORTCUT)
			{
				home.startActivity(item.getLaunchIntent());
			}
		}
	}

	public boolean onLongClick(View v) {
		Object tag = v.getTag();
		
		if((tag instanceof DesktopItem))
		{
			final DesktopItem item = (DesktopItem) tag;
			
			if(v.getParent() instanceof DragSource)
			{
				v.cancelLongPress();
				v.clearFocus();
				v.setPressed(false);
				
				this.onDragBegin((DragSource) v.getParent(), v, item);
			}
		}
		else if(v instanceof DesktopView)
		{
			home.startWidgetPicker();
		}
		
		return true;
	}

	public int getDesktopCount() {
		return NUM_DESKTOPS;
	}
	
	public DesktopView getDesktop(int i) {
		if(i >= 0 && i < getChildCount())
			return (DesktopView) getChildAt(i);
		return null;
	}
    
    public void loadWorkspaceDatabase()
    {
    	MyHomeDB homeDb = new MyHomeDB(home);
    	SQLiteDatabase db = homeDb.getReadableDatabase();
    	
    	Cursor data = db.query(MyHomeDB.WORKSPACE_TABLE, new String[] {DesktopView.DESKTOP_NUMBER,
    			DesktopItem.INTENT, DesktopItem.LAYOUT_PARAMS, DesktopItem.TYPE},
    					null, null, null, null, null);
    	
    	while(data.moveToNext())
    	{
    		final CellLayout.LayoutParams params = MyHomeDB.string2LayoutParams(
    											data.getString(data.getColumnIndex(
    													DesktopItem.LAYOUT_PARAMS)));
    		
    		String intentUri = data.getString(data.getColumnIndex(DesktopItem.INTENT));
    		Intent intent = null;
    		
    		int type = data.getInt(data.getColumnIndex(DesktopItem.TYPE));
    		
    		final int desktop = data.getInt(data.getColumnIndex(DesktopView.DESKTOP_NUMBER));
    		
    		if(type == DesktopItem.APPLICATION_SHORTCUT)
    		{

    			try {
    				intent = Intent.parseUri(intentUri, 0);
    				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    			} catch (URISyntaxException e) {
    				e.printStackTrace();
    				continue;
    			}
    			
    			final ApplicationInfo info = AppsCache.getInstance().searchByIntent(intent);
    			
    			if(info != null)
    			{
    				if(desktop >= 0 && desktop < getChildCount())
    				{
						final DesktopView d = (DesktopView) getChildAt(desktop);
    					home.runOnUiThread(new Runnable() {
							public void run() {
		    					Point to = new Point(params.cellX, params.cellY);
		    					
		    					d.addDesktopShortcut(true, to, null, info);
							}
    					});
    				}
    			}
    		}
    		else if(type == DesktopItem.APP_WIDGET)
    		{
    			final int widgetid = Integer.parseInt(intentUri);
    			
    			final AppWidgetProviderInfo info = AppWidgetManager.getInstance(home).getAppWidgetInfo(widgetid);
    			final AppWidgetHostView view = myHome.getAppWidgetHost().createView(home, widgetid, info);
    			
    			if(desktop >= 0 && desktop < getChildCount())
				{
					final DesktopView d = (DesktopView) getChildAt(desktop);
					home.runOnUiThread(new Runnable() {
						public void run() {
	    					Point to = new Point(params.cellX, params.cellY);
	    					Point size = new Point(params.cellHSpan, params.cellVSpan);
	    					
	    					d.addAppWidget(view, info, widgetid, to, size);
						}
					});
				}
    		}
    	}
    	
    	data.close();
    	db.close();
    	
    	home.runOnUiThread(new Runnable() {
			public void run() {
		    	invalidate();
			}
		});
    }

	@Override
	public void loadingDone() {
		loadWorkspaceDatabase();
	}
}
