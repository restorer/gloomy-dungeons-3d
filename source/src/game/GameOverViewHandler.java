package {$PKG_CURR};

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.graphics.*;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.KeyEvent;
import android.media.AudioManager;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;

public class GameOverViewHandler implements IViewHandler
{
	public void setView(Activity callerActivity)
	{
		callerActivity.setContentView(R.layout.game_over);
		Typeface btnTypeface = Typeface.createFromAsset(callerActivity.getAssets(), "fonts/" + callerActivity.getString(R.string.font_name));

		((TextView)callerActivity.findViewById(R.id.TxtGameOver)).setTypeface(btnTypeface);
		// ((Button)callerActivity.findViewById(R.id.BtnNewGame)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnLoadAutosave)).setTypeface(btnTypeface);

		// ((Button)callerActivity.findViewById(R.id.BtnNewGame)).setOnClickListener(new View.OnClickListener() {
		//	public void onClick(View v)
		//	{
		//		SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
		//		SoundManager.setPlaylist(SoundManager.LIST_MAIN);
		//		GameActivity.self.handler.post(GameActivity.self.showGameViewAndReInitialize);
		//	}
		// });

		((Button)callerActivity.findViewById(R.id.BtnLoadAutosave)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				SoundManager.setPlaylist(SoundManager.LIST_MAIN);
				GameActivity.self.handler.post(GameActivity.self.showGameViewAndLoadAutosave);
			}
		});
	}

	public void onResume()
	{
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
	}
}
