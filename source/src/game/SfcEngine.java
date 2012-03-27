package {$PKG_CURR};

// #if !TYPE_SFC

public class SfcEngine
{
}

// #end
// #if TYPE_SFC

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;
import android.util.Log;
import android.telephony.SmsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.content.Context;
import java.util.List;
import com.google.android.apps.analytics.easytracking.EasyTracker;

public class SfcEngine
{
	private BroadcastReceiver sentReceiver = null;
	private BroadcastReceiver deliveredReceiver = null;

	public SfcEngine()
	{
		sentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(ZameApplication.self, "SMS sent", Toast.LENGTH_SHORT).show();
						break;

					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(ZameApplication.self, "Generic failure", Toast.LENGTH_SHORT).show();
						break;

					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(ZameApplication.self, "No service", Toast.LENGTH_SHORT).show();
						break;

					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(ZameApplication.self, "Null PDU", Toast.LENGTH_SHORT).show();
						break;

					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(ZameApplication.self, "Radio off", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		};

		deliveredReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						ZameApplication.self.unregisterReceiver(sentReceiver);
						ZameApplication.self.unregisterReceiver(deliveredReceiver);

						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ZameApplication.self);
						SharedPreferences.Editor spEditor = sp.edit();
						spEditor.putBoolean("Charged", true);
						spEditor.commit();
						Config.charged = true;

						if (SfcBlockerViewHandler.self != null) {
							SfcBlockerViewHandler.self.handler.post(SfcBlockerViewHandler.self.paymentSuccessfull);
						} else {
							Toast.makeText(ZameApplication.self, ZameApplication.self.getString(R.string.sfc_payment_successfull), Toast.LENGTH_SHORT).show();
						}

						EasyTracker.getTracker().trackPageView("/pay/action/charged");
						break;

					case Activity.RESULT_CANCELED:
						ZameApplication.self.unregisterReceiver(sentReceiver);
						ZameApplication.self.unregisterReceiver(deliveredReceiver);

						if (SfcBlockerViewHandler.self != null) {
							SfcBlockerViewHandler.self.handler.post(SfcBlockerViewHandler.self.paymentCancelled);
						} else {
							Toast.makeText(ZameApplication.self, ZameApplication.self.getString(R.string.sfc_payment_cancelled), Toast.LENGTH_SHORT).show();
						}

						EasyTracker.getTracker().trackPageView("/pay/action/cancelled");
						break;
				}
			}
		};
	}

	public void sendSMS()
	{
		// #if SMS_PREFIX_SFC
			String text = "XXXX XXXX";
		// #end
		// #if SMS_PREFIX_RAZLO4KA
			String text = "YYYY YYYY";
		// #end

		String recipient = "XXXX";
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(ZameApplication.self, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(ZameApplication.self, 0, new Intent(DELIVERED), 0);

		ZameApplication.self.registerReceiver(sentReceiver, new IntentFilter(SENT));
		ZameApplication.self.registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		List<String> messages = sms.divideMessage(text);

		for (String message : messages) {
			sms.sendTextMessage(recipient, null, message, sentPI, deliveredPI);
		}

		EasyTracker.getTracker().trackPageView("/pay/action");
	}
}

// #end
