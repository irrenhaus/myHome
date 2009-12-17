package com.irrenhaus.myhome;

import android.view.MotionEvent;
import android.view.View;

public interface DragController {
	void onDragBegin(DragSource src, View view, Object info);
	void onDragEnd();
	void onDragMotionEvent(MotionEvent event);
}
