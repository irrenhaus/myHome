package com.irrenhaus.myhome;

import android.app.Application;
import dalvik.system.VMRuntime;

public class HomeApplication extends Application {
	@Override
	public void onCreate()
	{
		VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
		CrashHandler.getInstance().Init(this);
	}
}
