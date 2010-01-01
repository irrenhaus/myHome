package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class TrayView extends LinearLayout {
	public static final int	ALL_APPS_GRID = 0;
	public static final int MY_PLACES_GRID = 1;
	
	private AppsGrid		allAppsGrid = null;
	private AppsGrid		myPlacesGrid = null;
	private AppsGrid		curGrid = null;
	
	private Animation		fadeInAnimation;
	private Animation		fadeOutAnimation;
	
	private Runnable		doOnAnimationEnd = null;
	
	private Folder			openedFolder = null;

	public TrayView(Context context, AppsGrid allAppsGrid, AppsGrid myPlacesGrid) {
		super(context);
		
		setOrientation(LinearLayout.HORIZONTAL);
		
		this.setBackgroundColor(Color.argb(192, 64, 64, 64));
		this.setBackgroundResource(R.drawable.tray_bg);
		
		this.allAppsGrid = allAppsGrid;
		this.myPlacesGrid = myPlacesGrid;

        fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fadein);
        fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fadeout);
		
        curGrid = allAppsGrid;
        
		addView(allAppsGrid);
		addView(myPlacesGrid);
	}
	
	public void openFolder(Folder f)
	{
		openedFolder = f;
		
		addView(f);
		
		openedFolder.setOpen(true);
		
		openedFolder.setTitlebar(false);
		openedFolder.setBackground(false);
		
		if(curGrid != null)
		{
			curGrid.setDoOnAnimationEnd(new Runnable() {
				public void run() {
					curGrid.setVisibility(View.GONE);
					curGrid = null;
				}
			});
			curGrid.startAnimation(fadeOutAnimation);
		}
	}
	
	public void closeFolder()
	{
		if(openedFolder != null)
		{
			removeView(openedFolder);
			openedFolder.setTitlebar(true);
			openedFolder.setBackground(true);
			
			openedFolder.setOpen(false);
			openedFolder = null;
		}
	}
	
	public void gotoGrid(int grid)
	{
		final AppsGrid oldGridView = curGrid;
		
		if(grid == ALL_APPS_GRID)
		{
			curGrid = allAppsGrid;
		}
		else if(grid == MY_PLACES_GRID)
		{
			curGrid = myPlacesGrid;
		}
		
		if(oldGridView != null)
		{
			oldGridView.setDoOnAnimationEnd(new Runnable() {
				public void run() {
					oldGridView.setVisibility(View.GONE);
				}
			});
			oldGridView.startAnimation(fadeOutAnimation);
		}
		else if(openedFolder != null)
		{
			openedFolder.setDoOnAnimationEnd(new Runnable() {
				public void run() {
					closeFolder();
				}
			});
			openedFolder.startAnimation(fadeOutAnimation);
		}
		
		if(curGrid != null)
		{
			curGrid.setDoOnAnimationEnd(new Runnable() {
				public void run() {
					curGrid.setVisibility(View.VISIBLE);
				}
			});
			curGrid.startAnimation(fadeInAnimation);
		}
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

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		this.doOnAnimationEnd = doOnAnimationEnd;
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

	public boolean isMyPlacesGridOpened() {
		return curGrid == myPlacesGrid;
	}

	public boolean isAllAppsGridOpened() {
		return curGrid == allAppsGrid;
	}

	public void setGrid(int grid) {
		final AppsGrid oldGridView = curGrid;
		
		if(grid == ALL_APPS_GRID)
			curGrid = allAppsGrid;
		else if(grid == MY_PLACES_GRID)
			curGrid = myPlacesGrid;
		
		if(oldGridView != null)
			oldGridView.setVisibility(View.GONE);
		else if(openedFolder != null)
			myHome.getInstance().getWorkspace().closeFolderWithoutTrayView();
		
		curGrid.setVisibility(View.VISIBLE);
	}
}
