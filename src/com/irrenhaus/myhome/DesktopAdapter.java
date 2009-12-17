package com.irrenhaus.myhome;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.irrenhaus.myhome.AppsCache.ApplicationInfo;

public class DesktopAdapter extends BaseAdapter {
	public final static int NUM_COLUMNS_DESKTOPVIEW = 4;
	
	private View[][] entries;
	private ApplicationInfo[][] infos;
	private Context context;
	
	private DesktopView desktop = null;
	
	public DesktopAdapter(Context context, DesktopView d)
	{
		super();
		
		this.context = context;

		entries = new View[NUM_COLUMNS_DESKTOPVIEW][NUM_COLUMNS_DESKTOPVIEW];
		infos = new ApplicationInfo[NUM_COLUMNS_DESKTOPVIEW][NUM_COLUMNS_DESKTOPVIEW];
		
		desktop = d;
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		desktop.removeAllViews();
		
		for(int x = 0; x < NUM_COLUMNS_DESKTOPVIEW; x++)
		{
			for(int y = 0; y < NUM_COLUMNS_DESKTOPVIEW; y++)
			{
				if(entries[x][y] != null)
				{
					desktop.addView(entries[x][y]);
				}
			}
		}
		
		desktop.invalidate();
	}
	
	public void init()
	{
		desktop.setCellHeight(desktop.getHeight() / NUM_COLUMNS_DESKTOPVIEW);
		desktop.setCellWidth(desktop.getWidth() / NUM_COLUMNS_DESKTOPVIEW);
		
		desktop.setLongAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		desktop.setShortAxisCells(NUM_COLUMNS_DESKTOPVIEW);
		
		for(int x = 0; x < NUM_COLUMNS_DESKTOPVIEW; x++)
		{
			for(int y = 0; y < NUM_COLUMNS_DESKTOPVIEW; y++)
			{
				entries[x][y] = null;
			}
		}
	}
	
	public boolean isFree(int x, int y)
	{
		return entries[x][y] == null;
	}
	
	public void setView(final int x, final int y, View view, ApplicationInfo info)
	{
		if(isFree(x, y))
		{
			LinearLayout layout = new LinearLayout(context);
			layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			layout.setGravity(Gravity.CENTER);
			
			view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.cell_bg));
			
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					context.startActivity(infos[x][y].intent);
				}
			});
			
			layout.addView(view);
			
			entries[x][y] = layout;
	
			infos[x][y] = info;
			
			CellLayout.LayoutParams params = new CellLayout.LayoutParams(x, y, 1, 1);
			entries[x][y].setLayoutParams(params);
			
			Log.d("myHome", "Entry at "+x+"x"+y+" is set");
		}
	}
	
	public void removeView(int x, int y, View view)
	{
		entries[x][y] = null;
	}
	
	@Override
	public int getCount() {
		return NUM_COLUMNS_DESKTOPVIEW * NUM_COLUMNS_DESKTOPVIEW;
	}

	@Override
	public Object getItem(int position) {
		int y = position / NUM_COLUMNS_DESKTOPVIEW;
		int x = position % NUM_COLUMNS_DESKTOPVIEW;
		return entries[x][y];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int y = position / NUM_COLUMNS_DESKTOPVIEW;
		int x = position % NUM_COLUMNS_DESKTOPVIEW;
		return entries[x][y];
	}

}
