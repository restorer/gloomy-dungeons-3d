package {$PKG_CURR};

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.view.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import com.zeemote.zc.Controller;
import android.util.Log;

public class Config
{
	public static int controlsType;
	public static float maxRotateAngle;
	public static float trackballAcceleration;
	public static float moveSpeed;
	public static float strafeSpeed;
	public static float rotateSpeed;
	public static boolean invertRotation;
	public static float gamma;
	public static int levelTextureFilter;
	public static int weaponsTextureFilter;
	public static int[] keyMappings;
	public static float mapPosition;
	public static boolean showCrosshair;
	public static boolean rotateScreen;
	public static float zeemoteXAccel;
	public static float zeemoteYAccel;
	public static int[] zeemoteButtonMappings;
	public static boolean accelerometerEnabled;
	public static float controlsAlpha;
	public static float padXAccel;
	public static float padYAccel;
	public static float accelerometerAcceleration;

	// #if TYPE_IFREE | TYPE_SFC
		public static boolean charged;
	// #end

	protected static void updateKeyMap(SharedPreferences sp, String key, int type)
	{
		int keyCode = sp.getInt(key, 0);

		if (keyCode > 0 && keyCode < keyMappings.length) {
			keyMappings[keyCode] = type;
		}
	}

	protected static int getControlMaskByName(String name)
	{
		if (name.equals("Action")) {
			return Controls.ACTION;
		} else if (name.equals("NextWeapon")) {
			return Controls.NEXT_WEAPON;
		} else if (name.equals("ToggleMap")) {
			return Controls.TOGGLE_MAP;
		} else if (name.equals("Strafe")) {
			return Controls.STRAFE_MODE;
		} else {
			return 0;
		}
	}

	public static void initialize()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GameActivity.appContext);
		String controlsTypeStr = sp.getString("ControlsType", "Improved");

		if (controlsTypeStr.equals("Classic") || controlsTypeStr.equals("TypeA")) {
			controlsType = Controls.TYPE_CLASSIC;
		} else if (controlsTypeStr.equals("ExperimentalA") || controlsTypeStr.equals("Experimental") || controlsTypeStr.equals("TypeC")) {
			controlsType = Controls.TYPE_EXPERIMENTAL_A;
		} else if (controlsTypeStr.equals("ExperimentalB")) {
			controlsType = Controls.TYPE_EXPERIMENTAL_B;
		} else if (controlsTypeStr.equals("Zeemote")) {
			controlsType = Controls.TYPE_ZEEMOTE;
		} else if (controlsTypeStr.equals("PadL")) {
			controlsType = Controls.TYPE_PAD_L;
		} else if (controlsTypeStr.equals("PadR")) {
			controlsType = Controls.TYPE_PAD_R;
		} else {
			controlsType = Controls.TYPE_IMPROVED;
		}

		maxRotateAngle = (float)sp.getInt("MaxRotateAngle", 30);
		trackballAcceleration = (float)sp.getInt("TrackballAcceleration", 40);
		moveSpeed = 19.0f - (float)sp.getInt("MoveSpeed", 14);		// default = 5.0f
		strafeSpeed = 19.0f - (float)sp.getInt("StrafeSpeed", 7);	// default = 12.0f
		rotateSpeed = (float)sp.getInt("RotateSpeed", 6) / 2.0f;	// default = 3.0f
		invertRotation = sp.getBoolean("InvertRotation", false);
		gamma = (float)sp.getInt("Gamma", 0) / 25.0f;
		levelTextureFilter = (sp.getBoolean("LevelTextureSmoothing", false) ? GL10.GL_LINEAR : GL10.GL_NEAREST);
		weaponsTextureFilter = (sp.getBoolean("WeaponsTextureSmoothing", true) ? GL10.GL_LINEAR : GL10.GL_NEAREST);

		int tmpZeemoteXAccel = sp.getInt("ZeemoteXAccel", 8);	// zeemoteXAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)
		zeemoteXAccel = ((tmpZeemoteXAccel >= 8) ? (((float)tmpZeemoteXAccel - 8.0f) / 7.0f + 1.0f) : (1.0f / (2.0f - ((float)tmpZeemoteXAccel - 1.0f) / 7.0f)));

		int tmpZeemoteYAccel = sp.getInt("ZeemoteYAccel", 8);	// zeemoteYAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)
		zeemoteYAccel = ((tmpZeemoteYAccel >= 8) ? (((float)tmpZeemoteYAccel - 8.0f) / 7.0f + 1.0f) : (1.0f / (2.0f - ((float)tmpZeemoteYAccel - 1.0f) / 7.0f)));

		zeemoteButtonMappings = new int[Math.max(
			Math.max(
				Math.max(Controller.GAME_FIRE, Controller.GAME_A),
				Controller.GAME_B
			),
			Controller.GAME_C
		) + 1];

		zeemoteButtonMappings[Controller.GAME_FIRE] = getControlMaskByName(sp.getString("ZeemoteMappingFire", "None"));
		zeemoteButtonMappings[Controller.GAME_A] = getControlMaskByName(sp.getString("ZeemoteMappingA", "None"));
		zeemoteButtonMappings[Controller.GAME_B] = getControlMaskByName(sp.getString("ZeemoteMappingB", "None"));
		zeemoteButtonMappings[Controller.GAME_C] = getControlMaskByName(sp.getString("ZeemoteMappingC", "None"));

		keyMappings = new int[KeyEvent.getMaxKeyCode()];

		for (int i = 0; i < keyMappings.length; i++) {
			keyMappings[i] = 0;
		}

		updateKeyMap(sp, "KeyForward", Controls.FORWARD);
		updateKeyMap(sp, "KeyBackward", Controls.BACKWARD);
		updateKeyMap(sp, "KeyRotateLeft", Controls.ROTATE_LEFT);
		updateKeyMap(sp, "KeyRotateRight", Controls.ROTATE_RIGHT);
		updateKeyMap(sp, "KeyStrafeLeft", Controls.STRAFE_LEFT);
		updateKeyMap(sp, "KeyStrafeRight", Controls.STRAFE_RIGHT);
		updateKeyMap(sp, "KeyAction", Controls.ACTION);
		updateKeyMap(sp, "KeyNextWeapon", Controls.NEXT_WEAPON);
		updateKeyMap(sp, "KeyToggleMap", Controls.TOGGLE_MAP);
		updateKeyMap(sp, "KeyStrafeMode", Controls.STRAFE_MODE);

		mapPosition = (float)(sp.getInt("MapPosition", 5) - 5) / 5.0f;
		showCrosshair = sp.getBoolean("ShowCrosshair", false);
		rotateScreen = sp.getBoolean("RotateScreen", false);
		accelerometerEnabled = sp.getBoolean("AccelerometerEnabled", false);
		controlsAlpha = (float)sp.getInt("ControlsAlpha", 3) / 10.0f;

		int tmpPadXAccel = sp.getInt("PadXAccel", 6);	// padXAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)
		padXAccel = ((tmpPadXAccel >= 8) ? (((float)tmpPadXAccel - 8.0f) / 7.0f + 1.0f) : (1.0f / (2.0f - ((float)tmpPadXAccel - 1.0f) / 7.0f)));

		int tmpPadYAccel = sp.getInt("PadYAccel", 10);	// padYAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)
		padYAccel = ((tmpPadYAccel >= 8) ? (((float)tmpPadYAccel - 8.0f) / 7.0f + 1.0f) : (1.0f / (2.0f - ((float)tmpPadYAccel - 1.0f) / 7.0f)));

		accelerometerAcceleration = (float)sp.getInt("AccelerometerAcceleration", 5);

		// #if TYPE_IFREE | TYPE_SFC
			charged = sp.getBoolean("Charged", false);
		// #end
	}
}
