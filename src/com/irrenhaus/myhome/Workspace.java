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
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
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
	public static final int		NUM_DESKTOPS = 3;
	public static final int		DEFAULT_DESKTOP = 1;
	
	private DesktopView[] 		desktopView = null;
	private int					currentDesktop = DEFAULT_DESKTOP;
	private boolean				diamondLayout = false;
	
	private AppsGrid			allAppsGrid = null;
	
	private boolean				appsGridOpened = false;
	
	private ListView 			myPlacesList = null;
	private MyPlacesAdapter		myPlacesAdapter = null;
	
	private boolean				dragInProgress = false;
	private View				dragView = null;
	private Object				dragInfo = null;
	private DragSource			dragSource = null;
	
	private myHome				home = null;
	
	private GestureDetector		gestureDetector = null;
	private GestureListener		gestureListener = null;
	
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
        
        desktopView = new DesktopView[NUM_DESKTOPS];
        
        for(int i = 0; i < NUM_DESKTOPS; i++)
        {
        	desktopView[i] = new DesktopView(home);
            desktopView[i].setDragController(this);
            desktopView[i].setDesktopNumber(i);

            desktopView[i].setOnClickListener(this);
            desktopView[i].setOnLongClickListener(this);
        }
        
        addView(getCurrentDesktop());
        
        allAppsGrid = new AppsGrid(home);
        allAppsGrid.setDragController(this);
        allAppsGrid.setAdapter(new AppsAdapter(home, null));
        allAppsGrid.setOnItemClickListener(this);
        allAppsGrid.setOnItemLongClickListener(this);
        
        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(home, gestureListener);
	}
	
	public void gotoDesktop(int num)
	{
		if(num < 0 || num >= NUM_DESKTOPS)
			return;
		
		removeAllViews();
		
		currentDesktop = num;
		
		addView(getCurrentDesktop());
	}
	
	public void openAllAppsGrid()
    {
    	CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
    	((DesktopView)getCurrentDesktop()).addView(allAppsGrid, -1, lp);
    	appsGridOpened = true;
    }
    
    public void closeAllAppsGrid()
    {
    	((DesktopView)getCurrentDesktop()).removeView(allAppsGrid);
    	appsGridOpened = false;
    }
    
    @Override
	public void onDragBegin(DragSource src, View view, Object info) {
		closeAllAppsGrid();
		
		Vibrator vibrator = (Vibrator)home.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		
		dragInProgress = true;
		
		dragSource = src;
		
		dragView = view;
		dragInfo = info;
		
		Point eventPoint = new Point();
		int[] loc = new int[2];
		dragView.getLocationInWindow(loc);
		
		eventPoint.x = loc[0];
		eventPoint.y = loc[1];
		
		((DesktopView)getCurrentDesktop()).onIncomingDrag(dragView, dragInfo);
		dragSource.onDrag(dragView, dragInfo);
		((DesktopView)getCurrentDesktop()).onDragMovement(dragView, dragInfo, eventPoint);
		
		view.setVisibility(GONE);
	}

	@Override
	public void onDragEnd() {
		dragInProgress = false;
		
		dragView.setVisibility(VISIBLE);
		
		dragSource.onDropped(dragView, dragInfo);
		
		((DesktopView)getCurrentDesktop()).onDrop(dragSource, dragView, dragInfo);
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
		
		if(gestureDetector.onTouchEvent(event))
			return true;
			
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
					
					((DesktopView)getCurrentDesktop()).onDragMovement(dragView, dragInfo, eventPoint);
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
    		
    	
    	drawChild(canvas, getCurrentDesktop(), getDrawingTime());
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
				closeAllAppsGrid();
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
				
				onDragBegin((DragSource) getCurrentDesktop(), v, item);
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
    
    public void loadWorkspaceDatabase()
    {
    	Runnable run = new Runnable() {
			public void run() {
				//Dirty hack for timing
				try {
					Thread.sleep(150);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				MyHomeDB homeDb = new MyHomeDB(home);
		    	SQLiteDatabase db = homeDb.getReadableDatabase();
		    	
		    	Cursor data = db.query(MyHomeDB.WORKSPACE_TABLE, new String[] {DesktopView.DESKTOP_NUMBER,
		    			DesktopItem.INTENT, DesktopItem.LAYOUT_PARAMS, DesktopItem.TYPE},
		    					null, null, null, null, DesktopView.DESKTOP_NUMBER+" asc");
		    	
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
		    				if(desktop >= 0 && desktop < NUM_DESKTOPS)
		    				{
								final DesktopView d = getDesktop(desktop);
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
		    			view.setAppWidget(widgetid, info);
		    			info.configure = null;
		    			
		    			if(desktop >= 0 && desktop < NUM_DESKTOPS)
						{
							final DesktopView d = getDesktop(desktop);
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
			}
    	};
    	
    	Thread t = new Thread(run);
    	t.start();
    }

	@Override
	public void loadingDone() {
		((AppsAdapter)allAppsGrid.getAdapter()).reload();
		((AppsAdapter)allAppsGrid.getAdapter()).notifyDataSetChanged();
		loadWorkspaceDatabase();
	}

	public DesktopView getCurrentDesktop() {
		return (DesktopView)desktopView[currentDesktop];
	}

	public DesktopView getDesktop(int pos) {
		if(pos < 0 || pos >= NUM_DESKTOPS)
			return null;
		
		return (DesktopView)desktopView[pos];
	}

	public int getCurrentDesktopNum() {
		return currentDesktop;
	}
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			Log.d("myHome", "onFling: "+velocityX+", "+velocityY);
			
			if(Math.abs(velocityX) > Math.abs(velocityY))
			{
				if(velocityX < 0)
				{
					if(diamondLayout)
					{
						
					}
					else
						gotoDesktop(getCurrentDesktopNum()+1);
				}
				else
				{
					if(diamondLayout)
					{
						
					}
					else
						gotoDesktop(getCurrentDesktopNum()-1);
				}
			}
			else if(diamondLayout)
			{
				Log.d("myHome", "onFling: Vertical");
				
				if(velocityY < 0)
				{
					Log.d("myHome", "onFling: Bottom screen");
				}
				else
				{
					Log.d("myHome", "onFling: Top screen");
				}
			}
			
			return false;
		}
	}
}
