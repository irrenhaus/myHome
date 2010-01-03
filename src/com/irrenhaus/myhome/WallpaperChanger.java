package com.irrenhaus.myhome;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WallpaperChanger extends Activity {
	private GridView	gallery;
	private TextView	warningView;
	private Drawable	selected;
	private ImageView	selectedView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.wallpaper_chooser_layout);
		
		gallery = (GridView)findViewById(R.id.wallpaperGallery);
		warningView = (TextView)findViewById(R.id.noImagesText);
		selectedView = (ImageView)findViewById(R.id.selectedView);

		Button buttonOk = (Button)findViewById(R.id.buttonOk);
		Button buttonCancel = (Button)findViewById(R.id.buttonCancel);
		
		WallpaperAdapter adapter = new WallpaperAdapter(this);
		
		gallery.setAdapter(adapter);
		
		adapter.loadImages();
		
		if(adapter.getCount() <= 0)
			showErrorView();
		
		adapter.notifyDataSetChanged();
		
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				selected = ((ImageView)arg1).getDrawable();
				selectedView.setImageDrawable(selected);
			}
		});

		buttonOk.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				
				WallpaperManager.getInstance().selectWallpaperDrawable(selected);
				WallpaperManager.getInstance().set();
				
				finish();
			}
		});

		buttonCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	private void showErrorView()
	{
		gallery.setVisibility(View.GONE);
		warningView.setVisibility(View.VISIBLE);
	}
}
