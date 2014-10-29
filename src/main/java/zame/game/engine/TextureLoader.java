package zame.game.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLUtils;
import javax.microedition.khronos.opengles.GL10;
import zame.game.AppConfig;
import zame.game.R;

public class TextureLoader
{
	public static final int TEXTURE_MAIN = 0;
	public static final int TEXTURE_FLOOR = 1;
	public static final int TEXTURE_CEIL = 2;
	public static final int TEXTURE_MON = 3;

	public static final int TEXTURE_HAND = AppConfig.TEXTURE_HAND;
	public static final int TEXTURE_PIST = AppConfig.TEXTURE_PIST;
	public static final int TEXTURE_SHTG = AppConfig.TEXTURE_SHTG;
	public static final int TEXTURE_CHGN = AppConfig.TEXTURE_CHGN;
	public static final int TEXTURE_DBLSHTG = AppConfig.TEXTURE_DBLSHTG;
	public static final int TEXTURE_DBLCHGN = AppConfig.TEXTURE_DBLCHGN;
	public static final int TEXTURE_SAW = AppConfig.TEXTURE_SAW;
	public static final int TEXTURE_LAST = AppConfig.TEXTURE_LAST;

	public static final int BASE_ICONS = 0x00;
	public static final int BASE_WALLS = 0x10;
	public static final int BASE_TRANSPARENTS = 0x30;
	public static final int BASE_DOORS_F = 0x50;
	public static final int BASE_DOORS_S = 0x60;
	public static final int BASE_OBJECTS = 0x70;
	public static final int BASE_DECORATIONS = 0x80;
	public static final int BASE_ADDITIONAL = 0x90;

	public static final int COUNT_MONSTER = 0x10;	// block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot

	public static final int OBJ_ARMOR_GREEN = BASE_OBJECTS + 0;
	public static final int OBJ_ARMOR_RED = BASE_OBJECTS + 1;
	public static final int OBJ_KEY_BLUE = BASE_OBJECTS + 2;
	public static final int OBJ_KEY_RED = BASE_OBJECTS + 3;
	public static final int OBJ_STIM = BASE_OBJECTS + 4;
	public static final int OBJ_MEDI = BASE_OBJECTS + 5;
	public static final int OBJ_CLIP = BASE_OBJECTS + 6;
	public static final int OBJ_AMMO = BASE_OBJECTS + 7;
	public static final int OBJ_SHELL = BASE_OBJECTS + 8;
	public static final int OBJ_SBOX = BASE_OBJECTS + 9;
	public static final int OBJ_BPACK = BASE_OBJECTS + 10;
	public static final int OBJ_SHOTGUN = BASE_OBJECTS + 11;
	public static final int OBJ_KEY_GREEN = BASE_OBJECTS + 12;
	public static final int OBJ_CHAINGUN = BASE_OBJECTS + 13;
	public static final int OBJ_DBLSHOTGUN = BASE_OBJECTS + 14;

	public static class TextureToLoad
	{
		public static final int TYPE_RESOURCE = 0;
		public static final int TYPE_MONSTERS = 1;
		public static final int TYPE_FLOOR = 2;
		public static final int TYPE_CEIL = 3;

		public int tex;
		public int resId;
		public int type;

		public TextureToLoad(int tex, int resId)
		{
			this.tex = tex;
			this.resId = resId;
			this.type = TYPE_RESOURCE;
		}

		public TextureToLoad(int tex, int resId, int type)
		{
			this.tex = tex;
			this.resId = resId;
			this.type = type;
		}
	}

	public static final TextureToLoad[] TEXTURES_TO_LOAD = AppConfig.TEXTURES_TO_LOAD;

	private static final int[] floorTexMap = new int[] {
		R.drawable.floor_1,
		R.drawable.floor_2,
	};

	private static final int[] ceilTexMap = new int[] {
		R.drawable.ceil_1,
		R.drawable.ceil_2,
	};

	private static final int[] monTexMap = new int[] {
		R.drawable.texmap_mon_1,
		R.drawable.texmap_mon_2,
		R.drawable.texmap_mon_3,
		R.drawable.texmap_mon_4,
		R.drawable.texmap_mon_5,
		R.drawable.texmap_mon_6,
	};

	private static boolean texturesInitialized = false;
	public static int[] textures = new int[TEXTURE_LAST];

	private static BitmapFactory.Options tOpts;
	private static LevelConfig levelConf;

	private static void loadAndBindTexture(GL10 gl, int tex, int resId)
	{
		Bitmap img = BitmapFactory.decodeResource(Game.resources, resId, tOpts);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);

		img.recycle();
		img = null;
		System.gc();

		// Runtime.getRuntime().gc();
	}

	private static void loadAndBindMonTexture(GL10 gl, int tex, int resId1, int resId2, int resId3, int resId4)
	{
		Bitmap img = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(img);

		Bitmap mon = BitmapFactory.decodeResource(Game.resources, resId1, tOpts);
		canvas.drawBitmap(mon, 0.0f, 0.0f, null);
		mon.recycle();
		mon = null;

		mon = BitmapFactory.decodeResource(Game.resources, resId2, tOpts);
		canvas.drawBitmap(mon, 0.0f, 256.0f, null);
		mon.recycle();
		mon = null;

		mon = BitmapFactory.decodeResource(Game.resources, resId3, tOpts);
		canvas.drawBitmap(mon, 0.0f, 512.0f, null);
		mon.recycle();
		mon = null;

		mon = BitmapFactory.decodeResource(Game.resources, resId4, tOpts);
		canvas.drawBitmap(mon, 0.0f, 768.0f, null);
		mon.recycle();
		mon = null;

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);

		canvas = null;
		img.recycle();
		img = null;
		System.gc();
	}

	public static int getTexNum(int[] texMap, int texNum)
	{
		return texMap[(texNum < 1 || texNum > texMap.length) ? 0 : (texNum - 1)];
	}

	public static boolean loadTexture(GL10 gl, int createdTexturesCount)
	{
		if (createdTexturesCount >= TEXTURES_TO_LOAD.length) {
			return false;
		}

		if (createdTexturesCount == 0) {
			if (texturesInitialized) {
				gl.glDeleteTextures(TEXTURE_LAST, textures, 0);
			}

			texturesInitialized = true;
			gl.glGenTextures(TEXTURE_LAST, textures, 0);

			levelConf = LevelConfig.read(Game.assetManager, State.levelNum);
		} else {
			// re-ensure levelConf
			if (levelConf == null) {
				levelConf = LevelConfig.read(Game.assetManager, State.levelNum);
			}

			// re-ensure initialized textures
			if (!texturesInitialized) {
				texturesInitialized = true;
				gl.glGenTextures(TEXTURE_LAST, textures, 0);
			}
		}

		if (tOpts == null) {
			tOpts = new BitmapFactory.Options();
			tOpts.inDither = false;
			tOpts.inPurgeable = true;
			tOpts.inInputShareable = true;
		}

		TextureToLoad texToLoad = TEXTURES_TO_LOAD[createdTexturesCount];

		switch (texToLoad.type) {
			case TextureToLoad.TYPE_MONSTERS:
				loadAndBindMonTexture(
					gl,
					texToLoad.tex,
					getTexNum(monTexMap, levelConf.monsters[0].texture),
					getTexNum(monTexMap, levelConf.monsters[1].texture),
					getTexNum(monTexMap, levelConf.monsters[2].texture),
					getTexNum(monTexMap, levelConf.monsters[3].texture)
				);
				break;

			case TextureToLoad.TYPE_FLOOR:
				loadAndBindTexture(gl, texToLoad.tex, getTexNum(floorTexMap, levelConf.floorTexture));
				break;

			case TextureToLoad.TYPE_CEIL:
				loadAndBindTexture(gl, texToLoad.tex, getTexNum(ceilTexMap, levelConf.ceilTexture));
				break;

			default:
				loadAndBindTexture(gl, texToLoad.tex, texToLoad.resId);
				break;
		}

		return true;
	}
}
