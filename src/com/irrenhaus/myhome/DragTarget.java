package com.irrenhaus.myhome;

import android.graphics.Point;
import android.view.View;

public interface DragTarget {
	void setDragController(DragController ctrl);
	
	void onIncomingDrag(View view, Object info);
	void onDrop(View view, Object info);
	void onDragMovement(View view, Object info, Point position);
}
