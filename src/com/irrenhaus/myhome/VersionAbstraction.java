package com.irrenhaus.myhome;

import java.util.ArrayList;

import android.os.Build;

public class VersionAbstraction {
	private static ArrayList<AbstractionService>	services;
	private static int								sdkVersion;
	
	public static final String						WALLPAPER_MANAGER = "wallpaper_mgr";
	
	public static void init()
	{
		sdkVersion = Build.VERSION.SDK_INT;
		
		services = new ArrayList<AbstractionService>();

		// Initial creation of services
		
		registerService(new WallpaperManager_4());
		registerService(new WallpaperManager_5());
	}
	
	public static Object getService(String title)
	{
		if(services.size() <= 0)
			return null;
		
		AbstractionService bestService = null;
		for(AbstractionService service: services)
		{
			if(service.getServiceName().equals(title) && service.getSDKVersion() <= sdkVersion)
			{
				if(bestService == null || bestService.getSDKVersion() < service.getSDKVersion())
					bestService = service;
			}
		}
		
		return bestService;
	}
	
	public static void registerService(AbstractionService impl)
	{
		services.add(impl);
	}
}
