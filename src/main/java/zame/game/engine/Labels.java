package zame.game.engine;

import android.graphics.Paint;
import android.graphics.Typeface;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Config;
import zame.game.R;
import zame.game.ZameApplication;
import zame.libs.LabelMaker;
import zame.libs.NumericSprite;

@SuppressWarnings("WeakerAccess")
public final class Labels {
    public static final int LABEL_FPS = 1;
    public static final int LABEL_CANT_OPEN = 2;
    public static final int LABEL_NEED_BLUE_KEY = 3;
    public static final int LABEL_NEED_RED_KEY = 4;
    public static final int LABEL_NEED_GREEN_KEY = 5;
    public static final int LABEL_SECRET_FOUND = 6;
    public static final int LABEL_LAST = 7;

    @SuppressWarnings("unused") public static final int MSG_PRESS_FORWARD = 1;
    public static final int MSG_PRESS_ROTATE = 2;
    @SuppressWarnings("unused") public static final int MSG_PRESS_ACTION_TO_OPEN_DOOR = 3;
    @SuppressWarnings("unused") public static final int MSG_SWITCH_AT_RIGHT = 4;
    @SuppressWarnings("unused") public static final int MSG_PRESS_ACTION_TO_SWITCH = 5;
    @SuppressWarnings("unused") public static final int MSG_KEY_AT_LEFT = 6;
    @SuppressWarnings("unused") public static final int MSG_PRESS_ACTION_TO_FIGHT = 7;
    @SuppressWarnings("unused") public static final int MSG_PRESS_MAP = 8;
    @SuppressWarnings("unused") public static final int MSG_PRESS_NEXT_WEAPON = 9;
    @SuppressWarnings("unused") public static final int MSG_OPEN_DOOR_USING_KEY = 10;
    @SuppressWarnings("unused") public static final int MSG_PRESS_END_LEVEL_SWITCH = 11;
    @SuppressWarnings("unused") public static final int MSG_GO_TO_DOOR = 12;
    public static final int MSG_LAST = 13;

    public static int[] map = new int[LABEL_LAST];

    public static final int[] MSG_MAP = { 0, R.string.lblm_press_forward, // MSG_PRESS_FORWARD
            R.string.lblm_press_rotate, // MSG_PRESS_ROTATE
            R.string.lblm_press_action_to_open_door, // MSG_PRESS_ACTION_TO_OPEN_DOOR
            R.string.lblm_switch_at_right, // MSG_SWITCH_AT_RIGHT
            R.string.lblm_press_action_to_switch, // MSG_PRESS_ACTION_TO_SWITCH
            R.string.lblm_key_at_left, // MSG_KEY_AT_LEFT
            R.string.lblm_press_action_to_fight, // MSG_PRESS_ACTION_TO_FIGHT
            R.string.lblm_press_map, // MSG_PRESS_MAP
            R.string.lblm_press_next_weapon, // MSG_PRESS_NEXT_WEAPON
            R.string.lblm_open_door_using_key, // MSG_OPEN_DOOR_USING_KEY
            R.string.lblm_press_end_level_switch, // MSG_PRESS_END_LEVEL_SWITCH
            R.string.lblm_go_to_door, // MSG_GO_TO_DOOR
    };

    public static volatile LabelMaker maker;
    public static volatile LabelMaker msgMaker;
    public static volatile NumericSprite numeric;
    public static volatile NumericSprite statsNumeric;

    private static Paint labelPaint;
    private static Paint msgPaint;
    private static Paint statsPaint;
    private static int currentMessageId;
    private static int currentMessageLabelId;
    private static String currentMessageString;

    private Labels() {
    }

    @SuppressWarnings("MagicNumber")
    public static void init() {
        Typeface labelTypeface = Typeface.createFromAsset(Game.assetManager,
                "fonts/" + ZameApplication.self.getString(R.string.font_name));

        labelPaint = new Paint();
        labelPaint.setTypeface(labelTypeface);
        labelPaint.setAntiAlias(true);
        labelPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);

        msgPaint = new Paint();
        msgPaint.setTypeface(labelTypeface);
        msgPaint.setAntiAlias(true);
        msgPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);

        statsPaint = new Paint();
        statsPaint.setTypeface(labelTypeface);
        statsPaint.setAntiAlias(true);
        statsPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
    }

    private static int getInt(int resId) {
        return Integer.parseInt(ZameApplication.self.getString(resId));
    }

    @SuppressWarnings("MagicNumber")
    public static void surfaceSizeChanged(int width) {
        labelPaint.setTextSize(getInt((width < 480)
                ? R.string.font_lbl_size_sm
                : ((width < 800) ? R.string.font_lbl_size_md : R.string.font_lbl_size_lg)));

        msgPaint.setTextSize(getInt((width < 480)
                ? R.string.font_msg_size_sm
                : ((width < 800) ? R.string.font_msg_size_md : R.string.font_msg_size_lg)));

        statsPaint.setTextSize(getInt((width < 480)
                ? R.string.font_stats_size_sm
                : ((width < 800) ? R.string.font_stats_size_md : R.string.font_stats_size_lg)));
    }

    public static int getMessageLabelId(GL10 gl, int messageId) {
        if (currentMessageId == messageId) {
            return currentMessageLabelId;
        }

        String message;

        if (((Config.controlsType == Controls.TYPE_EXPERIMENTAL_A) || (Config.controlsType
                == Controls.TYPE_EXPERIMENTAL_B)) && (messageId == MSG_PRESS_ROTATE)) {

            message = ZameApplication.self.getString(R.string.lblm_slide_rotate);
        } else if ((messageId > 0) && (messageId < MSG_LAST)) {
            message = ZameApplication.self.getString(MSG_MAP[messageId]);
        } else {
            message = String.format(Locale.US, "[message #%d]", messageId);
        }

        msgMaker.beginAdding(gl);
        currentMessageLabelId = msgMaker.add(gl, message, msgPaint);
        msgMaker.endAdding(gl);

        currentMessageId = messageId;
        currentMessageString = "";

        return currentMessageLabelId;
    }

    @SuppressWarnings("unused")
    public static int getMessageLabelIdForString(GL10 gl, String message) {
        if (currentMessageString.equals(message)) {
            return currentMessageLabelId;
        }

        msgMaker.beginAdding(gl);
        currentMessageLabelId = msgMaker.add(gl, message, labelPaint);
        msgMaker.endAdding(gl);

        currentMessageId = 0;
        currentMessageString = message;

        return currentMessageLabelId;
    }

    @SuppressWarnings("MagicNumber")
    public static void createLabels(GL10 gl) {
        if (maker == null) {
            maker = new LabelMaker(true, 512, 256);
        } else {
            maker.shutdown(gl);
        }

        maker.initialize(gl);
        maker.beginAdding(gl);
        map[LABEL_FPS] = maker.add(gl, ZameApplication.self.getString(R.string.lbl_fps), labelPaint);
        map[LABEL_CANT_OPEN] = maker.add(gl, ZameApplication.self.getString(R.string.lbl_cant_open_door), labelPaint);

        map[LABEL_NEED_BLUE_KEY] = maker.add(gl,
                ZameApplication.self.getString(R.string.lbl_need_blue_key),
                labelPaint);

        map[LABEL_NEED_RED_KEY] = maker.add(gl, ZameApplication.self.getString(R.string.lbl_need_red_key), labelPaint);

        map[LABEL_NEED_GREEN_KEY] = maker.add(gl,
                ZameApplication.self.getString(R.string.lbl_need_green_key),
                labelPaint);

        map[LABEL_SECRET_FOUND] = maker.add(gl, ZameApplication.self.getString(R.string.lbl_secret_found), labelPaint);
        maker.endAdding(gl);

        if (msgMaker == null) {
            msgMaker = new LabelMaker(true, 1024, 64);
        } else {
            msgMaker.shutdown(gl);
        }

        msgMaker.initialize(gl);
        currentMessageId = 0;
        currentMessageString = "";

        if (numeric == null) {
            numeric = new NumericSprite();
        } else {
            numeric.shutdown(gl);
        }

        numeric.initialize(gl, labelPaint);

        if (statsNumeric == null) {
            statsNumeric = new NumericSprite();
        } else {
            statsNumeric.shutdown(gl);
        }

        statsNumeric.initialize(gl, statsPaint);
    }
}
