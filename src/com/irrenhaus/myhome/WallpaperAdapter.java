package com.irrenhaus.myhome;

import java.io.File;
import java.util.Vector;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

public class WallpaperAdapter extends BaseAdapter {
	private Context			context;
	
	private Vector<Uri>		wallpaperImages;
	
	public WallpaperAdapter(Context c)
	{
		context = c;
		
		wallpaperImages = new Vector<Uri>();
	}
	
	public void loadImages()
	{
		File dir = new File("/sdcard/myHome/wallpaper/");
		
		if(!dir.exists())
		{
			return;
		}
		
		walkDir(dir);
	}
	
	private void walkDir(File dir)
	{
		String[] files = dir.list();
		
		for(int i = 0; i < files.length; i++)
		{
			File file = new File(dir.getAbsolutePath()+"/"+files[i]);
			
			if(file.isDirectory())
				walkDir(file);
			else
			{
				String name = file.getName();
				if(name.endsWith("png") || name.endsWith("jpg") || name.endsWith("jpeg"))
				{
					if(name.contains("_small"))
					{
						wallpaperImages.add(Uri.parse(file.getAbsolutePath()));
					}
				}
			}
		}
	}
	
	public int getCount() {

		return wallpaperImages.size();
	}

	public Object getItem(int arg0) {

		return wallpaperImages.get(arg0);
	}

	public long getItemId(int arg0) {

		return 0;
	}

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ImageView view = new ImageView(context);
		view.setImageURI(wallpaperImages.get(arg0));
		view.setTag(wallpaperImages.get(arg0));
		view.setScaleType(ImageView.ScaleType.FIT_XY);
		view.setLayoutParams(new GridView.LayoutParams(300, 256));
		view.setPadding(15, 15, 15, 15);
		
		return view;
	}

}
