package {$PKG_CURR};

import android.app.Activity;

// #if !USE_EOD_BLOCKER

public class EodBlockerViewHandler implements IViewHandler
{
	public void setView(Activity callerActivity) {}
	public void onResume() {}
	public void onWindowFocusChanged(boolean hasFocus) {}
	public void onPause() {}
}

// #end
// #if USE_EOD_BLOCKER

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
import android.content.Intent;
import com.google.android.apps.analytics.easytracking.EasyTracker;

// #if TYPE_DEMO
	import android.net.Uri;
// #end

public class EodBlockerViewHandler implements IViewHandler
{
	private Activity callerGameActivity;
	private Resources resources;

	public void setView(Activity callerActivity)
	{
		callerGameActivity = callerActivity;
		resources = callerActivity.getResources();

		callerActivity.setContentView(R.layout.eod_blocker);
		Typeface btnTypeface = Typeface.createFromAsset(callerActivity.getAssets(), "fonts/" + callerActivity.getString(R.string.font_name));

		((Button)callerActivity.findViewById(R.id.BtnOk)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnCancel)).setTypeface(btnTypeface);
		((TextView)callerActivity.findViewById(R.id.TxtInfo)).setTypeface(btnTypeface);

		((Button)callerActivity.findViewById(R.id.BtnCancel)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				callerGameActivity.finish();
			}
		});

		((Button)callerActivity.findViewById(R.id.BtnOk)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				// #if TYPE_DEMO
					try {
						callerGameActivity.startActivity((
							new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=zame.GloomyDungeons.full.game"))
						).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));

						EasyTracker.getTracker().trackPageView("/end-of-demo/full-version");
					} catch (Exception ex) {
						Log.e(Common.LOG_KEY, "Exception", ex);
						Toast.makeText(GameActivity.appContext, "Could not launch the market application.", Toast.LENGTH_LONG).show();
					}
				// #end
				// #if TYPE_IFREE
					GameActivity.instantMusicPause = false;
					callerGameActivity.startActivity(new Intent(callerGameActivity, IfreePayActivity.class));
				// #end
			}
		});

		EasyTracker.getTracker().trackPageView("/end-of-demo");
	}

	public void onResume()
	{
		// #if TYPE_IFREE
			if (Config.charged) {
				GameActivity.self.handler.post(GameActivity.self.showGameView);
			}
		// #end
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
	}
}

// #end
