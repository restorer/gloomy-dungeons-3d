package {$PKG_CURR};

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import {$PKG_ROOT}.game.Common;

public class ListPreference extends android.preference.ListPreference
{
	public ListPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)
	{
		super.onSetInitialValue(restore, defaultValue);

		if (getEntry() != null) {
			setSummary(getEntry());
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		super.onDialogClosed(positiveResult);

		if (getEntry() != null) {
			setSummary(getEntry());
		}
	}
}
