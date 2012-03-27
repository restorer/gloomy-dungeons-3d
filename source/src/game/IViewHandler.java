package {$PKG_CURR};

import android.app.Activity;

public interface IViewHandler
{
	void setView(Activity callerActivity);
	void onResume();
	void onWindowFocusChanged(boolean hasFocus);
	void onPause();
}
