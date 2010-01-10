package com.irrenhaus.myhome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class PlacesView extends Slider {
	private MyPlacesGrid	myPlacesGrid = null;

	private Animation		slideInAnimation = null;
	private Animation		slideOutAnimation = null;
	
	private Folder			openedFolder = null;
	private boolean 		myPlacesGridVisible = true;
	
	private MyPlacesAdapter	myPlacesAdapter = null;

	public PlacesView(Context context, AttributeSet attr) {
		super(context, attr);
		
		//this.setBackgroundResource(R.drawable.tray_bg);

		slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
		slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);
	}
	
	public void init(Screen screen, Workspace workspace)
	{
		myPlacesGrid = (MyPlacesGrid)getContent();

        myPlacesAdapter = new MyPlacesAdapter(getContext());
        myPlacesGrid.setAdapter(myPlacesAdapter);
        myPlacesGrid.setDragController(screen);
        myPlacesGrid.setOnItemClickListener(workspace);
        myPlacesGrid.setOnItemLongClickListener(workspace);
	}
	
	public void openFolder(Folder f)
	{
		openedFolder = f;
		
		//setContent(f);
		
		openedFolder.setOpen(true);
		
		openedFolder.setTitlebar(false);
		openedFolder.setBackground(false);
		
		addView(openedFolder);
		
		hideMyPlacesGrid();
		openedFolder.startAnimation(slideInAnimation);
	}
	
	public void closeFolder()
	{
		showMyPlacesGrid(true, false);
		if(openedFolder != null)
		{
			openedFolder.setDoOnAnimationEnd(new Runnable() {
				public void run() {
					closeFolderDirect();
				}
			});
			openedFolder.startAnimation(slideOutAnimation);
		}
	}
	
	private void closeFolderDirect()
	{
		if(openedFolder == null)
			return;
		
		removeView(openedFolder);
		openedFolder.setTitlebar(true);
		openedFolder.setBackground(true);
		
		openedFolder.setOpen(false);
		openedFolder = null;
	}
	
	public void showMyPlacesGrid(boolean animate)
	{
		showMyPlacesGrid(animate, true);
	}
	
	private void showMyPlacesGrid(boolean animate, boolean closeFolderDirect)
	{
		if(closeFolderDirect)
			closeFolderDirect();
		
		myPlacesGrid.setVisibility(View.VISIBLE);
		myPlacesGridVisible = true;
		if(animate)
			myPlacesGrid.startAnimation(slideInAnimation);
	}
	
	public void hideMyPlacesGrid()
	{
		myPlacesGrid.setDoOnAnimationEnd(new Runnable() {
			public void run() {
				myPlacesGrid.setVisibility(View.GONE);
				myPlacesGridVisible = false;
			}
		});
		myPlacesGrid.startAnimation(slideOutAnimation);
	}

	public boolean isMyPlacesGridOpened() {
		return myPlacesGridVisible;
	}

	public MyPlacesAdapter getMyPlacesAdapter() {
		return myPlacesAdapter;
	}

	public MyPlacesGrid getMyPlacesGrid() {
		return myPlacesGrid;
	}
}
