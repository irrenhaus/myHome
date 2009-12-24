package com.irrenhaus.myhome;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

public class ShortcutTextView extends TextView {

	private Runnable doOnAnimationEnd;

	public ShortcutTextView(Context context) {
		super(context);
		
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

	public Object getDoOnAnimationEnd() {
		return doOnAnimationEnd;
	}

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		this.doOnAnimationEnd = doOnAnimationEnd;
	}
}
