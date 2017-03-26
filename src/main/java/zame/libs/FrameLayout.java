package zame.libs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import zame.game.Config;
import zame.game.R;

public class FrameLayout extends android.widget.FrameLayout {
    public FrameLayout(Context context) {
        super(context);
    }

    public FrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getContext(),
                    Config.rotateScreen ? R.anim.rotation : R.anim.no_rotation), 0));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (Config.rotateScreen) {
            event.setLocation((float)(getWidth() - 1) - event.getX(), (float)(getHeight() - 1) - event.getY());
        }

        return super.dispatchTouchEvent(event);
    }
}
