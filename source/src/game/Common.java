package {$PKG_CURR};

import java.util.Random;
import java.io.*;
import android.util.Log;
import java.util.Locale;
import android.content.res.*;

public class Common
{
	public static String LOG_KEY = "GloomyDungeons";
	public static float G2RAD_F = (float)(Math.PI / 180.0);
	public static float RAD2G_F = (float)(180.0 / Math.PI);

	public static float heroAr;	// angle in radians
	public static float heroCs;	// cos(heroAr)
	public static float heroSn;	// sin(heroAr)

	public static Random random;
	public static float ratio;

	public static void init()
	{
		random = new Random();
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

	public static int readObjectArray(ObjectInput is, Object[] list, Class theClass) throws IOException, ClassNotFoundException
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

	/*
	public static String getStackTrace(Throwable aThrowable)
	{
		final StringBuilder result = new StringBuilder(aThrowable.toString());
		final String NEW_LINE = System.getProperty("line.separator");
		result.append(NEW_LINE);

		for (StackTraceElement element : aThrowable.getStackTrace()) {
			result.append(element);
			result.append(NEW_LINE);
		}

		return result.toString();
	}
	*/

	public static InputStream openLocalizedAsset(AssetManager assetManager, String pathTemplate) throws IOException
	{
		String path = String.format(pathTemplate, "-" + Locale.getDefault().getLanguage().toLowerCase());
		InputStream res;

		try
		{
			res = assetManager.open(path);
		}
		catch (IOException ex)
		{
			path = String.format(pathTemplate, "");
			res = assetManager.open(path);
		}

		return res;
	}
}
