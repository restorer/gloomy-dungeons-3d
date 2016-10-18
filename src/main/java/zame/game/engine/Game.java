package zame.game.engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Debug;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.Config;
import zame.game.GameActivity;
import zame.game.MenuActivity;
import zame.game.R;
import zame.game.Renderer;
import zame.game.SoundManager;
import zame.game.ZameApplication;
import zame.game.ZameGame;

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
			Toast.makeText(ZameApplication.self, "Critical error!\nCan't open internal storage.", Toast.LENGTH_LONG).show();
		}

		return result;
	}

	public static String getExternalStoragePath()
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
		String externalStoragePath = GameHelper.initPaths(appContext);

		String noMediaPath = String.format(Locale.US, "%1$s%2$s.nomedia", externalStoragePath, File.separator);

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
		INSTANT_PATH = String.format(Locale.US, "%1$s%2$s%3$s.save", internalStoragePath, File.separator, INSTANT_NAME);
		AUTOSAVE_PATH = String.format(Locale.US, "%1$s%2$s%3$s.save", internalStoragePath, File.separator, AUTOSAVE_NAME);

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
		String[] codeList = codes.toLowerCase(Locale.US).split(" ");

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

				State.heroAmmo[Weapons.AMMO_PISTOL] = Weapons.MAX_PISTOL_AMMO;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Weapons.MAX_SHOTGUN_AMMO;
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
		}

		killedTime = 0;
		nextLevelTime = 0;
		renderBlackScreen = true;
		playStartLevelSound = true;

		Level.load(State.levelNum);
		saveGameState(AUTOSAVE_NAME);

		if (loadLevelType == LOAD_LEVEL_NEXT && !skipEndLevelActivityOnce) {
			GameActivity.changeView(R.layout.end_level);
		} else {
			GameActivity.changeView(R.layout.pre_level);
		}
	}

	private void showGameOverScreen()
	{
		renderBlackScreen = true;
		SoundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
		GameActivity.changeView(R.layout.game_over);
		ZameApplication.trackPageView("/game/game-over");
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

					if (door.mark != null) {
						processOneMark(100 + door.mark.id);
					}

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

					if (door.mark != null) {
						processOneMark(100 + door.mark.id);
					}

					SoundManager.playSound(SoundManager.SOUND_NOWAY);
					return true;
				}

				door.sticked = false;
			}

			if (door.open())
			{
				State.passableMap[door.y][door.x] |= Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;

				if (door.mark != null) {
					processOneMark(door.mark.id);
				}

				return true;
			}
			else
			{
				return false;
			}
		}
		else if (LevelRenderer.currVis.obj instanceof Mark)
		{
			processOneMark(((Mark)LevelRenderer.currVis.obj).id);
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
		Level.quickReturnFromFillInitialInWall = false;

		float acc = accel;
		float prevX = State.heroX;

		while (Math.abs(acc) >= 0.02f)
		{
			float add = acc * dx;

			while (Math.abs(add) > 0.1f)
			{
				Level.fillInitialInWallMap(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
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
				Level.fillInitialInWallMap(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
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
				Level.fillInitialInWallMap(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
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
				Level.fillInitialInWallMap(State.heroX, State.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
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

	protected void setNewControlsType(int controlsType)
	{
		Config.controlsType = controlsType;
		Controls.fillMap();

		SoundManager.playSound(SoundManager.SOUND_SWITCH);
		Overlay.showOverlay(Overlay.MARK);
	}

	protected void update()
	{
		if (!unprocessedGameCode.equals("")) {
			processGameCode(unprocessedGameCode);
			unprocessedGameCode = "";
		}

		if (renderBlackScreen || createdTexturesCount < totalTexturesCount) {
			return;
		}

		if (playStartLevelSound) {
			SoundManager.playSound(SoundManager.SOUND_LEVEL_START);
			playStartLevelSound = false;
		}

		// Debug.startMethodTracing("GloomyDungeons.update");

		hasMoved = false;

		updateOpenedDoors();
		updateMonsters();

		if ((nextLevelTime > 0) || (killedTime > 0)) {
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

		if (Weapons.shootCycle > 0) {
			Weapons.shootCycle = (Weapons.shootCycle + 1) % Weapons.currentCycle.length;

			if (Weapons.shootCycle == 0) {
				processedMask &= ~Controls.ACTION;
			}
		}

		if ((actionsMask & Controls.NEXT_WEAPON) != 0) {
			if (Weapons.shootCycle==0 && ((processedMask & Controls.NEXT_WEAPON) == 0) && Weapons.changeWeaponDir==0) {
				Weapons.nextWeapon();
				processedMask |= Controls.NEXT_WEAPON;
			}
		} else {
			processedMask &= ~Controls.NEXT_WEAPON;
		}

		if ((actionsMask & Controls.ACTION) != 0) {
			if ((processedMask & Controls.ACTION) == 0) {
				if (!processUse()) {
					if (Weapons.shootCycle == 0) {
						Weapons.shootCycle++;
					}
				}

				processedMask |= Controls.ACTION;
			}
		} else {
			processedMask &= ~Controls.ACTION;
		}

		if ((actionsMask & Controls.TOGGLE_MAP) != 0) {
			if ((processedMask & Controls.TOGGLE_MAP) == 0) {
				toggleAutoMap();
				processedMask |= Controls.TOGGLE_MAP;
			}
		} else {
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

	private void processOneMark(int markId)
	{
		if (Level.executeActions(markId)) {
			// if this is *not* end level switch, play sound
			if (nextLevelTime == 0) {
				SoundManager.playSound(SoundManager.SOUND_SWITCH);
				Overlay.showOverlay(Overlay.MARK);
			}
		}
	}

	protected void processMarks()
	{
		if (
			(Level.marksMap[(int)State.heroY][(int)State.heroX] != null) &&
			(Level.doorsMap[(int)State.heroY][(int)State.heroX] == null)
		) {
			processOneMark(Level.marksMap[(int)State.heroY][(int)State.heroX].id);
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
				if (State.heroAmmo[Weapons.AMMO_PISTOL] >= Weapons.MAX_PISTOL_AMMO) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHELL:
			case TextureLoader.OBJ_SBOX:
				if (State.heroAmmo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO) {
					return;
				}
				break;

			case TextureLoader.OBJ_BPACK:
				if (
					State.heroHealth >= 100
					&& State.heroAmmo[Weapons.AMMO_PISTOL] >= Weapons.MAX_PISTOL_AMMO
					&& State.heroAmmo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO
				) {
					return;
				}
				break;

			case TextureLoader.OBJ_SHOTGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_SHOTGUN] && State.heroAmmo[Weapons.AMMO_SHOTGUN]>=Weapons.MAX_SHOTGUN_AMMO) {
					return;
				}
				break;

			case TextureLoader.OBJ_CHAINGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_CHAINGUN] && State.heroAmmo[Weapons.AMMO_PISTOL]>=Weapons.MAX_PISTOL_AMMO) {
					return;
				}
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				if (State.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] && State.heroAmmo[Weapons.AMMO_SHOTGUN]>=Weapons.MAX_SHOTGUN_AMMO) {
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
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 5, Weapons.MAX_PISTOL_AMMO);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_AMMO:
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 20, Weapons.MAX_PISTOL_AMMO);
				if (bestWeapon < Weapons.WEAPON_PISTOL) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SHELL:
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 5, Weapons.MAX_SHOTGUN_AMMO);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN && State.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SBOX:
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 15, Weapons.MAX_SHOTGUN_AMMO);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN && State.heroHasWeapon[Weapons.WEAPON_SHOTGUN]) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_BPACK:
				State.heroHealth = Math.min(State.heroHealth + 10, 100);
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 5, Weapons.MAX_PISTOL_AMMO);
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 5, Weapons.MAX_SHOTGUN_AMMO);
				// do not check shotgun existing (than, if it didn't exists, pistol will be selected)
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_SHOTGUN:
				State.heroHasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 3, Weapons.MAX_SHOTGUN_AMMO);
				if (bestWeapon < Weapons.WEAPON_SHOTGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_CHAINGUN:
				State.heroHasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				State.heroAmmo[Weapons.AMMO_PISTOL] = Math.min(State.heroAmmo[Weapons.AMMO_PISTOL] + 20, Weapons.MAX_PISTOL_AMMO);
				if (bestWeapon < Weapons.WEAPON_CHAINGUN) { Weapons.selectBestWeapon(); }
				break;

			case TextureLoader.OBJ_DBLSHOTGUN:
				State.heroHasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
				State.heroAmmo[Weapons.AMMO_SHOTGUN] = Math.min(State.heroAmmo[Weapons.AMMO_SHOTGUN] + 6, Weapons.MAX_SHOTGUN_AMMO);
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

	private int createdTexturesCount = 0;
	private int totalTexturesCount = 0;

	@Override
	protected void surfaceCreated(GL10 gl)
	{
		createdTexturesCount = 0;
		totalTexturesCount = TextureLoader.TEXTURES_TO_LOAD.length + 1;
	}

	@Override
	protected void surfaceSizeChanged(GL10 gl)
	{
		Common.ratio = (float)width / (float)height;
		Labels.surfaceSizeChanged(width);
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

		long time = SystemClock.elapsedRealtime();
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
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		Renderer.init();

		Renderer.a2 = Math.min(1.0f, dt) * 0.9f;
		Renderer.a3 = Renderer.a2;

		Renderer.a1 = Math.min(1.0f, dt * 0.5f) * 0.9f;
		Renderer.a4 = Renderer.a1;

		Renderer.setQuadRGB(0.0f, 0.0f, 0.0f);
		Renderer.setQuadOrthoCoords(0.0f, 0.0f, 1.0f, 1.0f);
		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Renderer.flush(gl, false);

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

		Renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, Config.gamma);
		Renderer.setQuadOrthoCoords(0.0f, 0.0f, 1.0f, 1.0f);
		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		Renderer.flush(gl, false);
		gl.glDisable(GL10.GL_BLEND);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void drawCrosshair(GL10 gl)
	{
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, -Common.ratio, Common.ratio, -1.0f, 1.0f, 0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		Renderer.init();

		Renderer.setLineRGBA(1.0f, 1.0f, 1.0f, 1.0f);

		Renderer.drawLine(0.0f, 0.03f, 0.0f, 0.08f);
		Renderer.drawLine(0.0f, -0.03f, 0.0f, -0.08f);
		Renderer.drawLine(0.03f, 0.0f, 0.08f, 0.0f);
		Renderer.drawLine(-0.03f, 0.0f, -0.08f, 0.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Renderer.flush(gl, false);
		gl.glDisable(GL10.GL_BLEND);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void drawPreloader(GL10 gl)
	{
		if (totalTexturesCount <= 0) {
			return;
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		Renderer.loadIdentityAndOrthof(gl, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		Renderer.init();

		Renderer.setQuadRGBA(0.4f, 0.4f, 0.4f, 1.0f);
		Renderer.setQuadOrthoCoords(0.1f, 0.475f, 0.9f, 0.525f);
		Renderer.drawQuad();

		Renderer.setQuadRGB(0.8f, 0.8f, 0.8f);
		Renderer.x3 = (float)(createdTexturesCount + 1) / (float)totalTexturesCount * 0.8f + 0.1f;
		Renderer.x4 = Renderer.x3;
		Renderer.drawQuad();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_BLEND);
		Renderer.flush(gl, false);

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

		if (createdTexturesCount < totalTexturesCount) {
			drawPreloader(gl);

			if (createdTexturesCount == 0) {
				Labels.createLabels(gl);
				gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
			} else {
				TextureLoader.loadTexture(gl, createdTexturesCount - 1);
			}

			createdTexturesCount++;
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
		Controls.render(gl, elapsedTime);

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
		initPaths(ZameApplication.self);

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
		catch (Exception ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			try {
				Toast.makeText(ZameApplication.self, R.string.msg_cant_save_state, Toast.LENGTH_LONG).show();
			} catch (Exception ex) {
				// something really bad happened
			}
		}

		return success;
	}

	public static boolean loadGameState(String name)
	{
		initPaths(ZameApplication.self);

		String saveName = (
			name.equals(INSTANT_NAME) ? INSTANT_PATH : (
				name.equals(AUTOSAVE_NAME) ? AUTOSAVE_PATH :
				(SAVES_ROOT + name + ".save")
			)
		);

		boolean success = true;
		String errorMessage = ZameApplication.self.getString(R.string.msg_cant_load_state);

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
			errorMessage = ex.toString();
		}
		catch (Exception ex)
		{
			Log.e(Common.LOG_KEY, "Exception", ex);
			success = false;
		}

		if (!success) {
			Toast.makeText(ZameApplication.self, errorMessage, Toast.LENGTH_LONG).show();
		}

		if (success && State.tmpReloadLevel) {
			State.tmpReloadLevel = false;
			loadLevel(LOAD_LEVEL_RELOAD);
		}

		return success;
	}
}
