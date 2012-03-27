package {$PKG_CURR};

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.AssetManager;

public class GameViewHandler implements IViewHandler
{
	private ZameGameView view;
	public Game game;

	public static boolean noClearRenderBlackScreenOnce;

	public GameViewHandler(Resources res, AssetManager assets)
	{
		noClearRenderBlackScreenOnce = false;
		game = new Game(res, assets);
	}

	public void setView(Activity callerActivity)
	{
		callerActivity.setContentView(R.layout.game);
		view = (ZameGameView)callerActivity.findViewById(R.id.zameGameView);

		view.setGame(game);
		game.setView(view);
	}

	public void onResume()
	{
		if (noClearRenderBlackScreenOnce) {
			noClearRenderBlackScreenOnce = false;
		} else {
			Game.renderBlackScreen = false;
		}

		SoundManager.setPlaylist(SoundManager.LIST_MAIN);

		game.callResumeAfterSurfaceCreated = true;
		view.onResume();
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
		// gl renderer paused first, game paused second, because
		// at 2012-03-10 I think that it can fix "eglSwapBuffers failed" erorrs in android 2.2 and later

		view.onPause();
		game.pause();
	}
}
