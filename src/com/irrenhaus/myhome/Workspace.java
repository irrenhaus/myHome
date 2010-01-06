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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class Workspace extends ViewGroup
					   implements ApplicationLoadingListener,
					   			  OnClickListener, OnLongClickListener,
					   			  OnItemClickListener, OnItemLongClickListener{
	private DesktopView[] 		desktopView = null;
	private int					currentDesktop;
	private int					numDesktops;
	
	private AppsGrid			allAppsGrid = null;
	private MyPlacesGrid		myPlacesGrid = null;
	
	private MyPlacesAdapter		myPlacesAdapter = null;
	
	private boolean				appsGridOpened = false;
	
	private myHome				home = null;
	
	private	Folder				openedFolder;
	private boolean				folderOpenedInScreen = false;
	private boolean				myPlacesOpened = false;
	private boolean				firstLayout = false;
	private	boolean				trayViewOpened = false;
	
	private Animation			fadeInAnimation;
	private Animation			fadeOutAnimation;
	private Animation			desktopItemClickAnimation;
	private TrayView			trayView;
	
	public Workspace(Context context) {
		super(context);
	}

	public Workspace(Context context, AttributeSet set) {
		super(context, set);
	}
	
	public void setHome(myHome home)
	{
		this.home = home;
		
		numDesktops = Config.getInt(Config.NUM_DESKTOPS_KEY);
		
        desktopView = new DesktopView[numDesktops];
        
        currentDesktop = Config.getInt(Config.CURRENT_DESKTOP_NUM_KEY);

        Log.d("myHome", "numDesktops: "+numDesktops);
        Log.d("myHome", "defDesktop: "+currentDesktop);
        
        for(int i = 0; i < numDesktops; i++)
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
        
        trayView = new TrayView(home, allAppsGrid, myPlacesGrid);
	}
	
	public void gotoDesktop(int num)
	{
		if(num < 0 || num >= numDesktops)
		{
			if(!Config.getBoolean(Config.DESKTOP_ROTATION_KEY, true))
			{
				scrollTo(currentDesktop * getWidth(), 0);
				return;
			}
			else
			{
				if(num < 0)
					num = numDesktops-1;
				else
					num = 0;
			}
		}
		
		currentDesktop = num;
		
		scrollTo(currentDesktop * getWidth(), 0);
		
		Config.putInt(Config.CURRENT_DESKTOP_NUM_KEY, num);
	}
	
	public void setDesktop(int num)
	{
		if(num < 0 || num >= numDesktops)
			return;
		
		currentDesktop = num;
		
		Config.putInt(Config.CURRENT_DESKTOP_NUM_KEY, num);
	}

	public void closeAllOpen()
	{
		if(trayViewOpened)
			closeTrayView(null);
	}
	
	public void closeAllOpenFor(Runnable run)
	{
		boolean ran = false;
		if(trayViewOpened)
		{
			closeTrayView(run);
			ran = true;
		}
		
		if(!ran)
			run.run();
	}
	
	public void openAllAppsGrid()
    {
		if(!trayViewOpened)
		{
			trayView.setGrid(TrayView.ALL_APPS_GRID);
			openTrayView();
		}
		
		trayView.gotoGrid(TrayView.ALL_APPS_GRID);
    }
    
	public void openTrayView()
	{
		if(!trayViewOpened)
		{
			CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
	    	((DesktopView)getCurrentDesktop()).addView(trayView, -1, lp);
	    	trayViewOpened = true;
	    	trayView.startAnimation(fadeInAnimation);
		}
	}
	
    public boolean isTrayViewOpened() {
		return trayViewOpened;
	}

	public void closeTrayView(final Runnable run)
    {
    	trayView.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	((DesktopView)getCurrentDesktop()).removeView(trayView);
		    	trayViewOpened = false;
		    	if(openedFolder != null && !folderOpenedInScreen)
		    		closeFolderWithoutTrayView();
				if(run != null)
					run.run();
			}
		});
    	trayView.startAnimation(fadeOutAnimation);
    }

	public void openMyPlaces() {
		if(!trayViewOpened)
		{
			trayView.setGrid(TrayView.MY_PLACES_GRID);
			openTrayView();
		}
		
		trayView.gotoGrid(TrayView.MY_PLACES_GRID);
	}

	public boolean isMyPlacesOpened() {
		return (trayViewOpened && trayView.isMyPlacesGridOpened());
	}
    
    public void openFolder(final Folder f)
    {
		f.setNumColumns(4);
		
		openTrayView();
    	trayView.openFolder(f);
    	
    	openedFolder = f;
		folderOpenedInScreen = false;
    }
    
    public void openFolderInScreen(final Folder f, DesktopItem caller)
    {
		//closeAllOpen();
		
		if(openedFolder != null)
		{
			if(openedFolder == f)
			{
				closeFolderAnim();
				return;
			}
			else
				closeFolder();
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
		
		folderOpenedInScreen = true;
    }
    
    public void closeFolderAnim()
    {
    	if(openedFolder == null)
    		return; 
    	
    	openedFolder.setDoOnAnimationEnd(new Runnable() {
			public void run() {
				getCurrentDesktop().removeView(openedFolder);
				
				openedFolder = null;
				
				folderOpenedInScreen = false;
			}
    	});
    	openedFolder.startAnimation(fadeOutAnimation);
    }
    
    public void closeFolderWithoutTrayView()
    {
    	if(openedFolder == null)
    		return; 
    	
    	if(folderOpenedInScreen)
    	{
    		closeFolderAnim();
    		return;
    	}
    	
    	trayView.closeFolder();
    	
    	openedFolder.setOpen(false);
    	
		openedFolder = null;
		
		folderOpenedInScreen = false;
    }
    
    public void closeFolder()
    {
    	if(openedFolder == null)
    		return; 
    	
    	if(folderOpenedInScreen)
    	{
    		closeFolderAnim();
    		return;
    	}
    	
    	if(trayViewOpened)
    	{
    		closeTrayView(null);
    	}
    	
    	openedFolder.setOpen(false);
    	
		openedFolder = null;
		
		folderOpenedInScreen = false;
    }
	
	public boolean isAnythingOpen() {
		return (appsGridOpened || openedFolder != null || myPlacesOpened || trayViewOpened);
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
		return (trayViewOpened && trayView.isAllAppsGridOpened());
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
			Folder f = ((MyPlacesAdapter)parent.getAdapter()).getFolder(position);
			openFolder(f);
		}
		else if(parent instanceof AppsGrid)
		{
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
						closeFolder();
					}
					else
						closeAllOpenFor(run);
				}
			});
			view.startAnimation(desktopItemClickAnimation);
		}
	}

	public void onClick(View v) {
		if(openedFolder != null)
		{
			if(folderOpenedInScreen)
				closeFolderAnim();
			return;
		}
		
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
		if(openedFolder != null)
		{
			if(folderOpenedInScreen)
				openedFolder.close();
			return true;
		}
		
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
			home.startDesktopActionPicker();
		}
		
		return true;
	}

	public int getDesktopCount() {
		return numDesktops;
	}
    
    public void loadWorkspaceDatabase()
    {
    	Runnable run = new Runnable() {
			public void run() {
				closeAllOpen();
				
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
		    				if(desktop >= 0 && desktop < numDesktops)
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
		    				if(desktop >= 0 && desktop < numDesktops)
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
				    			
				    			if(desktop >= 0 && desktop < numDesktops)
								{
									final DesktopView d = getDesktop(desktop);
					    			Point to = new Point(params.cellX, params.cellY);
					    			Point size = new Point(params.cellHSpan, params.cellVSpan);
					    			
					    			WidgetCache.getInstance().widgetReady(widgetid);
					    			d.addAppWidget(false, widgetid, to, size);
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
			}
		});
		loadWorkspaceDatabase();
		/*home.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(home, R.string.loading_done, Toast.LENGTH_SHORT).show();
			}
		});*/
	}

	public DesktopView getCurrentDesktop() {
		return (DesktopView)desktopView[currentDesktop];
	}

	public DesktopView getDesktop(int pos) {
		if(pos < 0 || pos >= numDesktops)
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
		for(int i = 0; i < numDesktops; i++)
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

	public boolean isFolderOpenedInScreen() {
		return folderOpenedInScreen;
	}
}
