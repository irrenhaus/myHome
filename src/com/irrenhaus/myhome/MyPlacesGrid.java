package com.irrenhaus.myhome;

import android.content.Context;
import android.view.View;

public class MyPlacesGrid extends AppsGrid {

	public MyPlacesGrid(Context context) {
		super(context);
		
	}

	@Override
	public void onDrag(View view, Object info) {
		myHome.getInstance().getWorkspace().closeAllOpen();
	}

}
