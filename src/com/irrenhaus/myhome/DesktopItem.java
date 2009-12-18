package com.irrenhaus.myhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class DesktopItem {
	public static final int APPLICATION_SHORTCUT = 0;
	public static final int SYSTEM_FOLDER = 1;
	public static final int USER_FOLDER = 2;
	public static final int WIDGET = 3;

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
	
	private	View					view;
	
	private Context					context;
	
	public DesktopItem(Context context, int type,
			CellLayout.LayoutParams layoutParams)
	{
		this.type = type;
		this.layoutParams = layoutParams;
		this.context = context;
	}
	
	public View getView()
	{
		if(view == null)
		{
			if(type == APPLICATION_SHORTCUT)
			{
				TextView v = new TextView(context);
				
				v.setText(title);
				
				final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
		                				Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
				
				int width = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
		        int height = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);

		        Bitmap bmp = Bitmap.createBitmap(width, height, c);
				Canvas can = new Canvas(bmp);
				Rect bounds = new Rect();
				bounds.set(icon.getBounds());
				icon.setBounds(0, 0, width, height);
				icon.draw(can);
				icon.setBounds(bounds);
				
				v.setCompoundDrawablesWithIntrinsicBounds(null,
														  new BitmapDrawable(bmp),
														  null,
														  null);

				v.setSingleLine();

				v.setGravity(Gravity.CENTER);
				
				v.setLayoutParams(layoutParams);
				
				view = v;
			}
		}
		
		view.setTag(this);
		
		return view;
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
}
