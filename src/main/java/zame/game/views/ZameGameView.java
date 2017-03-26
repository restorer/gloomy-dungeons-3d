package zame.game.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import zame.game.ZameGame;

public class ZameGameView extends zame.libs.GLSurfaceView21 {
    private ZameGame game;

    public ZameGameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(true);
        requestFocus();
        setFocusableInTouchMode(true);
    }

    public void setGame(ZameGame game) {
        this.game = game;
        setRenderer(game);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (game != null) {
            if (hasWindowFocus) {
                game.resume();
            } else {
                game.pause();
            }
        }
    }

    public static boolean canUseKey(int keyCode) {
        return ((keyCode != KeyEvent.KEYCODE_BACK) && (keyCode != KeyEvent.KEYCODE_HOME) && (keyCode
                != KeyEvent.KEYCODE_MENU) && (keyCode != KeyEvent.KEYCODE_ENDCALL));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //noinspection SimplifiableIfStatement
        if (canUseKey(keyCode) && (game != null) && game.handleKeyDown(keyCode)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //noinspection SimplifiableIfStatement
        if (canUseKey(keyCode) && (game != null) && game.handleKeyUp(keyCode)) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (game != null) {
            game.handleTouchEvent(event);
        }

        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            // ignored
        }

        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (game != null) {
            game.handleTrackballEvent(event);
        }

        return true;
    }
}
