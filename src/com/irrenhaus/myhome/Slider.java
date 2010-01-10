package com.irrenhaus.myhome;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class Slider extends FrameLayout {
	private	View		contentView;
	private int			contentId;
	private Runnable 	doOnAnimationEnd;
	private boolean 	closeAnimation = false;

	private Animation	comeInAnimation;
	private Animation	goOutAnimation;
	
	private int			animationDuration = 0;
	private int			position = 1;
	
	private boolean		opened = false;
	private boolean		moving = false;

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    
	
	public Slider(Context context) {
		this(context, null);
	}

	public Slider(Context context, AttributeSet attr) {
		super(context, attr);
		
		TypedArray styled = context.obtainStyledAttributes(attr, R.styleable.Slider);
		
		animationDuration = styled.getInteger(R.styleable.Slider_animationDuration, 500);
		position = styled.getInteger(R.styleable.Slider_position, BOTTOM);
		contentId = styled.getResourceId(R.styleable.Slider_content, -1);
		
		Log.d("myHome", "Content id: "+contentId);
		
		setInterpolator(null);
	}
	
	@Override
	public void onFinishInflate()
	{
		super.onFinishInflate();
		
		if(contentId != -1)
		{
			contentView = findViewById(contentId);
		}
	}
	
	public void setInterpolator(Interpolator interpolator)
	{
		if(interpolator == null)
			interpolator = new LinearInterpolator();

		float inFromX = 0.0f;
		float inFromY = 0.0f;
		float inToX = 0.0f;
		float inToY = 0.0f;

		float outFromX = 0.0f;
		float outFromY = 0.0f;
		float outToX = 0.0f;
		float outToY = 0.0f;
		
		switch(position)
		{
		case TOP:
			inToY = 1.0f;
			outFromY = 1.0f;
			break;
			
		default:
		case BOTTOM:
			inFromY = 1.0f;
			outToY = -1.0f;
			break;
			
		case RIGHT:
			inFromX = 1.0f;
			outToX = -1.0f;
			break;
			
		case LEFT:
			inToX = 1.0f;
			outFromX = 1.0f;
			break;
		}
		
		if(comeInAnimation == null)
			comeInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, inFromX,
					 Animation.RELATIVE_TO_SELF, inToX,
					 Animation.RELATIVE_TO_SELF, inFromY,
					 Animation.RELATIVE_TO_SELF, inToY);
		
		if(goOutAnimation == null)
			goOutAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, outFromX,
					 Animation.RELATIVE_TO_SELF, outToX,
					 Animation.RELATIVE_TO_SELF, outFromY,
					 Animation.RELATIVE_TO_SELF, outToY);

		comeInAnimation.setInterpolator(interpolator);
		goOutAnimation.setInterpolator(interpolator);

		comeInAnimation.setDuration(animationDuration);
		goOutAnimation.setDuration(animationDuration);
	}
	
	public void open()
	{
		open(false);
	}
	
	public void close()
	{
		close(false);
	}
	
	public void animateOpen()
	{
		open(true);
	}
	
	public void animateClose()
	{
		close(true);
	}
	
	private void open(boolean animate)
	{
		setVisibility(View.VISIBLE);
		Log.d("myHome", "Open slider");
	
		if(animate)
		{
			moving = true;
			Log.d("myHome", "Start slider animation");
			startAnimation(comeInAnimation);
		}
		
		opened = true;
	}
	
	private void close(boolean animate)
	{
		if(animate)
		{
			closeAnimation = true;
			moving = true;
			startAnimation(goOutAnimation);
		}
		else
			setVisibility(View.GONE);
		
		opened = false;
	}

	public void setDoOnAnimationEnd(Runnable doOnAnimationEnd) {
		this.doOnAnimationEnd = doOnAnimationEnd;
	}

    @Override
    protected void onAnimationEnd()
    {
    	moving = false;
    	
    	if(closeAnimation)
    	{
    		setVisibility(View.GONE);
    		closeAnimation = false;
    	}
    	
    	if(doOnAnimationEnd != null)
    	{
    		doOnAnimationEnd.run();
    		doOnAnimationEnd = null;
    	}
    }

	public void setContent(View contentView) {
		this.contentView = contentView;
	}

	public View getContent() {
		return contentView;
	}

	public boolean isOpened() {
		return opened;
	}

	public boolean isMoving() {
		return moving;
	}
}
