package zame.game.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import zame.game.Common;
import zame.game.GameActivity;
import zame.game.R;
import zame.game.SoundManager;

public class GameOverView extends zame.libs.FrameLayout implements IZameView
{
	public GameOverView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		Common.setTypeface(this, new int[] {
			R.id.TxtGameOver,
			R.id.BtnLoadAutosave,
		});

		((Button)findViewById(R.id.BtnLoadAutosave)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				SoundManager.setPlaylist(SoundManager.LIST_MAIN);
				GameActivity.changeView(R.layout.game, GameActivity.ACTION_LOAD_AUTOSAVE);
			}
		});
	}

	public void onResume()
	{
	}

	public void onPause()
	{
	}
}
