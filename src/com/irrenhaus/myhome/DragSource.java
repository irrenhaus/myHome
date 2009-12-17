package com.irrenhaus.myhome;

import android.view.View;

public interface DragSource {
	void setDragController(DragController ctrl);

	void onDrag(View view, Object info);
	void onDropped(View view, Object info);
	
}
