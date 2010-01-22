package com.irrenhaus.myhome;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.Scroller;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;
import com.irrenhaus.myhome.WidgetCache.WidgetReadyListener;

public class Workspace extends ViewGroup
					   implements ApplicationLoadingListener,
					   			  OnClickListener, OnLongClickListener,
					   			  OnItemClickListener, OnItemLongClickListener{
	private static DesktopView[]	desktopView = null;
	private int						currentDesktop;
	private int						numDesktops;
	
	private myHome					home = null;
	
	private	Folder					openedFolder;
	private boolean					folderOpenedInScreen = false;
	private boolean					firstLayout = false;
	
	private Animation				fadeInAnimation;
	private Animation				fadeOutAnimation;
	private Animation				desktopItemClickAnimation;

	private PlacesView				placesView;
	private AppsView				appsView;
	private TaskManager				taskManager;
	
	private DesktopSwitcher			desktopSwitcher = null;
	private boolean 				desktopSwitcherOpened = false;
	
	private GestureView				gestureView = null;
	private boolean 				gestureViewOpened = false;
	private boolean 				childrenDrawingCacheEnabled = false;
	
	private static final float		scrollDuration = 150;
	
	public Workspace(Context context) {
		super(context);
	}

	public Workspace(Context context, AttributeSet set) {
		super(context, set);
	}
	
	public void setHome(myHome home, PlacesView placesView, AppsView appsView, TaskManager mgr)
	{
		this.home = home;
		
		numDesktops = Config.getInt(Config.NUM_DESKTOPS_KEY);
        
        currentDesktop = Config.getInt(Config.CURRENT_DESKTOP_NUM_KEY);
		
        if(desktopView == null)
        	desktopView = new DesktopView[numDesktops];
        else if(desktopView.length != numDesktops)
        {
        	desktopView = null;
        	desktopView = new DesktopView[numDesktops];
        }
        
        for(int i = 0; i < numDesktops; i++)
        {
        	if(desktopView[i] == null)
        		desktopView[i] = new DesktopView(home);
        	
            desktopView[i].setDragController(myHome.getInstance().getScreen());
            desktopView[i].setDesktopNumber(i);

            desktopView[i].setOnClickListener(this);
            desktopView[i].setOnLongClickListener(this);
            desktopView[i].setDrawingCacheEnabled(false);
            
            addView(desktopView[i]);
        }
        
        firstLayout = true;

        fadeInAnimation = AnimationUtils.loadAnimation(home, R.anim.fadein);
        fadeOutAnimation = AnimationUtils.loadAnimation(home, R.anim.fadeout);
        desktopItemClickAnimation = AnimationUtils.loadAnimation(home, R.anim.desktop_item_click);

        this.placesView = placesView;
        this.appsView = appsView;
        this.taskManager = mgr;

        placesView.init(home.getScreen(), this);
        appsView.init(home.getScreen(), this);
        
        placesView.close();
        appsView.close();
		taskManager.close();
        
		desktopSwitcher = new DesktopSwitcher(home);
		
		gestureView = new GestureView(home);
		
		//setPadding(4, 4, 4, 4);
	}
	
	public void onStop()
	{
		removeAllViews();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		//placesView.setVisibility(View.GONE);
		//appsView.setVisibility(View.GONE);
		
		if(firstLayout)
		{
			scrollTo(getCurrentDesktopNum() * getWidth(), 0);
			firstLayout = false;
		}

        AppsCache.getInstance().addLoadingListener(this);
		
		if(!AppsCache.getInstance().isLoadingDone())
		{
			Toast.makeText(home, R.string.loading_please_wait, Toast.LENGTH_SHORT).show();
			AppsCache.getInstance().start();
		}
		else
			AppsCache.getInstance().sendLoadingDone();
	}
	
	public void startScroll(int sx, int sy, int dx, int dy)
	{
		if(!childrenDrawingCacheEnabled)
			enableDrawingCache();
		
		final Screen screen = myHome.getInstance().getScreen();
		int duration = Math.abs((int)((scrollDuration / (float)getWidth()) * (float)dx));
		Interpolator i = null;
		if(Math.abs(dx) > getWidth() || Math.abs(dy) > getWidth())
			i = new AccelerateDecelerateInterpolator();
		else
			i = new DecelerateInterpolator();
		
		final Scroller scroller = new Scroller(home, i);
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(!scroller.computeScrollOffset())
				{
					home.runOnUiThread(new Runnable() {
						public void run() {
							scrollTo(scroller.getFinalX(), scroller.getFinalY());
							screen.invalidate();
							disableDrawingCache();
						}
					});
					this.cancel();
					timer.cancel();
					timer.purge();
				}
				else
				{
					home.runOnUiThread(new Runnable() {
						public void run() {
							scrollTo(scroller.getCurrX(), scroller.getCurrY());
							screen.invalidate();
						}
					});
				}
			}
		}, 0, 30);
		scroller.startScroll(sx, sy, dx, 0, duration);
	}
	
	public void gotoDesktop(int num)
	{
		if(myHome.getInstance().isDiamondLayout())
		{
			
		}
		else
		{
			if(num < 0 || num >= numDesktops)
			{
				if(!Config.getBoolean(Config.DESKTOP_ROTATION_KEY, true))
				{
					int sx = getScrollX();
					int sy = getScrollY();
					int dx = (currentDesktop * getWidth()) - sx;
					
					startScroll(sx, sy, dx, 0);
					//scrollTo(currentDesktop * getWidth(), 0);
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
			
			int sx = getScrollX();
			int sy = getScrollY();
			int dx = (currentDesktop * getWidth()) - sx;

			startScroll(sx, sy, dx, 0);
			
			Config.putInt(Config.CURRENT_DESKTOP_NUM_KEY, num);
		}
		
		home.getScreen().invalidate();
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
		if(placesView.isOpened())
			closePlacesView(null);
		if(appsView.isOpened())
			closeAppsView(null);
		if(desktopSwitcherOpened)
			closeDesktopSwitcher(null);
		if(gestureViewOpened)
			closeGestureView(null);
		if(taskManager.isOpened())
			closeTaskMgr(null);
	}

	public void closeAllOpenWithoutFolder()
	{
		if(placesView.isOpened() && placesView.isMyPlacesGridOpened())
			closePlacesView(null);
		if(appsView.isOpened())
			closeAppsView(null);
		if(desktopSwitcherOpened)
			closeDesktopSwitcher(null);
		if(gestureViewOpened)
			closeGestureView(null);
		if(taskManager.isOpened())
			closeTaskMgr(null);
	}
	
	public void closeAllOpenFor(Runnable run)
	{
		boolean ran = false;
		if(placesView.isOpened())
		{
			closePlacesView(run);
			ran = true;
		}
		
		if(appsView.isOpened())
		{
			closeAppsView(run);
			ran = true;
		}
		
		if(desktopSwitcherOpened)
		{
			closeDesktopSwitcher(run);
			ran = true;
		}
		
		if(gestureViewOpened)
		{
			closeGestureView(run);
			ran = true;
		}
		
		if(taskManager.isOpened())
		{
			closeTaskMgr(run);
			ran = true;
		}
		
		if(!ran)
			run.run();
	}
	
	public void openDesktopSwitcher()
	{
		closeAllOpen();
		
		RectF size = new RectF();
		getCurrentDesktop().cellToRect(0, 0, 4, 4, size);
		desktopSwitcher.init((int)size.width(), (int)size.height());
	
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
		getCurrentDesktop().addView(desktopSwitcher, -1, lp);
		
		desktopSwitcher.startAnimation(fadeInAnimation);
		desktopSwitcherOpened = true;
		invalidate();
	}
	
	public void closeDesktopSwitcher(final Runnable run)
	{
		desktopSwitcher.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	getCurrentDesktop().removeView(desktopSwitcher);
				desktopSwitcherOpened = false;
				desktopSwitcher.deinit();
				if(run != null)
					run.run();
				invalidate();
			}
		});
    	desktopSwitcher.startAnimation(fadeOutAnimation);
	}
	
	public void openGestureView()
	{
		closeAllOpen();
		
		RectF size = new RectF();
		getCurrentDesktop().cellToRect(0, 0, 4, 4, size);
	
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(0, 0, 4, 4);
		getCurrentDesktop().addView(gestureView, -1, lp);
		
		gestureView.startAnimation(fadeInAnimation);
		gestureViewOpened = true;
		invalidate();
	}
	
	public void closeGestureView(final Runnable run)
	{
		gestureView.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	getCurrentDesktop().removeView(gestureView);
				gestureViewOpened = false;
				if(run != null)
					run.run();
				invalidate();
			}
		});
    	gestureView.startAnimation(fadeOutAnimation);
	}
    
	public void openPlacesView()
	{
		closeAllOpen();
		placesView.showMyPlacesGrid(false);
		placesView.animateOpen();
	}
	
    public boolean isPlacesViewOpened() {
		return placesView.isOpened();
	}

    public void closePlacesView(final Runnable run)
    {
    	placesView.setDoOnAnimationEnd(new Runnable() {
			public void run() {
		    	if(openedFolder != null && !folderOpenedInScreen)
		    		closeFolderWithoutPlacesView();
				if(run != null)
					run.run();
			}
		});
    	placesView.animateClose();
    }
	
	public void closeAppsView(final Runnable run)
    {
    	appsView.setDoOnAnimationEnd(new Runnable() {
			public void run() {
				if(run != null)
					run.run();
			}
		});
    	appsView.animateClose();
    }
	
	public void closeTaskMgr(final Runnable run)
    {
    	taskManager.setDoOnAnimationEnd(new Runnable() {
			public void run() {
				if(run != null)
					run.run();
			}
		});
    	taskManager.animateClose();
    }

	public void openAppsView() {
		closeAllOpenWithoutFolder();
		appsView.animateOpen();
	}

	public void openTaskMgr() {
		closeAllOpenWithoutFolder();
		taskManager.init();
		taskManager.animateOpen();
	}

	public boolean isAppsViewOpened() {
		return appsView.isOpened();
	}

	public boolean isTaskMgrOpened() {
		return taskManager.isOpened();
	}

	public boolean isDesktopSwitcherOpened() {
		return (desktopSwitcherOpened);
	}

	public boolean isGestureViewOpened() {
		return (gestureViewOpened);
	}
    
    public void openFolder(final Folder f)
    {
		f.setNumColumns(4);
		
    	placesView.openFolder(f);
    	if(!isPlacesViewOpened())
    		openPlacesView();
    	
    	openedFolder = f;
		folderOpenedInScreen = false;
		
		openedFolder.setOnItemLongClickListener(this);
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
    
    public void closeFolderWithoutPlacesView()
    {
    	if(openedFolder == null)
    		return; 
    	
    	if(folderOpenedInScreen)
    	{
    		closeFolderAnim();
    		return;
    	}
    	
    	placesView.closeFolder();
    	
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
    	
    	if(placesView.isOpened())
    	{
    		placesView.animateClose();
    	}
    	
    	openedFolder.setOpen(false);
    	
		openedFolder = null;
		
		folderOpenedInScreen = false;
    }
	
	public boolean isAnythingOpen() {
		return (appsView.isOpened() || openedFolder != null || placesView.isOpened() ||
				isDesktopSwitcherOpened() || isGestureViewOpened() || taskManager.isOpened());
	}

	public void cancelAllLongPresses() {
		this.cancelLongPress();
		
		for(int i = 0; i < getChildCount(); i++)
			getChildAt(i).cancelLongPress();
	}

	public void applicationLoaded(final ApplicationInfo info) {
		home.runOnUiThread(new Runnable() {
			public void run() {
				AppsAdapter adapter = (AppsAdapter)appsView.getAppsGrid().getAdapter();
				adapter.add(info);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
		
		if(parent instanceof DragSource)
		{
			if(parent.getParent() instanceof Folder)
			{
				closeAllOpen();
				myHome.getInstance().getScreen().onDragBegin((DragSource)parent.getParent(),
						view, parent.getAdapter().getItem(position));
			}
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
		
		if(isAnythingOpen())
			return;
		
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
		
		if(isAnythingOpen())
			return true;
		
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
						    			Log.d("myHome", "AddDesktopShortcut called");
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
					    			final Point to = new Point(params.cellX, params.cellY);
					    			final Point size = new Point(params.cellHSpan, params.cellVSpan);
					    			
					    			WidgetCache.getInstance().widgetReady(widgetid, new WidgetReadyListener() {
										synchronized public void ready(final int widgetid) {
											home.runOnUiThread(new Runnable() {
												public void run()
												{
									    			d.addAppWidget(false, widgetid, to, size);
												}
											});
										}
									});
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

	public void loadingDone() {
		((AppsAdapter)appsView.getAppsGrid().getAdapter()).reload();
		((MyPlacesAdapter)placesView.getMyPlacesAdapter()).reload();
		home.runOnUiThread(new Runnable() {
			public void run() {
				((AppsAdapter)appsView.getAppsGrid().getAdapter()).notifyDataSetChanged();
				((MyPlacesAdapter)placesView.getMyPlacesAdapter()).notifyDataSetChanged();
			}
		});
		loadWorkspaceDatabase();
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
		return placesView.getMyPlacesGrid();
	}

	public Folder getOpenedFolder() {
		return openedFolder;
	}

	public AppsGrid getAllAppsGrid() {
		return appsView.getAppsGrid();
	}

	public void removeAllDesktopFolders(Folder f) {
		for(int i = 0; i < numDesktops; i++)
		{	
			getDesktop(i).removeDesktopFolder(f);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		setMeasuredDimension(width, height);
		
		for(int i = 0; i < getChildCount(); i++)
		{
			View child = getChildAt(i);
			
			child.measure(widthMeasureSpec, heightMeasureSpec);
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
	
	public void toggleAppsView()
	{
		if(isAppsViewOpened())
			closeAppsView(null);
		else
			openAppsView();
	}

	public void togglePlacesView() {
		if(placesView.isOpened())
		{
			if(placesView.isMyPlacesGridOpened())
				closePlacesView(null);
			else
				placesView.closeFolder();
		}
		else
			openPlacesView();
	}
	
	public void enableDrawingCache()
	{
		for(DesktopView view: desktopView)
		{
			view.setChildrenDrawnWithCacheEnabled(true);
			view.setChildrenDrawingCacheEnabled(true);
		}
		childrenDrawingCacheEnabled = true;
	}
	
	public void disableDrawingCache()
	{
		for(DesktopView view: desktopView)
			view.setChildrenDrawnWithCacheEnabled(false);
		childrenDrawingCacheEnabled = false;
	}
}
