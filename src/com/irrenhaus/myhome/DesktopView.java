package com.irrenhaus.myhome;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
	private Point			dragModifier = null;
	private View			dropView = null;
	private Object			dragInfo = null;
	
	private Context 		context = null;
	
	private CellInfo		vacantCells = null;

	private Point           estDropPosition;

	private Bitmap          dragViewBitmap;
	private Bitmap          dragViewAlphaBitmap;
	private OnClickListener onClickListener;
	private OnLongClickListener onLongClickListener;
	
	private Point			currentPointerPos = null;
	
	public DesktopView(Context context) {
		super(context);
		
		this.context = context;
		
		this.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
											  LayoutParams.FILL_PARENT));
		
		this.setFocusableInTouchMode(true);
		
		//Needed because of drawing the views for drag & drop
		this.setBackgroundColor(Color.argb(1, 128, 128, 128));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		setCellHeight(getHeight() / NUM_COLUMNS_DESKTOPVIEW);
		setCellWidth(getWidth() / NUM_COLUMNS_DESKTOPVIEW);
		
		setLongAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		setShortAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		
		invalidate();
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
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		currentPointerPos = new Point((int)event.getX(), (int)event.getY());
			
		return super.dispatchTouchEvent(event);
	}
	
	public void addAppWidget(AppWidgetHostView view, AppWidgetProviderInfo info, int id)
	{
		int span[] = this.rectToCell(info.minWidth, info.minHeight);
		
		vacantCells = this.findAllVacantCells(null, null);
		
		final Point p = currentPointerPos;
		
		int cell[] = findNearestVacantArea(p.x, p.y, span[0], span[1], vacantCells, null);
		
		if(cell == null)
		{
			Toast.makeText(context, context.getResources().getString(R.string.no_vacant_cells), Toast.LENGTH_SHORT);
			return;
		}
		
		Point to = new Point(cell[0], cell[1]);
		Point size = new Point(span[0], span[1]);
		
		addAppWidget(view, info, id, to, size);
	}
	
	public void addAppWidget(AppWidgetHostView view,
			AppWidgetProviderInfo info, int id, Point to, Point size) {
		CellLayout.LayoutParams params = new CellLayout.LayoutParams(to.x, to.y, size.x, size.y);
		
		view.setLayoutParams(params);
		
		DesktopItem item = new DesktopItem(context, DesktopItem.APP_WIDGET, params);
		item.setAppWidget(info, view, id);
		
		item.getView().setOnClickListener(onClickListener);
		item.getView().setOnLongClickListener(onLongClickListener);
		
		this.addView(item.getView());
		
		item.getView().invalidate();
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
		
		item.getView().invalidate();
	}
	
	public void addDesktopShortcut(boolean create, Point dest, View view, ApplicationInfo info)
	{
		if(create)
		{
			CellLayout.LayoutParams params = new CellLayout.LayoutParams(dest.x, dest.y, 1, 1);
			DesktopItem item = new DesktopItem(context,
											   DesktopItem.APPLICATION_SHORTCUT,
											   params);
			item.setApplicationInfo((ApplicationInfo)info);
			item.setContext(context);
			item.setLaunchIntent(((ApplicationInfo)info).intent);
			item.setIcon(((ApplicationInfo)info).icon);
			item.setTitle(((ApplicationInfo)info).name);

			item.getView().setOnClickListener(onClickListener);
			item.getView().setOnLongClickListener(onLongClickListener);
			
			this.addView(item.getView());
			
			item.getView().invalidate();
		}
		else
		{
			DesktopItem item = (DesktopItem) view.getTag();
			moveDesktopItem(item, dest);
		}
	}

	@Override
	public void onDrop(DragSource src, View view, Object info) {
		dropInProgress = false;
		dropView.setDrawingCacheEnabled(false);
		
		if(inDeletePosition(dragPosition))
		{
			Log.d("DeletePosition", "true");
			deleteIfItem(view.getTag());
			invalidate();
			return;
		}
		
		Point dest = calcDropCell(dragPosition);
		
		if((src instanceof AppsGrid))
			addDesktopShortcut(true, dest, view, (ApplicationInfo)info);
		else
		{
			if(info instanceof DesktopItem)
			{
				moveDesktopItem((DesktopItem)info, dest);
			}
		}
		
		invalidate();
	}
	
	private boolean inDeletePosition(Point p)
	{
		boolean land = getWidth() > getHeight();
		
		if(land)
		{
			if(p.x > (getWidth()-54))
				return true;
		}
		else
		{
			if(p.y > (getHeight()-54))
				return true;
		}
		
		return false;
	}
	
	private void deleteIfItem(Object info)
	{
		if(info instanceof DesktopItem)
		{
			DesktopItem item = (DesktopItem)info;
			
			int id = item.getAppWidgetId();
				
			myHome.getAppWidgetHost().deleteAppWidgetId(id);
		}
	}
	
	private Point calcDropCell(Point drop)
	{
		Point ret = new Point();

		int pos[] = findNearestVacantArea(drop.x, drop.y,
				   1, 1, vacantCells, null);
		
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
		
		vacantCells = this.findAllVacantCells(null, null);
		
		dragViewBitmap = null;
		dragViewAlphaBitmap = null;
		
		dragModifier = new Point();
	}

	@Override
	public void setDragController(DragController ctrl) {
		dragCtrl = ctrl;
	}

	@Override
	public void onDragMovement(View view, Object info, Point position) {
		dragPosition.x = position.x-dragModifier.x;
		dragPosition.y = position.y-dragModifier.y;
			
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
				
		int[] pixel = new int[2];
		this.cellToPoint(p[0], p[1], pixel);
				
		estDropPosition.x = pixel[0];
		estDropPosition.y = pixel[1];
		
		dragModifier.x = view.getWidth()/2;
		dragModifier.y = view.getHeight()/2;
		
		invalidate();
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
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if((dragViewBitmap == null || dragViewAlphaBitmap == null) && dropView != null)
		{
			dragViewBitmap = dropView.getDrawingCache();
			dragViewAlphaBitmap = Bitmap.createBitmap(
				dragViewBitmap.getWidth(), dragViewBitmap.getHeight(), dragViewBitmap.getConfig());
		
		
			for(int x = 0; x < dragViewBitmap.getWidth(); x++)
			{
				for(int y = 0; y < dragViewBitmap.getHeight(); y++)
				{
					int c = dragViewBitmap.getPixel(x, y);
					int a = Color.alpha(c);
					int r = darkenIt(Color.red(c));
					int g = darkenIt(Color.green(c));
					int b = darkenIt(Color.blue(c));
					if(a > 220) 
						a -= 128;
					int nc = Color.argb(a, r, g, b);
					dragViewAlphaBitmap.setPixel(x, y, nc);
				}
			}
		}
		
		if(dropInProgress && dropView != null && dragPosition != null)
		{
			if(dragViewAlphaBitmap != null && estDropPosition != null &&
				!inDeletePosition(dragPosition))
				canvas.drawBitmap(dragViewAlphaBitmap, estDropPosition.x,
						estDropPosition.y, null);
			
			canvas.drawBitmap(dragViewBitmap, dragPosition.x,
								   dragPosition.y, null);
		}
	}
	
	@Override
	public void onDrag(View view, Object info) {
		dragInProgress = true;

		this.removeView(view);
	}

	public DragController getDragCtrl() {
		return dragCtrl;
	}

	@Override
	public void onDropped(View view, Object info) {
		dragInProgress = false;
	}
}
