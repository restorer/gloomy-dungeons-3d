package zame.game.views;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import zame.game.GameActivity;
import zame.game.R;
import zame.game.SoundManager;
import zame.game.engine.Controls;
import zame.game.engine.Game;

public class GameView extends FrameLayout implements IZameView
{
	public static class Data
	{
		public Game game;
		public boolean noClearRenderBlackScreenOnce;

		public Data(Resources resources, AssetManager assets)
		{
			game = new Game(resources, assets);
			noClearRenderBlackScreenOnce = false;
		}
	}

	private GameActivity activity;
	private Data data;
	private ZameGameView view;

	public GameView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		activity = (GameActivity)context;
		data = activity.gameViewData;
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		view = (ZameGameView)findViewById(R.id.ZameGameView);

		view.setGame(data.game);
		data.game.setView(view);
	}

	public void onResume()
	{
		if (data.noClearRenderBlackScreenOnce) {
			data.noClearRenderBlackScreenOnce = false;
		} else {
			Game.renderBlackScreen = false;
		}

		SoundManager.setPlaylist(SoundManager.LIST_MAIN);
		Controls.fillMap();

		Game.callResumeAfterSurfaceCreated = true;
		view.onResume();
	}

	public void onPause()
	{
		view.onPause();
		data.game.pause();
	}
}
