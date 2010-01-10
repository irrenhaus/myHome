package com.irrenhaus.myhome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class AppsGrid extends GridView implements DragSource {
	public static final int NUM_COLUMNS_APPSVIEW = 4;
	
	private DragController dragCtrl = null;
	private boolean dragInProgress = false;

	private android.widget.AdapterView.OnItemClickListener onClickListener;

	private android.widget.AdapterView.OnItemLongClickListener onLongClickListener;

	private Runnable doOnAnimationEnd;
	
	public AppsGrid(final Context context) {
		this(context, null);
	}
	
	public AppsGrid(final Context context, AttributeSet attr) {
		super(context, attr);
		
		this.setNumColumns(NUM_COLUMNS_APPSVIEW);
		this.setVerticalSpacing((int) (context.getResources().getDimension(android.R.dimen.app_icon_size)/4));
		this.setHorizontalSpacing((int) (context.getResources().getDimension(android.R.dimen.app_icon_size)/4));
	
		if(this.getCacheColorHint() > 0)
			this.setCacheColorHint(0);
		
		this.setChildrenDrawingCacheEnabled(true);
	}

    @Override
    protected void onAnimationEnd()
    {
    	if(doOnAnimationEnd != null)
    	{
    		doOnAnimationEnd.run();
    		doOnAnimationEnd = null;
    	}
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

	public Runnable getDoOnAnimationEnd() {
		return doOnAnimationEnd;
	}

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		this.doOnAnimationEnd = doOnAnimationEnd;
	}
}
