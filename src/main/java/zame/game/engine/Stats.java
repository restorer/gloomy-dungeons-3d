package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.Renderer;

@SuppressWarnings("WeakerAccess")
public final class Stats {
    private static final float ITEM_WIDTH = 0.275f;
    private static final float KEY_WIDTH = 0.125f;

    private Stats() {
    }

    @SuppressWarnings("MagicNumber")
    private static void drawStatIcon(int pos, int texNum) {
        float sx = -0.0625f + (ITEM_WIDTH * (float)pos);
        float sy = Controls.currentVariant.statsBaseY;
        float ex = sx + 0.25f;
        float ey = sy + 0.25f;

        Renderer.x1 = sx;
        Renderer.y1 = sy;

        Renderer.x2 = sx;
        Renderer.y2 = ey;

        Renderer.x3 = ex;
        Renderer.y3 = ey;

        Renderer.x4 = ex;
        Renderer.y4 = sy;

        Renderer.drawQuad(TextureLoader.BASE_ICONS + texNum);
    }

    @SuppressWarnings("MagicNumber")
    private static void drawStatIconText(GL10 gl, int pos, int value) {
        Labels.statsNumeric.setValue(value);

        Labels.statsNumeric.draw(gl,
                (int)(((((float)pos * ITEM_WIDTH) + 0.12f) * (float)Game.width) / Common.ratio),
                (int)((float)Game.height * (Controls.currentVariant.statsBaseY + 0.095f)),
                Game.width,
                Game.height);
    }

    @SuppressWarnings("MagicNumber")
    private static void drawKeyIcon(@SuppressWarnings("UnusedParameters") GL10 gl, int pos, int texNum) {
        float sx = -0.0625f + (KEY_WIDTH * (float)pos);
        float sy = Controls.currentVariant.keysBaseY;
        float ex = sx + 0.25f;
        float ey = sy + 0.25f;

        Renderer.x1 = sx;
        Renderer.y1 = sy;
        Renderer.x2 = sx;
        Renderer.y2 = ey;
        Renderer.x3 = ex;
        Renderer.y3 = ey;
        Renderer.x4 = ex;
        Renderer.y4 = sy;

        Renderer.drawQuad(TextureLoader.BASE_ICONS + texNum);
    }

    @SuppressWarnings("MagicNumber")
    public static void render(GL10 gl) {
        Renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 0.8f);

        Renderer.z1 = 0.0f;
        Renderer.z2 = 0.0f;
        Renderer.z3 = 0.0f;
        Renderer.z4 = 0.0f;

        Renderer.init();
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

        drawStatIcon(0, 8);
        drawStatIcon(1, 9);

        if ((Weapons.currentParams.ammoIdx >= 0) && (State.heroAmmo[Weapons.currentParams.ammoIdx] >= 0)) {
            drawStatIcon(2, 10);
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
        Renderer.loadIdentityAndOrthof(gl, 0.0f, Common.ratio, 0.0f, 1.0f, 0.0f, 1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        Renderer.bindTextureCtl(gl, TextureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        Renderer.flush(gl);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();

        drawStatIconText(gl, 0, State.heroHealth);
        drawStatIconText(gl, 1, State.heroArmor);

        if ((Weapons.currentParams.ammoIdx >= 0) && (State.heroAmmo[Weapons.currentParams.ammoIdx] >= 0)) {
            drawStatIconText(gl, 2, State.heroAmmo[Weapons.currentParams.ammoIdx]);
        }
    }
}
