package zame.game;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

@SuppressWarnings("WeakerAccess")
public class ZameApplicationAnalyticsHelper {
    public void trackPageView(final String pageUrl) {
        // TODO
    }

    public void trackEvent(final String category, final String action, final String label, final int value) {
        // TODO
    }

    public void flushEvents() {
        // probably do nothing for Fabric
    }

    public void onCreate(ZameApplication app) {
        Fabric.with(new Fabric.Builder(app).kits(new Crashlytics()).debuggable(BuildConfig.DEBUG).build());

        // TODO:
        // tracker.setCustomVar(1, "Version", app.getVersionName(), 2); // slot: 1, scope: session
    }

    public void setInitialControlsType(String initialControlsType) {
        // TODO:
        // tracker.setCustomVar(2, "InitialControlsType", initialControlsType, 2); // slot: 2, scope: session
    }
}
