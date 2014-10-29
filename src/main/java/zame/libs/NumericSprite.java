/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.graphics.Paint;
import javax.microedition.khronos.opengles.GL10;

public class NumericSprite
{
	public NumericSprite()
	{
		mLabelMaker = null;

		mDigits[0] = 0;
		mDigitsCount = 1;
	}

	public void initialize(GL10 gl, Paint paint)
	{
		int height = roundUpPower2((int)paint.getFontSpacing());
		final float interDigitGaps = 9 * 1.0f;
		int width = roundUpPower2((int)(interDigitGaps + paint.measureText(sStrike)));

		mLabelMaker = new LabelMaker(true, width, height);
		mLabelMaker.initialize(gl);
		mLabelMaker.beginAdding(gl);

		for (int i = 0; i < 10; i++)
		{
			String digit = sStrike.substring(i, i+1);
			mLabelId[i] = mLabelMaker.add(gl, digit, paint);
			mWidth[i] = (int)Math.ceil(mLabelMaker.getWidth(i));
		}

		mLabelMaker.endAdding(gl);
	}

	public void shutdown(GL10 gl)
	{
		mLabelMaker.shutdown(gl);
		mLabelMaker = null;
	}

	/**
	 * Find the smallest power of two >= the input value.
	 * (Doesn't work for negative numbers.)
	 */
	private int roundUpPower2(int x)
	{
		x = x - 1;
		x = x | (x >> 1);
		x = x | (x >> 2);
		x = x | (x >> 4);
		x = x | (x >> 8);
		x = x | (x >>16);

		return x + 1;
	}

	public void setValue(int value)
	{
		mDigitsCount = 0;

		do
		{
			mDigits[mDigitsCount++] = value % 10;
			value /= 10;
		} while ((value > 0) && (mDigitsCount < sMaxDigits));
	}

	public void draw(GL10 gl, float x, float y, float viewWidth, float viewHeight)
	{
		mLabelMaker.beginDrawing(gl, viewWidth, viewHeight);

		for (int pos = mDigitsCount-1; pos >= 0; pos--)
		{
			mLabelMaker.draw(gl, x, y, mLabelId[mDigits[pos]]);
			x += mWidth[mDigits[pos]];
		}

		mLabelMaker.endDrawing(gl);
	}

	public float width()
	{
		float width = 0.0f;

		for (int i = 0; i < mDigitsCount; i++) {
			width += mWidth[mDigits[i]];
		}

		return width;
	}

	private int[] mDigits = new int[sMaxDigits];
	private int mDigitsCount = 0;

	private LabelMaker mLabelMaker;
	private int[] mWidth = new int[10];
	private int[] mLabelId = new int[10];

	private final static String sStrike = "0123456789";
	private final static int sMaxDigits = 10;
}
