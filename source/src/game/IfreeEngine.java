package {$PKG_CURR};

// #if !TYPE_IFREE

public class IfreeEngine
{
}

// #end
// #if TYPE_IFREE

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;
import android.util.Log;

import com.google.android.apps.analytics.easytracking.EasyTracker;

import java.util.List;

import com.ifree.sdk.monetization.Monetization;
import com.ifree.sdk.monetization.MonetizationInitializer;
import com.ifree.sdk.monetization.PaymentMethod;
import com.ifree.sdk.monetization.PaymentState;
import com.ifree.sdk.monetization.PurchaseListener;
import com.ifree.sdk.monetization.TransactionInfo;
import com.ifree.sdk.monetization.exception.PurchaseException;

public class IfreeEngine implements PurchaseListener
{
	public IfreeEngine()
	{
		try {
			new MonetizationInitializer(ZameApplication.self);
		} catch (PurchaseException ex) {
			Log.e(Common.LOG_KEY, "MonetizationInitializer error: " + ex, ex);
		}
	}

	public static boolean initializeAndProcessLostTransactions()
	{
		try {
			if (MenuActivity.self.monetization != null) {
				MenuActivity.self.monetization.unregisterListener(ZameApplication.ifreeEngine);
				MenuActivity.self.monetization = null;
			}

			MenuActivity.self.monetization = new Monetization(ZameApplication.ifreeEngine, MenuActivity.self);
		} catch (PurchaseException ex) {
			Log.e(Common.LOG_KEY, "Exception: " + ex, ex);
			MenuActivity.self.monetization = null;
			return false;
		}

		List<TransactionInfo> transactions = MenuActivity.self.monetization.getLostMoneyCharged();

		for (TransactionInfo transaction : transactions) {
			Log.w(Common.LOG_KEY, "Transaction " + transaction.getTransactionId() + " restored, charged=" + (transaction.isMoneyCharged() ? "y" : "n"));

			if (transaction.isMoneyCharged() && !Config.charged) {
				unlockGame();
			}

			MenuActivity.self.monetization.confirmTransaction(transaction.getTransactionId());
		}

		return Config.charged;
	}

	public static void unlockGame()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ZameApplication.self);
		SharedPreferences.Editor spEditor = sp.edit();
		spEditor.putBoolean("Charged", true);
		spEditor.commit();
		Config.charged = true;
		Toast.makeText(ZameApplication.self, ZameApplication.self.getString(R.string.ifree_unlocked), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPurchaseEventReceive(PaymentMethod paymentMethod, PaymentState state, String transactionId, String metaInfo)
	{
		if (state.equals(PaymentState.MONEY_CHARGED)) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ZameApplication.self);
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putBoolean("Charged", true);
			spEditor.commit();
			Config.charged = true;

			ZameApplication.nonProcessedPaymentResult = ZameApplication.PAYMENT_RESULT_SUCCESS;

			if (IfreePayActivity.self != null) {
				IfreePayActivity.self.handler.post(IfreePayActivity.self.paymentSuccessfull);
			} else {
				Toast.makeText(ZameApplication.self, ZameApplication.self.getString(R.string.ifree_payment_successfull), Toast.LENGTH_SHORT).show();
			}

			EasyTracker.getTracker().trackPageView("/pay/action/charged");
		} else if (state.equals(PaymentState.CANCELLED)) {
			ZameApplication.nonProcessedPaymentResult = ZameApplication.PAYMENT_RESULT_CANCELLED;

			if (IfreePayActivity.self != null) {
				IfreePayActivity.self.handler.post(IfreePayActivity.self.paymentCancelled);
			} else {
				Toast.makeText(ZameApplication.self, ZameApplication.self.getString(
					metaInfo.equals("paypal-full") ? R.string.ifree_paypal_cancelled : R.string.ifree_payment_cancelled
				), Toast.LENGTH_SHORT).show();
			}

			EasyTracker.getTracker().trackPageView("/pay/action/cancelled");
		}
	}
}

// #end
