package {$PKG_CURR};

import android.view.KeyEvent;
import android.view.MotionEvent;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;

public class Controls
{
	public static class ControlAcceleration
	{
		public static final float MIN_ACCELERATION = 0.01f;

		public float value = 0.0f;
		public float step;
		public boolean updated;

		public ControlAcceleration(float step)
		{
			this.step = step;
		}

		public boolean active()
		{
			return ((value <= -MIN_ACCELERATION) || (value >= MIN_ACCELERATION));
		}
	}

	public static class ControlAccelerationBind
	{
		public int controlType;
		public int accelerationType;
		public int mult;

		public ControlAccelerationBind(int controlType, int accelerationType, int mult)
		{
			this.controlType = controlType;
			this.accelerationType = accelerationType;
			this.mult = mult;
		}
	}

	public static class ControlItem
	{
		public int x;
		public int y;
		public int type;
		public boolean decoration;
		public int icon;

		public ControlItem(int x, int y, int type)
		{
			this.x = x;
			this.y = y;
			this.type = type;
			this.decoration = false;

			updateIcon();
		}

		public ControlItem(int x, int y, int type, boolean decoration)
		{
			this.x = x;
			this.y = y;
			this.type = type;
			this.decoration = decoration;

			updateIcon();
		}

		public void updateIcon()
		{
			switch (type)
			{
				case FORWARD:		icon = 0; break;
				case BACKWARD:		icon = 1; break;
				case STRAFE_LEFT:	icon = 2; break;
				case STRAFE_RIGHT:	icon = 3; break;
				case ACTION:		icon = 4; break;
				case NEXT_WEAPON:	icon = 5; break;
				case ROTATE_LEFT:	icon = 6; break;
				case ROTATE_RIGHT:	icon = 7; break;
				case TOGGLE_MAP:	icon = 14; break;
				default:			icon = 14; break;
			}
		}
	}

	public static class ControlVariant
	{
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

		public ControlVariant(boolean slidable, float statsBaseY, float keysBaseY, float debugLineBaseY, ControlItem[] items)
		{
			this.slidable = slidable;
			this.statsBaseY = statsBaseY;
			this.keysBaseY = keysBaseY;
			this.debugLineBaseY = debugLineBaseY;
			this.items = items;
			this.hasMap = false;
			this.hasPad = false;
		}

		public ControlVariant(boolean slidable, float statsBaseY, float keysBaseY, float debugLineBaseY, ControlItem[] items, int padX, int padY)
		{
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

		public ControlVariant(boolean slidable, float statsBaseY, float keysBaseY, float debugLineBaseY, ControlItem[] items, int mapX, int mapY, int[][] map)
		{
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
	public static final int TYPE_EXPERIMENTAL_A = 2;

	// #if USE_ZEEMOTE
		public static final int TYPE_ZEEMOTE = 3;
	// #end

	public static final int TYPE_EXPERIMENTAL_B = 4;
	public static final int TYPE_PAD_L = 5;
	public static final int TYPE_PAD_R = 6;

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
	public static final int MASK_MAX = 1024;

	public static final int ACCELERATION_MOVE = 0;
	public static final int ACCELERATION_STRAFE = 1;
	public static final int ACCELERATION_ROTATE = 2;

	private static final int POINTER_DOWN = 1;
	private static final int POINTER_MOVE = 2;
	private static final int POINTER_UP = 3;
	private static final int POINTER_MAX_ID = 4;

	private static final float PAD_MIN_OFF = 0.05f;
	private static final float PAD_MAX_OFF = 1.125f;
	private static final float PAD_INIT_OFF = 0.03f;

	public static ControlAcceleration[] ACCELERATIONS = new ControlAcceleration[] {
		new ControlAcceleration(0.1f),	// ACCELERATION_MOVE
		new ControlAcceleration(0.1f),	// ACCELERATION_STRAFE
		new ControlAcceleration(0.1f)	// ACCELERATION_ROTATE
	};

	public static final ControlAccelerationBind[] ACCELERATION_BINDS = new ControlAccelerationBind[] {
		new ControlAccelerationBind(FORWARD, ACCELERATION_MOVE, 1),
		new ControlAccelerationBind(BACKWARD, ACCELERATION_MOVE, -1),
		new ControlAccelerationBind(STRAFE_LEFT, ACCELERATION_STRAFE, -1),
		new ControlAccelerationBind(STRAFE_RIGHT, ACCELERATION_STRAFE, 1),
		new ControlAccelerationBind(ROTATE_LEFT, ACCELERATION_ROTATE, -1),
		new ControlAccelerationBind(ROTATE_RIGHT, ACCELERATION_ROTATE, 1)
	};

	public static final ControlVariant[] VARIANTS = new ControlVariant[] {
		// TYPE_CLASSIC
		new ControlVariant(false, 0.8125f, 0.7f, 0f, new ControlItem[] {
			new ControlItem(15, 1, TOGGLE_MAP),
			new ControlItem(18, 1, NEXT_WEAPON),
			new ControlItem(18, 5, ACTION),
			new ControlItem(1, 12, ROTATE_LEFT, true),
			new ControlItem(5, 12, ROTATE_RIGHT, true),
			new ControlItem(3, 10, FORWARD, true),
			new ControlItem(3, 14, BACKWARD, true),
			new ControlItem(15, 12, STRAFE_LEFT),
			new ControlItem(18, 12, STRAFE_RIGHT)
		}, 0, 8, new int[][] {
			new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
			new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
			new int[] { FORWARD | ROTATE_LEFT, FORWARD | ROTATE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT, FORWARD | ROTATE_RIGHT },
			new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, FORWARD, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
			new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, 0, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
			new int[] { ROTATE_LEFT, ROTATE_LEFT, 0, BACKWARD, 0, ROTATE_RIGHT, ROTATE_RIGHT, ROTATE_RIGHT },
			new int[] { BACKWARD | ROTATE_LEFT, BACKWARD | ROTATE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT },
			new int[] { BACKWARD | ROTATE_LEFT, BACKWARD | ROTATE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT, BACKWARD | ROTATE_RIGHT }
		}),
		// TYPE_IMPROVED
		new ControlVariant(false, 0.8125f, 0.7f, 0f, new ControlItem[] {
			new ControlItem(15, 1, TOGGLE_MAP),
			new ControlItem(18, 1, NEXT_WEAPON),
			new ControlItem(18, 5, ACTION),
			new ControlItem(1, 12, STRAFE_LEFT, true),
			new ControlItem(5, 12, STRAFE_RIGHT, true),
			new ControlItem(3, 10, FORWARD, true),
			new ControlItem(3, 14, BACKWARD, true),
			new ControlItem(15, 12, ROTATE_LEFT),
			new ControlItem(18, 12, ROTATE_RIGHT)
		}, 0, 8, new int[][] {
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, FORWARD, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, 0, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, 0, BACKWARD, 0, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT },
			new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT }
		}),
		// TYPE_EXPERIMENTAL_A
		new ControlVariant(true, -0.0625f, 0.05f, 0.2375f, new ControlItem[] {
			new ControlItem(4, 1, TOGGLE_MAP),
			new ControlItem(1, 1, NEXT_WEAPON),
			new ControlItem(18, 1, FORWARD),
			new ControlItem(18, 5, BACKWARD),
			new ControlItem(18, 10, ACTION),
			new ControlItem(15, 14, STRAFE_LEFT),
			new ControlItem(18, 14, STRAFE_RIGHT)
		}),
		// #if USE_ZEEMOTE
			// TYPE_ZEEMOTE
			new ControlVariant(false, 0.8125f, 0.7f, 0f, new ControlItem[] {
			}),
		// #end
		// TYPE_EXPERIMENTAL_B
		new ControlVariant(true, 0.8125f, 0.7f, 0f, new ControlItem[] {
			new ControlItem(15, 1, TOGGLE_MAP),
			new ControlItem(18, 1, NEXT_WEAPON),
			new ControlItem(18, 5, ACTION),
			new ControlItem(1, 12, STRAFE_LEFT, true),
			new ControlItem(5, 12, STRAFE_RIGHT, true),
			new ControlItem(3, 10, FORWARD, true),
			new ControlItem(3, 14, BACKWARD, true)
		}, 0, 8, new int[][] {
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { FORWARD | STRAFE_LEFT, FORWARD | STRAFE_LEFT, FORWARD, FORWARD, FORWARD, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT, FORWARD | STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, FORWARD, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, MASK_MAX, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { STRAFE_LEFT, STRAFE_LEFT, MASK_MAX, BACKWARD, MASK_MAX, STRAFE_RIGHT, STRAFE_RIGHT, STRAFE_RIGHT },
			new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT },
			new int[] { BACKWARD | STRAFE_LEFT, BACKWARD | STRAFE_LEFT, BACKWARD, BACKWARD, BACKWARD, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT, BACKWARD | STRAFE_RIGHT }
		}),
		// TYPE_PAD_L
		new ControlVariant(false, 0.8125f, 0.7f, 0f, new ControlItem[] {
			new ControlItem(15, 1, TOGGLE_MAP),
			new ControlItem(18, 1, ACTION),
			new ControlItem(18, 5, NEXT_WEAPON)
		}, 4, 12),
		// TYPE_PAD_R
		new ControlVariant(false, 0.8125f, 0.7f, 0f, new ControlItem[] {
			new ControlItem(1, 4, ACTION),
			new ControlItem(4, 4, TOGGLE_MAP),
			new ControlItem(1, 8, NEXT_WEAPON)
		}, 15, 12),
	};

	private static int controlsMap[][] = new int[20][16];
	private static int[] pointerActionsMask = new int[POINTER_MAX_ID];
	private static boolean[] pointerIsSlide = new boolean[POINTER_MAX_ID];
	private static boolean[] pointerIsPad = new boolean[POINTER_MAX_ID];
	private static float[] pointerPrevX = new float[POINTER_MAX_ID];
	private static int touchActionsMask;
	private static int keysActionsMask;
	private static int trackballActionsMask;
	private static float trackballX;
	private static float trackballY;
	private static float[] relativeOffset = new float[MASK_MAX];
	private static boolean padActive;
	private static float origPadCenterX = 0.0f;
	private static float origPadCenterY = 0.0f;
	private static float padCenterX = 0.0f;
	private static float padCenterY = 0.0f;

	public static ControlVariant currentVariant;
	public static float rotatedAngle;
	public static float joyX = 0.0f;
	public static float joyY = 0.0f;
	public static int joyButtonsMask = 0;
	public static float accelerometerX = 0.0f;
	public static float accelerometerY = 0.0f;
	public static float padX = 0.0f;
	public static float padY = 0.0f;

	public static int getActionsMask()
	{
		int maskLeft, maskRight, maskUp, maskDown;

		// TODO: test it
		if (Config.rotateScreen)
		{
			trackballX = -trackballX;
			trackballY = -trackballY;

			maskLeft = Config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
			maskRight = Config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
			maskUp = Config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
			maskDown = Config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
		}
		else
		{
			maskLeft = Config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
			maskRight = Config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
			maskUp = Config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
			maskDown = Config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
		}

		trackballActionsMask = 0;

		if ((trackballX <= -0.01f) || (trackballX >= 0.01f))
		{
			if ((trackballX < 0) && (maskLeft != 0))
			{
				trackballActionsMask |= maskLeft;
				relativeOffset[maskLeft] = -trackballX;
			}
			else if (maskRight != 0)
			{
				trackballActionsMask |= maskRight;
				relativeOffset[maskRight] = trackballX;
			}
		}

		if ((trackballY <= -0.01f) || (trackballY >= 0.01f))
		{
			if ((trackballY < 0) && (maskUp != 0))
			{
				trackballActionsMask |= maskUp;
				relativeOffset[maskUp] = -trackballY;
			}
			else if (maskDown != 0)
			{
				trackballActionsMask |= maskDown;
				relativeOffset[maskDown] = trackballY;
			}
		}

		trackballX = 0.0f;
		trackballY = 0.0f;

		int mask = (touchActionsMask | keysActionsMask | trackballActionsMask | joyButtonsMask);

		if ((mask & STRAFE_MODE) != 0)
		{
			mask = mask & ~(ROTATE_LEFT | ROTATE_RIGHT | STRAFE_LEFT | STRAFE_RIGHT)
				| ((mask & ROTATE_LEFT) != 0 ? STRAFE_LEFT : 0)
				| ((mask & ROTATE_RIGHT) != 0 ? STRAFE_RIGHT : 0)
				| ((mask & STRAFE_LEFT) != 0 ? ROTATE_LEFT : 0)
				| ((mask & STRAFE_RIGHT) != 0 ? ROTATE_RIGHT : 0);
		}

		return mask;
	}

	public static void fillMap()
	{
		rotatedAngle = 0;
		touchActionsMask = 0;
		keysActionsMask = 0;
		trackballX = 0.0f;
		trackballY = 0.0f;
		trackballActionsMask = 0;
		padActive = false;
		padX = 0.0f;
		padY = 0.0f;
		joyX = 0.0f;
		joyY = 0.0f;
		joyButtonsMask = 0;
		accelerometerX = 0.0f;
		accelerometerY = 0.0f;

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

		for (int i = 0; i < POINTER_MAX_ID; i++)
		{
			pointerActionsMask[i] = 0;
			pointerIsSlide[i] = false;
			pointerIsPad[i] = false;
			pointerPrevX[i] = 0.0f;
		}
	}

	public static void processOnePointer(int pid, float x, float y, int pointerAction)
	{
		if ((pid < 0) || (pid >= POINTER_MAX_ID)) {
			return;
		}

		if (Config.rotateScreen)
		{
			x = (float)Game.width - x;
			y = (float)Game.height - y;
		}

		int ctlX = (int)((int)x * 20 / (Game.width + 1));
		int ctlY = (int)((int)y * 16 / (Game.height + 1));

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

		if (pointerAction == POINTER_DOWN || pointerAction == POINTER_MOVE)
		{
			if (pointerAction == POINTER_DOWN)
			{
				pointerActionsMask[pid] = 0;
				pointerPrevX[pid] = x;
				pointerIsPad[pid] = false;
				pointerIsSlide[pid] = false;

				if (currentVariant.hasPad && ((currentVariant.padX < 10) ? (ctlX < 10) : (ctlX >= 10))) {
					pointerIsPad[pid] = true;
				} else if (currentVariant.slidable && (controlsMap[ctlX][ctlY] == 0)) {
					pointerIsSlide[pid] = true;
				}
			}

			if (pointerIsSlide[pid])
			{
				float distX = x - pointerPrevX[pid];
				float da = distX * Config.maxRotateAngle / (float)Game.width;

				pointerPrevX[pid] = x;

				// if angle is more than half of max angle, this is incorrect MotionEvent (in most of cases)
				// if (Math.abs(da) < (Config.maxRotateAngle / 2.0f)) {
					rotatedAngle += (Config.invertRotation ? da : -da);
				// }
			}
			else if (pointerIsPad[pid])
			{
				if (!padActive)
				{
					padActive = true;
					padCenterX = x / (float)(Game.width + 1);
					padCenterY = y / (float)(Game.height + 1);

					if (padCenterX < (origPadCenterX - PAD_INIT_OFF)) { padCenterX = origPadCenterX - PAD_INIT_OFF; }
					if (padCenterX > (origPadCenterX + PAD_INIT_OFF)) { padCenterX = origPadCenterX + PAD_INIT_OFF; }
					if (padCenterY < (origPadCenterY - PAD_INIT_OFF)) { padCenterY = origPadCenterY - PAD_INIT_OFF; }
					if (padCenterY > (origPadCenterY + PAD_INIT_OFF)) { padCenterY = origPadCenterY + PAD_INIT_OFF; }
				}

				int padWidth = Game.width * 3 / 20;
				if (padWidth < 1) { padWidth = 1; }

				int padHeight = Game.height * 3 / 16;
				if (padHeight < 1) { padHeight = 1; }

				padX = (x - padCenterX * (float)Game.width) / (float)padWidth;
				padY = (y - padCenterY * (float)Game.height) / (float)padHeight;

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
			}
			else
			{
				pointerActionsMask[pid] = controlsMap[ctlX][ctlY];
			}
		}
		else if (pointerAction == POINTER_UP)
		{
			if (pointerIsPad[pid])
			{
				padActive = false;
				padX = 0.0f;
				padY = 0.0f;
				padCenterX = origPadCenterX;
				padCenterY = origPadCenterY;
			}

			pointerActionsMask[pid] = 0;
		}
	}

	public static void touchEvent(MotionEvent event)
	{
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		int points = event.getPointerCount();
		int i, apid, pid;

		switch (actionCode)
		{
			case MotionEvent.ACTION_DOWN:
				for (i = 0; i < points; i++) {
					processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_DOWN);
				}
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				apid = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

				for (i = 0; i < points; i++)
				{
					pid = event.getPointerId(i);
					processOnePointer(pid, event.getX(i), event.getY(i), (pid == apid ? POINTER_DOWN : POINTER_MOVE));
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
				apid = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

				for (i = 0; i < points; i++)
				{
					pid = event.getPointerId(i);
					processOnePointer(pid, event.getX(i), event.getY(i), (pid == apid ? POINTER_UP : POINTER_MOVE));
				}
				break;
		}

		touchActionsMask = 0;

		for (i = 0; i < POINTER_MAX_ID; i++) {
			touchActionsMask |= pointerActionsMask[i];
		}
	}

	public static boolean keyDown(int keyCode)
	{
		if (keyCode >= 0 && keyCode < Config.keyMappings.length && Config.keyMappings[keyCode] != 0)
		{
			keysActionsMask |= Config.keyMappings[keyCode];
			return true;
		}

		return false;
	}

	public static boolean keyUp(int keyCode)
	{
		if (keyCode >= 0 && keyCode < Config.keyMappings.length && Config.keyMappings[keyCode] != 0)
		{
			keysActionsMask &= ~(Config.keyMappings[keyCode]);
			return true;
		}

		return false;
	}

	public static void updateAccelerations(int mask)
	{
		for (ControlAcceleration ca : ACCELERATIONS) {
			ca.updated = false;
		}

		for (ControlAccelerationBind cb : ACCELERATION_BINDS)
		{
			if ((mask & cb.controlType) != 0)
			{
				ControlAcceleration ca = ACCELERATIONS[cb.accelerationType];

				if ((trackballActionsMask & cb.controlType) == 0)
				{
					ca.updated = true;
					ca.value += ca.step * (float)cb.mult;

					if (ca.value < -1.0f) {
						ca.value = -1.0f;
					} else if (ca.value > 1.0f) {
						ca.value = 1.0f;
					}
				}
				else
				{
					ca.value += ca.step * (float)cb.mult * relativeOffset[cb.controlType] * Config.trackballAcceleration;
				}
			}
		}

		for (ControlAcceleration ca : ACCELERATIONS)
		{
			if (!ca.updated)
			{
				ca.value /= 2.0;

				if (!ca.active()) {
					ca.value = 0.0f;
				}
			}
		}
	}

	public static void trackballEvent(MotionEvent event)
	{
		trackballX += event.getX();
		trackballY += event.getY();
	}

	private static void drawControlIcon(int xpos, int ypos, int texNum, boolean pressed)
	{
		float sx = ((float)xpos + 0.5f) * Common.ratio / 20.0f - 0.125f;
		float sy = ((float)(15 - ypos) + 0.5f) / 16.0f - 0.125f;
		float ex = sx + 0.25f;
		float ey = sy + 0.25f;

		Renderer.x1 = sx; Renderer.y1 = sy;
		Renderer.x2 = sx; Renderer.y2 = ey;
		Renderer.x3 = ex; Renderer.y3 = ey;
		Renderer.x4 = ex; Renderer.y4 = sy;

		Renderer.a1 = (pressed ? 1.0f : Config.controlsAlpha);
		Renderer.a2 = Renderer.a1;
		Renderer.a3 = Renderer.a1;
		Renderer.a4 = Renderer.a1;

		Renderer.drawQuad(TextureLoader.BASE_ICONS + texNum);
	}

	private static void drawPadIcon(float sx, float sy, int absTexNum)
	{
		float ex = sx + 0.25f;
		float ey = sy + 0.25f;

		Renderer.x1 = sx; Renderer.y1 = sy;
		Renderer.x2 = sx; Renderer.y2 = ey;
		Renderer.x3 = ex; Renderer.y3 = ey;
		Renderer.x4 = ex; Renderer.y4 = sy;

		Renderer.a1 = (padActive ? 1.0f : Config.controlsAlpha);
		Renderer.a2 = Renderer.a1;
		Renderer.a3 = Renderer.a1;
		Renderer.a4 = Renderer.a1;

		Renderer.drawQuad(absTexNum);
	}

	private static void drawPad()
	{
		float sx = padCenterX * Common.ratio - 0.125f;
		float sy = (1.0f - padCenterY) - 0.125f;

		drawPadIcon(sx, sy + 0.15f, TextureLoader.BASE_ICONS + 0);
		drawPadIcon(sx, sy - 0.15f, TextureLoader.BASE_ICONS + 1);
		drawPadIcon(sx - 0.15f, sy, TextureLoader.BASE_ICONS + 2);
		drawPadIcon(sx + 0.15f, sy, TextureLoader.BASE_ICONS + 3);
		drawPadIcon(sx + padX * Common.ratio * 2.5f / 20.0f, sy - padY * 2.5f / 16.0f, TextureLoader.BASE_ADDITIONAL + 0);
	}

	public static void render(GL10 gl)
	{
		Renderer.r1 = 1.0f; Renderer.g1 = 1.0f; Renderer.b1 = 1.0f;
		Renderer.r2 = 1.0f; Renderer.g2 = 1.0f; Renderer.b2 = 1.0f;
		Renderer.r3 = 1.0f; Renderer.g3 = 1.0f; Renderer.b3 = 1.0f;
		Renderer.r4 = 1.0f; Renderer.g4 = 1.0f; Renderer.b4 = 1.0f;

		Renderer.z1 = 0.0f;
		Renderer.z2 = 0.0f;
		Renderer.z3 = 0.0f;
		Renderer.z4 = 0.0f;

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glShadeModel(GL10.GL_FLAT);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0f, Common.ratio, 0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		Renderer.init();

		for (ControlItem ci : currentVariant.items) {
			drawControlIcon(ci.x, ci.y, ci.icon, (touchActionsMask & ci.type) != 0);
		}

		if (currentVariant.hasPad) {
			drawPad();
		}

		Renderer.bindTextureCtl(gl, TextureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		Renderer.flush(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}
}
