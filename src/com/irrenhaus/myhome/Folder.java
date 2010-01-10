package com.irrenhaus.myhome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
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
	private	TextView			noContentView = null;
	
	private Context				context = null;
	
	private	String				title = null;
	
	private DragController		dragCtrl;
	
	private boolean				open = false;
	private Runnable			doOnAnimationEnd;

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
		
		TextView text = new TextView(context);
		
		int height = text.getLineHeight();
		
		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, height+16);
		
		titleBar = new LinearLayout(context);
		titleBar.setOrientation(LinearLayout.HORIZONTAL);
		titleBar.setLayoutParams(p);
		
		LayoutParams pTitle = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		text.setText(title);
		text.setLayoutParams(pTitle);
		text.setBackgroundResource(R.drawable.folder_title_background);
		Drawable right = context.getResources().getDrawable(R.drawable.folder_close_button);
		text.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null);
		
		text.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myHome.getInstance().getWorkspace().closeFolder();
			}
		});
		
		text.setFocusable(false);
		
		titleBar.addView(text);
		
		this.addView(titleBar);
		
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		
		grid = new AppsGrid(context);
		grid.setLayoutParams(params);
		
		adapter = new AppsAdapter(context, new FolderAppsFilter(title, context));
		
		grid.setAdapter(adapter);
		grid.setBackgroundDrawable(null);
		
		addView(grid);
		setBackgroundResource(R.drawable.folder_bg);
		
		adapter.reload();
		adapter.notifyDataSetChanged();
		
		noContentView = new TextView(context);
		noContentView.setLayoutParams(params);
		noContentView.setText(R.string.folder_no_content);
		noContentView.setGravity(Gravity.CENTER);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		contentChanged();
	}
	
	public void contentChanged()
	{
		removeAllViews();
		
		if(adapter.getCount() <= 0)
			addView(noContentView);
		else
			addView(grid);
	}
	
	public void setNumColumns(int num)
	{
		grid.setNumColumns(num);
	}
	
	@Override
	protected void onAnimationEnd()
	{
		Log.d("myHome", "Animation end. Open:"+open);
		
		if(open)
			close();
		else
			open = true;
		
		Log.d("myHome", "Animation end. After Open:"+open+". run: "+(doOnAnimationEnd != null));
		
		if(doOnAnimationEnd != null)
		{
			doOnAnimationEnd.run();
			doOnAnimationEnd = null;
		}
	}
	
	public void close()
	{
		//myHome.getInstance().getWorkspace().closeFolder();
		
		open = false;
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

	public Runnable getDoOnAnimationEnd() {
		return doOnAnimationEnd;
	}

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		Log.d("myHome", "setDoOnAnimationEnd");
		this.doOnAnimationEnd = doOnAnimationEnd;
	}

	public AppsGrid getGrid() {
		return grid;
	}
	
	public void setTitlebar(boolean t)
	{
		if(t)
			titleBar.setVisibility(View.VISIBLE);
		else
			titleBar.setVisibility(View.GONE);
	}
	
	public void setBackground(boolean t)
	{
		if(!t)
			this.setBackgroundDrawable(null);
		else
			this.setBackgroundResource(R.drawable.folder_bg);
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean opened) {
		this.open = opened;
	}
}
