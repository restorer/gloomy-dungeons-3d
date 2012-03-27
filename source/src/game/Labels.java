package {$PKG_CURR};

import {$PKG_ROOT}.libs.*;
import android.graphics.*;
import android.text.TextPaint;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Labels
{
	public static final int LABEL_FPS = 1;
	public static final int LABEL_CANT_OPEN = 2;
	public static final int LABEL_NEED_BLUE_KEY = 3;
	public static final int LABEL_NEED_RED_KEY = 4;
	public static final int LABEL_NEED_GREEN_KEY = 5;
	public static final int LABEL_SECRET_FOUND = 6;
	public static final int LABEL_LAST = 7;

	public static int[] map = new int[LABEL_LAST];

	public static LabelMaker maker;
	public static NumericSprite numeric;
	public static NumericSprite statsNumeric;

	private static Typeface labelTypeface;
	private static Paint labelPaint;
	private static Paint statsPaint;

	public static void init()
	{
		labelTypeface = Typeface.createFromAsset(Game.assetManager, "fonts/" + GameActivity.appContext.getString(R.string.font_name));

		labelPaint = new Paint();
		labelPaint.setTypeface(labelTypeface);
		labelPaint.setTextSize(Integer.parseInt(GameActivity.appContext.getString(R.string.font_lbl_size)));
		labelPaint.setAntiAlias(true);
		labelPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);

		statsPaint = new Paint();
		statsPaint.setTypeface(labelTypeface);
		statsPaint.setTextSize(Integer.parseInt(GameActivity.appContext.getString(R.string.font_stats_size)));
		statsPaint.setAntiAlias(true);
		statsPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
	}

	public static void surfaceCreated(GL10 gl)
	{
		if (maker == null) {
			maker = new LabelMaker(true, 512, 256);
		} else {
			maker.shutdown(gl);
		}

		maker.initialize(gl);
		maker.beginAdding(gl);
		map[LABEL_FPS] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_fps), labelPaint);
		map[LABEL_CANT_OPEN] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_cant_open_door), labelPaint);
		map[LABEL_NEED_BLUE_KEY] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_need_blue_key), labelPaint);
		map[LABEL_NEED_RED_KEY] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_need_red_key), labelPaint);
		map[LABEL_NEED_GREEN_KEY] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_need_green_key), labelPaint);
		map[LABEL_SECRET_FOUND] = maker.add(gl, GameActivity.appContext.getString(R.string.lbl_secret_found), labelPaint);
		maker.endAdding(gl);

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
