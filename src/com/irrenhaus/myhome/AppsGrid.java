package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.GridView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class AppsGrid extends GridView implements DragSource {
	public static final int NUM_COLUMNS_APPSVIEW = 4;
	
	private DragController dragCtrl = null;
	private boolean dragInProgress = false;

	private android.widget.AdapterView.OnItemClickListener onClickListener;

	private android.widget.AdapterView.OnItemLongClickListener onLongClickListener;
	
	public AppsGrid(final Context context) {
		super(context);
		
		this.setNumColumns(NUM_COLUMNS_APPSVIEW);
		this.setPadding(4, 4, 4, 4);
		this.setVerticalSpacing((int) (context.getResources().getDimension(android.R.dimen.app_icon_size)/4));
		this.setHorizontalSpacing((int) (context.getResources().getDimension(android.R.dimen.app_icon_size)/4));
		this.setBackgroundColor(Color.argb(192, 64, 64, 64));
	}

	@Override
	public void onDrag(View view, Object info) {
		dragInProgress = true;
	}

	@Override
	public void onDropped(View view, Object info) {
		dragInProgress = false;
	}

	@Override
	public void setDragController(DragController ctrl) {
		dragCtrl = ctrl;
	}
	
	public void setOnItemClickListener(OnItemClickListener l)
	{
		super.setOnItemClickListener(l);
		onClickListener = l;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener l)
	{
		super.setOnItemLongClickListener(l);
		
		onLongClickListener = l;
	}
}
