package zame.game;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ZameApplication extends Application {
    public static ZameApplication self;

    private String cachedVersionName;

    @SuppressWarnings("ConstantConditions")
    private ZameApplicationAnalyticsHelper analyticsHelper = (BuildConfig.WITH_ANALYTICS
            ? new ZameApplicationAnalyticsHelper()
            : null);

    public static void trackPageView(String pageUrl) {
        if ((ZameApplication.self != null) && (ZameApplication.self.analyticsHelper != null)) {
            ZameApplication.self.analyticsHelper.trackPageView(pageUrl);
        }
    }

    public static void trackEvent(String category, String action, String label, int value) {
        if ((ZameApplication.self != null) && (ZameApplication.self.analyticsHelper != null)) {
            ZameApplication.self.analyticsHelper.trackEvent(category, action, label, value);
        }
    }

    public static void flushEvents() {
        if ((ZameApplication.self != null) && (ZameApplication.self.analyticsHelper != null)) {
            ZameApplication.self.analyticsHelper.flushEvents();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        String initialControlsType = sp.getString("InitialControlsType", "");

        //noinspection SizeReplaceableByIsEmpty
        if (initialControlsType.length() == 0) {
            Common.init();
            initialControlsType = "Improved";

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putString("InitialControlsType", initialControlsType);
            spEditor.putString("ControlsType", initialControlsType);
            spEditor.putString("PrevControlsType", initialControlsType);
            spEditor.commit();
        }

        if (analyticsHelper != null) {
            analyticsHelper.onCreate(this, initialControlsType);
        }
    }

    public String getVersionName() {
        if (cachedVersionName == null) {
            cachedVersionName = "xxxx.xx.xx.xxxx";

            try {
                cachedVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception ex) {
                Log.e(Common.LOG_KEY, "Exception", ex);
            }
        }

        return cachedVersionName;
    }
}
