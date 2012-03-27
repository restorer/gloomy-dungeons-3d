package {$PKG_CURR};

// #if USE_MAILRU

import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLEncoder;
import java.math.BigInteger;

// #end

public class MailRuApi
{
	// #if USE_MAILRU

	public static final String APP_ID = "XX";
	public static final String PRIVATE_KEY = "XXXXXXXX";

	public static void initialize(Context appContext)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
		boolean mailRuInitialized = sp.getBoolean("MailRuInitialized", false);

		if (mailRuInitialized) {
			// return;
		}

		String event_time = String.valueOf(System.currentTimeMillis() / 1000);

		List<NameValuePair> data = new ArrayList<NameValuePair>(2);
		data.add(new BasicNameValuePair("ip", getLocalIpAddress()));
		data.add(new BasicNameValuePair("event_time", event_time));
		data.add(new BasicNameValuePair("sys_name", "Android"));
		data.add(new BasicNameValuePair("sys_version", android.os.Build.VERSION.RELEASE));

		if (sendRequest(appContext, "log.registration_native", data, event_time) != null) {
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putBoolean("MailRuInitialized", true);
			spEditor.commit();
		}
	}

	protected static HttpResponse sendRequest(Context appContext, String method, List<NameValuePair> additionalData, String time)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
		String uid = sp.getString("MailRuUID", "");

		if (uid.length() == 0) {
			uid = toUnsignedString(UUID.randomUUID().getMostSignificantBits());
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putString("MailRuUID", uid);
			spEditor.commit();
		}

		if (time == null) {
			time = String.valueOf(System.currentTimeMillis() / 1000);
		}

		String sig = md5(method + APP_ID + uid + time + PRIVATE_KEY);

		List<NameValuePair> data = new ArrayList<NameValuePair>(2);
		data.add(new BasicNameValuePair("app_id", APP_ID));
		data.add(new BasicNameValuePair("method", method));
		data.add(new BasicNameValuePair("sig", sig));
		data.add(new BasicNameValuePair("time", time));
		data.add(new BasicNameValuePair("uid", uid));
		data.add(new BasicNameValuePair("secure", "1"));
		data.add(new BasicNameValuePair("format", "json"));

		for (NameValuePair pair : additionalData) {
			data.add(pair);
		}

		try {
			StringBuilder sb = new StringBuilder();

			for (NameValuePair pair : data) {
				if (sb.length() != 0) {
					sb.append("&");
				}

				sb.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			}

			Log.w(Common.LOG_KEY, "[mailru] req = " + sb.toString());

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://m.games.mail.ru/api/?" + sb.toString());
			HttpResponse resp = httpClient.execute(httpPost);

			try {
				Log.w(Common.LOG_KEY, "[mailru] resp = " + EntityUtils.toString(resp.getEntity(), "UTF-8"));
			} catch (Exception ex) {
				Log.e(Common.LOG_KEY, "Exception", ex);
			}

			return resp;
		} catch (ClientProtocolException ex) {
			Log.e(Common.LOG_KEY, "Exception", ex);
		} catch (IOException ex) {
			Log.e(Common.LOG_KEY, "Exception", ex);
		} catch (Exception ex) {
			Log.e(Common.LOG_KEY, "Exception", ex);
		}

		return null;
	}

	protected static String md5(String s)
	{
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(s.getBytes());
			byte[] a = digester.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);

			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	protected static String getLocalIpAddress()
	{
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(Common.LOG_KEY, ex.toString());
		}

		return null;
	}

	public static String toUnsignedString(long num)
	{
		if (num >= 0) {
			return String.valueOf(num);
		} else {
			return BigInteger.valueOf(num).add(BigInteger.ZERO.setBit(64)).toString();
		}
	}

	// #end
}
