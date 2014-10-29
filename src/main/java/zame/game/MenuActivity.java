package zame.game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import zame.game.engine.Game;
import zame.game.views.MenuView;

public class MenuActivity extends Activity
{
	public static boolean justLoaded = false;	// or just saved, or just new game started
	public static MenuActivity self;

	private boolean justAfterPause = false;
	private boolean soundAlreadyStopped = false; // fix multi-activity issues
	private Dialog dialogToShow = null;

	public boolean instantMusicPause = true;
	public MenuView.Data menuViewData = new MenuView.Data();

	@Override
	protected void onCreate(Bundle state)
	{
		super.onCreate(state);
		self = this;

		SoundManager.init(getApplicationContext(), getAssets(), true);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		Game.initPaths(getApplicationContext());
		setContentView(R.layout.menu);

		MenuView.onActivityCreate(this);
		ZameApplication.trackPageView("/menu");
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// moved from onResume, because onResume called even when app is not visible, but lock screen is visible
			if (!justAfterPause) {
				SoundManager.setPlaylist(SoundManager.LIST_MAIN);
				SoundManager.onStart();
				soundAlreadyStopped = false;
			}
		} else {
			// moved from onPause, because onPause is not called when task manager is on screen
			if (!soundAlreadyStopped) {
				SoundManager.onPause(instantMusicPause);
				soundAlreadyStopped = true;
			}

			instantMusicPause = true;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		justAfterPause = false;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		justAfterPause = true;

		if (!soundAlreadyStopped) {
			SoundManager.onPause(instantMusicPause);
			soundAlreadyStopped = true;
		}

		instantMusicPause = true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		self = null;
	}

	@Override
	public void onBackPressed()
	{
		if (MenuActivityHelper.onBackPressed(this)) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_options:
				startActivity(new Intent(MenuActivity.this, GamePreferencesActivity.class));
				return true;
		}

		return MenuView.onOptionsItemSelected(this, item);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id)
	{
		return MenuView.onCreateDialog(this, id);
	}
}
