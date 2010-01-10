package com.irrenhaus.myhome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class MyPlacesGrid extends AppsGrid {

	public MyPlacesGrid(Context context) {
		super(context);
	}

	public MyPlacesGrid(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	public void onDrag(View view, Object info) {
		myHome.getInstance().getWorkspace().closeAllOpen();
	}

}
