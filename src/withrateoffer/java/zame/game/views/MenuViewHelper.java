package zame.game.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import zame.game.Common;
import zame.game.MenuActivity;
import zame.game.R;
import zame.game.ZameApplication;

public class MenuViewHelper {
    private static final int DIALOG_RATE_OFFER = 107;

    public static boolean canExit(MenuActivity activity) {
        return (!showRateOffer(activity));
    }

    @SuppressWarnings("deprecation")
    public static boolean showRateOffer(final MenuActivity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        int showRateOfferCnt = sp.getInt("RateOfferCnt", 5);

        if (showRateOfferCnt > 0) {
            showRateOfferCnt--;
            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putInt("RateOfferCnt", showRateOfferCnt);
            spEditor.commit();

            if (showRateOfferCnt <= 0) {
                int rateOfferDlgShownCnt = sp.getInt("RateOfferDlgShownCnt", 0);
                spEditor = sp.edit();
                spEditor.putInt("RateOfferDlgShownCnt", rateOfferDlgShownCnt + 1);
                spEditor.commit();

                ZameApplication.trackEvent("Menu", "RateDialog", "", 0);
                ZameApplication.flushEvents();

                activity.showDialog(DIALOG_RATE_OFFER);
                return true;
            }
        }

        return false;
    }

    public static Dialog onCreateDialog(final MenuActivity activity, final MenuView.Data data, int id) {
        switch (id) {
            case DIALOG_RATE_OFFER: {
                AlertDialog.Builder resDialog = new AlertDialog.Builder(activity)
                    .setIcon(R.drawable.ic_dialog_alert)
                    .setTitle(R.string.dlg_rate_offer)
                    .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (Common.openMarket(activity, ZameApplication.self.getPackageName())) {
                                ZameApplication.trackEvent("Menu", "RateDialogOk", "", 0);
                                ZameApplication.flushEvents();
                            }

                            activity.finish();
                        }
                    });

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                int rateOfferDlgShownCnt = sp.getInt("RateOfferDlgShownCnt", 0);

                DialogInterface.OnClickListener remindMeLaterClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        rateOfferRemindMeLater(activity);
                    }
                };

                if (rateOfferDlgShownCnt > 1) {
                    resDialog.setNegativeButton(R.string.dlg_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ZameApplication.trackEvent("Menu", "RateDialogCancel", "", 0);
                            ZameApplication.flushEvents();
                            activity.finish();
                        }
                    });

                    if (rateOfferDlgShownCnt < 5) {
                        resDialog.setNeutralButton(R.string.dlg_later, remindMeLaterClickListener);
                    }
                } else {
                    resDialog.setNegativeButton(R.string.dlg_later, remindMeLaterClickListener);
                }

                resDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        rateOfferRemindMeLater(activity);
                    }
                });

                return resDialog.create();
            }
        }

        return null;
    }

    private static void rateOfferRemindMeLater(MenuActivity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putInt("RateOfferCnt", 5);
        spEditor.commit();

        ZameApplication.trackEvent("Menu", "RateDialogLater", "", 0);
        ZameApplication.flushEvents();
        activity.finish();
    }
}
