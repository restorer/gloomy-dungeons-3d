package {$PKG_CURR};

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.util.Log;

import {$PKG_ROOT}.game.R;
import {$PKG_ROOT}.game.Config;

public class FrameLayout extends android.widget.FrameLayout
{
	public FrameLayout(Context context)
	{
		super(context);
		setupRotation();
	}

	public FrameLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setupRotation();
	}

	public FrameLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setupRotation();
	}

	protected void setupRotation()
	{
		if (Config.rotateScreen)
		{
			Animation rotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotation);
			LayoutAnimationController animController = new LayoutAnimationController(rotateAnim, 0);
			this.setLayoutAnimation(animController);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if (Config.rotateScreen)
		{
			event.setLocation(
				(float)(getWidth() - 1) - event.getX(),
				(float)(getHeight() - 1) - event.getY()
			);
		}

		return super.dispatchTouchEvent(event);
	}
}
