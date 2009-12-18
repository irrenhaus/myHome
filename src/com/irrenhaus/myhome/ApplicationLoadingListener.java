package com.irrenhaus.myhome;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public interface ApplicationLoadingListener {
	public void applicationLoaded(ApplicationInfo info);

	public void loadingDone();
}
