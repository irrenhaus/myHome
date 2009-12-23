package com.irrenhaus.myhome;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Screen extends LinearLayout implements DragController {
	//private ImageView desktopOverview = null;
	private Context				context;
	
	private boolean				dragInProgress = false;
	private View				dragView = null;
	private Object				dragInfo = null;
	private DragSource			dragSource = null;
	private Bitmap          	dragViewBitmap;
	private Paint 				dragViewAlphaPaint;

	private boolean				desktopChangeInProgress;

	private float				clickX;
	private float				clickY;
	private int					desktopToSet = 0;
	
	private Workspace			workspace;

	private MotionEvent 		lastMovementEvent;

	private float dragModX;

	private float dragModY;

	private float lastMovementEventX;

	private float lastMovementEventY;
	
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
	}
	
	@Override
    public void dispatchDraw(Canvas canvas)
    {
    	WallpaperManager mgr = WallpaperManager.getInstance();
    	Bitmap bmp = Utilities.centerToFit(mgr.getWallpaper(), getWidth(), getHeight(), context);
    	
    	if(bmp != null)
    	{
    		canvas.drawBitmap(bmp, 0, 0, new Paint());
    	}
    	
    	super.dispatchDraw(canvas);
    	
    	if(dragInProgress && dragViewBitmap == null && dragView != null)
		{
			dragViewBitmap = dragView.getDrawingCache();
			dragViewAlphaPaint = new Paint();
			dragViewAlphaPaint.setARGB(128, 66, 66, 66);
		}
		
		float toX = lastMovementEventX - dragModX;
		float toY = lastMovementEventY - dragModY;
		
		if(dragInProgress && dragViewBitmap != null)
		{
			Point estDropPosition = workspace.getCurrentDesktop().getEstDropPosition();
			if(estDropPosition != null && !inDeletePosition(lastMovementEventX,
											lastMovementEventY) && 
				myHome.getInstance().getWorkspace().getOpenedFolder() == null)
				canvas.drawBitmap(dragViewBitmap, estDropPosition.x,
						estDropPosition.y, dragViewAlphaPaint);
			
			canvas.drawBitmap(dragViewBitmap, toX, toY, null);
		}
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
    	{
    		workspace.closeAllOpen();
    		if(workspace.getOpenedFolder() != null)
    			workspace.closeFolder(workspace.getOpenedFolder());
    		
    		return true;
    	}
    	
    	return super.dispatchKeyEvent(event);
    }
	
	@Override
	public void onDragMotionEvent(MotionEvent event)
	{
		onTouchEvent(event);
	}
	
	private float distance(float f, float g, float h, float i)
	{
		return Math.abs((float)
				Math.sqrt(
						((f - g) * (f - g)) +
						((h - i) * (h - i))
						)
					);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			clickX = event.getX();
			clickY = event.getY();

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
			if(distance(clickX, event.getX(),
						clickY, event.getY()) > 10.0f && !desktopChangeInProgress)
			{
				workspace.cancelAllLongPresses();
				desktopChangeInProgress = true;
			}
			
			if(desktopChangeInProgress)
			{
				performDesktopChange(clickX, clickY,
									event.getX(), event.getY());
				return true;
			}
		}
		
		if(dragInProgress)
			onTouchEvent(event);
		
		//if(gestureDetector.onTouchEvent(event))
		//	return true;
			
		return (dragInProgress || super.dispatchTouchEvent(event));
	}

	private void performDesktopChange(float startX, float startY, float x, float y)
	{
		if(myHome.getInstance().isDiamondLayout())
		{
			
		}
		else
		{
			float difference = startX - x;
			
			if(difference < 0)
				workspace.scrollTo((int) ((workspace.getCurrentDesktopNum() * getWidth())+difference), 0);
			else
				workspace.scrollTo((int) ((workspace.getCurrentDesktopNum() * getWidth())-(difference*-1)), 0);
			
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
	}
    
    @Override
	public void onDragBegin(DragSource src, View view, Object info) {
		workspace.closeAllAppsGrid();
		workspace.closeMyPlaces();
		
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
		workspace.getCurrentDesktop().onDragMovement(dragView, dragInfo, eventPoint);
		invalidate();
		
		view.setVisibility(GONE);
	}

	@Override
	public void onDragEnd() {
		dragInProgress = false;
		
		dragView.setVisibility(VISIBLE);
		
		dragViewBitmap = null;
		
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
					
					invalidate();
				}
				
				lastMovementEvent = event;

				lastMovementEventX = event.getX();
				lastMovementEventY = event.getY();
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
			else if(grid.getParent() instanceof Folder)
			{
				myHome.getInstance().storeRemoveShortcutFromPlace(
										((ApplicationInfo)info).intent.toURI(),
										((Folder)grid.getParent()));
				((Folder)grid.getParent()).getAdapter().reload();
				((Folder)grid.getParent()).getAdapter().notifyDataSetChanged();
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
				myHome.getAppWidgetHost().deleteAppWidgetId(id);
			}
			
			myHome.getInstance().storeRemoveItem(item);
		}
	}
	
	private boolean inDeletePosition(float x, float y)
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

	public float getClickX() {
		return clickX;
	}

	public float getClickY() {
		return clickY;
	}
}
