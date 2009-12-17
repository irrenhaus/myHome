package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class DesktopView extends CellLayout implements DragTarget {
	public final static int HOME_SCREEN = 0;
	
	private DragController 	dragCtrl = null;
	private boolean			dragInProgress = false;
	private Point			dragPosition = null;
	private Point			dragModifier = null;
	private View			dragView = null;
	private Object			dragInfo = null;
	private boolean			firstDragMovement = true;
	
	private Context 		context = null;
	
	private DesktopAdapter	adapter = null;
	
	private CellInfo		vacantCells = null;

	private Point           estDropPosition;

	private Bitmap          dragViewBitmap;
	private Bitmap          dragViewAlphaBitmap;
	
	public DesktopView(Context context) {
		super(context);
		
		this.context = context;
		
		this.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
											  LayoutParams.FILL_PARENT));
		
		this.setFocusableInTouchMode(true);
		
		//Needed because of drawing the views for drag & drop
		this.setBackgroundColor(Color.argb(1, 128, 128, 128));
		
		adapter = new DesktopAdapter(context, this);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		adapter.init();
		adapter.notifyDataSetChanged();
	}
	
	public View createView(ApplicationInfo info) {
		View convertView = new TextView(context);
		
		((TextView)convertView).setText(info.name);
		
		final Bitmap.Config c =
            info.icon.getOpacity() != PixelFormat.OPAQUE ?
                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		
		int width = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
        int height = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);

        //width = (int) (width * 0.7);
        //height = (int) (height * 0.7);
		
		Bitmap bmp = Bitmap.createBitmap(width, height, c);
		Canvas can = new Canvas(bmp);
		Rect bounds = new Rect();
		bounds.set(info.icon.getBounds());
		info.icon.setBounds(0, 0, width, height);
		info.icon.draw(can);
		info.icon.setBounds(bounds);
		
		((TextView)convertView).setCompoundDrawablesWithIntrinsicBounds(null,
																		new BitmapDrawable(bmp),
																		null,
																		null);

		((TextView)convertView).setSingleLine();
		((TextView)convertView).setMinWidth(getWidth() / 4 - 10);
		((TextView)convertView).setMaxWidth(getWidth() / 4 - 10);

		((TextView)convertView).setGravity(Gravity.CENTER);
		
		return convertView;
	}

	@Override
	public void onDrop(View view, Object info) {
		dragInProgress = false;
		dragView.setDrawingCacheEnabled(false);
		
		Point dest = calcDropCell(dragPosition);
		Log.d("myHome", "Cell location: "+dest.x+"x"+dest.y);
		
		View copy = createView((ApplicationInfo)dragInfo);

		adapter.setView(dest.x, dest.y, copy, (ApplicationInfo)info);
		adapter.notifyDataSetChanged();
	}
	
	public Point calcDropCell(Point drop)
	{
		Point ret = new Point();

		int pos[] = findNearestVacantArea(drop.x-dragModifier.x, drop.y-dragModifier.y,
				   1, 1, vacantCells, null);
		
		ret.x = pos[0];
		ret.y = pos[1];
		
		return ret;
	}

	@Override
	public void onIncomingDrag(View view, Object info) {
		dragInProgress = true;
		dragView = view;
		dragView.setDrawingCacheEnabled(true);
		dragInfo = info;

		firstDragMovement = true;
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
		Point pos = calcDropCell(position);
		
		int[] pixel = new int[2];
		this.cellToPoint(pos.x, pos.y, pixel);

		dragPosition.x = position.x-dragModifier.x;
		dragPosition.y = position.y-dragModifier.y;

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
		
		if((dragViewBitmap == null || dragViewAlphaBitmap == null) && dragView != null)
		{
			dragViewBitmap = dragView.getDrawingCache();
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
		
		if(dragInProgress && dragView != null && dragPosition != null)
		{
			if(dragViewAlphaBitmap != null && estDropPosition != null)
				canvas.drawBitmap(dragViewAlphaBitmap, estDropPosition.x,
						estDropPosition.y, null);
			
			canvas.drawBitmap(dragViewBitmap, dragPosition.x,
								   dragPosition.y, null);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_MOVE:
				if(dragInProgress) {
					dragCtrl.onDragMotionEvent(event);
					return true;
				}
				break;
			
			case MotionEvent.ACTION_UP:
				if(dragInProgress) {
					dragCtrl.onDragMotionEvent(event);
					return true;
				}
				break;
		}
		
		return true;
	}
}
