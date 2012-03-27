package {$PKG_CURR};

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

// #if USE_HOOKEDMEDIA
	import com.hookedmediagroup.wasabi.WasabiApi;
// #end

@ReportsCrashes(formKey = "XXXXXXXX")
public class ZameApplication extends Application
{
	public static ZameApplication self;

	// #if TYPE_IFREE
		public static final int PAYMENT_RESULT_SUCCESS = 1;
		public static final int PAYMENT_RESULT_CANCELLED = 2;

		public static IfreeEngine ifreeEngine;
		public static int nonProcessedPaymentResult = 0;
	// #end
	// #if TYPE_SFC
		public static SfcEngine sfcEngine;
	// #end

	@Override
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
		self = this;

		// #if USE_HOOKEDMEDIA
			WasabiApi.init(getApplicationContext());
		// #end
		// #if TYPE_IFREE
			ifreeEngine = new IfreeEngine();
		// #end
		// #if TYPE_SFC
			sfcEngine = new SfcEngine();
		// #end
	}
}
