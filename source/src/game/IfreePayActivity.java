package {$PKG_CURR};

// #if !TYPE_IFREE

import android.app.Activity;

public class IfreePayActivity extends Activity
{
}

// #end
// #if TYPE_IFREE

import android.app.*;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.content.pm.PackageManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.text.DecimalFormat;
import java.util.Set;
import java.net.UnknownHostException;

import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.google.android.apps.analytics.easytracking.EasyTracker;

import com.ifree.sdk.monetization.Monetization;
import com.ifree.sdk.monetization.PaymentMethod;
import com.ifree.sdk.monetization.PaymentState;
import com.ifree.sdk.monetization.PurchaseListener;
import com.ifree.sdk.monetization.Tariff;
import com.ifree.sdk.monetization.exception.PurchaseException;

public class IfreePayActivity extends TrackedActivity
{
	private String latestTransactionId;
	private String latestMetaInfo;
	private Tariff tariff;

	private static boolean instantMusicPause = true;

	public static IfreePayActivity self;
	public final Handler handler = new Handler();

	public final Runnable paymentSuccessfull = new Runnable() {
		public void run() {
			ZameApplication.nonProcessedPaymentResult = 0;
			showResult(getString(R.string.ifree_payment_successfull));
		}
	};

	public final Runnable paymentCancelled = new Runnable() {
		public void run() {
			ZameApplication.nonProcessedPaymentResult = 0;

			showResult(getString(
				latestMetaInfo.equals("paypal-full") ? R.string.ifree_paypal_cancelled : R.string.ifree_payment_cancelled
			));
		}
	};

	@Override
	protected void onCreate(Bundle state)
	{
		super.onCreate(state);
		self = this;

		ZameApplication.nonProcessedPaymentResult = 0;

		SoundManager.init(getApplicationContext(), getAssets(), true);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (IfreeEngine.initializeAndProcessLostTransactions()) {
			finish();
			return;
		}

		setContentView(R.layout.ifree_pay);
		Typeface btnTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + getString(R.string.font_name));

		((Button)findViewById(R.id.BtnPayPal)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnSms)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnOk)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnCancel)).setTypeface(btnTypeface);
		((Button)findViewById(R.id.BtnClose)).setTypeface(btnTypeface);
		((TextView)findViewById(R.id.TxtInfo)).setTypeface(btnTypeface);

		((Button)findViewById(R.id.BtnPayPal)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnSms)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnOk)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnCancel)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnClose)).setVisibility(View.GONE);

		View.OnClickListener cancelOnClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				instantMusicPause = false;
				finish();
			}
		};

		((Button)findViewById(R.id.BtnCancel)).setOnClickListener(cancelOnClickListener);
		((Button)findViewById(R.id.BtnClose)).setOnClickListener(cancelOnClickListener);

		if (MenuActivity.self.monetization == null) {
			showResult(getString(R.string.ifree_no_connection));
			return;
		}

		Set<PaymentMethod> availableMethods = MenuActivity.self.monetization.getAvailablePaymentMethods();

		if (!availableMethods.contains(PaymentMethod.SMS) && !availableMethods.contains(PaymentMethod.PAYPAL)) {
			showResult(getString(R.string.ifree_no_connection));
			return;
		}

		if (availableMethods.contains(PaymentMethod.SMS) && !availableMethods.contains(PaymentMethod.PAYPAL)) {
			initializeSms();
			return;
		}

		if (!availableMethods.contains(PaymentMethod.SMS) && availableMethods.contains(PaymentMethod.PAYPAL)) {
			initializePayPal();
			return;
		}

		((Button)findViewById(R.id.BtnPayPal)).setVisibility(View.VISIBLE);
		((Button)findViewById(R.id.BtnSms)).setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.TxtInfo)).setText(getString(R.string.ifree_select_payment_method));

		((Button)findViewById(R.id.BtnPayPal)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				initializePayPal();
			}
		});

		((Button)findViewById(R.id.BtnSms)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);
				initializeSms();
			}
		});
	}

	public void initializeCommon()
	{
		((Button)findViewById(R.id.BtnPayPal)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnSms)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnOk)).setVisibility(View.VISIBLE);
		((Button)findViewById(R.id.BtnCancel)).setVisibility(View.VISIBLE);
	}

	public void initializePayPal()
	{
		final String paypalCurrency = "USD";
		final int paypalAmount = 100;

		// nosfer_1313670035_per@mail.ru
		// 12345678

		initializeCommon();

		((TextView)findViewById(R.id.TxtInfo)).setText(String.format(
			getString(R.string.ifree_paypal_info),
			paypalCurrency,
			new DecimalFormat("#.##").format(paypalAmount / 100.f)
		));

		((Button)findViewById(R.id.BtnOk)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				try {
					latestMetaInfo = "paypal-full";
					latestTransactionId = MenuActivity.self.monetization.payPalPay(paypalAmount, paypalCurrency, latestMetaInfo, "Gloomy Dungeons 3D", "Gloomy Dungeons 3D Full");
					EasyTracker.getTracker().trackPageView("/pay/using-paypal/action");
				} catch (Exception ex) {
					showResult(getString(R.string.ifree_paypal_cancelled));
					return;
				}

				Toast.makeText(IfreePayActivity.this, getString(R.string.ifree_please_wait), Toast.LENGTH_LONG).show();
				((Button)findViewById(R.id.BtnOk)).setEnabled(false);
				((Button)findViewById(R.id.BtnCancel)).setEnabled(false);
			}
		});

		EasyTracker.getTracker().trackPageView("/pay/using-paypal");
	}

	public void initializeSms()
	{
		try {
			tariff = MenuActivity.self.monetization.findNearestTariffWithRate("USD", 100, 50, 100);
		} catch (Exception ex) {
			Log.e(Common.LOG_KEY, "Exception: " + ex, ex);
			showResult(getString(R.string.ifree_no_connection));
			return;
		}

		if (tariff == null) {
			/*
			try {
				String promotionId = getPackageManager()
					.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)
					.metaData
					.getString("com.ifree.sdk.monetization.PROMOTION_ID");

				HttpResponse resp = (new DefaultHttpClient()).execute(new HttpGet(
					"https://monetization.i-free.ru/api1/update/get_promotion_settings?&promotion=" +
					(promotionId == null ? "" : promotionId)
				));
			} catch (UnknownHostException ex) {
				showResult(getString(R.string.ifree_no_connection));
				return;
			} catch (Exception ex) {
				Log.e(Common.LOG_KEY, "Exception", ex);
				showResult(getString(R.string.ifree_no_connection));
				return;
			}
			*/

			// #if USE_AUTO_UNLOCK_IF_NO_TARIFF
				IfreeEngine.unlockGame();
			// #end
			// #if !USE_AUTO_UNLOCK_IF_NO_TARIFF
				showResult(getString(R.string.ifree_country_not_available));
			// #end

			return;
		}

		initializeCommon();

		((TextView)findViewById(R.id.TxtInfo)).setText(String.format(
			getString(R.string.ifree_info),
			tariff.getCurrency(),
			new DecimalFormat("#.##").format(tariff.getAmount() / 100.f)
		));

		((Button)findViewById(R.id.BtnOk)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SoundManager.playSound(SoundManager.SOUND_BTN_PRESS);

				try {
					latestMetaInfo = "sms-full";
					latestTransactionId = MenuActivity.self.monetization.smsPay(tariff.getCurrency(), tariff.getAmount(), "Gloomy Dungeons 3D Full", latestMetaInfo);
					EasyTracker.getTracker().trackPageView("/pay/using-sms/action");
				} catch (Exception ex) {
					showResult(getString(R.string.ifree_country_not_available));
					return;
				}

				Toast.makeText(IfreePayActivity.this, getString(R.string.ifree_please_wait), Toast.LENGTH_LONG).show();
				((Button)findViewById(R.id.BtnOk)).setEnabled(false);
				((Button)findViewById(R.id.BtnCancel)).setEnabled(false);
			}
		});

		EasyTracker.getTracker().trackPageView("/pay/using-sms");
	}

	@Override
	public void onBackPressed()
	{
		instantMusicPause = false;
		super.onBackPressed();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		self = this;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if (hasFocus) {
			SoundManager.setPlaylist(SoundManager.LIST_MAIN);
			SoundManager.onStart();

			if (ZameApplication.nonProcessedPaymentResult == ZameApplication.PAYMENT_RESULT_SUCCESS) {
				IfreePayActivity.self.handler.post(IfreePayActivity.self.paymentSuccessfull);
			} else if (ZameApplication.nonProcessedPaymentResult == ZameApplication.PAYMENT_RESULT_CANCELLED) {
				IfreePayActivity.self.handler.post(IfreePayActivity.self.paymentCancelled);
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		self = null;

		SoundManager.onPause(instantMusicPause);
		instantMusicPause = true;
	}

	protected void showResult(String resultText)
	{
		((TextView)findViewById(R.id.TxtInfo)).setText(resultText);
		((Button)findViewById(R.id.BtnPayPal)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnSms)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnOk)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnCancel)).setVisibility(View.GONE);
		((Button)findViewById(R.id.BtnClose)).setVisibility(View.VISIBLE);
	}
}

// #end
