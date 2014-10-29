package zame.game;

import zame.game.views.MenuViewHelper;

public class MenuActivityHelper {
    public static boolean onBackPressed(MenuActivity activity) {
        return (!MenuViewHelper.showRateOffer(activity));
    }
}
