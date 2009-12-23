package com.irrenhaus.myhome;

import android.app.Application;

public class HomeApplication extends Application {
	@Override
	public void onCreate()
	{
		CrashHandler.getInstance().Init(this);
	}
}
