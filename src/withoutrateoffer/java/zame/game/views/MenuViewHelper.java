package zame.game.views;

import android.app.Dialog;
import zame.game.MenuActivity;

public final class MenuViewHelper {
    private MenuViewHelper() {
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean canExit(@SuppressWarnings("UnusedParameters") MenuActivity activity) {
        return true;
    }

    @SuppressWarnings({ "WeakerAccess", "UnusedParameters" })
    public static Dialog onCreateDialog(final MenuActivity activity, final MenuView.Data data, int id) {
        return null;
    }
}
