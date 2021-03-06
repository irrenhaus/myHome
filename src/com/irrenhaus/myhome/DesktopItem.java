package com.irrenhaus.myhome;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class DesktopItem {
	public static final int APPLICATION_SHORTCUT = 0;
	public static final int SYSTEM_FOLDER = 1;
	public static final int USER_FOLDER = 2;
	public static final int APP_WIDGET = 3;

	public static final String		_ID = "id";
	public static final String		TYPE = "type";
	public static final String		LAYOUT_PARAMS = "layout_params";
	public static final String		INTENT = "launch_intent";

	private int						type;
	private CellLayout.LayoutParams	layoutParams;
	
	private String					title;
	private	Drawable				icon;
	
	private ApplicationInfo			applicationInfo;
	private Intent					launchIntent;
	private String					launchIntentUri;
	
	private AppWidgetProviderInfo	appWidgetInfo;
	private AppWidgetHostView		appWidgetView;
	private int						appWidgetId;
	
	private	View					view;
	
	private int						desktopNumber;
	
	private Folder					folder;
	
	private Context					context;
	
	public DesktopItem(Context context, int type,
			CellLayout.LayoutParams layoutParams, int desktopNum)
	{
		this.type = type;
		this.layoutParams = layoutParams;
		this.context = context;
		this.desktopNumber = desktopNum;
	}
	
	public View getView()
	{
		if(type == APP_WIDGET)
			view = appWidgetView;
		
		if(view == null)
		{
			BubbleTextView v = (BubbleTextView) LayoutInflater.from(context).
								inflate(R.layout.desktop_item_view, null);
			
			if(type == APPLICATION_SHORTCUT || type == USER_FOLDER)
			{
				v.setText(title);
				v.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				
				v.setCompoundDrawables(null,
														  Utilities.createIconThumbnail(icon,
																  context),
														  null,
														  null);

				v.setSingleLine();
				
				v.setLayoutParams(layoutParams);
				
				view = v;
			}
		}
		
		view.setFocusable(false);
		
		view.setTag(this);
		
		return view;
	}
	
	public void setAppWidget(AppWidgetProviderInfo info, MyHomeAppWidgetHostView view, int id)
	{
		appWidgetView = view;
		appWidgetInfo = info;
		appWidgetId = id;
		
		appWidgetView.setTag(this);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public CellLayout.LayoutParams getLayoutParams() {
		return layoutParams;
	}

	public void setLayoutParams(CellLayout.LayoutParams layoutParams) {
		this.layoutParams = layoutParams;
		if(this.view != null)
			this.view.setLayoutParams(layoutParams);
		else if(this.appWidgetView != null)
			this.appWidgetView.setLayoutParams(layoutParams);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public void setApplicationInfo(ApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
	}

	public Intent getLaunchIntent() {
		return launchIntent;
	}

	public void setLaunchIntent(Intent launchIntent) {
		this.launchIntent = launchIntent;
		this.launchIntentUri = launchIntent.toURI();
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setView(View view) {
		this.view = view;
	}

	public AppWidgetProviderInfo getAppWidgetInfo() {
		return appWidgetInfo;
	}

	public void setAppWidgetInfo(AppWidgetProviderInfo appWidgetInfo) {
		this.appWidgetInfo = appWidgetInfo;
	}

	public AppWidgetHostView getAppWidgetView() {
		return appWidgetView;
	}

	public void setAppWidgetView(AppWidgetHostView appWidgetView) {
		this.appWidgetView = appWidgetView;
	}

	public void setAppWidgetId(int appWidgetId) {
		this.appWidgetId = appWidgetId;
	}

	public int getAppWidgetId() {
		return appWidgetId;
	}

	public ContentValues makeContentValues() {
		ContentValues values = new ContentValues();
		
		String params = MyHomeDB.layoutParams2String(getLayoutParams());
		int type = getType();
		int desktopnum = getDesktopNumber();
		String intent = "";
		if(type == APP_WIDGET)
			intent = String.valueOf(getAppWidgetId());
		if(type == APPLICATION_SHORTCUT)
			intent = launchIntentUri;
		if(type == USER_FOLDER)
			intent = folder.getTitle();
		
		values.put(DesktopItem.INTENT, intent);
		values.put(DesktopItem.LAYOUT_PARAMS, params);
		values.put(DesktopItem.TYPE, type);
		values.put(DesktopView.DESKTOP_NUMBER, desktopnum);
		
		return values;
	}

	public int getDesktopNumber() {
		return desktopNumber;
	}

	public void setDesktopNumber(int desktopNumber) {
		this.desktopNumber = desktopNumber;
	}

	public void setFolder(Folder folder) {
		if(folder == null)
			return;
		
		this.folder = folder;
		
		this.title = folder.getTitle();
		//TODO: Folder icon
		this.icon = context.getResources().getDrawable(R.drawable.desktop_icon);
	}

	public Folder getFolder() {
		return folder;
	}
}
