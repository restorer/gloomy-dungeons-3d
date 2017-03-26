package zame.game.engine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.Config;
import zame.game.Renderer;
import zame.game.ZameApplication;

public final class Controls {
    @SuppressWarnings("WeakerAccess")
    public static class ControlAcceleration {
        public static final float MIN_ACCELERATION = 0.01f;

        public float value;
        public float step;
        public boolean updated;

        public ControlAcceleration(float step) {
            this.step = step;
        }

        public boolean active() {
            return ((value <= -MIN_ACCELERATION) || (value >= MIN_ACCELERATION));
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ControlAccelerationBind {
        public int controlType;
        public int accelerationType;
        public int mult;

        public ControlAccelerationBind(int controlType, int accelerationType, int mult) {
            this.controlType = controlType;
            this.accelerationType = accelerationType;
            this.mult = mult;
        }
    }

    @SuppressWarnings({ "WeakerAccess", "MagicNumber" })
    public static class ControlItem {
        public int x;
        public int y;
        public int type;
        public boolean decoration;
        public int icon;

        public ControlItem(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.decoration = false;

            updateIcon();
        }

        public ControlItem(int x, int y, int type, boolean decoration) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.decoration = decoration;

            updateIcon();
        }

        public void updateIcon() {
            switch (type) {
                case FORWARD:
                    icon = 0;
                    break;

                case BACKWARD:
                    icon = 1;
                    break;

                case STRAFE_LEFT:
                    icon = 2;
                    break;

                case STRAFE_RIGHT:
                    icon = 3;
                    break;

                case ACTION:
                    icon = 4;
                    break;

                case NEXT_WEAPON:
                    icon = 5;
                    break;

                case ROTATE_LEFT:
                    icon = 6;
                    break;

                case ROTATE_RIGHT:
                    icon = 7;
                    break;

                case TOGGLE_MAP:
                    icon = 14;
                    break;

                case OPEN_MENU:
                    icon = (TextureLoader.BASE_ADDITIONAL - TextureLoader.BASE_ICONS) + 1;
                    break;

                default:
                    icon = 14;
                    break;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ControlVariant {
        public ControlItem[] items;
        public boolean slidable;
        public float statsBaseY;
        public float keysBaseY;
        public float debugLineBaseY;
        public boolean hasMap;
        public int mapX;
        public int mapY;
        public int[][] map;
        public boolean hasPad;
        public int padX;
        public int padY;

        public ControlVariant(boolean slidable,
                float statsBaseY,
                float keysBaseY,
                float debugLineBaseY,
                ControlItem[] items) {

            this.slidable = slidable;
            this.statsBaseY = statsBaseY;
            this.keysBaseY = keysBaseY;
            this.debugLineBaseY = debugLineBaseY;
            this.items = items;
            this.hasMap = false;
            this.hasPad = false;
        }

        public ControlVariant(boolean slidable,
                float statsBaseY,
                float keysBaseY,
                float debugLineBaseY,
                ControlItem[] items,
                int padX,
                int padY) {

            this.slidable = slidable;
            this.statsBaseY = statsBaseY;
            this.keysBaseY = keysBaseY;
            this.debugLineBaseY = debugLineBaseY;
            this.items = items;
            this.hasMap = false;
            this.hasPad = true;
            this.padX = padX;
            this.padY = padY;
        }

        public ControlVariant(boolean slidable,
                float statsBaseY,
                float keysBaseY,
                float debugLineBaseY,
                ControlItem[] items,
                int mapX,
                int mapY,
                int[][] map) {

            this.slidable = slidable;
            this.statsBaseY = statsBaseY;
            this.keysBaseY = keysBaseY;
            this.debugLineBaseY = debugLineBaseY;
            this.items = items;
            this.hasMap = true;
            this.mapX = mapX;
            this.mapY = mapY;
            this.map = map;
            this.hasPad = false;
        }
    }

    public static final int TYPE_CLASSIC = 0;
    public static final int TYPE_IMPROVED = 1;
    public static final int TYPE_PAD_L = 2;
    public static final int TYPE_PAD_R = 3;
    public static final int TYPE_EXPERIMENTAL_A = 4;
    public static final int TYPE_EXPERIMENTAL_B = 5;
    public static final int TYPE_ZEEMOTE = 6; // Must be last

    public static final int FORWARD = 1;
    public static final int BACKWARD = 2;
    public static final int STRAFE_LEFT = 4;
    public static final int STRAFE_RIGHT = 8;
    public static final int ACTION = 16;
    public static final int NEXT_WEAPON = 32;
    public static final int ROTATE_LEFT = 64;
    public static final int ROTATE_RIGHT = 128;
    public static final int TOGGLE_MAP = 256;
    public static final int STRAFE_MODE = 512;
    @SuppressWarnings("WeakerAccess") public static final int OPEN_MENU = 1024;
    @SuppressWarnings("WeakerAccess") public static final int MASK_MAX = 2048;

    @SuppressWarnings("WeakerAccess") public static final int ACCELERATION_MOVE = 0;
    @SuppressWarnings("WeakerAccess") public static final int ACCELERATION_STRAFE = 1;
    @SuppressWarnings("WeakerAccess") public static final int ACCELERATION_ROTATE = 2;

    private static final int POINTER_DOWN = 1;
    private static final int POINTER_MOVE = 2;
    private static final int POINTER_UP = 3;

    private static final int POINTER_MAX_ID = 4;

    private static final float PAD_MIN_OFF = 0.05f;
    private static final float PAD_MAX_OFF = 1.125f;
    private static final float PAD_INIT_OFF = 0.03f;

    @SuppressWarnings("WeakerAccess")
    public static final ControlAcceleration[] ACCELERATIONS = { new ControlAcceleration(0.1f),
            // ACCELERATION_MOVE
            new ControlAcceleration(0.1f),
            // ACCELERATION_STRAFE
            new ControlAcceleration(0.1f)
            // ACCELERATION_ROTATE
    };

    @SuppressWarnings("WeakerAccess")
    public static final ControlAccelerationBind[] ACCELERATION_BINDS = { new ControlAccelerationBind(FORWARD,
            ACCELERATION_MOVE,
            1),
            new ControlAccelerationBind(BACKWARD, ACCELERATION_MOVE, -1),
            new ControlAccelerationBind(STRAFE_LEFT, ACCELERATION_STRAFE, -1),
            new ControlAccelerationBind(STRAFE_RIGHT, ACCELERATION_STRAFE, 1),
            new ControlAccelerationBind(ROTATE_LEFT, ACCELERATION_ROTATE, -1),
            new ControlAccelerationBind(ROTATE_RIGHT, ACCELERATION_ROTATE, 1) };

    // @formatter:off
    @SuppressWarnings("WeakerAccess") public static final ControlVariant[] VARIANTS = {
            // TYPE_CLASSIC
            new ControlVariant(false,
                    0.8125f,
                    0.7f,
                    0.0f,
                    new ControlItem[] {
                            new ControlItem(12, 1, OPEN_MENU),
                            new ControlItem(15, 1, TOGGLE_MAP),
                            new ControlItem(18, 1, NEXT_WEAPON),
                            new ControlItem(18, 5, ACTION),
                            new ControlItem(1, 12, ROTATE_LEFT, true),
                            new ControlItem(5, 12, ROTATE_RIGHT, true),
                            new ControlItem(3, 10, FORWARD, true),
                            new ControlItem(3, 14, BACKWARD, true),
                            new ControlItem(15, 12, STRAFE_LEFT),
                            new ControlItem(18, 12, STRAFE_RIGHT) },
                    0,
                    8,
                    new int[][] {
                            new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
                            new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
                            new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
                            new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, FORWARD, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
                            new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, 0, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
                            new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, BACKWARD, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
                            new int[] { BACKWARD | ROTATE_LEFT, BACKWARD | ROTATE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT },
                            new int[] { BACKWARD | ROTATE_LEFT, BACKWARD | ROTATE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT } }),
            // TYPE_IMPROVED
            new ControlVariant(false,
                    0.8125f,
                    0.7f,
                    0.0f,
                    new ControlItem[] {
                            new ControlItem(12, 1, OPEN_MENU),
                            new ControlItem(15, 1, TOGGLE_MAP),
                            new ControlItem(18, 1, NEXT_WEAPON),
                            new ControlItem(18, 5, ACTION),
                            new ControlItem(1, 12, STRAFE_LEFT, true),
                            new ControlItem(5, 12, STRAFE_RIGHT, true),
                            new ControlItem(3, 10, FORWARD, true),
                            new ControlItem(3, 14, BACKWARD, true),
                            new ControlItem(15, 12, ROTATE_LEFT),
                            new ControlItem(18, 12, ROTATE_RIGHT) },
                    0,
                    8,
                    new int[][] {
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, FORWARD, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, 0, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, BACKWARD, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT },
                            new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT } }),
            // TYPE_PAD_L
            new ControlVariant(false,
                    0.8125f,
                    0.7f,
                    0.0f,
                    new ControlItem[] {
                            new ControlItem(12, 1, OPEN_MENU),
                            new ControlItem(15, 1, TOGGLE_MAP),
                            new ControlItem(18, 1, ACTION),
                            new ControlItem(18, 5, NEXT_WEAPON) },
                    4,
                    12),
            // TYPE_PAD_R
            new ControlVariant(false,
                    0.8125f,
                    0.7f,
                    0.0f,
                    new ControlItem[] {
                            new ControlItem(1, 14, OPEN_MENU),
                            new ControlItem(1, 4, ACTION),
                            new ControlItem(4, 4, TOGGLE_MAP),
                            new ControlItem(1, 8, NEXT_WEAPON) },
                    15,
                    12),
            // TYPE_EXPERIMENTAL_A
            new ControlVariant(true,
                    -0.0625f,
                    0.05f,
                    0.2375f,
                    new ControlItem[] {
                            new ControlItem(7, 1, OPEN_MENU),
                            new ControlItem(4, 1, TOGGLE_MAP),
                            new ControlItem(1, 1, NEXT_WEAPON),
                            new ControlItem(18, 1, FORWARD),
                            new ControlItem(18, 5, BACKWARD),
                            new ControlItem(18, 10, ACTION),
                            new ControlItem(15, 14, STRAFE_LEFT),
                            new ControlItem(18, 14, STRAFE_RIGHT) }),
            // TYPE_EXPERIMENTAL_B
            new ControlVariant(true,
                    0.8125f,
                    0.7f,
                    0.0f,
                    new ControlItem[] {
                            new ControlItem(12, 1, OPEN_MENU),
                            new ControlItem(15, 1, TOGGLE_MAP),
                            new ControlItem(18, 1, NEXT_WEAPON),
                            new ControlItem(18, 5, ACTION),
                            new ControlItem(1, 12, STRAFE_LEFT, true),
                            new ControlItem(5, 12, STRAFE_RIGHT, true),
                            new ControlItem(3, 10, FORWARD, true),
                            new ControlItem(3, 14, BACKWARD, true) },
                    0,
                    8,
                    new int[][] {
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, FORWARD, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, MASK_MAX, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, BACKWARD, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
                            new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT },
                            new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT } }),
            // TYPE_ZEEMOTE
            new ControlVariant(false, 0.8125f, 0.7f, 0.0f, new ControlItem[] {
                    new ControlItem(18, 1, OPEN_MENU), }), };
    // @formatter:on

    @SuppressWarnings("MagicNumber") private static int[][] controlsMap = new int[20][16];
    private static int[] pointerActionsMask = new int[POINTER_MAX_ID];
    private static boolean[] pointerIsSlide = new boolean[POINTER_MAX_ID];
    private static boolean[] pointerIsPad = new boolean[POINTER_MAX_ID];
    private static float[] pointerPrevX = new float[POINTER_MAX_ID];
    private static int pointerClickCounter;
    private static float[] pointerClickX = new float[POINTER_MAX_ID];
    private static float[] pointerClickY = new float[POINTER_MAX_ID];
    private static boolean[] pointerIsClick = new boolean[POINTER_MAX_ID];
    private static int touchActionsMask;
    private static int keysActionsMask;
    @SuppressLint("UseSparseArrays") private static Map<Integer, Long> keyDownTimeMap = new HashMap<Integer, Long>();
    @SuppressLint("UseSparseArrays") private static Map<Integer, Long> keyUpTimeMap = new HashMap<Integer, Long>();
    private static int trackballActionsMask;
    private static float trackballX;
    private static float trackballY;
    private static float[] relativeOffset = new float[MASK_MAX];
    private static boolean padActive;
    private static float origPadCenterX;
    private static float origPadCenterY;
    private static float padCenterX;
    private static float padCenterY;

    @SuppressWarnings("WeakerAccess") public static ControlVariant currentVariant;
    @SuppressWarnings("WeakerAccess") public static float rotatedAngle;
    @SuppressWarnings("WeakerAccess") public static float joyX;
    @SuppressWarnings("WeakerAccess") public static float joyY;
    @SuppressWarnings("WeakerAccess") public static int joyButtonsMask;
    public static float accelerometerX;
    public static float accelerometerY;
    @SuppressWarnings("WeakerAccess") public static float padX;
    @SuppressWarnings("WeakerAccess") public static float padY;

    private Controls() {
    }

    @SuppressWarnings({ "WeakerAccess", "MagicNumber" })
    public static int getActionsMask() {
        int maskLeft;
        int maskRight;
        int maskUp;
        int maskDown;

        // TODO: test it
        if (Config.rotateScreen) {
            trackballX = -trackballX;
            trackballY = -trackballY;

            maskLeft = Config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
            maskRight = Config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
            maskUp = Config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
            maskDown = Config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
        } else {
            maskLeft = Config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
            maskRight = Config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
            maskUp = Config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
            maskDown = Config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
        }

        trackballActionsMask = 0;

        if ((trackballX <= -0.01f) || (trackballX >= 0.01f)) {
            if ((trackballX < 0) && (maskLeft != 0)) {
                trackballActionsMask |= maskLeft;
                relativeOffset[maskLeft] = -trackballX;
            } else if (maskRight != 0) {
                trackballActionsMask |= maskRight;
                relativeOffset[maskRight] = trackballX;
            }
        }

        if ((trackballY <= -0.01f) || (trackballY >= 0.01f)) {
            if ((trackballY < 0) && (maskUp != 0)) {
                trackballActionsMask |= maskUp;
                relativeOffset[maskUp] = -trackballY;
            } else if (maskDown != 0) {
                trackballActionsMask |= maskDown;
                relativeOffset[maskDown] = trackballY;
            }
        }

        trackballX = 0.0f;
        trackballY = 0.0f;

        for (Map.Entry<Integer, Long> entry : keyUpTimeMap.entrySet()) {
            if ((entry.getValue() != null) && (Game.elapsedTime > entry.getValue())) {
                keysActionsMask &= ~(Config.keyMappings[entry.getKey()]);
                entry.setValue(null);
            }
        }

        int mask = (touchActionsMask | keysActionsMask | trackballActionsMask | joyButtonsMask);

        if ((mask & STRAFE_MODE) != 0) {
            mask = (mask & ~(ROTATE_LEFT | ROTATE_RIGHT | STRAFE_LEFT | STRAFE_RIGHT)) | (((mask & ROTATE_LEFT) != 0)
                    ? STRAFE_LEFT
                    : 0) | (((mask & ROTATE_RIGHT) != 0) ? STRAFE_RIGHT : 0) | (((mask & STRAFE_LEFT) != 0)
                    ? ROTATE_LEFT
                    : 0) | (((mask & STRAFE_RIGHT) != 0) ? ROTATE_RIGHT : 0);
        }

        return mask;
    }

    @SuppressWarnings("WeakerAccess")
    public static void initJoystickVars() {
        joyX = 0.0f;
        joyY = 0.0f;
        joyButtonsMask = 0;
    }

    @SuppressWarnings("MagicNumber")
    public static void fillMap() {
        rotatedAngle = 0;
        touchActionsMask = 0;
        keysActionsMask = 0;
        keyDownTimeMap.clear();
        keyUpTimeMap.clear();
        trackballX = 0.0f;
        trackballY = 0.0f;
        trackballActionsMask = 0;
        padActive = false;
        padX = 0.0f;
        padY = 0.0f;
        accelerometerX = 0.0f;
        accelerometerY = 0.0f;

        initJoystickVars();

        for (int i = 1; i < MASK_MAX; i *= 2) {
            relativeOffset[i] = 0.0f;
        }

        currentVariant = VARIANTS[Config.controlsType];
        origPadCenterX = ((float)currentVariant.padX + 0.5f) / 20.0f;
        origPadCenterY = ((float)currentVariant.padY + 0.5f) / 16.0f;
        padCenterX = origPadCenterX;
        padCenterY = origPadCenterY;

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 16; j++) {
                controlsMap[i][j] = 0;
            }
        }

        for (ControlItem ci : currentVariant.items) {
            if (!ci.decoration) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        controlsMap[ci.x + i][ci.y + j] = ci.type;
                    }
                }
            }
        }

        if (currentVariant.hasMap) {
            for (int i = 0; i < currentVariant.map.length; i++) {
                for (int j = 0; j < currentVariant.map[i].length; j++) {
                    controlsMap[currentVariant.mapX + j][currentVariant.mapY + i] = currentVariant.map[i][j];
                }
            }
        }

        for (int i = 0; i < POINTER_MAX_ID; i++) {
            pointerActionsMask[i] = 0;
            pointerIsSlide[i] = false;
            pointerIsPad[i] = false;
            pointerPrevX[i] = 0.0f;
            pointerClickX[i] = 0.0f;
            pointerClickY[i] = 0.0f;
            pointerIsClick[i] = false;
        }
    }

    @SuppressWarnings({ "WeakerAccess", "MagicNumber" })
    public static void processOnePointer(int pid, float x, float y, int pointerAction) {
        if ((pid < 0) || (pid >= POINTER_MAX_ID)) {
            return;
        }

        if (Config.rotateScreen) {
            x = (float)Game.width - x;
            y = (float)Game.height - y;
        }

        int ctlX = ((int)x * 20) / (Game.width + 1);
        int ctlY = ((int)y * 16) / (Game.height + 1);

        if (ctlX < 0) {
            ctlX = 0;
        } else if (ctlX >= 20) {
            ctlX = 19;
        }

        if (ctlY < 0) {
            ctlY = 0;
        } else if (ctlY >= 16) {
            ctlY = 15;
        }

        if ((pointerAction == POINTER_DOWN) || (pointerAction == POINTER_MOVE)) {
            if (pointerAction == POINTER_DOWN) {
                pointerActionsMask[pid] = 0;
                pointerPrevX[pid] = x;
                pointerIsPad[pid] = false;
                pointerIsSlide[pid] = false;
                pointerIsClick[pid] = false;

                if (currentVariant.hasPad && ((currentVariant.padX < 10) ? (ctlX < 10) : (ctlX >= 10))) {
                    pointerIsPad[pid] = true;
                } else if (currentVariant.slidable && (controlsMap[ctlX][ctlY] == 0)) {
                    pointerIsSlide[pid] = true;
                }

                if ((State.levelNum == 1) && (controlsMap[ctlX][ctlY] == 0)) {
                    pointerClickX[pid] = x;
                    pointerClickY[pid] = y;
                    pointerIsClick[pid] = true;
                }
            } else if ((State.levelNum == 1) && pointerIsClick[pid]) {
                float distSq = ((pointerClickX[pid] - x) * (pointerClickX[pid] - x)) + ((pointerClickY[pid] - y) * (
                        pointerClickY[pid]
                                - y));

                if (distSq > (10.0f * 10.0f)) {
                    pointerIsClick[pid] = false;
                }
            }

            // ----

            if (pointerIsSlide[pid]) {
                float distX = x - pointerPrevX[pid];
                float da = (distX * Config.maxRotateAngle) / (float)Game.width;

                pointerPrevX[pid] = x;

                // if angle is more than half of max angle, this is incorrect MotionEvent (in most of cases)
                // if (Math.abs(da) < (Config.maxRotateAngle / 2.0f)) {
                rotatedAngle += (Config.invertRotation ? da : -da);
                // }
            } else if (pointerIsPad[pid]) {
                if (!padActive) {
                    padActive = true;
                    padCenterX = x / (float)(Game.width + 1);
                    padCenterY = y / (float)(Game.height + 1);

                    if (padCenterX < (origPadCenterX - PAD_INIT_OFF)) {
                        padCenterX = origPadCenterX - PAD_INIT_OFF;
                    }
                    if (padCenterX > (origPadCenterX + PAD_INIT_OFF)) {
                        padCenterX = origPadCenterX + PAD_INIT_OFF;
                    }
                    if (padCenterY < (origPadCenterY - PAD_INIT_OFF)) {
                        padCenterY = origPadCenterY - PAD_INIT_OFF;
                    }
                    if (padCenterY > (origPadCenterY + PAD_INIT_OFF)) {
                        padCenterY = origPadCenterY + PAD_INIT_OFF;
                    }
                }

                int padWidth = (Game.width * 3) / 20;
                if (padWidth < 1) {
                    padWidth = 1;
                }

                int padHeight = (Game.height * 3) / 16;
                if (padHeight < 1) {
                    padHeight = 1;
                }

                padX = (x - (padCenterX * (float)Game.width)) / (float)padWidth;
                padY = (y - (padCenterY * (float)Game.height)) / (float)padHeight;

                if (padX > 0.0f) {
                    padX -= PAD_MIN_OFF;

                    if (padX < 0.0f) {
                        padX = 0.0f;
                    } else if (padX > PAD_MAX_OFF) {
                        padX = PAD_MAX_OFF;
                    }
                } else {
                    padX += PAD_MIN_OFF;

                    if (padX > 0.0f) {
                        padX = 0.0f;
                    } else if (padX < -PAD_MAX_OFF) {
                        padX = -PAD_MAX_OFF;
                    }
                }

                if (padY > 0.0f) {
                    padY -= PAD_MIN_OFF;

                    if (padY < 0.0f) {
                        padY = 0.0f;
                    } else if (padY > PAD_MAX_OFF) {
                        padY = PAD_MAX_OFF;
                    }
                } else {
                    padY += PAD_MIN_OFF;

                    if (padY > 0.0f) {
                        padY = 0.0f;
                    } else if (padY < -PAD_MAX_OFF) {
                        padY = -PAD_MAX_OFF;
                    }
                }
            } else {
                pointerActionsMask[pid] = controlsMap[ctlX][ctlY];
            }
        } else if (pointerAction == POINTER_UP) {
            if ((State.levelNum == 1) && pointerIsClick[pid]) {
                pointerClickCounter += 1;

                if (pointerClickCounter == 5) {
                    ZameApplication.trackEvent("Tutorial", "Click", "", 0);
                }
            }

            if (pointerIsPad[pid]) {
                padActive = false;
                padX = 0.0f;
                padY = 0.0f;
                padCenterX = origPadCenterX;
                padCenterY = origPadCenterY;
            }

            pointerActionsMask[pid] = 0;
            pointerIsClick[pid] = false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void touchEvent(MotionEvent event) {
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        int points = event.getPointerCount();
        int i;
        int aidx;

        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_DOWN);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                aidx = action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i),
                            event.getX(i),
                            event.getY(i),
                            ((i == aidx) ? POINTER_DOWN : POINTER_MOVE));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_MOVE);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_UP);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                aidx = action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i),
                            event.getX(i),
                            event.getY(i),
                            ((i == aidx) ? POINTER_UP : POINTER_MOVE));
                }
                break;
        }

        touchActionsMask = 0;

        for (i = 0; i < POINTER_MAX_ID; i++) {
            touchActionsMask |= pointerActionsMask[i];
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean keyDown(int keyCode) {
        if ((keyCode >= 0) && (keyCode < Config.keyMappings.length) && (Config.keyMappings[keyCode] != 0)) {
            keysActionsMask |= Config.keyMappings[keyCode];

            keyDownTimeMap.put(keyCode, Game.elapsedTime);
            keyUpTimeMap.put(keyCode, null);
            return true;
        }

        return false;
    }

    @SuppressWarnings({ "WeakerAccess", "MagicNumber" })
    public static boolean keyUp(int keyCode) {
        if ((keyCode >= 0) && (keyCode < Config.keyMappings.length) && (Config.keyMappings[keyCode] != 0)) {
            // fix for emulator, because it fires onKeyDown, than immediately onKeyUp with the same keyCode
            if ((keyDownTimeMap.get(keyCode) != null) && ((Game.elapsedTime - keyDownTimeMap.get(keyCode)) < 16L)) {
                keyUpTimeMap.put(keyCode, Game.elapsedTime + 100L);
            } else {
                keyUpTimeMap.put(keyCode, null);
                keysActionsMask &= ~(Config.keyMappings[keyCode]);
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings({ "WeakerAccess", "MagicNumber" })
    public static void updateAccelerations(int mask) {
        for (ControlAcceleration ca : ACCELERATIONS) {
            ca.updated = false;
        }

        for (ControlAccelerationBind cb : ACCELERATION_BINDS) {
            if ((mask & cb.controlType) != 0) {
                ControlAcceleration ca = ACCELERATIONS[cb.accelerationType];

                if ((trackballActionsMask & cb.controlType) == 0) {
                    ca.updated = true;
                    ca.value += ca.step * (float)cb.mult;

                    if (ca.value < -1.0f) {
                        ca.value = -1.0f;
                    } else if (ca.value > 1.0f) {
                        ca.value = 1.0f;
                    }
                } else {
                    ca.value += ca.step
                            * (float)cb.mult
                            * relativeOffset[cb.controlType]
                            * Config.trackballAcceleration;
                }
            }
        }

        for (ControlAcceleration ca : ACCELERATIONS) {
            if (!ca.updated) {
                ca.value *= 0.5f;

                if (!ca.active()) {
                    ca.value = 0.0f;
                }
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void trackballEvent(MotionEvent event) {
        trackballX += event.getX();
        trackballY += event.getY();
    }

    @SuppressWarnings("MagicNumber")
    private static void drawIcon(float sx,
            float sy,
            int texNum,
            boolean pressed,
            boolean highlighted,
            long elapsedTime,
            boolean inverseHighlighting) {

        float ex = sx + 0.25f;
        float ey = sy + 0.25f;

        Renderer.x1 = sx;
        Renderer.y1 = sy;
        Renderer.x2 = sx;
        Renderer.y2 = ey;
        Renderer.x3 = ex;
        Renderer.y3 = ey;
        Renderer.x4 = ex;
        Renderer.y4 = sy;

        if (pressed) {
            Renderer.a1 = 1.0f;
        } else if (highlighted) {
            Renderer.a1 = ((float)Math.sin(((double)elapsedTime / 150.0) + (inverseHighlighting ? 3.14f : 0.0f))
                    / 2.01f) + 0.5f;
        } else {
            Renderer.a1 = Config.controlsAlpha;
        }

        Renderer.a2 = Renderer.a1;
        Renderer.a3 = Renderer.a1;
        Renderer.a4 = Renderer.a1;

        Renderer.drawQuad(texNum);
    }

    @SuppressWarnings("MagicNumber")
    private static void drawControlIcon(int xpos,
            int ypos,
            int texNum,
            boolean pressed,
            boolean highlighted,
            long elapsedTime) {

        float sx = ((((float)xpos + 0.5f) * Common.ratio) / 20.0f) - 0.125f;
        float sy = (((float)(15 - ypos) + 0.5f) / 16.0f) - 0.125f;

        drawIcon(sx, sy, texNum, pressed, highlighted, elapsedTime, false);
    }

    @SuppressWarnings({ "MagicNumber", "PointlessArithmeticExpression" })
    private static void drawPad(long elapsedTime) {
        float sx = (padCenterX * Common.ratio) - 0.125f;
        float sy = (1.0f - padCenterY) - 0.125f;

        drawIcon(sx,
                sy + 0.15f,
                TextureLoader.BASE_ICONS + 0,
                padActive,
                (State.highlightedControlTypeMask & FORWARD) != 0,
                elapsedTime,
                false);

        drawIcon(sx,
                sy - 0.15f,
                TextureLoader.BASE_ICONS + 1,
                padActive,
                (State.highlightedControlTypeMask & BACKWARD) != 0,
                elapsedTime,
                false);

        drawIcon(sx - 0.15f,
                sy,
                TextureLoader.BASE_ICONS + 2,
                padActive,
                (State.highlightedControlTypeMask & ROTATE_LEFT) != 0,
                elapsedTime,
                false);

        drawIcon(sx + 0.15f,
                sy,
                TextureLoader.BASE_ICONS + 3,
                padActive,
                (State.highlightedControlTypeMask & ROTATE_RIGHT) != 0,
                elapsedTime,
                false);

        drawIcon(sx + ((padX * Common.ratio * 2.5f) / 20.0f),
                sy - ((padY * 2.5f) / 16.0f),
                TextureLoader.BASE_ADDITIONAL + 0,
                padActive,
                false,
                elapsedTime,
                false);
    }

    @SuppressWarnings("WeakerAccess")
    public static void render(GL10 gl, long elapsedTime) {
        Renderer.setQuadRGB(1.0f, 1.0f, 1.0f);

        Renderer.z1 = 0.0f;
        Renderer.z2 = 0.0f;
        Renderer.z3 = 0.0f;
        Renderer.z4 = 0.0f;

        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glShadeModel(GL10.GL_FLAT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        Renderer.loadIdentityAndOrthof(gl, 0.0f, Common.ratio, 0.0f, 1.0f, 0.0f, 1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        Renderer.init();

        for (ControlItem ci : currentVariant.items) {
            drawControlIcon(ci.x,
                    ci.y,
                    TextureLoader.BASE_ICONS + ci.icon,
                    (touchActionsMask & ci.type) != 0,
                    (State.highlightedControlTypeMask & ci.type) != 0,
                    elapsedTime);
        }

        if (currentVariant.hasPad) {
            drawPad(elapsedTime);
        }

        Renderer.bindTextureCtl(gl, TextureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        Renderer.flush(gl);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
    }
}
