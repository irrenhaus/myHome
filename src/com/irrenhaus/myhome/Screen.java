package com.irrenhaus.myhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.Debug;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class Screen extends LinearLayout implements DragController {
	//private ImageView desktopOverview = null;
	private Context				context;
	
	private boolean				dragInProgress = false;
	private View				dragView = null;
	private Object				dragInfo = null;
	private DragSource			dragSource = null;
	private Bitmap          	dragViewBitmap;
	private Paint 				dragViewAlphaPaint;
	private Paint 				dragViewDeletePaint;

	private boolean				desktopChangeInProgress;

	private int					clickX;
	private int					clickY;
	private int					startScrollX;
	private int					startScrollY;
	private int					desktopToSet = 0;
	
	private Workspace			workspace;

	private int					dragModX;

	private int					dragModY;

	private int					lastMovementEventX;

	private int					lastMovementEventY;

	private Paint				defaultPaint = new Paint();

	private Paint				whitePaint;

	private Paint				blackPaint;

	private WallpaperManager 	wallpaperMgr;

	private Bitmap 				wallpaperBmp;
	
	public Screen(Context context) {
		super(context);
		
		this.context = context;
	}
	
	public Screen(Context context, AttributeSet set) {
		super(context, set);
	
		this.context = context;
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		init();
	}
	
	public void init()
	{
		workspace = myHome.getInstance().getWorkspace();
		
		dragViewAlphaPaint = new Paint();
		dragViewAlphaPaint.setARGB(128, 66, 66, 66);
		
		dragViewDeletePaint = new Paint();
		dragViewDeletePaint.setARGB(96, 255, 0, 0);

		blackPaint = new Paint();
		blackPaint.setARGB(192, 0, 0, 0);
		blackPaint.setStyle(Style.FILL);
		
		whitePaint = new Paint();
		whitePaint.setARGB(192, 255, 255, 255);
		whitePaint.setStyle(Style.FILL);
		
    	wallpaperMgr = WallpaperManager.getInstance();
    	wallpaperChanged();
	}
	
	public void wallpaperChanged()
	{
		wallpaperBmp = wallpaperMgr.getWallpaper(getWidth(), getHeight());
		
		invalidate();
	}
	
	@Override
    public void dispatchDraw(Canvas canvas)
    {
		boolean opened = workspace.isAnythingOpen();
		
		if(wallpaperMgr.wallpaperChanged() || wallpaperBmp == null || wallpaperBmp.isRecycled())
		{
			wallpaperBmp = wallpaperMgr.getWallpaper(getWidth(), getHeight());
		}
			
    	if(wallpaperBmp != null && !wallpaperBmp.isRecycled())
    	{
    		int count = Config.getInt(Config.NUM_DESKTOPS_KEY) + 1;
    		
    		int width = getWidth();
    		int wallpaperWidth = wallpaperBmp.getWidth();
    		int scrollX = myHome.getInstance().getWorkspace().getScrollX();
    		
    		float offset = wallpaperWidth > width ? (count * width - wallpaperWidth) /
    				(count * (float) width) : 1.0f;
    		
    		float x = scrollX * offset * -1;

    		if (x + wallpaperWidth < getRight() - getLeft()) {
    			x = getRight() - getLeft() - wallpaperWidth;
    		}
    		
    		if(scrollX < 0)
    			x = 0;

    		canvas.drawBitmap(wallpaperBmp, x, (getBottom() - getTop() - wallpaperBmp.getHeight()) / 2, defaultPaint);
    	}
    	
    	super.dispatchDraw(canvas);
    	
    	if((dragInProgress && dragViewBitmap == null && dragView != null) ||
    		(dragInProgress && dragView != null && dragViewBitmap.isRecycled()))
		{
    		if(dragViewBitmap != null && dragViewBitmap.isRecycled())
    			dragView.invalidate();
    		
			dragViewBitmap = dragView.getDrawingCache();
		}
		
		int toX = lastMovementEventX - dragModX;
		int toY = lastMovementEventY - dragModY;
		
		if(dragInProgress && dragViewBitmap != null)
		{
			Paint toUse = defaultPaint;
			
			Point estDropPosition = workspace.getCurrentDesktop().getEstDropPosition();
			if(estDropPosition != null && !inDeletePosition(lastMovementEventX,
											lastMovementEventY) && 
				myHome.getInstance().getWorkspace().getOpenedFolder() == null)
				canvas.drawBitmap(dragViewBitmap, estDropPosition.x,
						estDropPosition.y, dragViewAlphaPaint);
			else
				toUse = dragViewDeletePaint;
			
			canvas.drawBitmap(dragViewBitmap, toX, toY, toUse);
		}
    	
    	int curDesktop = workspace.getCurrentDesktopNum();
    	int numDesktops = workspace.getDesktopCount();
    	int desktopWidth = (getRight() - getLeft());
    	
    	int desktopDisplayWidth = (getRight() - getLeft()) / numDesktops;
    	int posY = getHeight() - 8;
    	
    	boolean land = getWidth() > getHeight();
    	
    	if(land)
    	{
    		posY = getRight() - 8;
    		desktopDisplayWidth = (getBottom() - getTop()) / numDesktops;
    	}
    	
    	for(int i = 0; i < numDesktops; i++)
    	{
    		int posX = desktopDisplayWidth * i;
    		RectF rect = null;
    		
    		if(land)
    			rect = new RectF(posY, posX, posY + 8, posX + desktopDisplayWidth);
    		else
    			rect = new RectF(posX, posY, posX + desktopDisplayWidth, posY + 8);
    		
    		canvas.drawRoundRect(rect, 4, 4, blackPaint);
    	}
    	RectF rect = null;
    	float num = numDesktops - 1;
    	int posX = (int)(((num * desktopDisplayWidth) / (num * desktopWidth)) * workspace.getScrollX());
    	
		if(land)
			rect = new RectF(posY, posX, posY + 8, posX + desktopDisplayWidth);
		else
			rect = new RectF(posX, posY, posX + desktopDisplayWidth, posY + 8);
		canvas.drawRoundRect(rect, 4, 4, whitePaint);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
    	{
    		workspace.closeAllOpen();
    		if(workspace.getOpenedFolder() != null)
    			workspace.closeFolder();
    		
    		return true;
    	}
    	
    	return super.dispatchKeyEvent(event);
    }
	
	@Override
	public void onDragMotionEvent(MotionEvent event)
	{
		onTouchEvent(event);
	}
	
	private int abs(int num)
	{
		if(num < 0)
			return num * -1;
		
		return num;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			clickX = (int)event.getX();
			clickY = (int)event.getY();

			lastMovementEventX = clickX;
			lastMovementEventY = clickY;
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			if(desktopChangeInProgress)
			{
				workspace.gotoDesktop(workspace.getCurrentDesktopNum() + desktopToSet);
				
				desktopChangeInProgress = false;
				
				return true;
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_MOVE && !dragInProgress &&
				!workspace.isAnythingOpen())
		{
			if((abs(clickX - (int)event.getX()) > 10 ||
					abs(clickY - (int)event.getY()) > 10) &&
					!desktopChangeInProgress)
			{
				workspace.cancelAllLongPresses();
				desktopChangeInProgress = true;
				startScrollX = workspace.getScrollX();
				startScrollY = workspace.getScrollY();
			}
			
			if(desktopChangeInProgress)
			{
				performDesktopChange(clickX, clickY,
									(int)event.getX(), (int)event.getY(),
									startScrollX, startScrollY);
				return true;
			}
		}
		
		if(dragInProgress)
		{
			invalidate();
			onTouchEvent(event);
		}
		
		//if(gestureDetector.onTouchEvent(event))
		//	return true;
			
		return (dragInProgress || super.dispatchTouchEvent(event));
	}

	private void performDesktopChange(int startX, int startY, int x, int y, int scrollX, int scrollY)
	{
		if(myHome.getInstance().isDiamondLayout())
		{
			
		}
		else
		{
			int difference = startX - x;
				
			workspace.scrollTo(scrollX+difference, 0);
			
			if(Math.abs(difference) > getWidth()*0.6)
			{
				if(difference < 0)
					desktopToSet = -1;
				else
					desktopToSet = 1;
			}
			else
				desktopToSet = 0;
		}
		
		invalidate();
	}
    
    @Override
	public void onDragBegin(DragSource src, View view, Object info) {
		if(!(src instanceof MyPlacesGrid) && src instanceof AppsGrid)
			workspace.closeAppsView(null);
			
    	myHome.getInstance().getToolbar().showTrayIcon();
		
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(50);
		
		dragInProgress = true;
		
		dragSource = src;
		
		dragView = view;
		dragInfo = info;
		
		Point eventPoint = new Point();
		
		int[] loc = new int[2];
		dragView.getLocationInWindow(loc);
		
		dragModX = clickX - loc[0];
		dragModY = clickY - loc[1];
		
		eventPoint.x = (int) clickX - (int)dragModX;
		eventPoint.y = (int) clickY - (int)dragModY;
		
		workspace.getCurrentDesktop().onIncomingDrag(dragView, dragInfo);
		dragSource.onDrag(dragView, dragInfo);
		workspace.getCurrentDesktop().setInitialDragPosition(eventPoint);
		
		view.setVisibility(GONE);
	}

	@Override
	public void onDragEnd() {
		dragInProgress = false;
		
		dragView.setVisibility(VISIBLE);
		
		dragViewBitmap = null;
		
		myHome.getInstance().getToolbar().showButtons();
		
		if(inDeletePosition(lastMovementEventX, lastMovementEventY))
		{
			Log.d("DeletePosition", "true");
			if(dragView.getTag() instanceof DesktopItem)
				deleteIfItem(dragSource, dragView.getTag());
			else
				deleteIfItem(dragSource, dragInfo);
			invalidate();
			return;
		}
		
		dragSource.onDropped(dragView, dragInfo);
		
		workspace.getCurrentDesktop().onDrop(dragSource, dragView, dragInfo);
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		switch(event.getAction())
		{
			case MotionEvent.ACTION_MOVE:
				if(dragInProgress) {
					Point eventPoint = new Point();
					eventPoint.x = (int) event.getX() - (int)dragModX;
					eventPoint.y = (int) event.getY() - (int)dragModY;
					
					workspace.getCurrentDesktop().onDragMovement(dragView, dragInfo, eventPoint);
				}

				lastMovementEventX = (int)event.getX();
				lastMovementEventY = (int)event.getY();
				break;
			
			case MotionEvent.ACTION_UP:
				if(dragInProgress) {
					onDragEnd();
				}
				break;
		}
		
		return true;
	}
	
	private void deleteIfItem(DragSource src, Object info)
	{
		if(src instanceof AppsGrid)
		{
			AppsGrid grid = (AppsGrid)src;

			final ApplicationInfo appInfo = (ApplicationInfo)info;
			
			if(grid instanceof MyPlacesGrid)
			{
				String title = context.getResources().getString(R.string.dialog_title_rm_place);
				String msg = context.getResources().getString(R.string.dialog_message_rm_place);

				String ok = context.getResources().getString(R.string.dialog_button_ok);
				String cancel = context.getResources().getString(R.string.dialog_button_cancel);
				
				title += " '"+appInfo.name+"'";
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
				builder.setTitle(title);
				builder.setMessage(msg);
				
				builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
						Folder f = ((MyPlacesAdapter)myHome.getInstance().getWorkspace().
								getMyPlacesGrid().getAdapter()).getFolder(appInfo.name);
				
						myHome.getInstance().storeRemovePlace(f);
						
						MyPlacesAdapter a = (MyPlacesAdapter) myHome.getInstance().getWorkspace().
												getMyPlacesGrid().getAdapter();
						a.reload();
						a.notifyDataSetChanged();
						
						myHome.getInstance().getWorkspace().removeAllDesktopFolders(f);
					}
				});
				
				builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				builder.create().show();
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);

				String title = context.getResources().getString(R.string.dialog_title_uninstall);
				String msg = context.getResources().getString(R.string.dialog_message_uninstall);

				String ok = context.getResources().getString(R.string.dialog_button_ok);
				String cancel = context.getResources().getString(R.string.dialog_button_cancel);
				
				title += " '"+appInfo.name+"'";
				
				builder.setTitle(title);
				builder.setMessage(msg);
				
				builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
						String pkg = null;
						
						PackageManager mgr = context.getPackageManager();
						ResolveInfo res = mgr.resolveActivity(appInfo.intent, 0);
						pkg = res.activityInfo.packageName;
						
						Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+pkg));
						context.startActivity(uninstallIntent);
					}
				});
				
				builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				
				builder.create().show();
			}
		}
		else if(info instanceof DesktopItem)
		{
			DesktopItem item = (DesktopItem)info;
			
			if(item.getType() == DesktopItem.APP_WIDGET)
			{
				int id = item.getAppWidgetId();
				item.setAppWidgetView(null);
				WidgetCache.getInstance().deleteAppWidgetId(id);
			}
			
			myHome.getInstance().storeRemoveItem(item);
		} else if(src instanceof Folder)
		{
			Folder folder = (Folder) src;
			myHome.getInstance().storeRemoveShortcutFromPlace(
									((ApplicationInfo)info).intentUri,
									folder);
			folder.getAdapter().reload();
			folder.getAdapter().notifyDataSetChanged();
			folder.contentChanged();
		}
	}
	
	private boolean inDeletePosition(int x, int y)
	{
		boolean land = getWidth() > getHeight();
		
		if(land)
		{
			if(x > (getWidth()-54))
				return true;
		}
		else
		{
			if(y > (getHeight()-54))
				return true;
		}
		
		return false;
	}

	public int getClickX() {
		return clickX;
	}

	public int getClickY() {
		return clickY;
	}
}
