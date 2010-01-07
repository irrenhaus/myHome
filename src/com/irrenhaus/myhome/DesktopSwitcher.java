package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class DesktopSwitcher extends LinearLayout {
	private Context					context;
	private Runnable				doOnAnimationEnd = null;
	
	private GridView				grid;
	private DesktopSwitcherAdapter	adapter;
	
	private Workspace				workspace;
	private int						desktopCount;

	public DesktopSwitcher(Context context) {
		super(context);
		this.context = context;
		create();
	}

	public DesktopSwitcher(Context context, AttributeSet set) {
		super(context, set);
		this.context = context;
		create();
	}
	
	public void create()
	{
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setGravity(Gravity.CENTER);
		
		grid = new GridView(context);
		adapter = new DesktopSwitcherAdapter(context);
		
		workspace = myHome.getInstance().getWorkspace();
		desktopCount = workspace.getDesktopCount();
		
		int cols = desktopCount;
		if(cols > 4)
			cols = 4;
		
		grid.setNumColumns(cols);
		grid.setAdapter(adapter);
		
		addView(grid);
	}

	public void setDoOnAnimationEnd(Runnable runnable) {
		doOnAnimationEnd = runnable;
	}

	public void init(int w, int h) {
		
		adapter.init(workspace, desktopCount, w, h);
		
		adapter.notifyDataSetChanged();
	}

	public void deinit() {
		adapter.deinit();
	}
	
	@Override
	public void onAnimationEnd()
	{
		super.onAnimationEnd();
		
		if(doOnAnimationEnd != null)
		{
			doOnAnimationEnd.run();
			doOnAnimationEnd = null;
		}
	}

}
