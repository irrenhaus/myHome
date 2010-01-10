package com.irrenhaus.myhome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SlidingDrawer;

public class AppsView extends Slider {
	private AppsGrid		appsGrid = null;
	
	//maybe http://code.google.com/p/android-misc-widgets/source/browse/trunk/android-misc-widgets/res/layout/panel_main.xml

	public AppsView(Context context, AttributeSet attr) {
		super(context, attr);
		
		//this.setBackgroundResource(R.drawable.tray_bg);
	}
	
	public void init(Screen screen, Workspace workspace)
	{
        appsGrid = (AppsGrid)getContent();
        
        appsGrid.setAdapter(new AppsAdapter(getContext(), null));
		appsGrid.setDragController(screen);
        appsGrid.setOnItemClickListener(workspace);
        appsGrid.setOnItemLongClickListener(workspace);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int count = getChildCount();
		
		for(int i = 0; i < count; i++)
		{
			View v = getChildAt(i);
			
			v.measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public AppsGrid getAppsGrid() {
		return appsGrid;
	}
}
