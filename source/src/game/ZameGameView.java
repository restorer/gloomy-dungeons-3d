package {$PKG_CURR};

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.util.Log;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;

public class ZameGameView extends GLSurfaceView
{
	private ZameGame game;

	public ZameGameView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setFocusable(true);
		requestFocus();
		setFocusableInTouchMode(true);
	}

	public void setGame(ZameGame game)
	{
		this.game = game;
		setRenderer(game);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		if (game != null)
		{
			if (!hasWindowFocus) {
				game.pause();
			} else {
				game.resume();
			}
		}
	}

	public static boolean canUseKey(int keyCode)
	{
		return (
			(keyCode != KeyEvent.KEYCODE_BACK) &&
			(keyCode != KeyEvent.KEYCODE_HOME) &&
			(keyCode != KeyEvent.KEYCODE_MENU) &&
			(keyCode != KeyEvent.KEYCODE_ENDCALL)
		);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (canUseKey(keyCode) && (game != null)) {
			if (game.handleKeyDown(keyCode)) {
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (canUseKey(keyCode) && (game != null)) {
			if (game.handleKeyUp(keyCode)) {
				return true;
			}
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (game != null) {
			game.handleTouchEvent(event);
		}

		try {
			Thread.sleep(16);	// was: 16
		} catch (InterruptedException e) {}

		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event)
	{
		if (game != null) {
			game.handleTrackballEvent(event);
		}

		return true;
	}
}
