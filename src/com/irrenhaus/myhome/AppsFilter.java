package com.irrenhaus.myhome;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public interface AppsFilter {
	public void		init();
	public void		done();
	public boolean	filterApplicationInfo(ApplicationInfo info);
}
