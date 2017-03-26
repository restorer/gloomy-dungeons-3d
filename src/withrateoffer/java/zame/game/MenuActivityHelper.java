package zame.game;

import zame.game.views.MenuViewHelper;

@SuppressWarnings("WeakerAccess")
public final class MenuActivityHelper {
    private MenuActivityHelper() {
    }

    public static boolean onBackPressed(MenuActivity activity) {
        return (!MenuViewHelper.showRateOffer(activity));
    }
}
