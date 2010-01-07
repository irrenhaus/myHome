package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.Contacts;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Toolbar extends LinearLayout {
	private Vector<View>	toolbarButtons;
	private ImageView		trayIcon;
	private RectF			bgRect;
	private Paint			bgPaint;
	
	public Toolbar(Context context) {
		super(context);
	}
	
	public Toolbar(Context context, AttributeSet set) {
		super(context, set);
	}

	public void initToolbar()
	{
		final Context context = getContext();
		final Workspace workspace = myHome.getInstance().getWorkspace();
		
		removeAllViews();
		
		if(toolbarButtons == null)
			toolbarButtons = new Vector<View>();
		else
			toolbarButtons.clear();
		
		Button allAppsGrid = new Button(context);
		allAppsGrid.setBackgroundResource(R.drawable.all_apps_grid_toolbar_button);
		
		Button myPlaces = new Button(context);
		myPlaces.setBackgroundResource(R.drawable.my_places_toolbar_button);
		
		if(Config.getBoolean(Config.TOOLBAR_SHOW_CALLER_BUTTON_KEY, true))
		{
			Button caller = new Button(context);
			caller.setBackgroundResource(R.drawable.caller_toolbar_button);
			caller.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					context.startActivity(new Intent(Intent.ACTION_DIAL));
				}
	        });
			
			toolbarButtons.add(caller);
		}
		
		if(Config.getBoolean(Config.TOOLBAR_SHOW_CONTACTS_BUTTON_KEY, true))
		{
			Button contacts = new Button(context);
			contacts.setBackgroundResource(R.drawable.contacts_toolbar_button);
			contacts.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Contacts.People.CONTENT_URI);
					context.startActivity(intent);
				}
	        });

			toolbarButtons.add(contacts);
		}
		
		allAppsGrid.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(workspace.isAppsGridOpened())
					workspace.closeTrayView(null);
				else
					workspace.openAllAppsGrid();
			}
        });
        
		 myPlaces.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(workspace.isMyPlacesOpened())
						workspace.closeTrayView(null);
					else if(workspace.getOpenedFolder() != null && !workspace.isFolderOpenedInScreen())
						workspace.closeFolder();
					else
						workspace.openMyPlaces();
				}
	        });
			
		toolbarButtons.add(allAppsGrid);
		toolbarButtons.add(myPlaces);
		
		if(Config.getBoolean(Config.TOOLBAR_SHOW_DESKTOP_SWITCHER_BUTTON_KEY, true))
		{
			Button desktopSwitcher = new Button(context);
			desktopSwitcher.setBackgroundResource(R.drawable.desktop_switcher_toolbar_button);
			desktopSwitcher.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(workspace.isDesktopSwitcherOpened())
						workspace.closeDesktopSwitcher(null);
					else
						workspace.openDesktopSwitcher();
				}
	        });

			toolbarButtons.add(desktopSwitcher);
		}
		
		showButtons();
		
		trayIcon = new ImageView(context);
		trayIcon.setImageResource(R.drawable.tray_icon);
		
		bgPaint = new Paint();
		bgPaint.setARGB(192, 0, 0, 0);
		
		setPadding(4, 4, 4, 4);
	}
	
	public void showButtons()
	{
		removeAllViews();
		for(View v: toolbarButtons)
		{
			addView(v);
		}
	}
	
	public void showTrayIcon()
	{
		removeAllViews();
		addView(trayIcon);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		if(w > h) //land
		{
			bgRect = new RectF(0, 0, getWidth(), 0);
			for(int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);
				int y = child.getMeasuredHeight();
				
				if(bgRect.bottom < y)
					bgRect.bottom = y;
			}
			bgRect.bottom += this.getPaddingBottom();
		}
		else //port
		{
			bgRect = new RectF(0, 0, 0, getHeight());
			for(int i = 0; i < getChildCount(); i++)
			{
				View child = getChildAt(i);
				int x = child.getMeasuredWidth();
				
				if(bgRect.right < x)
					bgRect.right = x;
			}
			bgRect.right += this.getPaddingRight();
		}
	}
	
	@Override
    public void dispatchDraw(Canvas canvas)
	{
		canvas.drawRoundRect(bgRect, 6, 6, bgPaint);
		
		super.dispatchDraw(canvas);
	}
	
}
