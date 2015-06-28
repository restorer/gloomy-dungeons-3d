package zame.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Random;
import zame.game.engine.State;

// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3_r1/android/os/FileUtils.java#FileUtils

public class Common
{
	public static String LOG_KEY = "GloomyDungeons";
	public static float G2RAD_F = (float)(Math.PI / 180.0);
	public static float RAD2G_F = (float)(180.0 / Math.PI);

	public static float heroAr;	// angle in radians
	public static float heroCs;	// cos of angle
	public static float heroSn;	// sin of angle

	public static volatile Random random = null;
	public static float ratio;

	public static void init()
	{
		if (random == null) {
			random = new Random();
		}

		ratio = 1.0f;
	}

	public static void heroAngleUpdated()
	{
		State.heroA = (360.0f + (State.heroA % 360.0f)) % 360.0f;

		heroAr = State.heroA * G2RAD_F;
		heroCs = (float)Math.cos(heroAr);
		heroSn = (float)Math.sin(heroAr);
	}

	// modified Level_CheckLine from wolf3d for iphone by Carmack
	public static boolean traceLine(float x1, float y1, float x2, float y2, int mask)
	{
		int cx1 = (int)x1;
		int cy1 = (int)y1;
		int cx2 = (int)x2;
		int cy2 = (int)y2;

		if ((cx1 < 0) || (cx1 >= State.levelWidth) ||
			(cx2 < 0) || (cx2 >= State.levelWidth) ||
			(cy1 < 0) || (cy1 >= State.levelHeight) ||
			(cy2 < 0) || (cy2 >= State.levelHeight)
		) {
			return false;
		}

		if (cx1 != cx2)
		{
			int stepX;
			float partial;

			if (cx2 > cx1)
			{
				partial = 1.0f - (x1 - (float)((int)x1));
				stepX = 1;
			}
			else
			{
				partial = x1 - (float)((int)x1);
				stepX = -1;
			}

			float dx = ((x2 >= x1) ? (x2 - x1) : (x1 - x2));
			float stepY = (y2 - y1) / dx;
			float y = y1 + (stepY * partial);

			cx1 += stepX;
			cx2 += stepX;

			do
			{
				if ((State.passableMap[(int)y][cx1] & mask) != 0) {
					return false;
				}

				y += stepY;
				cx1 += stepX;
			}
			while (cx1 != cx2);
		}

		if (cy1 != cy2)
		{
			int stepY;
			float partial;

			if (cy2 > cy1)
			{
				partial = 1.0f - (y1 - (float)((int)y1));
				stepY = 1;
			}
			else
			{
				partial = y1 - (float)((int)y1);
				stepY = -1;
			}

			float dy = ((y2 >= y1) ? (y2 - y1) : (y1 - y2));
			float stepX = (x2 - x1) / dy;
			float x = x1 + (stepX * partial);

			cy1 += stepY;
			cy2 += stepY;

			do
			{
				if ((State.passableMap[cy1][(int)x] & mask) != 0) {
					return false;
				}

				x += stepX;
				cy1 += stepY;
			}
			while (cy1 != cy2);
		}

		return true;
	}

	public static int getRealHits(int maxHits, float dist)
	{
		float div = Math.max(1.0f, dist * 0.35f);
		int minHits = Math.max(1, (int)((float)maxHits / div));

		return (random.nextInt(maxHits - minHits + 1) + minHits);
	}

	public static void writeBooleanArray(ObjectOutput os, boolean[] list) throws IOException
	{
		os.writeInt(list.length);

		for (int i = 0; i < list.length; i++) {
			os.writeBoolean(list[i]);
		}
	}

	public static void writeIntArray(ObjectOutput os, int[] list) throws IOException
	{
		os.writeInt(list.length);

		for (int i = 0; i < list.length; i++) {
			os.writeInt(list[i]);
		}
	}

	public static void writeObjectArray(ObjectOutput os, Object[] list, int size) throws IOException
	{
		os.writeInt(size);

		for (int i = 0; i < size; i++) {
			((Externalizable)list[i]).writeExternal(os);
		}
	}

	public static void writeInt2dArray(ObjectOutput os, int[][] map) throws IOException
	{
		os.writeInt(map.length);
		os.writeInt(map[0].length);

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				os.writeInt(map[i][j]);
			}
		}
	}

	public static boolean[] readBooleanArray(ObjectInput is) throws IOException
	{
		int size = is.readInt();
		boolean[] list = new boolean[size];

		for (int i = 0; i < size; i++) {
			list[i] = is.readBoolean();
		}

		return list;
	}

	public static int[] readIntArray(ObjectInput is) throws IOException
	{
		int size = is.readInt();
		int[] list = new int[size];

		for (int i = 0; i < size; i++) {
			list[i] = is.readInt();
		}

		return list;
	}

	public static int[][] readInt2dArray(ObjectInput is) throws IOException
	{
		int h = is.readInt();
		int w = is.readInt();
		int[][] map = new int[h][w];

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				map[i][j] = is.readInt();
			}
		}

		return map;
	}

	public static int readObjectArray(ObjectInput is, Object[] list, Class<?> theClass) throws IOException, ClassNotFoundException
	{
		int size = is.readInt();

		for (int i = 0; i < size; i++)
		{
			Externalizable instance;

			try {
				instance = (Externalizable)theClass.newInstance();
			} catch (Exception ex) {
				Log.e(LOG_KEY, "Exception", ex);
				throw new ClassNotFoundException("Couldn't create class instance");
			}

			instance.readExternal(is);
			list[i] = instance;
		}

		return size;
	}

	public static InputStream openLocalizedAsset(AssetManager assetManager, String pathTemplate) throws IOException
	{
		String path = String.format(Locale.US, pathTemplate, "-" + Locale.getDefault().getLanguage().toLowerCase(Locale.US));
		InputStream res;

		try
		{
			res = assetManager.open(path);
		}
		catch (IOException ex)
		{
			path = String.format(Locale.US, pathTemplate, "");
			res = assetManager.open(path);
		}

		return res;
	}

	public static void setTypeface(View view, int[] viewIds)
	{
		Context context = view.getContext();
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + context.getString(R.string.font_name));

		for (int i = 0; i < viewIds.length; i++) {
			View childView = view.findViewById(viewIds[i]);

			if (childView != null && childView instanceof TextView) {
				((TextView)childView).setTypeface(typeface);
			}
		}
	}

	public static void setTypeface(Activity activity, int[] viewIds)
	{
		Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/" + activity.getString(R.string.font_name));

		for (int i = 0; i < viewIds.length; i++) {
			View childView = activity.findViewById(viewIds[i]);

			if (childView != null && childView instanceof TextView) {
				((TextView)childView).setTypeface(typeface);
			}
		}
	}

	public static boolean openMarket(Context context, String packageName)
	{
		try {
			context.startActivity((
				new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName))
			).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));

			return true;
		} catch (Exception ex) {
			Log.e(LOG_KEY, "Exception", ex);
			Toast.makeText(ZameApplication.self, "Could not launch the market application.", Toast.LENGTH_LONG).show();
		}

		return false;
	}

	public static boolean openBrowser(Context context, String uri)
	{
		try {
			context.startActivity((
				new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
			).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));

			return true;
		} catch (Exception ex) {
			Log.e(LOG_KEY, "Exception", ex);
			Toast.makeText(ZameApplication.self, "Could not launch the browser application.", Toast.LENGTH_LONG).show();
		}

		return false;
	}

	public static boolean copyFile(String srcFileName, String destFileName)
	{
		boolean success = true;

		try {
			InputStream in = new FileInputStream(srcFileName);
			OutputStream out = new FileOutputStream(destFileName);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
		} catch (Exception ex) {
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			Toast.makeText(ZameApplication.self, R.string.msg_cant_copy_state, Toast.LENGTH_LONG).show();
		}

		return success;
	}
}
