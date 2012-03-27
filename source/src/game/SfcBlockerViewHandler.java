package {$PKG_CURR};

import android.app.Activity;

// #if !TYPE_SFC

public class SfcBlockerViewHandler implements IViewHandler
{
	public void setView(Activity callerActivity) {}
	public void onResume() {}
	public void onWindowFocusChanged(boolean hasFocus) {}
	public void onPause() {}
}

// #end
// #if TYPE_SFC

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

public class SfcBlockerViewHandler implements IViewHandler
{
	private Activity callerGameActivity;
	private Resources resources;
	private boolean wasSuccessfull;

	public static SfcBlockerViewHandler self;
	public final Handler handler = new Handler();

	public final Runnable paymentSuccessfull = new Runnable() {
		public void run() {
			wasSuccessfull = true;
			showResult(resources.getString(R.string.sfc_payment_successfull));
		}
	};

	public final Runnable paymentCancelled = new Runnable() {
		public void run() {
			wasSuccessfull = false;
			showResult(resources.getString(R.string.sfc_payment_cancelled));
		}
	};

	public void setView(Activity callerActivity)
	{
		self = this;
		callerGameActivity = callerActivity;
		resources = callerActivity.getResources();

		callerActivity.setContentView(R.layout.sfc_blocker);
		Typeface btnTypeface = Typeface.createFromAsset(callerActivity.getAssets(), "fonts/" + callerActivity.getString(R.string.font_name));

		((Button)callerActivity.findViewById(R.id.BtnOk)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnCancel)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnClose)).setTypeface(btnTypeface);
		((TextView)callerActivity.findViewById(R.id.TxtInfo)).setTypeface(btnTypeface);
		((Button)callerActivity.findViewById(R.id.BtnClose)).setVisibility(View.GONE);

		((Button)callerActivity.findViewById(R.id.BtnCancel)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				callerGameActivity.finish();
			}
		});

		wasSuccessfull = false;

		((Button)callerActivity.findViewById(R.id.BtnClose)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				if (wasSuccessfull) {
					GameActivity.self.handler.post(GameActivity.self.showGameView);
				} else {
					callerGameActivity.finish();
				}
			}
		});

		((TextView)callerActivity.findViewById(R.id.TxtInfo)).setText(resources.getString(R.string.sfc_info));

		((Button)callerActivity.findViewById(R.id.BtnOk)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				Toast.makeText(callerGameActivity, resources.getString(R.string.sfc_please_wait), Toast.LENGTH_LONG).show();
				((Button)callerGameActivity.findViewById(R.id.BtnOk)).setEnabled(false);
				((Button)callerGameActivity.findViewById(R.id.BtnCancel)).setEnabled(false);

				try {
					ZameApplication.sfcEngine.sendSMS();
				} catch (Exception ex) {
					Log.e(Common.LOG_KEY, "Exception: " + ex, ex);
					showError(resources.getString(R.string.sfc_payment_cancelled));
				}
			}
		});

		EasyTracker.getTracker().trackPageView("/pay");
	}

	protected void showError(String errorText)
	{
		((TextView)callerGameActivity.findViewById(R.id.TxtInfo)).setText(errorText);
		((Button)callerGameActivity.findViewById(R.id.BtnOk)).setEnabled(false);
		((Button)callerGameActivity.findViewById(R.id.BtnCancel)).setEnabled(true);
	}

	protected void showResult(String resultText)
	{
		((TextView)callerGameActivity.findViewById(R.id.TxtInfo)).setText(resultText);
		((Button)callerGameActivity.findViewById(R.id.BtnOk)).setVisibility(View.GONE);
		((Button)callerGameActivity.findViewById(R.id.BtnCancel)).setVisibility(View.GONE);
		((Button)callerGameActivity.findViewById(R.id.BtnClose)).setVisibility(View.VISIBLE);
	}

	public void onResume()
	{
		self = this;
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
		self = null;
	}
}

// #end
