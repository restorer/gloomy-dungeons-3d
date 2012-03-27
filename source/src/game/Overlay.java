package {$PKG_CURR};

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Overlay
{
	public static final int BLOOD = 1;
	public static final int ITEM = 2;

	private static float[][] COLORS = new float[][] {
		new float[] { 1.0f, 0.0f, 0.0f },	// BLOOD
		new float[] { 1.0f, 1.0f, 1.0f }	// ITEM
	};

	private static int overlayType = 0;
	private static long overlayTime = 0;
	private static int labelType = 0;
	private static long labelTime = 0;

	public static void init()
	{
		overlayType = 0;
		labelType = 0;
	}

	public static void showOverlay(int type)
	{
		overlayType = type;
		overlayTime = Game.elapsedTime;
	}

	public static void showLabel(int type)
	{
		labelType = type;
		labelTime = Game.elapsedTime;
	}

	public static void render(GL10 gl)
	{
		renderOverlay(gl);
		renderLabel(gl);
	}

	private static void appendOverlayColor(float r, float g, float b, float a)
	{
		float d = Renderer.a1 + a - Renderer.a1 * a;

		if (d < 0.001) {
			return;
		}

		Renderer.r1 = (Renderer.r1 * Renderer.a1 - Renderer.r1 * Renderer.a1 * a + r * a) / d;
		Renderer.g1 = (Renderer.g1 * Renderer.a1 - Renderer.g1 * Renderer.a1 * a + g * a) / d;
		Renderer.b1 = (Renderer.b1 * Renderer.a1 - Renderer.b1 * Renderer.a1 * a + b * a) / d;
		Renderer.a1 = d;
	}

	private static void renderOverlay(GL10 gl)
	{
		Renderer.r1 = 0.0f;
		Renderer.g1 = 0.0f;
		Renderer.b1 = 0.0f;
		Renderer.a1 = 0.0f;

		float bloodAlpha = Math.max(0.0f, 0.4f - ((float)State.heroHealth / 20.0f) * 0.4f);	// less than 20 health - show blood overlay

		if (bloodAlpha > 0.0f) {
			appendOverlayColor(COLORS[BLOOD - 1][0], COLORS[BLOOD - 1][1], COLORS[BLOOD - 1][2], bloodAlpha);
		}

		if (overlayType != 0)
		{
			float alpha = 0.5f - (float)(Game.elapsedTime - overlayTime) / 300.0f;

			if (alpha > 0.0f) {
				appendOverlayColor(COLORS[overlayType - 1][0], COLORS[overlayType - 1][1], COLORS[overlayType - 1][2], alpha);
			} else {
				overlayType = 0;
			}
		}

		if (Renderer.a1 < 0.001f) {
			return;
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		Renderer.init();

		Renderer.x1 = 0.0f; Renderer.y1 = 0.0f; Renderer.z1 = 0.0f;
		Renderer.x2 = 0.0f; Renderer.y2 = 1.0f; Renderer.z2 = 0.0f;
		Renderer.x3 = 1.0f; Renderer.y3 = 1.0f; Renderer.z3 = 0.0f;
		Renderer.x4 = 1.0f; Renderer.y4 = 0.0f; Renderer.z4 = 0.0f;

		Renderer.r2 = Renderer.r1; Renderer.g2 = Renderer.g1; Renderer.b2 = Renderer.b1; Renderer.a2 = Renderer.a1;
		Renderer.r3 = Renderer.r1; Renderer.g3 = Renderer.g1; Renderer.b3 = Renderer.b1; Renderer.a3 = Renderer.a1;
		Renderer.r4 = Renderer.r1; Renderer.g4 = Renderer.g1; Renderer.b4 = Renderer.b1; Renderer.a4 = Renderer.a1;

		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Renderer.flush(gl, false);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	private static void renderLabel(GL10 gl)
	{
		if (labelType == 0) {
			return;
		}

		float op = Math.min(1.0f, 3.0f - (float)(Game.elapsedTime - labelTime) / 500.0f);

		if (op <= 0.0f)
		{
			labelType = 0;
			return;
		}

		gl.glColor4f(1.0f, 1.0f, 1.0f, op);
		int labelId = Labels.map[labelType];

		Labels.maker.beginDrawing(gl, Game.width, Game.height);
		Labels.maker.draw(gl, (Game.width - Labels.maker.getWidth(labelId)) / 2, (Game.height - Labels.maker.getHeight(labelId)) / 2, labelId);
		Labels.maker.endDrawing(gl);
	}
}
