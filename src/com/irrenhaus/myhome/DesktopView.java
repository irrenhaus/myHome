package com.irrenhaus.myhome;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class DesktopView extends CellLayout implements DragTarget, DragSource {
	public final static int NUM_COLUMNS_DESKTOPVIEW = 4;
	public final static int HOME_SCREEN = 0;
	public final static String DESKTOP_NUMBER = "desktop_number";
	
	private DragController 	dragCtrl = null;
	private boolean			dropInProgress = false;
	private boolean			dragInProgress = false;
	private Point			dragPosition = null;
	private View			dropView = null;
	private Object			dragInfo = null;
	
	private Context 		context = null;
	
	private CellInfo		vacantCells = null;

	private Point           estDropPosition;

	private OnClickListener onClickListener;
	private OnLongClickListener onLongClickListener;
	
	private	int				desktopNumber = -1;
	private boolean 		messageShown;
	private boolean 		vacantCellsUpdated;
	private WidgetCache 	widgetCache = WidgetCache.getInstance();
	
	private Paint			defaultPaint = new Paint();
	private boolean 		drawingCacheEnabled = false;
	private Bitmap drawingCache;
	
	public DesktopView(Context context) {
		super(context);
		
		this.context = context;
		
		this.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
											  LayoutParams.FILL_PARENT));
		
		this.setFocusableInTouchMode(true);
		
		//Needed because of drawing the views for drag & drop
		this.setBackgroundColor(Color.argb(0, 128, 128, 128));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

        final float scale = getContext().getResources().getDisplayMetrics().density;

		float cw = 80 * scale;
		float ch = 100 * scale;
		
		if(w > h) //land
		{
			cw = 106 * scale;
			ch = 73 * scale;
		}
		
		setCellWidth((int) cw);
		setCellHeight((int) ch);
		
		setLongAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		setShortAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		
		forceLayout();
	}
	
	public void setOnClickListener(OnClickListener l)
	{
		super.setOnClickListener(l);
		onClickListener = l;
		
		for(int i = 0; i < getChildCount(); i++)
			getChildAt(i).setOnClickListener(l);
	}
	
	public void setOnLongClickListener(OnLongClickListener l)
	{
		super.setOnLongClickListener(l);
		
		onLongClickListener = l;
		
		for(int i = 0; i < getChildCount(); i++)
			getChildAt(i).setOnLongClickListener(l);
	}
	
	public void addAppWidget(boolean configure, int id)
	{
		int pos = widgetCache.getWidgetPosForId(id);
		AppWidgetProviderInfo info = widgetCache.getAppWidgetInfo(pos);
		MyHomeAppWidgetHostView view = widgetCache.getAppWidgetView(pos);
		
		int span[] = rectToCell(info.minWidth, info.minHeight);
		
		vacantCells = findAllVacantCells(null, null);
		
		final Point p = new Point((int)myHome.getInstance().getScreen().getClickX(),
								(int)myHome.getInstance().getScreen().getClickY());
		
		int cell[] = findNearestVacantArea(p.x, p.y, span[0], span[1], vacantCells, null);
		
		if(cell == null)
		{
			Toast.makeText(context, context.getResources().getString(R.string.no_vacant_cells), Toast.LENGTH_SHORT);
			return;
		}
		
		Point to = new Point(cell[0], cell[1]);
		Point size = new Point(span[0], span[1]);
		
		addAppWidget(configure, id, to, size);
	}
	
	public void addAppWidget(boolean configure, int id, Point to, Point size) {
		CellLayout.LayoutParams params = new CellLayout.LayoutParams(to.x, to.y, size.x, size.y);
		
		int pos = widgetCache.getWidgetPosForId(id);
		widgetCache.setLayoutParams(pos, params);
		
		if((configure && !widgetCache.startWidgetConfigure(pos)) || !configure)
		{
			completeAddWidget(id);
		}
	}
	
	public void completeAddWidget(int id)
	{
		int pos = widgetCache.getWidgetPosForId(id);
		AppWidgetProviderInfo info = widgetCache.getAppWidgetInfo(pos);
		MyHomeAppWidgetHostView view = widgetCache.getAppWidgetView(pos);
		
		DesktopItem item = new DesktopItem(context, DesktopItem.APP_WIDGET,
				(CellLayout.LayoutParams) view.getLayoutParams(), desktopNumber);
		item.setAppWidget(info, view, id);
		
		item.getView().setOnClickListener(onClickListener);
		item.getView().setOnLongClickListener(onLongClickListener);
		
		if(item.getView().getParent() != null)
			((DesktopView)item.getView().getParent()).removeView(item.getView());
		
		addView(item.getView());
		
		myHome.getInstance().storeAddItem(item);
	}
	
	public void moveDesktopItem(DesktopItem item, Point dest)
	{
		CellLayout.LayoutParams params = item.getLayoutParams();
		CellLayout.LayoutParams np = new CellLayout.LayoutParams(dest.x, dest.y,
				params.cellHSpan, params.cellVSpan);
		item.setLayoutParams(np);
		item.getView().setTag(item);

		item.getView().setOnClickListener(onClickListener);
		item.getView().setOnLongClickListener(onLongClickListener);
		params = ((DesktopItem)item.getView().getTag()).getLayoutParams();
		
		this.addView(item.getView());
		
		myHome.getInstance().storeUpdateItem(item);
	}
	
	public void addDesktopShortcut(boolean create, Point dest, View view, ApplicationInfo info)
	{
		if(create)
		{
			for(int i = 0; i < getChildCount(); i++)
			{
				if(getChildAt(i).getTag() instanceof DesktopItem)
				{
					DesktopItem item = (DesktopItem) getChildAt(i).getTag();
					
					if(item.getType() == DesktopItem.APPLICATION_SHORTCUT &&
						item.getApplicationInfo().equals(info))
						return;
				}
			}
			
			CellLayout.LayoutParams params = new CellLayout.LayoutParams(dest.x, dest.y, 1, 1);
			DesktopItem item = new DesktopItem(context,
											   DesktopItem.APPLICATION_SHORTCUT,
											   params, desktopNumber);
			item.setApplicationInfo((ApplicationInfo)info);
			item.setContext(context);
			item.setLaunchIntent(((ApplicationInfo)info).intent);
			item.setIcon(((ApplicationInfo)info).icon);
			item.setTitle(((ApplicationInfo)info).name);

			item.getView().setOnClickListener(onClickListener);
			item.getView().setOnLongClickListener(onLongClickListener);
			item.getView().setLayoutParams(params);
			
			this.addView(item.getView());
			
			myHome.getInstance().storeAddItem(item);
		}
		else
		{
			DesktopItem item = (DesktopItem) view.getTag();
			moveDesktopItem(item, dest);
		}
	}
	
	public void addDesktopFolder(boolean create, Point dest, View view, ApplicationInfo info)
	{
		if(create)
		{
			for(int i = 0; i < getChildCount(); i++)
			{
				if(getChildAt(i).getTag() instanceof DesktopItem)
				{
					DesktopItem item = (DesktopItem) getChildAt(i).getTag();
					
					if(item.getType() == DesktopItem.USER_FOLDER &&
						item.getApplicationInfo().equals(info))
						return;
				}
			}
			
			CellLayout.LayoutParams params = new CellLayout.LayoutParams(dest.x, dest.y, 1, 1);
			DesktopItem item = new DesktopItem(context,
											   DesktopItem.USER_FOLDER,
											   params, desktopNumber);
			item.setApplicationInfo((ApplicationInfo)info);
			item.setContext(context);
			item.setIcon(((ApplicationInfo)info).icon);
			item.setTitle(((ApplicationInfo)info).name);
			Folder f = ((MyPlacesAdapter)myHome.getInstance().getWorkspace().
							getMyPlacesGrid().getAdapter()).getFolder(item.getTitle());
			item.setFolder(f);

			item.getView().setOnClickListener(onClickListener);
			item.getView().setOnLongClickListener(onLongClickListener);
			item.getView().setLayoutParams(params);
			
			this.addView(item.getView());
			
			myHome.getInstance().storeAddItem(item);
		}
		else
		{
			DesktopItem item = (DesktopItem) view.getTag();
			moveDesktopItem(item, dest);
		}
	}
	
	public void removeDesktopFolder(Folder f)
	{
		for(int i = 0; i < getChildCount(); i++)
		{
			if(getChildAt(i).getTag() instanceof DesktopItem)
			{
				DesktopItem child = (DesktopItem) getChildAt(i).getTag();
				
				if(child.getType() == DesktopItem.USER_FOLDER && child.getFolder() == f)
					removeView(getChildAt(i));
			}
		}
	}

	@Override
	public void onDrop(DragSource src, View view, Object info) {
		dropInProgress = false;
		dropView.setDrawingCacheEnabled(false);
		
		Folder openedFolder = myHome.getInstance().getWorkspace().getOpenedFolder();
		
		if(openedFolder != null)
		{
			if(info instanceof ApplicationInfo)
			{
				ApplicationInfo i = (ApplicationInfo)info;
				
				myHome.getInstance().storeAddShortcutToPlace(i.intentUri, openedFolder);
				
				openedFolder.getAdapter().reload();
				openedFolder.getAdapter().notifyDataSetChanged();
				openedFolder.contentChanged();
			}
		}
		else
		{
			Point dest = null;
			if((src instanceof AppsGrid) || (src instanceof Folder))
			{
				dest = calcDropCell(dragPosition, 1, 1);
			}
			else
			{
				if(info instanceof DesktopItem)
				{
					DesktopItem item = (DesktopItem)info;
					dest = calcDropCell(dragPosition, item.getLayoutParams().cellHSpan,
							item.getLayoutParams().cellVSpan);
				}
			}
			
			if(dest != null)
			{
				if(src instanceof MyPlacesGrid)
				{
					addDesktopFolder(true, dest, view, (ApplicationInfo)info);
				}
				else if((src instanceof AppsGrid) || (src instanceof Folder))
				{
					addDesktopShortcut(true, dest, view, (ApplicationInfo)info);
				}
				else
				{
					if(info instanceof DesktopItem)
					{
						moveDesktopItem((DesktopItem)info, dest);
					}
				}
			}
		}
		
		invalidate();
	}
	
	private Point calcDropCell(Point drop, int w, int h)
	{
		Point ret = new Point();

		int pos[] = findNearestVacantArea(drop.x, drop.y,
				   w, h, vacantCells, null);
		
		if(pos == null)
			return null;
		
		ret.x = pos[0];
		ret.y = pos[1];
		
		return ret;
	}

	@Override
	public void onIncomingDrag(View view, Object info) {
		dropInProgress = true;
		dropView = view;
		dropView.setDrawingCacheEnabled(true);
		dragInfo = info;

		dragPosition = new Point();
		estDropPosition = new Point();
		messageShown = false;
		
		vacantCellsUpdated = false;
	}

	@Override
	public void setDragController(DragController ctrl) {
		dragCtrl = ctrl;
	}
	
	@Override
	public void setInitialDragPosition(Point position)
	{
		dragPosition.x = position.x;
		dragPosition.y = position.y;
				
		estDropPosition.x = position.x;
		estDropPosition.y = position.y;
	}

	@Override
	public void onDragMovement(View view, Object info, Point position) {
		dragPosition.x = position.x;
		dragPosition.y = position.y;
		
		if(myHome.getInstance().getWorkspace().getOpenedFolder() == null)
		{
			vacantCells = findAllVacantCells(null, null);
			
			int[] p = null;
			
			if(info instanceof DesktopItem)
			{
				DesktopItem item = (DesktopItem)info;
				p = findNearestVacantArea(dragPosition.x, dragPosition.y,
						item.getLayoutParams().cellHSpan,
						item.getLayoutParams().cellVSpan,
						vacantCells, null);
			}
			else
				p = findNearestVacantArea(dragPosition.x, dragPosition.y,
						1, 1, vacantCells, null);
					
			if(p == null)
			{
				estDropPosition = null;
				
				if(!messageShown)
				{
					final Toast t = Toast.makeText(context, context.getResources().
							      getString(R.string.error_desktop_full), Toast.LENGTH_LONG);
					t.show();
					
					//Hack if Toast refuses to cancel itself
					Runnable cancel = new Runnable()
					{
						public void run() {
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							myHome.getInstance().runOnUiThread(new Runnable() {
								public void run() {
									t.cancel();
								}
							});
						}
					};
					Thread thread = new Thread(cancel);
						thread.start();
						
					messageShown = true;
				}
				
				return;
			}
			
			int[] pixel = new int[2];
			this.cellToPoint(p[0], p[1], pixel);
			
			if(pixel == null)
				return;
			if(estDropPosition == null)
				estDropPosition = new Point();
					
			estDropPosition.x = pixel[0];
			estDropPosition.y = pixel[1];
		}
	}
	
	private int darkenIt(int color)
	{
		if(color < 64)
		{
			return 64-color;
		}
		
		return color-64;
	}
	
	@Override
	public void onDrag(View view, Object info) {
		dragInProgress = true;

		this.removeView(view);
	}

	public void enableDrawingCache()
	{
		drawingCacheEnabled = true;
	}
	
	public void disableDrawingCache()
	{
		drawingCacheEnabled = false;
		if(drawingCache != null)
			drawingCache.recycle();
		drawingCache = null;
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		if(drawingCacheEnabled)
		{
			Bitmap bmp = drawingCache;
			
			if(bmp == null)
			{
				bmp = drawingCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(drawingCache);
				this.draw(c);
			}
			
			if(bmp != null)
			{
				canvas.drawBitmap(bmp, 0, 0, defaultPaint);
				return;
			}
		}
		
		super.onDraw(canvas);
	}

	public DragController getDragCtrl() {
		return dragCtrl;
	}

	@Override
	public void onDropped(View view, Object info) {
		dragInProgress = false;
	}

	public int getDesktopNumber() {
		return desktopNumber;
	}

	public void setDesktopNumber(int desktopNumber) {
		this.desktopNumber = desktopNumber;
	}

	public Point getDragPosition() {
		return dragPosition;
	}

	public Point getEstDropPosition() {
		return estDropPosition;
	}

	public boolean isDrawingCacheEnabled() {
		return drawingCacheEnabled;
	}
}
