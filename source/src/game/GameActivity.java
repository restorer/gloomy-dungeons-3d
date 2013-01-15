package {$PKG_CURR};

import android.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.media.AudioManager;
import android.content.DialogInterface;
import android.widget.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// #if USE_ZEEMOTE
	import com.zeemote.zc.Configuration;
	import com.zeemote.zc.Controller;
	import com.zeemote.zc.event.BatteryEvent;
	import com.zeemote.zc.event.ButtonEvent;
	import com.zeemote.zc.event.ControllerEvent;
	import com.zeemote.zc.event.DisconnectEvent;
	import com.zeemote.zc.event.IButtonListener;
	import com.zeemote.zc.event.IJoystickListener;
	import com.zeemote.zc.event.IStatusListener;
	import com.zeemote.zc.event.JoystickEvent;
	import com.zeemote.zc.ui.android.ControllerAndroidUi;
// #end

import com.google.android.apps.analytics.easytracking.TrackedActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;

// #if USE_ZEEMOTE
public class GameActivity extends Activity implements IStatusListener, IJoystickListener, IButtonListener, SensorEventListener
// #end
// #if !USE_ZEEMOTE
public class GameActivity extends Activity implements SensorEventListener
// #end
{
	private static final int DIALOG_ENTER_CODE = 1;
	private static final int REQUEST_CODE_PREFERENCES = 1;

	private static final int VIEW_TYPE_GAME = 0;
	private static final int VIEW_TYPE_PRE_LEVEL = 1;
	private static final int VIEW_TYPE_END_LEVEL = 2;
	private static final int VIEW_TYPE_GAME_OVER = 3;
	private static final int VIEW_TYPE_LAST = 4;

	public static Context appContext;
	public static GameActivity self;

	// #if USE_ZEEMOTE
		public static Controller zeemoteController = null;
		public static ControllerAndroidUi zeemoteControllerUi = null;
	// #end

	public static boolean instantMusicPause = true;
	private static View codeDialogView;

	private IViewHandler[] viewHandlers = new IViewHandler[VIEW_TYPE_LAST];
	private IViewHandler currentViewHandler = null;
	private int currentViewType = -1;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	public final Handler handler = new Handler();

	public final Runnable showGameView = new Runnable() {
		public void run() {
			setViewByType(VIEW_TYPE_GAME);
		}
	};

	public final Runnable showGameViewAndReloadLevel = new Runnable() {
		public void run() {
			GameViewHandler.noClearRenderBlackScreenOnce = true;
			setViewByType(VIEW_TYPE_GAME);
			((GameViewHandler)currentViewHandler).game.loadLevel(Game.LOAD_LEVEL_RELOAD);
		}
	};

	public final Runnable showPreLevelView = new Runnable() {
		public void run() {
			setViewByType(VIEW_TYPE_PRE_LEVEL);
		}
	};

	public final Runnable showEndLevelView = new Runnable() {
		public void run() {
			setViewByType(VIEW_TYPE_END_LEVEL);
		}
	};

	public final Runnable showGameOverView = new Runnable() {
		public void run() {
			setViewByType(VIEW_TYPE_GAME_OVER);
		}
	};

	public final Runnable showGameViewAndReInitialize = new Runnable() {
		public void run() {
			MenuActivity.justLoaded = true;
			Game.savedGameParam = "";
			((GameViewHandler)viewHandlers[VIEW_TYPE_GAME]).game.initialize();

			GameViewHandler.noClearRenderBlackScreenOnce = true;
			setViewByType(VIEW_TYPE_GAME);
		}
	};

	public final Runnable showGameViewAndLoadAutosave = new Runnable() {
		public void run() {
			MenuActivity.justLoaded = true;
			Game.savedGameParam = Game.AUTOSAVE_NAME;
			((GameViewHandler)viewHandlers[VIEW_TYPE_GAME]).game.initialize();

			// GameViewHandler.noClearRenderBlackScreenOnce = true;
			setViewByType(VIEW_TYPE_GAME);
		}
	};

	@Override
	protected void onCreate(Bundle state)
	{
		super.onCreate(state);

		appContext = getApplicationContext();
		self = this;

		SoundManager.init(appContext, getAssets(), true);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		viewHandlers[VIEW_TYPE_GAME] = new GameViewHandler(getResources(), getAssets());
		viewHandlers[VIEW_TYPE_PRE_LEVEL] = new PreLevelViewHandler();
		viewHandlers[VIEW_TYPE_END_LEVEL] = new EndLevelViewHandler();
		viewHandlers[VIEW_TYPE_GAME_OVER] = new GameOverViewHandler();

		if (currentViewType < 0) {
			setViewByType(VIEW_TYPE_GAME);
		}
	}

	public void setViewByType(int viewType)
	{
		if (viewType == currentViewType) {
			return;
		}

		if (currentViewHandler != null) {
			currentViewHandler.onPause();
		}

		currentViewHandler = viewHandlers[viewType];
		currentViewHandler.setView(this);
		currentViewHandler.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_code:
				showDialog(DIALOG_ENTER_CODE);
				return true;

			case R.id.menu_options:
				startActivity(new Intent(GameActivity.this, GamePreferencesActivity.class));
				return true;

			case R.id.menu_menu:
				instantMusicPause = false;
				finish();
				return true;
		}

		return false;
	}

	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)	// android 2.0 introduce onBackPressed, but I want to support 1.6
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			instantMusicPause = false;
		}

		return super.onKeyDown(keyCode, event);
	}
	*/

	@Override
	public void onBackPressed()
	{
		instantMusicPause = false;
		super.onBackPressed();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		Config.initialize();
		Controls.fillMap();
		initJoystickVars();

		Controls.accelerometerX = 0.0f;
		Controls.accelerometerY = 0.0f;

		// #if USE_ZEEMOTE
		if (Config.controlsType == Controls.TYPE_ZEEMOTE)
		{
			if (zeemoteController == null)
			{
				zeemoteController = new Controller(Controller.CONTROLLER_1);
				zeemoteController.addStatusListener(this);
				zeemoteController.addButtonListener(this);
				zeemoteController.addJoystickListener(this);
			}

			if (zeemoteControllerUi == null) {
				zeemoteControllerUi = new ControllerAndroidUi(this, zeemoteController);
			}

			if (!zeemoteController.isConnected()) {
				zeemoteControllerUi.startConnectionProcess();
			}
		}
		// #end

		if (Config.accelerometerEnabled) {
			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		} else {
			sensorManager = null;
			accelerometer = null;
		}

		SoundManager.setPlaylist(SoundManager.LIST_MAIN);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (Config.accelerometerEnabled && (sensorManager != null)) {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (currentViewHandler != null) {
			currentViewHandler.onResume();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			SoundManager.onStart();

			if (currentViewHandler != null) {
				currentViewHandler.onWindowFocusChanged(hasFocus);
			}
		}
	}


	@Override
	protected void onPause()
	{
		super.onPause();

		if (currentViewHandler != null) {
			currentViewHandler.onPause();
		}

		if (Config.accelerometerEnabled && (sensorManager != null)) {
			sensorManager.unregisterListener(this);
		}

		SoundManager.onPause(instantMusicPause);
		instantMusicPause = true;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
			case DIALOG_ENTER_CODE:
			{
				codeDialogView = LayoutInflater.from(GameActivity.this).inflate(R.layout.code_dialog, null);

				return new AlertDialog.Builder(GameActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle(R.string.dlg_enter_code)
					.setView(codeDialogView)
					.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							EditText inp = (EditText)codeDialogView.findViewById(R.id.CodeText);

							if (currentViewHandler instanceof GameViewHandler) {
								((GameViewHandler)currentViewHandler).game.setGameCode(inp.getText().toString());
							}

							inp.setText("");
						}
					})
					.setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					})
					.create();
			}
		}

		return null;
	}

	protected void initJoystickVars()
	{
    	Controls.joyX = 0.0f;
    	Controls.joyY = 0.0f;
    	Controls.joyButtonsMask = 0;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	@Override
	public void onSensorChanged(SensorEvent e)
	{
		if (Config.accelerometerEnabled) {
			if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				Controls.accelerometerY = e.values[0] / SensorManager.GRAVITY_EARTH;
				Controls.accelerometerX = e.values[1] / SensorManager.GRAVITY_EARTH;
			}
		}
	}

	// #if USE_ZEEMOTE
    @Override
    public void batteryUpdate(BatteryEvent event)
    {
    }

    @Override
    public void connected(ControllerEvent event)
    {
    	initJoystickVars();
    }

    @Override
    public void disconnected(DisconnectEvent event)
    {
    	initJoystickVars();
    }

    @Override
    public void joystickMoved(JoystickEvent e)
    {
    	if (Config.controlsType == Controls.TYPE_ZEEMOTE) {
    		Controls.joyX = (float)(e.getScaledX(-100, 100)) / 150.0f * Config.zeemoteXAccel;
    		Controls.joyY = - (float)(e.getScaledY(-100, 100)) / 150.0f * Config.zeemoteYAccel;
    	}
    }

    @Override
    public void buttonPressed(ButtonEvent e)
    {
    	if (Config.controlsType == Controls.TYPE_ZEEMOTE)
    	{
    		int buttonId = e.getButtonGameAction();

    		if ((buttonId >= 0) && (buttonId < Config.zeemoteButtonMappings.length)) {
    			Controls.joyButtonsMask |= Config.zeemoteButtonMappings[buttonId];
    		}
    	}
    }

    @Override
    public void buttonReleased(ButtonEvent e)
    {
    	if (Config.controlsType == Controls.TYPE_ZEEMOTE)
    	{
    		int buttonId = e.getButtonGameAction();

    		if ((buttonId >= 0) && (buttonId < Config.zeemoteButtonMappings.length)) {
    			Controls.joyButtonsMask &= ~(Config.zeemoteButtonMappings[buttonId]);
    		}
    	}
    }
    // #end
}
