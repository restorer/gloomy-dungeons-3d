package zame.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import zame.game.engine.Controls;
import zame.game.engine.Game;
import zame.game.views.GameView;
import zame.game.views.IZameView;

public class GameActivity extends BaseActivity implements SensorEventListener {
    private static final int DIALOG_ENTER_CODE = 1;

    public static final int ACTION_RELOAD_LEVEL = 1;
    public static final int ACTION_REINITIALIZE = 2;
    public static final int ACTION_LOAD_AUTOSAVE = 3;

    @SuppressLint("StaticFieldLeak") public static GameActivity self;

    private View currentView;
    private int currentLayoutResId;
    private final Handler handler = new Handler();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int deviceRotation;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean justAfterPause;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean soundAlreadyStopped; // fix multi-activity issues
    private GameActivityZeemoteHelper zeemoteHelper;

    public boolean instantMusicPause = true;
    public GameView.Data gameViewData;

    public static void doOpenOptionsMenu() {
        if (GameActivity.self != null) {
            GameActivity.self.handler.post(new Runnable() {
                @Override
                public void run() {
                    GameActivity.self.appOpenOptionsMenu(GameActivity.self.findViewById(R.id.MenuAnchorView));
                }
            });
        }
    }

    public static void changeView(int viewId) {
        changeView(viewId, 0);
    }

    public static void changeView(int viewId, int additionalAction) {
        if (GameActivity.self == null) {
            return;
        }

        final int _viewId = viewId;
        final int _additionalAction = additionalAction;

        GameActivity.self.handler.post(new Runnable() {
            @Override
            public void run() {
                switch (_additionalAction) {
                    case ACTION_RELOAD_LEVEL:
                        GameActivity.self.gameViewData.noClearRenderBlackScreenOnce = true;
                        break;

                    case ACTION_REINITIALIZE:
                        MenuActivity.justLoaded = true;
                        Game.savedGameParam = "";
                        GameActivity.self.gameViewData.game.initialize();
                        GameActivity.self.gameViewData.noClearRenderBlackScreenOnce = true;
                        break;

                    case ACTION_LOAD_AUTOSAVE:
                        MenuActivity.justLoaded = true;
                        Game.savedGameParam = Game.AUTOSAVE_NAME;
                        GameActivity.self.gameViewData.game.initialize();
                        // gameViewData.noClearRenderBlackScreenOnce = true;
                        break;
                }

                GameActivity.self.setZameView(_viewId);

                if (_additionalAction == ACTION_RELOAD_LEVEL) {
                    Game.loadLevel(Game.LOAD_LEVEL_RELOAD);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        self = this;
        zeemoteHelper = new GameActivityZeemoteHelper();

        SoundManager.init(getApplicationContext(), getAssets(), true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        gameViewData = new GameView.Data(getResources(), getAssets());

        currentView = null;
        currentLayoutResId = -1;
        setZameView(R.layout.game);
    }

    public void setZameView(int layoutResId) {
        if (currentLayoutResId == layoutResId) {
            return;
        }

        if (currentView instanceof IZameView) {
            ((IZameView)currentView).onPause();
        }

        currentLayoutResId = layoutResId;
        setContentView(layoutResId);
        currentView = findViewById(R.id.RootZameView);

        if (currentView instanceof IZameView) {
            ((IZameView)currentView).onResume();
        } else {
            Log.w(Common.LOG_KEY, "GameActivity.setZameView: non-IZameView view");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate((zeemoteHelper.getMenuResId() == 0) ? R.menu.game : zeemoteHelper.getMenuResId(), menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        zeemoteHelper.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_code:
                showDialog(DIALOG_ENTER_CODE);
                return true;

            case R.id.menu_options:
                startActivity(new Intent(this, GamePreferencesActivity.class));
                return true;

            case R.id.menu_menu:
                instantMusicPause = false;
                finish();
                return true;
        }

        return zeemoteHelper.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        instantMusicPause = false;
        super.onBackPressed();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onStart() {
        super.onStart();

        Config.initialize();
        zeemoteHelper.onStart(this);

        if (Config.accelerometerEnabled) {
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            assert sensorManager != null;

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            // documentation says that getOrientation() is deprecated, and we must use getRotation instead()
            // but getRotation() available only for >= 2.2
            // if we look for getRotation() into android sources, we found nice piece of code:
            // public int getRotation() { return getOrientation(); }
            // so it should be safe to use getOrientation() instead of getRotation()
            deviceRotation = getWindowManager().getDefaultDisplay().getOrientation();
        } else {
            sensorManager = null;
            accelerometer = null;
        }

        SoundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (!justAfterPause) {
                SoundManager.ensurePlaylist();
                SoundManager.onStart();
                soundAlreadyStopped = false;
            }
        } else {
            if (!soundAlreadyStopped) {
                SoundManager.onPause(instantMusicPause);
                soundAlreadyStopped = true;
            }

            instantMusicPause = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        justAfterPause = false;

        if (Config.accelerometerEnabled && (sensorManager != null)) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (currentView instanceof IZameView) {
            ((IZameView)currentView).onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        justAfterPause = true;
        zeemoteHelper.onPause();

        if (!soundAlreadyStopped) {
            SoundManager.onPause(instantMusicPause);
            soundAlreadyStopped = true;
        }

        instantMusicPause = true;

        if (currentView instanceof IZameView) {
            ((IZameView)currentView).onPause();
        }

        if (Config.accelerometerEnabled && (sensorManager != null)) {
            sensorManager.unregisterListener(this);
        }

        App.flushEvents();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ENTER_CODE: {
                @SuppressLint("InflateParams")
                final View codeDialogView = LayoutInflater.from(this).inflate(R.layout.code_dialog, null);

                return new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dlg_enter_code)
                        .setView(codeDialogView)
                        .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText inp = codeDialogView.findViewById(R.id.CodeText);
                                gameViewData.game.setGameCode(inp.getText().toString());
                                inp.setText("");
                            }
                        })
                        .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create();
            }
        }

        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float sensorX;
        float sensorY;

        if (Config.accelerometerEnabled && (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
            switch (deviceRotation) {
                case Surface.ROTATION_90:
                    sensorX = e.values[1];
                    sensorY = -e.values[0];
                    break;

                case Surface.ROTATION_180:
                    sensorX = -e.values[0];
                    sensorY = -e.values[1];
                    break;

                case Surface.ROTATION_270:
                    sensorX = -e.values[1];
                    sensorY = e.values[0];
                    break;

                default:
                    sensorX = e.values[0];
                    sensorY = e.values[1];
                    break;
            }

            Controls.accelerometerX = sensorX / SensorManager.GRAVITY_EARTH;
            Controls.accelerometerY = sensorY / SensorManager.GRAVITY_EARTH;

            if (Config.rotateScreen) {
                Controls.accelerometerX = -Controls.accelerometerX;
                Controls.accelerometerY = -Controls.accelerometerY;
            }
        }
    }
}
