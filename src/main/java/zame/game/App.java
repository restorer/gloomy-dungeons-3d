package zame.game;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class App extends Application {
    public static App self;

    private String cachedVersionName;

    @SuppressWarnings("ConstantConditions")
    private ZameApplicationAnalyticsHelper analyticsHelper = new ZameApplicationAnalyticsHelper();

    public static void trackPageView(String pageUrl) {
        if ((App.self != null) && (App.self.analyticsHelper != null)) {
            App.self.analyticsHelper.trackPageView(pageUrl);
        }
    }

    public static void trackEvent(String category, String action, String label, int value) {
        if ((App.self != null) && (App.self.analyticsHelper != null)) {
            App.self.analyticsHelper.trackEvent(category, action, label, value);
        }
    }

    public static void flushEvents() {
        if ((App.self != null) && (App.self.analyticsHelper != null)) {
            App.self.analyticsHelper.flushEvents();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        self = this;
        analyticsHelper.onCreate(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        String initialControlsType = sp.getString("InitialControlsType", "");

        //noinspection SizeReplaceableByIsEmpty
        if (initialControlsType.length() == 0) {
            initialControlsType = "Improved";

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putString("InitialControlsType", initialControlsType);
            spEditor.putString("ControlsType", initialControlsType);
            spEditor.putString("PrevControlsType", initialControlsType);
            spEditor.apply();
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
