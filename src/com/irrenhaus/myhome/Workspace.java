package com.irrenhaus.myhome;

import java.net.URISyntaxException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class Workspace extends ViewGroup
					   implements ApplicationLoadingListener,
					   			  OnClickListener, OnLongClickListener,
					   			  OnItemClickListener, OnItemLongClickListener{
	public static final int		NUM_DESKTOPS = 3;
	public static final int		DEFAULT_DESKTOP = 1;
	
	private DesktopView[] 		desktopView = null;
	private int					currentDesktop = DEFAULT_DESKTOP;
	
	private AppsGrid			allAppsGrid = null;
	private MyPlacesGrid		myPlacesGrid = null;
	
	private MyPlacesAdapter		myPlacesAdapter = null;
	
	private boolean				appsGridOpened = false;
	
	private myHome				home = null;
	
	private	Folder				openedFolder;
	private boolean				myPlacesOpened = false;
	private boolean				firstLayout = false;
	
	private Animation			fadeInAnimation;
	private Animation			fadeOutAnimation;
	private Animation			desktopItemClickAnimation;
	
	public Workspace(Context context) {
		super(context);
	}

	public Workspace(Context context, AttributeSet set) {
		super(context, set);
	}
	
	public void setHome(myHome home)
	{
		this.home = home;
		
        desktopView = new DesktopView[NUM_DESKTOPS];
        
        for(int i = 0; i < NUM_DESKTOPS; i++)
        {
        	desktopView[i] = new DesktopView(home);
            desktopView[i].setDragController(myHome.getInstance().getScreen());
            desktopView[i].setDesktopNumber(i);

            desktopView[i].setOnClickListener(this);
            desktopView[i].setOnLongClickListener(this);
            
            addView(desktopView[i]);
        }
        
        firstLayout = true;
        
        allAppsGrid = new AppsGrid(home);
        allAppsGrid.setDragController(myHome.getInstance().getScreen());
        allAppsGrid.setAdapter(new AppsAdapter(home, null));
        allAppsGrid.setOnItemClickListener(this);
        allAppsGrid.setOnItemLongClickListener(this);

        myPlacesGrid = new MyPlacesGrid(home);
        myPlacesGrid.setDragController(myHome.getInstance().getScreen());
        myPlacesGrid.setOnItemClickListener(this);
        myPlacesGrid.setOnItemLongClickListener(this);

        myPlacesAdapter = new MyPlacesAdapter(home);
        myPlacesGrid.setAdapter(myPlacesAdapter);

        fadeInAnimation = AnimationUtils.loadAnimation(home, R.anim.fadein);
        fadeOutAnimation = AnimationUtils.loadAnimation(home, R.anim.fadeout);
        desktopItemClickAnimation = AnimationUtils.loadAnimation(home, R.anim.desktop_item_click);
	}
	
	public void gotoDesktop(int num)
	{
		if(num < 0 || num >= NUM_DESKTOPS)
		{
			scrollTo(currentDesktop * getWidth(), 0);
			return;
		}
		
		currentDesktop = num;
		
		scrollTo(currentDesktop * getWidth(), 0);
	}
	
	public void setDesktop(int num)
	{
		if(num < 0 || num >= NUM_DESKTOPS)
			return;
		
		currentDesktop = num;
	}

	public void closeAllOpen()
	{
		if(myPlacesOpened)
			closeMyPlaces(null);
		if(isAppsGridOpened())
			closeAllAppsGrid(null);
	}
	
	public void closeAllOpenFor(Runnable run)
	{
		if(myPlacesOpened)
			closeMyPlaces(run);
		if(isAppsGridOpened())
			closeAllAppsGrid(run);
	}
	
	public void openAllAppsGrid()
    {
		closeAllOpen();
    	CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
    	((DesktopView)getCurrentDesktop()).addView(allAppsGrid, -1, lp);
    	appsGridOpened = true;
    	allAppsGrid.startAnimation(fadeInAnimation);
    }
    
    public void closeAllAppsGrid(final Runnable run)
    {
    	allAppsGrid.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	((DesktopView)getCurrentDesktop()).removeView(allAppsGrid);
		    	appsGridOpened = false;
				if(run != null)
					run.run();
			}
		});
    	allAppsGrid.startAnimation(fadeOutAnimation);
    }

	public void openMyPlaces() {
		closeAllOpen();
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
    	((DesktopView)getCurrentDesktop()).addView(myPlacesGrid, -1, lp);
		myPlacesOpened = true;
		myPlacesGrid.startAnimation(fadeInAnimation);
	}

	public void closeMyPlaces(final Runnable run) {
		myPlacesGrid.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	((DesktopView)getCurrentDesktop()).removeView(myPlacesGrid);
				myPlacesOpened = false;
				if(run != null)
					run.run();
			}
		});
		myPlacesGrid.startAnimation(fadeOutAnimation);
	}

	public boolean isMyPlacesOpened() {
		return myPlacesOpened;
	}
    
    public void openFolder(final Folder f)
    {
		closeAllOpen();
		
		if(openedFolder != null)
			closeFolder(openedFolder);
		
		f.setNumColumns(AppsGrid.NUM_COLUMNS_APPSVIEW);
    	
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
		((DesktopView)getCurrentDesktop()).addView(f, -1, lp);
		openedFolder = f;
		
		f.startAnimation(fadeInAnimation);

		openedFolder.setOnItemClickListener(this);
		openedFolder.setOnItemLongClickListener(this);
		openedFolder.setDragController(myHome.getInstance().getScreen());
    }
    
    public void openFolderInScreen(final Folder f, DesktopItem caller)
    {
		closeAllOpen();
		
		if(openedFolder != null)
		{
			if(openedFolder == f)
			{
				closeFolderAnim(openedFolder);
				return;
			}
			else
				closeFolder(openedFolder);
		}
    	
		int posX = 0;
		int posY = 0;

		int callerX = caller.getLayoutParams().cellX;
		int callerY = caller.getLayoutParams().cellY;
		
		if(callerX < 2) //left position
			posX = callerX+1;
		if(callerX > 2) //right position
			posX = 0 + Math.abs(2 - callerX);
		
		if(callerY == 3)
			posY = 1;
		
		f.setNumColumns(2);
		
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(posX, posY, 2, 3);
		((DesktopView)getCurrentDesktop()).addView(f, -1, lp);
		openedFolder = f;
		
		f.startAnimation(fadeInAnimation);

		openedFolder.setOnItemClickListener(this);
		openedFolder.setOnItemLongClickListener(this);
		openedFolder.setDragController(myHome.getInstance().getScreen());
    }
    
    public void closeFolderAnim(final Folder f)
    {
		f.startAnimation(fadeOutAnimation);
    }
    
    public void closeFolder(final Folder f)
    {
    	((DesktopView)getCurrentDesktop()).removeView(f);
		openedFolder = null;
    }
	
	public boolean isAnythingOpen() {
		return (appsGridOpened || openedFolder != null || myPlacesOpened);
	}

	public void cancelAllLongPresses() {
		this.cancelLongPress();
		
		for(int i = 0; i < getChildCount(); i++)
			getChildAt(i).cancelLongPress();
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

	public boolean isAppsGridOpened() {
		return appsGridOpened;
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
		
		if(parent instanceof DragSource)
		{
			if(parent.getParent() instanceof Folder)
				myHome.getInstance().getScreen().onDragBegin((DragSource)parent.getParent(),
						view, parent.getAdapter().getItem(position));
			else
				myHome.getInstance().getScreen().onDragBegin((DragSource)parent, view,
						parent.getAdapter().getItem(position));
		}
			
		return true;
	}
	
	public void onItemClick(final AdapterView<?> parent, View view, final int position,
				long id) {
		
		if(parent instanceof MyPlacesGrid)
		{
			closeAllOpenFor(new Runnable() {
				public void run() {
					Folder f = ((MyPlacesAdapter)parent.getAdapter()).getFolder(position);
					openFolder(f);
				}
			});
		}
		else if(parent instanceof AppsGrid)
		{
			Log.d("myHome", "AppsGrid");
			((ShortcutTextView)view).setDoOnAnimationEnd(new Runnable() {
				public void run() {
					Runnable run = new Runnable() {
						public void run() {
							home.startActivity(((ApplicationInfo)parent.getAdapter().getItem(position)).intent);
						}
					};
					if(parent.getParent() instanceof Folder)
					{
						((Folder)parent.getParent()).setDoOnAnimationEnd(run);
						closeFolder(((Folder)parent.getParent()));
					}
					else
						closeAllOpenFor(run);
				}
			});
			view.startAnimation(desktopItemClickAnimation);
		}
	}

	public void onClick(View v) {
		Object tag = v.getTag();
		
		if((tag instanceof DesktopItem))
		{
			final DesktopItem item = (DesktopItem) tag;
			if(item.getType() == DesktopItem.APPLICATION_SHORTCUT)
			{
				((BubbleTextView)v).setDoOnAnimationEnd(new Runnable() {
					public void run() {
						home.startActivity(item.getLaunchIntent());
					}
				});
				v.startAnimation(desktopItemClickAnimation);
			}
			else if(item.getType() == DesktopItem.USER_FOLDER)
			{
				Animation anim = AnimationUtils.loadAnimation(home, R.anim.desktop_item_click);
				v.startAnimation(anim);
				openFolderInScreen(item.getFolder(), item);
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
				
				myHome.getInstance().getScreen().onDragBegin((DragSource) getCurrentDesktop(),
															v, item);
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
		    		
		    		final String intentUri = data.getString(data.getColumnIndex(DesktopItem.INTENT));
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
		    		else if(type == DesktopItem.USER_FOLDER)
		    		{
		    			final ApplicationInfo info = AppsCache.getInstance().new ApplicationInfo();
		    			info.filtered = true;
		    			info.isFolder = true;
		    			info.icon = Folder.getIcon(home);
		    			info.name = intentUri;
		    			
		    			if(info != null)
		    			{
		    				if(desktop >= 0 && desktop < NUM_DESKTOPS)
		    				{
								final DesktopView d = getDesktop(desktop);
		    					home.runOnUiThread(new Runnable() {
									public void run() {
				    					Point to = new Point(params.cellX, params.cellY);
				    					
				    					d.addDesktopFolder(true, to, null, info);
									}
		    					});
		    				}
		    			}
		    		}
		    		else if(type == DesktopItem.APP_WIDGET)
		    		{
						home.runOnUiThread(new Runnable() {
							public void run() {
				    			final int widgetid = Integer.parseInt(intentUri);
				    			
				    			final AppWidgetProviderInfo info = AppWidgetManager.
				    								getInstance(home).getAppWidgetInfo(widgetid);
				    			final MyHomeAppWidgetHostView view = (MyHomeAppWidgetHostView) myHome.getAppWidgetHost().
				    									createView(home, widgetid, info);
				    			view.setAppWidget(widgetid, info);
				    			info.configure = null;
				    			
				    			if(desktop >= 0 && desktop < NUM_DESKTOPS)
								{
									final DesktopView d = getDesktop(desktop);
					    			Point to = new Point(params.cellX, params.cellY);
					    			Point size = new Point(params.cellHSpan, params.cellVSpan);
					    					
					    			d.addAppWidget(view, info, widgetid, to, size);
								}
							}
						});
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
		((MyPlacesAdapter)myPlacesGrid.getAdapter()).reload();
		home.runOnUiThread(new Runnable() {
			public void run() {
				((AppsAdapter)allAppsGrid.getAdapter()).notifyDataSetChanged();
				((MyPlacesAdapter)myPlacesGrid.getAdapter()).notifyDataSetChanged();
				closeAllOpen();
				if(openedFolder != null)
					closeFolderAnim(openedFolder);
			}
		});
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

	public MyPlacesGrid getMyPlacesGrid() {
		return myPlacesGrid;
	}

	public Folder getOpenedFolder() {
		return openedFolder;
	}

	public AppsGrid getAllAppsGrid() {
		return allAppsGrid;
	}

	public void removeAllDesktopFolders(Folder f) {
		for(int i = 0; i < NUM_DESKTOPS; i++)
		{	
			getDesktop(i).removeDesktopFolder(f);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		if(firstLayout)
		{
			scrollTo(getCurrentDesktopNum() * getWidth(), 0);
			firstLayout = false;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = MeasureSpec.getSize(widthMeasureSpec);
		
		for(int i = 0; i < getChildCount(); i++)
		{
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		int pos = 0;
		
		for(int i = 0; i < getChildCount(); i++)
		{
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			
			if(child.getVisibility() != View.GONE)
			{
				child.layout(pos, 0, pos + width, height);
				pos += width;
			}
		}
	}
}
