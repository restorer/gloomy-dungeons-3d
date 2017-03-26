package zame.game.views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import zame.game.Common;
import zame.game.GameActivity;
import zame.game.R;
import zame.game.SoundManager;
import zame.game.engine.State;

public class PreLevelView extends zame.libs.FrameLayout implements IZameView {
    private GameActivity activity;
    private final Handler handler = new Handler();
    private ScrollView scrollWrap;
    private TextView txtText;
    private String preLevelText;
    private int currentLength;
    private int currentHeight;
    private boolean showMoreTextTaskActive;
    private TimerTask showMoreTextTask;

    private final Runnable updateText = new Runnable() {
        @Override
        public void run() {
            currentLength += 3;

            if (currentLength >= preLevelText.length()) {
                currentLength = preLevelText.length();

                if (showMoreTextTaskActive) {
                    showMoreTextTaskActive = false;
                    showMoreTextTask.cancel();
                }
            }

            updateTextValue(!showMoreTextTaskActive);
        }
    };

    private void updateTextValue(boolean ensureScroll) {
        txtText.setText(preLevelText.substring(0, currentLength));
        int newHeight = txtText.getHeight();

        if ((newHeight > currentHeight) || ensureScroll) {
            currentHeight = newHeight;

            scrollWrap.post(new Runnable() {
                @Override
                public void run() {
                    scrollWrap.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    public PreLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (GameActivity)context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Common.setTypeface(this, new int[] { R.id.TxtText, R.id.BtnStartLevel, });

        scrollWrap = (ScrollView)findViewById(R.id.ScrollWrap);
        txtText = (TextView)findViewById(R.id.TxtText);

        String imgId;

        try {
            InputStreamReader isr = new InputStreamReader(Common.openLocalizedAsset(activity.getAssets(),
                    String.format(Locale.US, "prelevel%%s/level-%d.txt", State.levelNum)), "UTF-8");

            BufferedReader br = new BufferedReader(isr);

            imgId = br.readLine();
            StringBuilder sb = new StringBuilder();
            boolean appendNewline = false;

            for (; ; ) {
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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        ImageView imgImage = (ImageView)findViewById(R.id.ImgImage);
        imgImage.setVisibility(View.VISIBLE);

        if ("ep_1".equals(imgId)) {
            imgImage.setImageResource(R.drawable.pre_ep_1);
        } else if ("ep_2".equals(imgId)) {
            imgImage.setImageResource(R.drawable.pre_ep_2);
        } else if ("ep_3".equals(imgId)) {
            imgImage.setImageResource(R.drawable.pre_ep_3);
        } else if ("ep_4".equals(imgId)) {
            imgImage.setImageResource(R.drawable.pre_ep_4);
        } else if ("ep_5".equals(imgId)) {
            imgImage.setImageResource(R.drawable.pre_ep_5);
        } else {
            imgImage.setVisibility(View.GONE);
        }

        findViewById(R.id.BtnStartLevel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

                currentLength = preLevelText.length();
                updateTextValue(true);

                GameActivity.changeView(R.layout.game);
            }
        });
    }

    @Override
    public void onResume() {
        currentHeight = 0;
        currentLength = 1;
        updateTextValue(false);

        // there is also handler.postDelayed and handler.postAtTime (don't forget about handler.removeCallbacks in such case)

        showMoreTextTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(updateText);
            }
        };

        Timer showMoreTextTimer = new Timer();
        showMoreTextTaskActive = true;

        //noinspection MagicNumber
        showMoreTextTimer.schedule(showMoreTextTask, 30, 30);
    }

    @Override
    public void onPause() {
        if (showMoreTextTaskActive) {
            showMoreTextTaskActive = false;
            showMoreTextTask.cancel();
        }
    }
}
