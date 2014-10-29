package zame.game;

import android.os.Handler;
import android.util.Log;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.util.ArrayList;

public class ZameApplicationAnalyticsHelper
{
    private final Handler handler = new Handler();

    public static class EventToTrack
    {
        public String category;
        public String action;
        public String label;
        public int value;

        public EventToTrack(String category, String action, String label, int value)
        {
            this.category = category;
            this.action = action;
            this.label = label;
            this.value = value;
        }
    }

    private GoogleAnalyticsTracker tracker = null;
    private ArrayList<EventToTrack> eventsToTrack = new ArrayList<EventToTrack>();

    public void trackPageView(final String pageUrl)
    {
        handler.post(new Runnable() {
            public void run() {
                try {
                    tracker.trackPageView(pageUrl);
                } catch (Exception ex) {
                    Log.e(Common.LOG_KEY, "Exception", ex);
                }
            }
        });
    }

    public void trackEvent(final String category, final String action, final String label, final int value)
    {
        handler.post(new Runnable() {
            public void run() {
                eventsToTrack.add(new EventToTrack(category, action, label, value));
            }
        });
    }

    public void flushEvents()
    {
        handler.post(new Runnable() {
            public void run() {
                for (EventToTrack ev : eventsToTrack) {
                    try {
                        tracker.trackEvent(ev.category, ev.action, ev.label, ev.value);
                    } catch (Exception ex) {
                        Log.e(Common.LOG_KEY, "Exception", ex);
                    }
                }

                eventsToTrack.clear();
            }
        });
    }

    public void onCreate(ZameApplication app, String initialControlsType)
    {
        try {
            tracker = GoogleAnalyticsTracker.getInstance();
            tracker.startNewSession(BuildConfig.GA_ACCT, 10, app);
            tracker.setDebug(true);
            tracker.setDryRun(false);
            tracker.setSampleRate(100);
            tracker.setAnonymizeIp(true);
            tracker.setCustomVar(1, "Version", app.getVersionName(), 2);    // slot: 1, scope: session
            tracker.setCustomVar(2, "InitialControlsType", initialControlsType, 2); // slot: 2, scope: session
        } catch (Exception ex) {
            Log.e(Common.LOG_KEY, "Exception", ex);
            tracker = null;
        }
    }
}
