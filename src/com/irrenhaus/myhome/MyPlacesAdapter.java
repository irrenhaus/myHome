package com.irrenhaus.myhome;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;

public class MyPlacesAdapter extends BaseAdapter {
	Vector<String> myPlaces = null;
	Context context = null;

	public MyPlacesAdapter(myHome context)
	{
		super();
		
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return myPlaces.size();
	}

	@Override
	public Object getItem(int position) {
		return myPlaces.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		return null;
	}

}
