package zame.game.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import zame.game.Common;
import zame.game.GameActivity;
import zame.game.R;
import zame.game.SoundManager;
import zame.game.engine.Game;

public class EndLevelView extends zame.libs.FrameLayout implements IZameView {
    private GameActivity activity;
    private final Handler handler = new Handler();
    private TextView txtKills;
    private TextView txtItems;
    private TextView txtSecrets;
    private float currentKills;
    private float currentItems;
    private float currentSecrets;
    private float currentAdd;
    private boolean increaseValuesTaskActive;
    private TimerTask increaseValuesTask;

    private final Runnable updateValues = new Runnable() {
        @SuppressWarnings("MagicNumber")
        @Override
        public void run() {
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

    private void updateTxtValues() {
        txtKills.setText(String.format(activity.getString(R.string.endl_kills), (int)currentKills));
        txtItems.setText(String.format(activity.getString(R.string.endl_items), (int)currentItems));
        txtSecrets.setText(String.format(activity.getString(R.string.endl_secrets), (int)currentSecrets));
    }

    public EndLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (GameActivity)context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Common.setTypeface(this, new int[] { R.id.TxtKills, R.id.TxtItems, R.id.TxtSecrets, R.id.BtnNextLevel, });

        txtKills = (TextView)findViewById(R.id.TxtKills);
        txtItems = (TextView)findViewById(R.id.TxtItems);
        txtSecrets = (TextView)findViewById(R.id.TxtSecrets);

        findViewById(R.id.BtnNextLevel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

                currentKills = Game.endlTotalKills;
                currentItems = Game.endlTotalItems;
                updateTxtValues();

                SoundManager.setPlaylist(SoundManager.LIST_MAIN);
                GameActivity.changeView(R.layout.pre_level);
            }
        });

        currentKills = 0.0f;
        currentItems = 0.0f;
        currentSecrets = 0.0f;
        currentAdd = 1.0f;

        updateTxtValues();
        startTask();
    }

    private void startTask() {
        if (!increaseValuesTaskActive) {
            increaseValuesTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(updateValues);
                }
            };

            Timer increaseValuesTimer = new Timer();
            increaseValuesTaskActive = true;
            increaseValuesTimer.schedule(increaseValuesTask, 100, 100);
        }
    }

    private void stopTask() {
        if (increaseValuesTaskActive) {
            increaseValuesTaskActive = false;
            increaseValuesTask.cancel();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            startTask();
        } else {
            stopTask();
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
        stopTask();
    }
}
