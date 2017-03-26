/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zame.libs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Renderer;

/**
 * An OpenGL text label maker.
 * <p>
 * <p>
 * OpenGL labels are implemented by creating a Bitmap, drawing all the labels
 * into the Bitmap, converting the Bitmap into an Alpha texture, and creating a
 * mesh for each label
 * <p>
 * The benefits of this approach are that the labels are drawn using the high
 * quality anti-aliased font rasterizer, full character set support, and all the
 * text labels are stored on a single texture, which makes it faster to use.
 * <p>
 * The drawbacks are that you can only have as many labels as will fit onto one
 * texture, and you have to recreate the whole texture if any label text
 * changes.
 */
public class LabelMaker {
    private int mStrikeWidth;
    private int mStrikeHeight;
    private boolean mFullColor;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    private int mTextureID;

    private float mTexelWidth;  // Convert texel to U
    private float mTexelHeight; // Convert texel to V
    private int mU;
    private int mV;
    private int mLineHeight;
    private ArrayList<Label> mLabels = new ArrayList<Label>();

    private static final int STATE_NEW = 0;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_ADDING = 2;
    private static final int STATE_DRAWING = 3;
    private int mState;

    /**
     * Create a label maker
     * or maximum compatibility with various OpenGL ES implementations,
     * the strike width and height must be powers of two,
     * We want the strike width to be at least as wide as the widest window.
     *
     * @param fullColor true if we want a full color backing store (4444),
     * otherwise we generate a grey L8 backing store.
     * @param strikeWidth width of strike
     * @param strikeHeight height of strike
     */
    public LabelMaker(boolean fullColor, int strikeWidth, int strikeHeight) {
        mFullColor = fullColor;
        mStrikeWidth = strikeWidth;
        mStrikeHeight = strikeHeight;
        mTexelWidth = (float)(1.0 / mStrikeWidth);
        mTexelHeight = (float)(1.0 / mStrikeHeight);
        mState = STATE_NEW;
    }

    /**
     * Call to initialize the class.
     * Call whenever the surface has been created.
     *
     * @param gl GL
     */
    public void initialize(GL10 gl) {
        mState = STATE_INITIALIZED;

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureID = textures[0];

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        // Use Nearest for performance.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
    }

    /**
     * Call when the surface has been destroyed
     *
     * @param gl GL
     */
    public void shutdown(GL10 gl) {
        if ((gl != null) && (mState > STATE_NEW)) {
            int[] textures = new int[1];
            textures[0] = mTextureID;
            gl.glDeleteTextures(1, textures, 0);
            mState = STATE_NEW;
        }
    }

    /**
     * Call before adding labels. Clears out any existing labels.
     *
     * @param gl GL
     */
    public void beginAdding(@SuppressWarnings("UnusedParameters") GL10 gl) {
        checkState(STATE_INITIALIZED, STATE_ADDING);

        mLabels.clear();
        mU = 0;
        mV = 0;
        mLineHeight = 0;

        Bitmap.Config config = (mFullColor ? Bitmap.Config.ARGB_4444 : Bitmap.Config.ALPHA_8);
        mBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config);
        mCanvas = new Canvas(mBitmap);
        mBitmap.eraseColor(0);
    }

    /**
     * Call to add a label
     *
     * @param gl GL
     * @param text the text of the label
     * @param textPaint the paint of the label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, String text, Paint textPaint) {
        return add(gl, null, text, textPaint);
    }

    /**
     * Call to add a label
     *
     * @param gl GL
     * @param text the text of the label
     * @param textPaint the paint of the label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, Drawable background, String text, Paint textPaint) {
        return add(gl, background, text, textPaint, 0, 0);
    }

    /**
     * Call to add a label
     *
     * @return the id of the label, used to measure and draw the label
     */
    public int add(GL10 gl, Drawable drawable, int minWidth, int minHeight) {
        return add(gl, drawable, null, null, minWidth, minHeight);
    }

    /**
     * Call to add a label
     *
     * @param gl GL
     * @param text the text of the label
     * @param textPaint the paint of the label
     * @return the id of the label, used to measure and draw the label
     */
    public int add(@SuppressWarnings("UnusedParameters") GL10 gl,
            Drawable background,
            String text,
            Paint textPaint,
            int minWidth,
            int minHeight) {

        checkState(STATE_ADDING, STATE_ADDING);

        boolean drawBackground = (background != null);
        boolean drawText = ((text != null) && (textPaint != null));
        Rect padding = new Rect();

        if (drawBackground) {
            background.getPadding(padding);
            minWidth = Math.max(minWidth, background.getMinimumWidth());
            minHeight = Math.max(minHeight, background.getMinimumHeight());
        }

        int ascent = 0;
        int descent = 0;
        int measuredTextWidth = 0;

        if (drawText) {
            // Paint.ascent is negative, so negate it.
            ascent = (int)Math.ceil(-textPaint.ascent());
            descent = (int)Math.ceil(textPaint.descent());
            measuredTextWidth = (int)Math.ceil(textPaint.measureText(text));
        }

        int textHeight = ascent + descent;
        int textWidth = Math.min(mStrikeWidth, measuredTextWidth);

        int padHeight = padding.top + padding.bottom;
        int padWidth = padding.left + padding.right;
        int height = Math.max(minHeight, textHeight + padHeight);
        int width = Math.max(minWidth, textWidth + padWidth);
        int effectiveTextHeight = height - padHeight;
        int effectiveTextWidth = width - padWidth;

        int centerOffsetHeight = (effectiveTextHeight - textHeight) / 2;
        int centerOffsetWidth = (effectiveTextWidth - textWidth) / 2;

        // Make changes to the local variables, only commit them
        // to the member variables after we've decided not to throw
        // any exceptions.

        int u = mU;
        int v = mV;
        int lineHeight = mLineHeight;

        if (width > mStrikeWidth) {
            width = mStrikeWidth;
        }

        // Is there room for this string on the current line?
        if ((u + width) > mStrikeWidth) {
            // No room, go to the next line:
            u = 0;
            v += lineHeight;
            lineHeight = 0;
        }

        lineHeight = Math.max(lineHeight, height);

        if ((v + lineHeight) > mStrikeHeight) {
            throw new IllegalArgumentException("Out of texture space.");
        }

        int vBase = v + ascent;

        if (drawBackground) {
            background.setBounds(u, v, u + width, v + height);
            background.draw(mCanvas);
        }

        if (drawText) {
            mCanvas.drawText(text,
                    u + padding.left + centerOffsetWidth,
                    vBase + padding.top + centerOffsetHeight,
                    textPaint);
        }

        // We know there's enough space, so update the member variables
        mU = u + width;
        mV = v;
        mLineHeight = lineHeight;

        float sx = u * mTexelWidth;
        float sy = v * mTexelHeight;
        float ex = sx + (width * mTexelWidth);
        float ey = sy + (height * mTexelHeight);

        mLabels.add(new Label(width, height, ascent, sx, sy, ex, ey));
        return mLabels.size() - 1;
    }

    /**
     * Call to end adding labels. Must be called before drawing starts.
     *
     * @param gl GL
     */
    public void endAdding(GL10 gl) {
        checkState(STATE_ADDING, STATE_INITIALIZED);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);

        // Reclaim storage used by bitmap and canvas.
        mCanvas = null;
        mBitmap.recycle();
        mBitmap = null;
        System.gc();
    }

    /**
     * Get the width in pixels of a given label.
     *
     * @param labelID ID of the label
     * @return the width in pixels
     */
    public float getWidth(int labelID) {
        return mLabels.get(labelID).width;
    }

    /**
     * Get the height in pixels of a given label.
     *
     * @param labelID ID of the label
     * @return the height in pixels
     */
    public float getHeight(int labelID) {
        return mLabels.get(labelID).height;
    }

    /**
     * Get the baseline of a given label. That's how many pixels from the top of
     * the label to the text baseline. (This is equivalent to the negative of
     * the label's paint's ascent.)
     *
     * @param labelID ID of the label
     * @return the baseline in pixels.
     */
    @SuppressWarnings("unused")
    public float getBaseline(int labelID) {
        return mLabels.get(labelID).baseline;
    }

    /**
     * Begin drawing labels. Sets the OpenGL state for rapid drawing.
     * Don't forget to set appropriate colors via glColor4x or glColor4f
     *
     * @param gl GL
     * @param viewWidth width of the view
     * @param viewHeight height of the view
     */
    public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
        checkState(STATE_INITIALIZED, STATE_DRAWING);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        Renderer.loadIdentityAndOrthof(gl, 0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        Renderer.init();
        Renderer.bindTexture(gl, mTextureID);

        gl.glShadeModel(GL10.GL_FLAT);
        gl.glDisable(GL10.GL_DEPTH_TEST);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        Renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Draw a given label at a given x,y position, expressed in pixels, with the
     * lower-left-hand-corner of the view being (0,0).
     *
     * @param gl GL
     * @param x x position
     * @param y y position
     * @param labelID ID of the label
     */
    @SuppressWarnings("MagicNumber")
    public void draw(@SuppressWarnings("UnusedParameters") GL10 gl, float x, float y, int labelID) {
        checkState(STATE_DRAWING, STATE_DRAWING);

        float snappedX = (float)Math.floor(x) + 0.375f; // magic offset to promote consistent rasterization
        float snappedY = (float)Math.floor(y) + 0.375f; // magic offset to promote consistent rasterization

        Label label = mLabels.get(labelID);
        Renderer.setQuadOrthoCoords(snappedX, snappedY, snappedX + label.width, snappedY + label.height);

        Renderer.u1 = label.sx;
        Renderer.v1 = label.ey;

        Renderer.u2 = label.sx;
        Renderer.v2 = label.sy;

        Renderer.u3 = label.ex;
        Renderer.v3 = label.sy;

        Renderer.u4 = label.ex;
        Renderer.v4 = label.ey;

        Renderer.drawQuad();
    }

    /**
     * Ends the drawing and restores the OpenGL state.
     *
     * @param gl GL
     */
    public void endDrawing(GL10 gl) {
        checkState(STATE_DRAWING, STATE_INITIALIZED);

        Renderer.flush(gl);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
    }

    private void checkState(int oldState, int newState) {
        if (mState != oldState) {
            throw new IllegalArgumentException("Can't call this method now.");
        }

        mState = newState;
    }

    @SuppressWarnings("WeakerAccess")
    private static class Label {
        public float width;
        public float height;
        public float baseline;
        public float sx;
        public float sy;
        public float ex;
        public float ey;

        public Label(float width, float height, float baseLine, float sx, float sy, float ex, float ey) {
            this.width = width;
            this.height = height;
            this.baseline = baseLine;
            this.sx = sx;
            this.sy = sy;
            this.ex = ex;
            this.ey = ey;
        }
    }
}
