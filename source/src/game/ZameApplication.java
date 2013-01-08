package {$PKG_CURR};

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

// @ReportsCrashes(formKey = "XXXXXXXX")
public class ZameApplication extends Application
{
	public static ZameApplication self;

	@Override
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
		self = this;
	}
}
