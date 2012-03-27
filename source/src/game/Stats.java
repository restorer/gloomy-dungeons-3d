package {$PKG_CURR};

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Stats
{
	private static final float ITEM_WIDTH = 0.275f;
	private static final float KEY_WIDTH = 0.125f;

	private static void drawStatIcon(GL10 gl, int pos, int texNum, int value)
	{
		float sx = -0.0625f + ITEM_WIDTH * (float)pos;
		float sy = Controls.currentVariant.statsBaseY;
		float ex = sx + 0.25f;
		float ey = sy + 0.25f;

		Renderer.x1 = sx; Renderer.y1 = sy;
		Renderer.x2 = sx; Renderer.y2 = ey;
		Renderer.x3 = ex; Renderer.y3 = ey;
		Renderer.x4 = ex; Renderer.y4 = sy;

		Renderer.drawQuad(TextureLoader.BASE_ICONS + texNum);
		Labels.statsNumeric.setValue(value);

		Labels.statsNumeric.draw(
			gl,
			(int)(((float)pos * ITEM_WIDTH + 0.12f) * (float)Game.width / Common.ratio),
			(int)((float)Game.height * (Controls.currentVariant.statsBaseY + 0.095f)),
			Game.width,
			Game.height
		);
	}

	private static void drawKeyIcon(GL10 gl, int pos, int texNum)
	{
		float sx = -0.0625f + KEY_WIDTH * (float)pos;
		float sy = Controls.currentVariant.keysBaseY;
		float ex = sx + 0.25f;
		float ey = sy + 0.25f;

		Renderer.x1 = sx; Renderer.y1 = sy;
		Renderer.x2 = sx; Renderer.y2 = ey;
		Renderer.x3 = ex; Renderer.y3 = ey;
		Renderer.x4 = ex; Renderer.y4 = sy;

		Renderer.drawQuad(TextureLoader.BASE_ICONS + texNum);
	}

	public static void render(GL10 gl)
	{
		Renderer.r1 = 1.0f; Renderer.g1 = 1.0f; Renderer.b1 = 1.0f; Renderer.a1 = 0.8f;
		Renderer.r2 = 1.0f; Renderer.g2 = 1.0f; Renderer.b2 = 1.0f; Renderer.a2 = 0.8f;
		Renderer.r3 = 1.0f; Renderer.g3 = 1.0f; Renderer.b3 = 1.0f; Renderer.a3 = 0.8f;
		Renderer.r4 = 1.0f; Renderer.g4 = 1.0f; Renderer.b4 = 1.0f; Renderer.a4 = 0.8f;

		Renderer.z1 = 0.0f;
		Renderer.z2 = 0.0f;
		Renderer.z3 = 0.0f;
		Renderer.z4 = 0.0f;

		Renderer.init();
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

		drawStatIcon(gl, 0, 8, State.heroHealth);
		drawStatIcon(gl, 1, 9, State.heroArmor);

		if ((Weapons.currentParams.ammoIdx >= 0) && (State.heroAmmo[Weapons.currentParams.ammoIdx] >= 0)) {
			drawStatIcon(gl, 2, 10, State.heroAmmo[Weapons.currentParams.ammoIdx]);
		}

		if ((State.heroKeysMask & 1) != 0) {
			drawKeyIcon(gl, 0, 11);
		}

		if ((State.heroKeysMask & 2) != 0) {
			drawKeyIcon(gl, 1, 12);
		}

		if ((State.heroKeysMask & 4) != 0) {
			drawKeyIcon(gl, 2, 13);
		}

		gl.glDisable(GL10.GL_DEPTH_TEST);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0f, Common.ratio, 0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		Renderer.bindTextureCtl(gl, TextureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		Renderer.flush(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}
}
