package {$PKG_CURR};

import android.graphics.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLUtils;

public class TextureLoader
{
	public static final int TEXTURE_MAIN = 0;
	public static final int TEXTURE_FLOOR = 1;
	public static final int TEXTURE_CEIL = 2;
	public static final int TEXTURE_HAND = 3;		// 3, 4, 5, 6
	public static final int TEXTURE_PIST = 7;		// 7, 8, 9, 10
	public static final int TEXTURE_SHTG = 11;		// 11, 12, 13, 14
	public static final int TEXTURE_CHGN = 15;		// 15, 16, 17, 18
	public static final int TEXTURE_DBLSHTG = 19;	// 19, 20, 21, 22, 23, 24, 25, 26, 27
	public static final int TEXTURE_DBLCHGN = 28;	// 28, 29, 30, 31
	public static final int TEXTURE_SAW = 32;		// 32, 33, 34
	public static final int TEXTURE_MON = 35;
	public static final int TEXTURE_LAST = 36;

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

	private static boolean texturesInitialized = false;
	public static int[] textures = new int[TEXTURE_LAST];

	private static BitmapFactory.Options tOpts;

	private static void loadAndBindTexture(GL10 gl, int id, int tex)
	{
		Bitmap img = BitmapFactory.decodeResource(Game.resources, id, tOpts);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
		img.recycle();
	}

	public static void surfaceCreated(GL10 gl)
	{
		if (texturesInitialized) {
			gl.glDeleteTextures(TEXTURE_LAST, textures, 0);
		}

		texturesInitialized = true;
		gl.glGenTextures(TEXTURE_LAST, textures, 0);

		tOpts = new BitmapFactory.Options();
		tOpts.inDither = false;
		tOpts.inPurgeable = true;

		loadAndBindTexture(gl, R.drawable.texmap, TEXTURE_MAIN);
		loadAndBindTexture(gl, R.drawable.texmap_mon, TEXTURE_MON);

		if (State.levelNum == 9 || State.levelNum == 10)	// TODO: remove these hacked magic numbers "9" and "10"
		{
			loadAndBindTexture(gl, R.drawable.floor_1, TEXTURE_FLOOR);
			loadAndBindTexture(gl, R.drawable.ceil_1, TEXTURE_CEIL);
		}
		else
		{
			loadAndBindTexture(gl, R.drawable.floor_2, TEXTURE_FLOOR);
			loadAndBindTexture(gl, R.drawable.ceil_2, TEXTURE_CEIL);
		}

		loadAndBindTexture(gl, R.drawable.hit_hand_1, TEXTURE_HAND + 0);
		loadAndBindTexture(gl, R.drawable.hit_hand_2, TEXTURE_HAND + 1);
		loadAndBindTexture(gl, R.drawable.hit_hand_3, TEXTURE_HAND + 2);
		loadAndBindTexture(gl, R.drawable.hit_hand_4, TEXTURE_HAND + 3);

		loadAndBindTexture(gl, R.drawable.hit_pist_1, TEXTURE_PIST + 0);
		loadAndBindTexture(gl, R.drawable.hit_pist_2, TEXTURE_PIST + 1);
		loadAndBindTexture(gl, R.drawable.hit_pist_3, TEXTURE_PIST + 2);
		loadAndBindTexture(gl, R.drawable.hit_pist_4, TEXTURE_PIST + 3);

		loadAndBindTexture(gl, R.drawable.hit_shtg_1, TEXTURE_SHTG + 0);
		loadAndBindTexture(gl, R.drawable.hit_shtg_2, TEXTURE_SHTG + 1);
		loadAndBindTexture(gl, R.drawable.hit_shtg_3, TEXTURE_SHTG + 2);
		loadAndBindTexture(gl, R.drawable.hit_shtg_4, TEXTURE_SHTG + 3);

		loadAndBindTexture(gl, R.drawable.hit_chgn_1, TEXTURE_CHGN + 0);
		loadAndBindTexture(gl, R.drawable.hit_chgn_2, TEXTURE_CHGN + 1);
		loadAndBindTexture(gl, R.drawable.hit_chgn_3, TEXTURE_CHGN + 2);
		loadAndBindTexture(gl, R.drawable.hit_chgn_4, TEXTURE_CHGN + 3);

		loadAndBindTexture(gl, R.drawable.hit_dblshtg_1, TEXTURE_DBLSHTG + 0);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_2, TEXTURE_DBLSHTG + 1);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_3, TEXTURE_DBLSHTG + 2);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_4, TEXTURE_DBLSHTG + 3);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_5, TEXTURE_DBLSHTG + 4);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_6, TEXTURE_DBLSHTG + 5);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_7, TEXTURE_DBLSHTG + 6);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_8, TEXTURE_DBLSHTG + 7);
		loadAndBindTexture(gl, R.drawable.hit_dblshtg_9, TEXTURE_DBLSHTG + 8);

		loadAndBindTexture(gl, R.drawable.hit_dblchgn_1, TEXTURE_DBLCHGN + 0);
		loadAndBindTexture(gl, R.drawable.hit_dblchgn_2, TEXTURE_DBLCHGN + 1);
		loadAndBindTexture(gl, R.drawable.hit_dblchgn_3, TEXTURE_DBLCHGN + 2);
		loadAndBindTexture(gl, R.drawable.hit_dblchgn_4, TEXTURE_DBLCHGN + 3);

		loadAndBindTexture(gl, R.drawable.hit_saw_1, TEXTURE_SAW + 0);
		loadAndBindTexture(gl, R.drawable.hit_saw_2, TEXTURE_SAW + 1);
		loadAndBindTexture(gl, R.drawable.hit_saw_3, TEXTURE_SAW + 2);
	}
}
