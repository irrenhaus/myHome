package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Folder extends LinearLayout implements DragSource {
	public static final String	TITLE = "folder";
	public static final String	INTENT = "intent";
	
	private AppsGrid 			grid = null;
	private AppsAdapter			adapter = null;
	
	private LinearLayout		titleBar = null;
	private ImageView			closeButton = null;
	
	private Context				context = null;
	
	private	String				title = null;
	
	private DragController		dragCtrl;

	public Folder(Context context, String title) {
		super(context);

		this.context = context;
		this.title = title;
		
		init();
	}

	public Folder(Context context, AttributeSet set) {
		super(context, set);
	
		this.context = context;
		
		init();
	}

	private void init()
	{
		this.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, 24);
		
		titleBar = new LinearLayout(context);
		titleBar.setOrientation(LinearLayout.HORIZONTAL);
		titleBar.setLayoutParams(p);
		
		LayoutParams pTitle = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		TextView text = new TextView(context);
		text.setText(title);
		text.setLayoutParams(pTitle);
		text.setBackgroundResource(R.drawable.folder_title_background);
		Drawable right = context.getResources().getDrawable(R.drawable.folder_close_button);
		text.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null);
		
		text.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myHome.getInstance().getWorkspace().closeFolder(Folder.this);
			}
		});
		
		text.setFocusable(false);
		
		titleBar.addView(text);
		
		this.addView(titleBar);
		
		
		grid = new AppsGrid(context);
		
		adapter = new AppsAdapter(context, new FolderAppsFilter(title, context));
		
		grid.setAdapter(adapter);
		grid.setBackgroundDrawable(null);
		
		this.addView(grid);
		this.setBackgroundResource(R.drawable.cell_bg);
		
		adapter.reload();
		adapter.notifyDataSetChanged();
	}
	
	public void close()
	{
		myHome.getInstance().getWorkspace().closeFolder(this);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public static Drawable getIcon(Context context) {
		//TODO: Icon
		return context.getResources().getDrawable(R.drawable.desktop_icon);
	}

	public AppsAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void onDrag(View view, Object info) {
		close();
	}

	@Override
	public void onDropped(View view, Object info) {
		
	}

	@Override
	public void setDragController(DragController ctrl) {
		dragCtrl = ctrl;
	}
	
	public void setOnItemClickListener(OnItemClickListener l)
	{
		grid.setOnItemClickListener(l);
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener l)
	{
		grid.setOnItemLongClickListener(l);
	}
}
