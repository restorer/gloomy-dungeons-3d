package zame.game;

import android.content.SharedPreferences;
import com.zeemote.zc.event.ButtonEvent;

@SuppressWarnings("WeakerAccess")
public final class ConfigZeemote {
    public static float zeemoteXAccel;
    public static float zeemoteYAccel;
    public static int[] zeemoteButtonMappings;

    private ConfigZeemote() {
    }

    @SuppressWarnings("MagicNumber")
    public static void initialize(SharedPreferences sp) {
        int tmpZeemoteXAccel = sp.getInt("ZeemoteXAccel", 8); // zeemoteXAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)

        zeemoteXAccel = ((tmpZeemoteXAccel >= 8)
                ? ((((float)tmpZeemoteXAccel - 8.0f) / 7.0f) + 1.0f)
                : (1.0f / (2.0f - (((float)tmpZeemoteXAccel - 1.0f) / 7.0f))));

        int tmpZeemoteYAccel = sp.getInt("ZeemoteYAccel", 8); // zeemoteYAccel : 0.5 (1) -> 1.0 (8) -> 2.0 (15)

        zeemoteYAccel = ((tmpZeemoteYAccel >= 8)
                ? ((((float)tmpZeemoteYAccel - 8.0f) / 7.0f) + 1.0f)
                : (1.0f / (2.0f - (((float)tmpZeemoteYAccel - 1.0f) / 7.0f))));

        zeemoteButtonMappings = new int[Math.max(Math.max(Math.max(ButtonEvent.BUTTON_A, ButtonEvent.BUTTON_B),
                ButtonEvent.BUTTON_C), ButtonEvent.BUTTON_D) + 1];

        zeemoteButtonMappings[ButtonEvent.BUTTON_A] = Config.getControlMaskByName(sp.getString("ZeemoteMappingFire",
                "None"));

        zeemoteButtonMappings[ButtonEvent.BUTTON_B] = Config.getControlMaskByName(sp.getString("ZeemoteMappingA",
                "None"));

        zeemoteButtonMappings[ButtonEvent.BUTTON_C] = Config.getControlMaskByName(sp.getString("ZeemoteMappingB",
                "None"));

        zeemoteButtonMappings[ButtonEvent.BUTTON_D] = Config.getControlMaskByName(sp.getString("ZeemoteMappingC",
                "None"));
    }

    public static boolean isZeemoteControlsType(String controlsTypeStr) {
        return "Zeemote".equals(controlsTypeStr);
    }
}
