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
import android.widget.Toast;
import com.google.android.apps.analytics.easytracking.EasyTracker;

public class EndLevelViewHandler implements IViewHandler
{
	private Activity callerGameActivity;
	private Timer increaseValuesTimer;
	private final Handler handler = new Handler();
	private Resources resources;
	private TextView txtKills;
	private TextView txtItems;
	private TextView txtSecrets;
	private float currentKills;
	private float currentItems;
	private float currentSecrets;
	private float currentAdd;
	private boolean increaseValuesTaskActive = false;
	private TimerTask increaseValuesTask;

	private final Runnable updateValues = new Runnable()
	{
		public void run()
		{
			boolean shouldCancel = true;

			currentKills += currentAdd;
			currentItems += currentAdd;
			currentSecrets += currentAdd;

			if (currentKills >= Game.endlTotalKills) {
				currentKills = (float)Game.endlTotalKills;
			} else {
				shouldCancel = false;
			}

			if (currentItems >= Game.endlTotalItems) {
				currentItems = (float)Game.endlTotalItems;
			} else {
				shouldCancel = false;
			}

			if (currentSecrets >= Game.endlTotalSecrets) {
				currentSecrets = (float)Game.endlTotalSecrets;
			} else {
				shouldCancel = false;
			}

			currentAdd += 0.2f;
			updateTxtValues();

			if (shouldCancel) {
				if (increaseValuesTaskActive) {
					increaseValuesTaskActive = false;
					increaseValuesTask.cancel();
				}
			} else {
				SoundManager.playSound(SoundManager.SOUND_SHOOT_PIST);
			}
		}
	};

	private void updateTxtValues()
	{
		txtKills.setText(String.format(resources.getString(R.string.endl_kills), (int)currentKills));
		txtItems.setText(String.format(resources.getString(R.string.endl_items), (int)currentItems));
		txtSecrets.setText(String.format(resources.getString(R.string.endl_secrets), (int)currentSecrets));
	}

	public void setView(Activity callerActivity)
	{
		callerActivity.setContentView(R.layout.end_level);
		Typeface btnTypeface = Typeface.createFromAsset(callerActivity.getAssets(), "fonts/" + callerActivity.getString(R.string.font_name));

		((TextView)callerActivity.findViewById(R.id.TxtKills)).setTypeface(btnTypeface);
		((TextView)callerActivity.findViewById(R.id.TxtItems)).setTypeface(btnTypeface);
		((TextView)callerActivity.findViewById(R.id.TxtSecrets)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnNextLevel)).setTypeface(btnTypeface);

		resources = callerActivity.getResources();

		txtKills = (TextView)callerActivity.findViewById(R.id.TxtKills);
		txtItems = (TextView)callerActivity.findViewById(R.id.TxtItems);
		txtSecrets = (TextView)callerActivity.findViewById(R.id.TxtSecrets);

		((Button)callerActivity.findViewById(R.id.BtnNextLevel)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				currentKills = Game.endlTotalKills;
				currentItems = Game.endlTotalItems;
				updateTxtValues();

				SoundManager.setPlaylist(SoundManager.LIST_MAIN);
				GameActivity.self.handler.post(GameActivity.self.showPreLevelView);
			}
		});

		callerGameActivity = callerActivity;
	}

	public void onResume()
	{
		currentKills = 0.0f;
		currentItems = 0.0f;
		currentSecrets = 0.0f;
		currentAdd = 1.0f;

		updateTxtValues();

		// there is also handler.postDelayed and handler.postAtTime (don't forget about handler.removeCallbacks in such case)

		increaseValuesTask = new TimerTask() {
			public void run() {
				handler.post(updateValues);
			}
		};

		increaseValuesTimer = new Timer();
		increaseValuesTaskActive = true;
		increaseValuesTimer.schedule(increaseValuesTask, 100, 100);
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
		if (increaseValuesTaskActive) {
			increaseValuesTaskActive = false;
			increaseValuesTask.cancel();
		}
	}
}
