package {$PKG_CURR};

import android.os.Bundle;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.app.Activity;
import android.graphics.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.view.View;
import android.view.KeyEvent;
import android.media.AudioManager;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;
import java.io.*;

public class PreLevelViewHandler implements IViewHandler
{
	private Timer showMoreTextTimer;
	private final Handler handler = new Handler();
	private ScrollView scrollWrap;
	private TextView txtText;
	private String preLevelText;
	private int currentLength;
	private int currentHeight;
	private boolean showMoreTextTaskActive = false;
	private TimerTask showMoreTextTask;

	private final Runnable updateText = new Runnable()
	{
		public void run()
		{
			currentLength += 3;

			if (currentLength >= preLevelText.length())
			{
				currentLength = preLevelText.length();

				if (showMoreTextTaskActive) {
					showMoreTextTaskActive = false;
					showMoreTextTask.cancel();
				}
			}

			updateTextValue(!showMoreTextTaskActive);
		}
	};

	private void updateTextValue(boolean ensureScroll)
	{
		txtText.setText(preLevelText.substring(0, currentLength));
		int newHeight = txtText.getHeight();

		if (newHeight > currentHeight || ensureScroll)
		{
			currentHeight = newHeight;

			scrollWrap.post(new Runnable() {
				public void run() {
					scrollWrap.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}

	public void setView(Activity callerActivity)
	{
		callerActivity.setContentView(R.layout.pre_level);
		Typeface btnTypeface = Typeface.createFromAsset(callerActivity.getAssets(), "fonts/" + callerActivity.getString(R.string.font_name));

		scrollWrap = (ScrollView)callerActivity.findViewById(R.id.ScrollWrap);

		txtText = (TextView)callerActivity.findViewById(R.id.TxtText);
		txtText.setTypeface(btnTypeface);

		Button btnStartLevel = (Button)callerActivity.findViewById(R.id.BtnStartLevel);
		btnStartLevel.setTypeface(btnTypeface);

		Button btnSkipTutorial = (Button)callerActivity.findViewById(R.id.BtnSkipTutorial);
		btnSkipTutorial.setTypeface(btnTypeface);
		btnSkipTutorial.setVisibility(State.levelNum < Level.FIRST_REAL_LEVEL ? View.VISIBLE : View.GONE);

		String imgId = "";

		try
		{
			InputStreamReader isr = new InputStreamReader(
				Common.openLocalizedAsset(
					callerActivity.getAssets(),
					"prelevel%s/level-" + String.valueOf(State.levelNum) + ".txt"
				),
				"UTF-8"
			);

			BufferedReader br = new BufferedReader(isr);

			imgId = br.readLine();
			StringBuffer sb = new StringBuffer();
			boolean appendNewline = false;

			for (;;)
			{
				String line = br.readLine();

				if (line == null) {
					break;
				}

				if (appendNewline) {
					sb.append("\n");
				}

				sb.append(line);
				appendNewline = true;
			}

			preLevelText = sb.toString();
			br.close();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}

		ImageView imgImage = (ImageView)callerActivity.findViewById(R.id.ImgImage);
		imgImage.setVisibility(View.VISIBLE);

		if (imgId.equals("controls_move")) {
			imgImage.setImageResource(R.drawable.pre_controls_move);
		} else if (imgId.equals("controls_rotate")) {
			imgImage.setImageResource(R.drawable.pre_controls_rotate);
		} else if (imgId.equals("controls_action")) {
			imgImage.setImageResource(R.drawable.pre_controls_action);
		} else if (imgId.equals("controls_next_weapon")) {
			imgImage.setImageResource(R.drawable.pre_controls_next_weapon);
		} else if (imgId.equals("blue_key")) {
			imgImage.setImageResource(R.drawable.pre_blue_key);
		} else if (imgId.equals("switch")) {
			imgImage.setImageResource(R.drawable.pre_switch);
		} else if (imgId.equals("endl_switch")) {
			imgImage.setImageResource(R.drawable.pre_endl_switch);
		} else if (imgId.equals("ep_1")) {
			imgImage.setImageResource(R.drawable.pre_ep_1);
		} else if (imgId.equals("ep_2")) {
			imgImage.setImageResource(R.drawable.pre_ep_2);
		} else if (imgId.equals("ep_3")) {
			imgImage.setImageResource(R.drawable.pre_ep_3);
		} else if (imgId.equals("ep_4")) {
			imgImage.setImageResource(R.drawable.pre_ep_4);
		} else if (imgId.equals("ep_5")) {
			imgImage.setImageResource(R.drawable.pre_ep_5);
		} else {
			imgImage.setVisibility(View.GONE);
		}

		btnStartLevel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				currentLength = preLevelText.length();
				updateTextValue(true);

				GameActivity.self.handler.post(GameActivity.self.showGameView);
			}
		});

		btnSkipTutorial.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				State.levelNum = Level.FIRST_REAL_LEVEL;
				State.heroHealth = 100;
				State.reInitPistol();
				State.heroWeapon = Weapons.WEAPON_PISTOL;

				Weapons.updateWeapon();
				GameActivity.self.handler.post(GameActivity.self.showGameViewAndReloadLevel);
			}
		});
	}

	public void onResume()
	{
		currentHeight = 0;
		currentLength = 1;
		updateTextValue(false);

		// there is also handler.postDelayed and handler.postAtTime (don't forget about handler.removeCallbacks in such case)

		showMoreTextTask = new TimerTask() {
			public void run() {
				handler.post(updateText);
			}
		};

		showMoreTextTimer = new Timer();
		showMoreTextTaskActive = true;
		showMoreTextTimer.schedule(showMoreTextTask, 30, 30);
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
	}

	public void onPause()
	{
		if (showMoreTextTaskActive) {
			showMoreTextTaskActive = false;
			showMoreTextTask.cancel();
		}
	}
}
