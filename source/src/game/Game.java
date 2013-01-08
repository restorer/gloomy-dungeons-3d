package {$PKG_CURR};

import {$PKG_ROOT}.libs.*;
import android.graphics.*;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.os.Bundle;
import android.util.Log;
import android.content.res.Resources;
import android.content.res.AssetManager;
import java.io.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLUtils;
import java.util.ArrayList;
import java.util.List;
import android.os.Debug;
import android.widget.Toast;
import android.content.Intent;
import java.util.ArrayList;
import android.os.Environment;
import android.content.Context;
import java.util.regex.*;
import com.google.android.apps.analytics.easytracking.EasyTracker;

public class Game extends ZameGame
{
	private static final float WALK_WALL_DIST = 0.2f;
	private static final int LOAD_LEVEL_NORMAL = 1;
	private static final int LOAD_LEVEL_NEXT = 2;
	public static final int LOAD_LEVEL_RELOAD = 3;

	public static final String INSTANT_NAME = "instant";
	public static final String AUTOSAVE_NAME = "autosave";
	private static boolean pathsInitialized = false;

	// paths initialized in MenuActivity (which started first)
	// TODO: initialize paths in some common place, and re-initialize in necessary
	// but I'm think this is already fixed. anyway, check it.
	public static String SAVES_FOLDER;
	public static String SAVES_ROOT;
	public static String INSTANT_PATH;
	public static String AUTOSAVE_PATH;

	private boolean showFps = false;
	private int actionsMask = 0;
	private int processedMask = 0;
	private boolean hasMoved = false;
	private long prevMovedTime = 0;
	private String unprocessedGameCode = "";

	private static long nextLevelTime;
	private static long killedTime;
	private static float killedAngle;
	private static float killedHeroAngle;
	private static boolean isGameOverFlag;
	private static boolean playStartLevelSound;
	private static boolean skipEndLevelActivityOnce;
	public static boolean renderBlackScreen;
	public static String savedGameParam = INSTANT_NAME;

	public static int endlTotalKills;
	public static int endlTotalItems;
	public static int endlTotalSecrets;

	private static int heroCellX = 0;
	private static int heroCellY = 0;

	public Game(Resources res, AssetManager assets)
	{
		super(res, assets);
		setUpdateInterval(25);	// updates per second - 40
		initialize();
	}

	public void initialize()
	{
		nextLevelTime = 0;
		killedTime = 0;
		isGameOverFlag = false;
		playStartLevelSound = false;
		skipEndLevelActivityOnce = false;
		renderBlackScreen = false;

		Labels.init();
		Common.init();
		Overlay.init();
		State.init();
		Level.init();
		LevelRenderer.init();
		Weapons.init();

		if (savedGameParam.equals("") || !loadGameState(savedGameParam)) {
			loadLevel(LOAD_LEVEL_NORMAL);
		}

		savedGameParam = INSTANT_NAME;
	}

	private static String getInternalStoragePath(Context appContext)
	{
		String result = "";

		if (appContext == null) {
			Log.e(Common.LOG_KEY, "Game.getInternalStoragePath : appContext == null");
		} else if (appContext.getFilesDir() == null) {
			Log.e(Common.LOG_KEY, "Game.getInternalStoragePath : appContext.getFilesDir() == null");
		} else {
			try {
				result = appContext.getFilesDir().getCanonicalPath();
			} catch (IOException ex) {
				Log.e(Common.LOG_KEY, "Can't open internal storage", ex);
			}
		}

		if (result.equals("") && appContext != null) {
			Toast.makeText(appContext, "Critical error!\nCan't open internal storage.", Toast.LENGTH_LONG).show();
		}

		return result;
	}

	private static String getExternalStoragePath()
	{
		try {
			if (Environment.getExternalStorageDirectory() == null) {
				// mystical error? return default value
				return "/sdcard";
			} else {
				return Environment.getExternalStorageDirectory().getCanonicalPath();
			}
		} catch (IOException ex) {
			// sdcard missing or mounted. it is not essential for the game, so let's assume it is sdcard
			return "/sdcard";
		}
	}

	public static void initPaths(Context appContext)
	{
		if (pathsInitialized) {
			return;
		}

		String internalStoragePath = getInternalStoragePath(appContext);

		String externalStoragePath = String.format(
			"%1$s%2$sAndroid%2$sdata%2$s{$PKG_COMMON}",
			getExternalStoragePath(),
			File.separator
		);

		File externalStorageFile = new File(externalStoragePath);

		if (!externalStorageFile.exists())
		{
			String oldExternalStoragePath = String.format(
				"%1$s%2$sAndroid%2$sdata%2$s{" + "PKG_COMMON" + "}",
				getExternalStoragePath(),
				File.separator
			);

			File oldExternalStorageFile = new File(oldExternalStoragePath);

			if (oldExternalStorageFile.exists())
			{
				oldExternalStorageFile.renameTo(externalStorageFile);
			}
			else
			{
				oldExternalStoragePath = String.format(
					"%1$s%2$sAndroid%2$sdata%2$s{$PKG_ROOT}.common",
					getExternalStoragePath(),
					File.separator
				);

				oldExternalStorageFile = new File(oldExternalStoragePath);

				if (oldExternalStorageFile.exists()) {
					oldExternalStorageFile.renameTo(externalStorageFile);
				} else {
					externalStorageFile.mkdirs();
				}
			}
		}
		else
		{
			String oldExternalStoragePath = String.format(
				"%1$s%2$sAndroid%2$sdata%2$s{" + "PKG_COMMON" + "}",
				getExternalStoragePath(),
				File.separator
			);

			File oldExternalStorageFile = new File(oldExternalStoragePath);

			if (oldExternalStorageFile.exists())
			{
				// both old good folder and folder with bad name exists
				String[] files = oldExternalStorageFile.list();

				if (files != null)
				{
					Pattern pat = Pattern.compile("^slot\\-(\\d)\\.(\\d{4}\\-\\d{2}\\-\\d{2}\\-\\d{2}\\-\\d{2})\\.save$");

					for (int i = 0; i < files.length; i++)
					{
						Matcher mt = pat.matcher(files[i]);

						if (mt.find())
						{
							int slotNum = Integer.valueOf(mt.group(1)) - 1;

							if (slotNum >= 0 && slotNum < 4)
							{
								MenuActivity.copyFile(
									String.format(
										"%1$s%2$s%3$s",
										oldExternalStoragePath,
										File.separator,
										files[i]
									),
									String.format(
										"%1$s%2$sslot-%3$s.%4$s.save",
										externalStoragePath,
										File.separator,
										String.valueOf(slotNum + 5),
										mt.group(2)
									)
								);
							}
						}

						(new File(String.format(
							"%1$s%2$s%3$s",
							oldExternalStoragePath,
							File.separator,
							files[i]
						))).delete();
					}
				}

				oldExternalStorageFile.delete();
				Toast.makeText(ZameApplication.self, R.string.msg_old_saves_restored, Toast.LENGTH_LONG).show();
			}
		}

		String noMediaPath = String.format("%1$s%2$s.nomedia", externalStoragePath, File.separator);

		if (!(new File(noMediaPath)).exists())
		{
			try
			{
				FileOutputStream out = new FileOutputStream(noMediaPath);
				out.close();
			}
			catch (FileNotFoundException ex) {}
			catch (SecurityException ex) {}
			catch (IOException ex) {}
		}

		SAVES_FOLDER = externalStoragePath;
		SAVES_ROOT = externalStoragePath + File.separator;
		INSTANT_PATH = String.format("%1$s%2$s%3$s.save", internalStoragePath, File.separator, INSTANT_NAME);
		AUTOSAVE_PATH = String.format("%1$s%2$s%3$s.save", internalStoragePath, File.separator, AUTOSAVE_NAME);

		pathsInitialized = true;
	}

	public void setGameCode(String code)
	{
		synchronized (lockUpdate) {
			unprocessedGameCode = code;
		}
	}

	public void processGameCode(String codes)
	{
		String[] codeList = codes.toLowerCase().split(" ");

		for (String code : codeList)
		{
			if (code.length() < 2) {
				continue;
			}

			if (code.equals("gmfa"))
			{
				State.heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
				State.heroHasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				State.heroHasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				State.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
				State.heroHasWeapon[Weapons.WEAPON_DBLCHAINGUN] = true;
				State.heroHasWeapon[Weapons.WEAPON_CHAINSAW] = true;

				State.heroAmmo[Weapons.AMMO_PISTOL] = 100;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = 50;
			}
			else if (code.equals("gmfh"))
			{
				State.heroHealth = 100;
				State.heroArmor = 200;
			}
			else if (code.equals("gmak"))
			{
				State.heroKeysMask = 7;
			}
			else if (code.equals("tmnl"))
			{
				skipEndLevelActivityOnce = true;
				loadLevel(LOAD_LEVEL_NEXT);
			}
			else if (code.equals("tfps"))
			{
				showFps = !showFps;
			}
			else if (code.equals("tmon"))
			{
				LevelRenderer.showMonstersOnMap = !LevelRenderer.showMonstersOnMap;
			}
			else if (code.equals("gmgm"))
			{
				State.godMode = !State.godMode;
			}
			else if (
				code.length() == 4 &&
				code.charAt(0) == 't' &&
				code.charAt(1) == 'l' &&
				(code.charAt(2) >= '0' && code.charAt(2) <= '9') &&
				(code.charAt(3) >= '0' && code.charAt(3) <= '9')
			) {
				int newLevelNum = (code.charAt(2) - '0') * 10 + (code.charAt(3) - '0');

				if (Level.exists(newLevelNum)) {
					State.levelNum = newLevelNum;
					loadLevel(LOAD_LEVEL_RELOAD);
				}
			}
			else if (code.equals("iddqd"))
			{
				State.godMode = false;
				State.heroHealth = 1;
				State.heroArmor = 0;
			}
		}
	}

	@Override
	public void saveState()
	{
		saveGameState(INSTANT_NAME);
	}

	public static void loadLevel(int loadLevelType)
	{
		if (loadLevelType == LOAD_LEVEL_NEXT) {
			if (!Level.exists(State.levelNum + 1)) {
				return;
			}

			State.levelNum++;

			if (State.levelNum == Level.MAX_DEMO_LEVEL) {
				State.levelNum++;
			}
		}

		killedTime = 0;
		nextLevelTime = 0;
		renderBlackScreen = true;
		playStartLevelSound = true;

		Level.load(State.levelNum);

		if (State.levelNum > 1) {
			saveGameState(AUTOSAVE_NAME);
		}

		if (loadLevelType == LOAD_LEVEL_NEXT && !skipEndLevelActivityOnce) {
			GameActivity.self.handler.post(GameActivity.self.showEndLevelView);
		} else {
			GameActivity.self.handler.post(GameActivity.self.showPreLevelView);
		}
	}

	private void showGameOverScreen()
	{
		renderBlackScreen = true;
		SoundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
		GameActivity.self.handler.post(GameActivity.self.showGameOverView);
		EasyTracker.getTracker().trackPageView("/game/game-over");
	}

	private void showEndLevelScreen()
	{
		endlTotalKills = (State.totalMonsters == 0 ? 100 : (State.killedMonsters * 100 / State.totalMonsters));
		endlTotalItems = (State.totalItems == 0 ? 100 : (State.pickedItems * 100 / State.totalItems));
		endlTotalSecrets = (State.totalSecrets == 0 ? 100 : (State.foundSecrets * 100 / State.totalSecrets));

		loadLevel(LOAD_LEVEL_NEXT);
		skipEndLevelActivityOnce = false;
	}

	public static void nextLevel(boolean isTutorial)
	{
		skipEndLevelActivityOnce = isTutorial;
		nextLevelTime = elapsedTime;

		SoundManager.playSound(SoundManager.SOUND_LEVEL_END);

		if (!isTutorial) {
			SoundManager.setPlaylist(SoundManager.LIST_ENDL);
		}
	}

	public void toggleAutoMap()
	{
		State.showAutoMap = !State.showAutoMap;
	}

	@Override
	protected boolean keyUp(int keyCode)
	{
		return Controls.keyUp(keyCode);
	}

	@Override
	protected boolean keyDown(int keyCode)
	{
		return Controls.keyDown(keyCode);
	}

	@Override
	protected void touchEvent(MotionEvent event)
	{
		Controls.touchEvent(event);
	}

	@Override
	protected void trackballEvent(MotionEvent event)
	{
		Controls.trackballEvent(event);
	}

	protected void updateControls()
	{
		actionsMask = Controls.getActionsMask();

		if (actionsMask != 0) {
			// if any button pressed, reset "justLoaded" flag
			MenuActivity.justLoaded = false;
		}

		if (Controls.currentVariant.slidable && (Math.abs(Controls.rotatedAngle) >= 0.1f))
		{
			State.setHeroA(State.heroA + Controls.rotatedAngle);
			Controls.rotatedAngle /= 2.0f;

			if (Math.abs(Controls.rotatedAngle) < 0.1f) {
				Controls.rotatedAngle = 0.0f;
			}

			// if hero rotated, reset "justLoaded" flag
			MenuActivity.justLoaded = false;
		}
	}

	public static void hitHero(int amt, int soundIdx, Monster mon)
	{
		if (killedTime > 0) {
			return;
		}

		if (State.levelNum > Level.FIRST_REAL_LEVEL) {
			amt += (State.levelNum - Level.FIRST_REAL_LEVEL) / 5;
		}

		SoundManager.playSound(soundIdx);
		Overlay.showOverlay(Overlay.BLOOD);

		if (!State.godMode)
		{
			if (State.heroArmor > 0) {
				State.heroArmor = Math.max(0, State.heroArmor - Math.max(1, amt * 3 / 4));
				State.heroHealth -= Math.max(1, amt / 4);
			} else {
				State.heroHealth -= amt;
			}
		}

		if (State.heroHealth <= 0)
		{
			State.heroHealth = 0;
			killedTime = elapsedTime;

			float dx = mon.x - State.heroX;
			float dy = mon.y - State.heroY;
			killedAngle = PortalTracer.getAngle(dx, dy) * Common.RAD2G_F;
			killedHeroAngle = ((Math.abs(360.0f + State.heroA - killedAngle) < Math.abs(State.heroA - killedAngle)) ? (360.0f + State.heroA) : State.heroA);

			SoundManager.playSound(SoundManager.SOUND_DETH_HERO);
		}
	}

	private boolean processUse()
	{
		if ((LevelRenderer.currVis == null) || (LevelRenderer.currVis.dist > 1.8)) {
			return false;
		}

		if (LevelRenderer.currVis.obj instanceof Door)
		{
			Door door = (Door)LevelRenderer.currVis.obj;

			if (door.sticked)
			{
				if (door.requiredKey == 0)
				{
					Overlay.showLabel(Labels.LABEL_CANT_OPEN);
					SoundManager.playSound(SoundManager.SOUND_NOWAY);
					return true;
				}

				if ((State.heroKeysMask & door.requiredKey) == 0)
				{
					if (door.requiredKey == 4 ) {
						Overlay.showLabel(Labels.LABEL_NEED_GREEN_KEY);
					} else if (door.requiredKey == 2) {
						Overlay.showLabel(Labels.LABEL_NEED_RED_KEY);
					} else {
						Overlay.showLabel(Labels.LABEL_NEED_BLUE_KEY);
					}

					SoundManager.playSound(SoundManager.SOUND_NOWAY);
					return true;
				}

				door.sticked = false;
			}

			if (door.open())
			{
				State.passableMap[door.y][door.x] |= Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;
				return true;
			}
			else
			{
				return false;
			}
		}
		else if (LevelRenderer.currVis.obj instanceof Mark)
		{
			processOneMark((Mark)LevelRenderer.currVis.obj);
			return true;
		}

		return false;
	}

	private boolean checkMonsterVisibilityAndHit(Monster mon, int hits)
	{
		float dx = mon.x - State.heroX;
		float dy = mon.y - State.heroY;
		float dist = (float)Math.sqrt(dx*dx + dy*dy);

		float shootXMain = State.heroX + Common.heroCs * dist;
		float shootYMain = State.heroY - Common.heroSn * dist;

		float xoff = Common.heroSn * 0.2f;
		float yoff = Common.heroCs * 0.2f;

		if (
			Common.traceLine(State.heroX + xoff, State.heroY + yoff, shootXMain + xoff, shootYMain + yoff, Level.PASSABLE_MASK_SHOOT_W) ||
			Common.traceLine(State.heroX, State.heroY, shootXMain, shootYMain, Level.PASSABLE_MASK_SHOOT_W) ||
			Common.traceLine(State.heroX - xoff, State.heroY - yoff, shootXMain - xoff, shootYMain - yoff, Level.PASSABLE_MASK_SHOOT_W)
		) {
			mon.hit(Common.getRealHits(hits, dist), Weapons.WEAPONS[State.heroWeapon].hitTimeout);
			return true;
		}

		return false;
	}

	private void processShoot()
	{
		// just for case
		if (Weapons.hasNoAmmo(State.heroWeapon)) {
			Weapons.selectBestWeapon();
		}

		boolean hit = false;

		if ((LevelRenderer.currVis != null) && (LevelRenderer.currVis.obj instanceof Monster))
		{
			if ((!Weapons.currentParams.isNear) || (LevelRenderer.currVis.dist <= 1.4))
			{
				Monster mon = (Monster)LevelRenderer.currVis.obj;

				if (checkMonsterVisibilityAndHit(mon, Weapons.currentParams.hits)) {
					hit = true;
				}
			}
		}

		if (Weapons.currentCycle[Weapons.shootCycle] > -1000) {
			SoundManager.playSound((Weapons.currentParams.noHitSoundIdx != 0 && !hit) ? Weapons.currentParams.noHitSoundIdx : Weapons.currentParams.soundIdx);
		}

		if (Weapons.currentParams.ammoIdx >= 0)
		{
			State.heroAmmo[Weapons.currentParams.ammoIdx] -= Weapons.currentParams.needAmmo;

			if (State.heroAmmo[Weapons.currentParams.ammoIdx] < Weapons.currentParams.needAmmo)
			{
				if (State.heroAmmo[Weapons.currentParams.ammoIdx] < 0) {
					State.heroAmmo[Weapons.currentParams.ammoIdx] = 0;
				}

				Weapons.selectBestWeapon();
			}
		}
	}

	protected void updateOpenedDoors()
	{
		for (int i = 0; i < State.doorsCount; i++) {
			State.doors[i].tryClose();
		}
	}

	protected void updateMonsters()
	{
		for (int i = 0; i < State.monstersCount; i++) {
			State.monsters[i].update();
		}

		for (int i = 0; i < State.monstersCount;)
		{
			if (State.monsters[i].removeTimeout <= 0) {
				State.monsters[i].remove();
			} else {
				i++;
			}
		}
	}

	protected void updateHeroPosition(float dx, float dy, float accel)
	{
		float acc = accel;
		float prevX = State.heroX;

		while (Math.abs(acc) >= 0.02f)
		{
			float add = acc * dx;

			while (Math.abs(add) > 0.1f)
			{
				State.heroX += (add > 0 ? 0.1f : -0.1f);

				if (!Level.isPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO))
				{
					add = 0;
					break;
				}

				add += (add > 0 ? -0.1f : 0.1f);
			}

			if (Math.abs(add) > 0.02f)
			{
				State.heroX += add;

				if (Level.isPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
					break;
				}
			}

			State.heroX = prevX;
			acc += (acc > 0 ? -0.01f : 0.01f);
		}

		acc = accel;
		float prevY = State.heroY;

		while (Math.abs(acc) >= 0.02f)
		{
			float add = acc * dy;

			while (Math.abs(add) > 0.1f)
			{
				State.heroY += (add > 0 ? 0.1f : -0.1f);

				if (!Level.isPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO))
				{
					add = 0;
					break;
				}

				add += (add > 0 ? -0.1f : 0.1f);
			}

			if (Math.abs(add) > 0.02f)
			{
				State.heroY += add;

				if (Level.isPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
					break;
				}
			}

			State.heroY = prevY;
			acc += (acc > 0 ? -0.01f : 0.01f);
		}

		hasMoved |= ((State.heroX != prevX) || (State.heroY != prevY));
	}

	protected void update()
	{
		if (!unprocessedGameCode.equals("")) {
			processGameCode(unprocessedGameCode);
			unprocessedGameCode = "";
		}

		if (playStartLevelSound) {
			SoundManager.playSound(SoundManager.SOUND_LEVEL_START);
			playStartLevelSound = false;
		}

		// Debug.startMethodTracing("GloomyDungeons.update");

		hasMoved = false;

		updateOpenedDoors();
		updateMonsters();

		if ((nextLevelTime > 0) || (killedTime > 0))
		{
			if (Weapons.shootCycle > 0) {
				Weapons.shootCycle = (Weapons.shootCycle + 1) % Weapons.currentCycle.length;
			}

			return;
		}

		if (((actionsMask & (~processedMask) & Controls.ACTION) != 0) || (Weapons.currentCycle[Weapons.shootCycle] < 0)) {
			LevelRenderer.sortVisibleObjects();
		}

		if ((Weapons.currentCycle[Weapons.shootCycle] < 0) && (Weapons.changeWeaponDir == 0)) {
			processShoot();
		}

		if (Weapons.shootCycle > 0)
		{
			Weapons.shootCycle = (Weapons.shootCycle + 1) % Weapons.currentCycle.length;

			if (Weapons.shootCycle == 0) {
				processedMask &= ~Controls.ACTION;
			}
		}

		if ((actionsMask & Controls.NEXT_WEAPON) != 0)
		{
			if (Weapons.shootCycle==0 && ((processedMask & Controls.NEXT_WEAPON) == 0) && Weapons.changeWeaponDir==0)
			{
				Weapons.nextWeapon();
				processedMask |= Controls.NEXT_WEAPON;
			}
		}
		else
		{
			processedMask &= ~Controls.NEXT_WEAPON;
		}

		if ((actionsMask & Controls.ACTION) != 0)
		{
			if ((processedMask & Controls.ACTION) == 0)
			{
				if (!processUse()) {
					if (Weapons.shootCycle == 0) {
						Weapons.shootCycle++;
					}
				}

				processedMask |= Controls.ACTION;
			}
		}
		else
		{
			processedMask &= ~Controls.ACTION;
		}

		if ((actionsMask & Controls.TOGGLE_MAP) != 0)
		{
			if ((processedMask & Controls.TOGGLE_MAP) == 0)
			{
				toggleAutoMap();
				processedMask |= Controls.TOGGLE_MAP;
			}
		}
		else
		{
			processedMask &= ~Controls.TOGGLE_MAP;
		}

		Controls.updateAccelerations(actionsMask);
		Level.clearPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_IS_HERO);

		if (Math.abs(Controls.accelerometerX) >= 0.1f) {
			if (Config.invertRotation) {
				State.setHeroA(State.heroA + Controls.accelerometerX * Config.accelerometerAcceleration);
			} else {
				State.setHeroA(State.heroA - Controls.accelerometerX * Config.accelerometerAcceleration);
			}

			// if hero has moved or rotated, reset "justLoaded" flag
			MenuActivity.justLoaded = false;
		}

		float joyY = Controls.joyY - Controls.padY * Config.padYAccel;
		float joyX = Controls.joyX + Controls.padX * Config.padXAccel;

		if (Math.abs(joyY) >= 0.05f)
		{
			updateHeroPosition(Common.heroCs, -Common.heroSn, joyY / 7.0f);

			// if hero has moved, reset "justLoaded" flag
			MenuActivity.justLoaded = false;
		}

		if (Math.abs(joyX) >= 0.01f)
		{
			// WAS: if ((Controls.joyButtonsMask & Controls.STRAFE_MODE) != 0) {
			if ((actionsMask & Controls.STRAFE_MODE) != 0) {
				updateHeroPosition(Common.heroSn, Common.heroCs, joyX / 9.0f);
				hasMoved = true;
			} else {
				State.setHeroA(State.heroA - joyX * 3.0f);
			}

			// if hero has moved or rotated, reset "justLoaded" flag
			MenuActivity.justLoaded = false;
		}

		if (Controls.ACCELERATIONS[Controls.ACCELERATION_MOVE].active()) {
			updateHeroPosition(Common.heroCs, -Common.heroSn, Controls.ACCELERATIONS[Controls.ACCELERATION_MOVE].value / Config.moveSpeed);
		}

		if (Controls.ACCELERATIONS[Controls.ACCELERATION_STRAFE].active()) {
			updateHeroPosition(Common.heroSn, Common.heroCs, Controls.ACCELERATIONS[Controls.ACCELERATION_STRAFE].value / Config.strafeSpeed);
		}

		Level.setPassable(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_IS_HERO);

		if (Controls.ACCELERATIONS[Controls.ACCELERATION_ROTATE].active()) {
			State.setHeroA(State.heroA - (Controls.ACCELERATIONS[Controls.ACCELERATION_ROTATE].value * Config.rotateSpeed));
		}

		if (((int)State.heroX != heroCellX) || ((int)State.heroY != heroCellY))
		{
			heroCellX = (int)State.heroX;
			heroCellY = (int)State.heroY;

			processMarks();
			pickObjects();
		}

		// Debug.stopMethodTracing();
	}

	private void processOneMark(Mark mark)
	{
		if (Level.executeActions(mark.id))
		{
			// if this is *not* end level switch, play sound
			if (nextLevelTime == 0) {
				SoundManager.playSound(SoundManager.SOUND_SWITCH);
			}
		}
	}

	protected void processMarks()
	{
		if (
			(Level.marksMap[(int)State.heroY][(int)State.heroX] != null) &&
			(Level.doorsMap[(int)State.heroY][(int)State.heroX] == null)
		) {
			processOneMark(Level.marksMap[(int)State.heroY][(int)State.heroX]);
		}
	}

	protected void pickObjects()
	{
		if ((State.passableMap[(int)State.heroY][(int)State.heroX] & Level.PASSABLE_IS_OBJECT) == 0) {
			return;
		}

		// decide shall we pick object or not

		switch (State.objectsMap[(int)State.heroY][(int)State.heroX])
		{
			case TextureLoader.OBJ_ARMOR_GREEN:
			case TextureLoader.OBJ_ARMOR_RED:
				if (State.heroArmor >= 200) {
					return;
				}
				break;

			case TextureLoader.OBJ_STIM:
			case TextureLoader.OBJ_MEDI:
				if (State.heroHealth >= 100) {
					return;
				}
				break;

			case TextureLoader.OBJ_CLIP:
			case TextureLoader.OBJ_AMMO:
				if (State.heroAmmo[Weapons.AMMO_PISTOL] >= 100) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHELL:
			case TextureLoader.OBJ_SBOX:
				if (State.heroAmmo[Weapons.AMMO_SHOTGUN] >= 50) {
					return;
				}
				break;

			case TextureLoader.OBJ_BPACK:
				if (State.heroHealth>=100 && State.heroAmmo[Weapons.AMMO_PISTOL]>=100 && State.heroAmmo[Weapons.AMMO_SHOTGUN]>=50) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHOTGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_SHOTGUN] && State.heroAmmo[Weapons.AMMO_SHOTGUN]>=50) {
					return;
				}
				break;

			case TextureLoader.OBJ_CHAINGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_CHAINGUN] && State.heroAmmo[Weapons.AMMO_PISTOL]>=100) {
					return;
				}
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] && State.heroAmmo[Weapons.AMMO_SHOTGUN]>=50) {
					return;
				}
				break;
		}

		// play sounds

		switch (State.objectsMap[(int)State.heroY][(int)State.heroX])
		{
			case TextureLoader.OBJ_CLIP:
			case TextureLoader.OBJ_AMMO:
			case TextureLoader.OBJ_SHELL:
			case TextureLoader.OBJ_SBOX:
				SoundManager.playSound(SoundManager.SOUND_PICK_AMMO);
				break;

			case TextureLoader.OBJ_BPACK:
			case TextureLoader.OBJ_SHOTGUN:
			case TextureLoader.OBJ_CHAINGUN:
			case TextureLoader.OBJ_DBLSHOTGUN:
				SoundManager.playSound(SoundManager.SOUND_PICK_WEAPON);
				break;

			default:
				SoundManager.playSound(SoundManager.SOUND_PICK_ITEM);
				break;
		}

		// add healh/armor/wepons/bullets

		int bestWeapon = Weapons.getBestWeapon();

		switch (State.objectsMap[(int)State.heroY][(int)State.heroX])
		{
			case TextureLoader.OBJ_ARMOR_GREEN:
				State.heroArmor = Math.min(State.heroArmor + 100, 200);
				break;

			case TextureLoader.OBJ_ARMOR_RED:
				State.heroArmor = Math.min(State.heroArmor + 200, 200);
				break;

			case TextureLoader.OBJ_KEY_BLUE:
				State.heroKeysMask |= 1;
				break;

			case TextureLoader.OBJ_KEY_RED:
				State.heroKeysMask |= 2;
				break;

			case TextureLoader.OBJ_KEY_GREEN:
				State.heroKeysMask |= 4;
				break;

			case TextureLoader.OBJ_STIM:
				State.heroHealth = Math.min(State.heroHealth + 10, 100);
				break;

			case TextureLoader.OBJ_MEDI:
				State.heroHealth = Math.min(State.heroHealth + 50, 100);
				break;

			case TextureLoader.OBJ_CLIP:
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 5, 100);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_AMMO:
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 20, 100);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SHELL:
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 5, 50);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN && State.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SBOX:
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 15, 50);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN && State.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_BPACK:
				State.heroHealth = Math.min(State.heroHealth + 10, 100);
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 5, 100);
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 5, 50);
				// do not check shotgun existing (than, if it didn't exists, pistol will be selected)
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SHOTGUN:
				State.heroHasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 3, 50);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_CHAINGUN:
				State.heroHasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 20, 100);
				if (bestWeapon < Weapons.WEAPON_CHAINGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				State.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 6, 50);
				if (bestWeapon < Weapons.WEAPON_DBLSHOTGUN) { Weapons.selectBestWeapon(); }
				break;
		}

		// don't count objects leaved by monsters
		if ((State.passableMap[(int)State.heroY][(int)State.heroX] & Level.PASSABLE_IS_OBJECT_ORIG) != 0) {
			State.pickedItems++;
		}

		// remove picked objects from map
		State.objectsMap[(int)State.heroY][(int)State.heroX] = 0;
		State.passableMap[(int)State.heroY][(int)State.heroX] &= ~Level.PASSABLE_MASK_OBJECT;

		Overlay.showOverlay(Overlay.ITEM);
	}

	@Override
	protected void surfaceCreated(GL10 gl)
	{
		TextureLoader.surfaceCreated(gl);
		Labels.surfaceCreated(gl);
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
	}

	@Override
	protected void surfaceSizeChanged(GL10 gl)
	{
		Common.ratio = (float)width / (float)height;
		LevelRenderer.surfaceSizeChanged(gl);
	}

	private static final int FPS_AVG_LEN = 2;

	private int mFrames = 0;
	private long mPrevRenderTime = 0;
	private int[] fpsList = new int[FPS_AVG_LEN];
	private int currFpsPtr = 0;

	private int getAvgFps()
	{
		mFrames++;

		long time = System.currentTimeMillis();
		long diff = time - mPrevRenderTime;

		if (diff > 1000)
		{
			int seconds = (int)(diff / 1000L);
			mPrevRenderTime += (long)seconds * 1000L;

			fpsList[currFpsPtr] = mFrames / seconds;
			currFpsPtr = (currFpsPtr + 1) % FPS_AVG_LEN;

			mFrames = 0;
		}

		int sum = 0;

		for (int v : fpsList) {
			sum += v;
		}

		return (sum / FPS_AVG_LEN);
	}

	protected void drawFps(GL10 gl)
	{
		int fps = getAvgFps();
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		int xpos = (int)(0.02f * (float)Game.width / Common.ratio);
		int ypos = (int)((float)Game.height * Controls.currentVariant.debugLineBaseY);

		Labels.maker.beginDrawing(gl, width, height);
		Labels.maker.draw(gl, xpos, ypos, Labels.map[Labels.LABEL_FPS]);
		Labels.maker.endDrawing(gl);

		Labels.numeric.setValue(fps);
		Labels.numeric.draw(gl, xpos + Labels.maker.getWidth(Labels.map[Labels.LABEL_FPS]) + 5, ypos, width, height);
	}

	private static void renderEndLevelLayer(GL10 gl, float dt)
	{
		Renderer.r1 = 0.0f;
		Renderer.g1 = 0.0f;
		Renderer.b1 = 0.0f;

		Renderer.a2 = Math.min(1.0f, dt) * 0.9f;
		Renderer.a3 = Renderer.a2;

		Renderer.a1 = Math.min(1.0f, dt * 0.5f) * 0.9f;
		Renderer.a4 = Renderer.a1;

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		Renderer.init();

		Renderer.x1 = 0.0f; Renderer.y1 = 0.0f; Renderer.z1 = 0.0f;
		Renderer.x2 = 0.0f; Renderer.y2 = 1.0f; Renderer.z2 = 0.0f;
		Renderer.x3 = 1.0f; Renderer.y3 = 1.0f; Renderer.z3 = 0.0f;
		Renderer.x4 = 1.0f; Renderer.y4 = 0.0f; Renderer.z4 = 0.0f;

		Renderer.r2 = Renderer.r1; Renderer.g2 = Renderer.g1; Renderer.b2 = Renderer.b1;
		Renderer.r3 = Renderer.r1; Renderer.g3 = Renderer.g1; Renderer.b3 = Renderer.b1;
		Renderer.r4 = Renderer.r1; Renderer.g4 = Renderer.g1; Renderer.b4 = Renderer.b1;

		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Renderer.flush(gl, false);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	private static void renderGammaLayer(GL10 gl)
	{
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		Renderer.init();

		Renderer.r1 = 1.0f; Renderer.g1 = 1.0f; Renderer.b1 = 1.0f;
		Renderer.r2 = 1.0f; Renderer.g2 = 1.0f; Renderer.b2 = 1.0f;
		Renderer.r3 = 1.0f; Renderer.g3 = 1.0f; Renderer.b3 = 1.0f;
		Renderer.r4 = 1.0f; Renderer.g4 = 1.0f; Renderer.b4 = 1.0f;

		Renderer.x1 = 0.0f; Renderer.y1 = 0.0f; Renderer.z1 = 0.0f;
		Renderer.x2 = 0.0f; Renderer.y2 = 1.0f; Renderer.z2 = 0.0f;
		Renderer.x3 = 1.0f; Renderer.y3 = 1.0f; Renderer.z3 = 0.0f;
		Renderer.x4 = 1.0f; Renderer.y4 = 0.0f; Renderer.z4 = 0.0f;

		Renderer.a1 = Config.gamma;
		Renderer.a2 = Config.gamma;
		Renderer.a3 = Config.gamma;
		Renderer.a4 = Config.gamma;

		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		Renderer.flush(gl, false);
		gl.glDisable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void drawCrosshair(GL10 gl)
	{
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_BLEND);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, -Common.ratio, Common.ratio, -1.0f, 1.0f, 0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		Renderer.init();

		Renderer.r1 = 1.0f; Renderer.g1 = 1.0f; Renderer.b1 = 1.0f;
		Renderer.r2 = 1.0f; Renderer.g2 = 1.0f; Renderer.b2 = 1.0f;

		Renderer.drawLine(0.0f, 0.03f, 0.0f, 0.08f);
		Renderer.drawLine(0.0f, -0.03f, 0.0f, -0.08f);
		Renderer.drawLine(0.03f, 0.0f, 0.08f, 0.0f);
		Renderer.drawLine(-0.03f, 0.0f, -0.08f, 0.0f);

		Renderer.flush(gl);

		gl.glDisable(GL10.GL_BLEND);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void render(GL10 gl)
	{
		// Debug.startMethodTracing("GloomyDungeons.render");

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (renderBlackScreen) {
			return;
		}

		long walkTime = 0;

		if (hasMoved) {
			if (prevMovedTime != 0) {
				walkTime = elapsedTime - prevMovedTime;
			} else {
				prevMovedTime = elapsedTime;
			}
		} else {
			prevMovedTime = 0;
		}

		float yoff = LevelRenderer.HALF_WALL / 8.0f + (float)Math.sin((double)walkTime / 100.0) * LevelRenderer.HALF_WALL / 16.0f;

		if (killedTime > 0) {
			yoff -= Math.min(1.0f, (float)(elapsedTime - killedTime) / 500.0f) * LevelRenderer.HALF_WALL / 2.0f;
			State.setHeroA(killedHeroAngle + (killedAngle - killedHeroAngle) * Math.min(1.0f, (float)(elapsedTime - killedTime) / 1000.0f));
		}

		LevelRenderer.render(gl, elapsedTime, -yoff);

		if (Config.showCrosshair) {
			drawCrosshair(gl);
		}

		Weapons.render(gl, walkTime);

		if (State.showAutoMap) {
			LevelRenderer.renderAutoMap(gl);
		}

		Overlay.render(gl);
		Stats.render(gl);
		Controls.render(gl);

		if (nextLevelTime > 0) {
			renderEndLevelLayer(gl, (float)(elapsedTime - nextLevelTime) / 500.0f);
		}

		if (Config.gamma > 0.01f) {
			renderGammaLayer(gl);
		}

		if (showFps) {
			drawFps(gl);
		}

		if (nextLevelTime > 0) {
			if (elapsedTime - nextLevelTime > 1000) {
				if (isGameOverFlag) {
					showGameOverScreen();
				} else {
					showEndLevelScreen();
				}
			}
		} else if ((killedTime > 0) && (elapsedTime - killedTime > 3500)) {
			isGameOverFlag = true;
			nextLevelTime = elapsedTime;
		}

		// Debug.stopMethodTracing();
	}

	public static void safeRename(String tmpName, String fileName)
	{
		String oldName = fileName + ".old";

		if ((new File(oldName)).exists()) {
			(new File(oldName)).delete();
		}

		if ((new File(fileName)).exists()) {
			(new File(fileName)).renameTo(new File(oldName));
		}

		(new File(tmpName)).renameTo(new File(fileName));

		if ((new File(oldName)).exists()) {
			(new File(oldName)).delete();
		}
	}

	public static boolean saveGameState(String name)
	{
		initPaths(GameActivity.appContext);

		String saveName = (
			name.equals(INSTANT_NAME) ? INSTANT_PATH : (
				name.equals(AUTOSAVE_NAME) ? AUTOSAVE_PATH :
				(SAVES_ROOT + name + ".save")
			)
		);

		String tmpName = saveName + ".tmp";
		boolean success = true;

		try
		{
			FileOutputStream fo = new FileOutputStream(tmpName, false);
			ObjectOutputStream os = new ObjectOutputStream(fo);

			State.writeTo(os);

			os.flush();
			fo.close();

			safeRename(tmpName, saveName);
		}
		catch (FileNotFoundException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (IOException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (Exception ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			Toast.makeText(GameActivity.appContext, R.string.msg_cant_save_state, Toast.LENGTH_LONG).show();
		}

		return success;
	}

	public static boolean loadGameState(String name)
	{
		initPaths(GameActivity.appContext);

		String saveName = (
			name.equals(INSTANT_NAME) ? INSTANT_PATH : (
				name.equals(AUTOSAVE_NAME) ? AUTOSAVE_PATH :
				(SAVES_ROOT + name + ".save")
			)
		);

		boolean success = true;

		try
		{
			FileInputStream fi = new FileInputStream(saveName);
			ObjectInputStream is = new ObjectInputStream(fi);

			State.readFrom(is);
		}
		catch (FileNotFoundException ex)
		{
			return false;
		}
		catch (ClassNotFoundException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (IOException ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}
		catch (Exception ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			Toast.makeText(GameActivity.appContext, R.string.msg_cant_load_state, Toast.LENGTH_LONG).show();
		}

		if (success && (State.levelNum == Level.MAX_DEMO_LEVEL)) {
			State.levelNum++;
			loadLevel(LOAD_LEVEL_RELOAD);
		}

		return success;
	}
}
