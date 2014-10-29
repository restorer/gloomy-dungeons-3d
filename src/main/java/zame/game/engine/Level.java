package zame.game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import zame.game.SoundManager;
import zame.game.ZameApplication;
import zame.game.ZameGame;

public class Level
{
	public static final int FIRST_REAL_LEVEL = 2;
	public static final int MAX_LEVEL = 27;

	public static final int MAX_WIDTH = 64;
	public static final int MAX_HEIGHT = 64;
	public static final int MAX_DOORS = 128;
	public static final int MAX_MONSTERS = 256;
	public static final int MAX_MARKS = 253;
	public static final int MAX_ACTIONS = MAX_MARKS + 1;

	public static final int ACTION_CLOSE = 1;
	public static final int ACTION_OPEN = 2;
	public static final int ACTION_REQ_KEY = 3;
	public static final int ACTION_WALL = 4;
	public static final int ACTION_NEXT_LEVEL = 5;
	public static final int ACTION_NEXT_TUTOR_LEVEL = 6;
	public static final int ACTION_DISABLE_PISTOL = 7;
	public static final int ACTION_ENABLE_PISTOL = 8;
	public static final int ACTION_WEAPON_HAND = 9;
	public static final int ACTION_RESTORE_HEALTH = 10;
	public static final int ACTION_SECRET = 11;
	public static final int ACTION_UNMARK = 12;
	public static final int ACTION_ENSURE_WEAPON = 13;
	public static final int ACTION_BTN_ON = 14;
	public static final int ACTION_BTN_OFF = 15;
	public static final int ACTION_MSG_ON = 16;
	public static final int ACTION_MSG_OFF = 17;

	public static final int PASSABLE_IS_WALL = 1;
	public static final int PASSABLE_IS_TRANSP_WALL = 2;
	public static final int PASSABLE_IS_OBJECT = 4;
	public static final int PASSABLE_IS_DECORATION = 8;
	public static final int PASSABLE_IS_DOOR = 16;
	public static final int PASSABLE_IS_HERO = 32;
	public static final int PASSABLE_IS_MONSTER = 64;
	public static final int PASSABLE_IS_DEAD_CORPSE = 128;
	public static final int PASSABLE_IS_OBJECT_ORIG = 256;	// original object [not leaved by monster]
	public static final int PASSABLE_IS_TRANSP = 512;		// additional state, used in LevelRenderer
	public static final int PASSABLE_IS_OBJECT_KEY = 1024;
	public static final int PASSABLE_IS_DOOR_OPENED_BY_HERO = 2048;    // was door opened by hero at least once?

	public static final int PASSABLE_MASK_HERO = PASSABLE_IS_WALL | PASSABLE_IS_TRANSP_WALL | PASSABLE_IS_DECORATION | PASSABLE_IS_DOOR | PASSABLE_IS_MONSTER;
	public static final int PASSABLE_MASK_MONSTER = PASSABLE_IS_WALL | PASSABLE_IS_TRANSP_WALL | PASSABLE_IS_DECORATION | PASSABLE_IS_DOOR | PASSABLE_IS_MONSTER | PASSABLE_IS_HERO;	// PASSABLE_IS_OBJECT_KEY
	public static final int PASSABLE_MASK_SHOOT_W = PASSABLE_IS_WALL | PASSABLE_IS_DOOR;
	public static final int PASSABLE_MASK_SHOOT_WM = PASSABLE_IS_WALL | PASSABLE_IS_DOOR | PASSABLE_IS_DECORATION | PASSABLE_IS_MONSTER;
	public static final int PASSABLE_MASK_WALL_N_TRANSP = PASSABLE_IS_WALL | PASSABLE_IS_TRANSP;
	public static final int PASSABLE_MASK_OBJECT = PASSABLE_IS_OBJECT | PASSABLE_IS_OBJECT_ORIG | PASSABLE_IS_OBJECT_KEY;
	public static final int PASSABLE_MASK_DOOR = ~PASSABLE_IS_DOOR_OPENED_BY_HERO;
	public static final int PASSABLE_MASK_OBJECT_DROP = PASSABLE_IS_WALL | PASSABLE_IS_TRANSP_WALL | PASSABLE_IS_DECORATION | PASSABLE_IS_DOOR | PASSABLE_IS_OBJECT;

	public static Door[][] doorsMap;
	public static Mark[][] marksMap;
	public static ArrayList<ArrayList<Mark>> marksHash;

	public static void init()
	{
		State.levelWidth = 1;
		State.levelHeight = 1;

		State.doors = new Door[MAX_DOORS];
		State.monsters = new Monster[MAX_MONSTERS];
		State.marks = new Mark[MAX_MARKS];
		State.actions = new ArrayList<ArrayList<Action>>();

		marksHash = new ArrayList<ArrayList<Mark>>();

		for (int i = 0; i < MAX_DOORS; i++) {
			State.doors[i] = new Door();
		}

		for (int i = 0; i < MAX_MONSTERS; i++) {
			State.monsters[i] = new Monster();
		}

		for (int i = 0; i < MAX_MARKS; i++) {
			State.marks[i] = new Mark();
		}
	}

	public static boolean exists(int idx)
	{
		if (idx > MAX_LEVEL) {
			return false;
		}

		try {
			Game.assetManager.open("levels/level-" + String.valueOf(idx) + ".map").close();
			return true;
		} catch (IOException ex) {
		}

		return false;
	}

	public static void load(int idx)
	{
		try {
			Level.create(
				ZameGame.readBytes(Game.assetManager.open(String.format(Locale.US, "levels/level-%d.map", idx))),
				LevelConfig.read(Game.assetManager, idx)
			);

			String levelUri;

			if (idx < FIRST_REAL_LEVEL) {
				levelUri = "/level/ep0-lv" + String.valueOf(idx);
			} else {
				levelUri = String.format(
					Locale.US,
					"/level/ep%d-lv%d",
					(idx - FIRST_REAL_LEVEL) / 5 + 1,
					(idx - FIRST_REAL_LEVEL) % 5 + 1
				);
			}

			ZameApplication.trackPageView(levelUri);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void create(byte[] data, LevelConfig conf)
	{
		int pos = 0;
		init();		// re-init level. just for case

		State.levelHeight = data[pos++];
		State.levelWidth = data[pos++];

		if (State.levelWidth > MAX_WIDTH || State.levelHeight > MAX_HEIGHT) {
			throw new RuntimeException("Too big level");
		}

		State.setStartValues();

		State.wallsMap = new int[State.levelHeight][State.levelWidth];
		State.transpMap = new int[State.levelHeight][State.levelWidth];
		State.objectsMap = new int[State.levelHeight][State.levelWidth];
		State.decorationsMap = new int[State.levelHeight][State.levelWidth];
		State.passableMap = new int[State.levelHeight][State.levelWidth];

		State.doorsCount = 0;
		State.monstersCount = 0;
		State.marksCount = 0;
		State.actions.clear();

		for (int i = 0; i < MAX_ACTIONS; i++) {
			State.actions.add(new ArrayList<Action>());
		}

		for (int i = 0; i < State.levelHeight; i++)
		{
			for (int j = 0; j < State.levelWidth; j++)
			{
				State.wallsMap[i][j] = 0;
				State.transpMap[i][j] = 0;
				State.objectsMap[i][j] = 0;
				State.decorationsMap[i][j] = 0;
				State.passableMap[i][j] = 0;
			}
		}

		// ----

		for (int i = 0; i < State.levelHeight; i++)
		{
			for (int j = 0; j < State.levelWidth; j++)
			{
				int value = (int)data[pos++] & 0x000000FF;

				// guarantee 1-cell wall border around level
				if ((value < 0x10 || value >= 0x30) && (i == 0 || j == 0 || i == (State.levelHeight - 1) || j == (State.levelWidth - 1))) {
					value = 0x10;
				}

				if (value < 0x10)
				{
					if (value > 0)
					{
						if (value <= 4)
						{
							State.heroX = (float)j + 0.5f;
							State.heroY = (float)i + 0.5f;
							State.setHeroA((float)(180 - value * 90));

							State.passableMap[i][j] |= PASSABLE_IS_HERO;
						}
						else if (value == 5)
						{
							// special invisible, non-rendererable transparent wall (to make special errects with transparents)
							State.passableMap[i][j] |= PASSABLE_IS_TRANSP;
						}
					}
				}
				else if (value < 0x30)
				{
					State.wallsMap[i][j] = value - 0x10 + TextureLoader.BASE_WALLS;
					State.passableMap[i][j] |= PASSABLE_IS_WALL;
				}
				else if (value < 0x50)
				{
					State.transpMap[i][j] = value - 0x30 + TextureLoader.BASE_TRANSPARENTS;
					State.passableMap[i][j] |= PASSABLE_IS_TRANSP;

					if (value < 0x38 || value >= 0x40) {
						State.passableMap[i][j] |= PASSABLE_IS_TRANSP_WALL;
					}
				}
				else if (value < 0x70)
				{
					if (State.doorsCount >= MAX_DOORS) {
						throw new RuntimeException("Too many doors");
					}

					State.wallsMap[i][j] = -1;	// mark door for PortalTracer
					State.passableMap[i][j] |= PASSABLE_IS_DOOR;
					Door door = State.doors[State.doorsCount++];

					door.init();
					door.x = j;
					door.y = i;
					door.texture = TextureLoader.BASE_DOORS_F + (value % 0x10);
					door.vert = (value >= 0x60);
				}
				else if (value < 0x80)
				{
					State.objectsMap[i][j] = value - 0x70 + TextureLoader.BASE_OBJECTS;
					State.decorationsMap[i][j] = -1;	// optimize renderer a bit - if there is object, than there is no transparents in this cell
					State.passableMap[i][j] |= PASSABLE_IS_OBJECT | PASSABLE_IS_OBJECT_ORIG;

					if (State.objectsMap[i][j]==TextureLoader.OBJ_KEY_BLUE || State.objectsMap[i][j]==TextureLoader.OBJ_KEY_RED || State.objectsMap[i][j]==TextureLoader.OBJ_KEY_GREEN) {
						State.passableMap[i][j] |= PASSABLE_IS_OBJECT_KEY;
					}

					State.totalItems++;
				}
				else if (value < 0xA0)
				{
					State.decorationsMap[i][j] = value - 0x80 + TextureLoader.BASE_DECORATIONS;

					if ((value % 0x10) < 12) {
						State.passableMap[i][j] |= PASSABLE_IS_DECORATION;
					}
				}
				else
				{
					if (State.monstersCount >= MAX_MONSTERS) {
						throw new RuntimeException("Too many monsters");
					}

					int num = (value - 0xA0) / 0x10;

					State.passableMap[i][j] |= PASSABLE_IS_MONSTER;
					Monster mon = State.monsters[State.monstersCount++];

					mon.init();
					mon.cellX = j;
					mon.cellY = i;
					mon.texture = TextureLoader.COUNT_MONSTER * num;
					mon.dir = (value % 0x10);

					mon.health = conf.monsters[num].health;
					mon.hits = conf.monsters[num].hits;
					mon.setAttackDist(conf.monsters[num].hitType != LevelConfig.HIT_TYPE_EAT);

					if (conf.monsters[num].hitType == LevelConfig.HIT_TYPE_PIST) {
						mon.shootSoundIdx = SoundManager.SOUND_SHOOT_PIST;
						mon.ammoType = TextureLoader.OBJ_CLIP;
					} else if (conf.monsters[num].hitType == LevelConfig.HIT_TYPE_SHTG) {
						mon.shootSoundIdx = SoundManager.SOUND_SHOOT_SHTG;
						mon.ammoType = TextureLoader.OBJ_SHELL;
					} else { // HIT_TYPE_EAT
						mon.shootSoundIdx = SoundManager.SOUND_SHOOT_EAT;
						mon.ammoType = 0;
					}

					State.totalMonsters++;
				}
			}
		}

		for (int i = 0; i < State.monstersCount; i++) {
			State.monsters[i].update();
		}

		while (((int)data[pos] & 0x000000FF) != 255)
		{
			Mark mark = State.marks[State.marksCount++];

			mark.id = (int)data[pos++] & 0x000000FF;
			mark.y = (int)data[pos++] & 0x000000FF;
			mark.x = (int)data[pos++] & 0x000000FF;
		}

		pos++;
		int secretsMask = 0;

		while (((int)data[pos] & 0x000000FF) != 255)
		{
			int mark = (int)data[pos++] & 0x000000FF;
			ArrayList<Action> actions = State.actions.get(mark);

			while (((int)data[pos] & 0x000000FF) != 0)
			{
				Action act = new Action();
				act.type = (int)data[pos++] & 0x000000FF;

				if (act.type == ACTION_SECRET) {
					act.param = (int)data[pos++] & 0x000000FF;

					if ((secretsMask & act.param) == 0) {
						secretsMask |= act.param;
						State.totalSecrets++;
					}
				} else if (
					act.type == ACTION_REQ_KEY
					|| act.type == ACTION_WALL
				) {
					act.mark = (int)data[pos++] & 0x000000FF;
					act.param = (int)data[pos++] & 0x000000FF;
				} else if (
					act.type == ACTION_CLOSE
					|| act.type == ACTION_OPEN
					|| act.type == ACTION_UNMARK
				) {
					act.mark = (int)data[pos++] & 0x000000FF;
				} else if (
					act.type == ACTION_ENSURE_WEAPON
					|| act.type == ACTION_MSG_ON
				) {
					act.param = (int)data[pos++] & 0x000000FF;
				} else if (act.type == ACTION_BTN_ON) {
					act.param = 1 << (((int)data[pos++] & 0x000000FF) - 1);
				}

				actions.add(act);
			}

			pos++;
		}

		updateMaps();
		executeActions(0);
	}

	public static void updateMaps()
	{
		for (int i = 0; i < MAX_DOORS; i++) {
			State.doors[i].index = i;
			State.doors[i].mark = null;
		}

		for (int i = 0; i < MAX_MONSTERS; i++) {
			State.monsters[i].index = i;
		}

		doorsMap = new Door[State.levelHeight][State.levelWidth];
		marksMap = new Mark[State.levelHeight][State.levelWidth];

		marksHash.clear();

		for (int i = 0; i <= MAX_MARKS; i++) {
			marksHash.add(new ArrayList<Mark>());
		}

		for (int i = 0; i < State.levelHeight; i++)
		{
			for (int j = 0; j < State.levelWidth; j++)
			{
				doorsMap[i][j] = null;
				marksMap[i][j] = null;
			}
		}

		for (int i = 0; i < State.doorsCount; i++)
		{
			Door door = State.doors[i];
			doorsMap[door.y][door.x] = door;
		}

		for (int i = 0; i < State.marksCount; i++)
		{
			Mark mark = State.marks[i];
			marksHash.get(mark.id).add(mark);

			if (doorsMap[mark.y][mark.x] == null) {
				marksMap[mark.y][mark.x] = mark;
			} else {
				doorsMap[mark.y][mark.x].mark = mark;
			}
		}
	}

	public static boolean executeActions(int id)
	{
		ArrayList<Action> actions = State.actions.get(id);

		if (actions.isEmpty()) {
			return false;
		}

		if (State.levelNum == 1) {
			ZameApplication.trackEvent("Tutorial", "ExecAction", String.valueOf(id), 0);
		}

		for (Action act : actions)
		{
			ArrayList<Mark> marks = marksHash.get(act.mark);

			switch (act.type)
			{
				case ACTION_CLOSE:
				case ACTION_OPEN:
				case ACTION_REQ_KEY:
				{
					for (Mark mark : marks)
					{
						Door door = doorsMap[mark.y][mark.x];

						if (door != null)
						{
							door.stick(act.type == ACTION_OPEN);

							if (act.type == ACTION_REQ_KEY) {
								door.requiredKey = act.param;
							}
						}
					}
				}
				break;

				case ACTION_UNMARK:
				{
					for (Mark mark : marks) {
						marksMap[mark.y][mark.x] = null;
					}

					for (int i = 0; i < State.doorsCount; i++) {
						Door door = State.doors[i];

						if (door.mark != null && door.mark.id == act.mark) {
							door.mark = null;
						}
					}

					marksHash.get(act.mark).clear();
					int idx = 0;

					for (int i = 0; i < State.marksCount; i++)
					{
						if (idx != i) {
							State.marks[idx] = State.marks[i];
						}

						if (State.marks[idx].id != act.mark) {
							idx++;
						}
					}

					if (idx < MAX_MARKS) {
						State.marks[idx] = new Mark();	// fix references
					}

					State.marksCount = idx;
				}
				break;

				case ACTION_WALL:
				{
					for (Mark mark : marks)
					{
						if ((State.passableMap[mark.y][mark.x] & Level.PASSABLE_IS_MONSTER) != 0)
						{
							for (int i = 0; i < State.monstersCount; i++)
							{
								Monster mon = State.monsters[i];

								if (mon.cellX == mark.x && mon.cellY == mark.y)
								{
									for (int j = i; j < State.monstersCount - 1; j++) {
										State.monsters[j] = State.monsters[j + 1];
									}

									State.monstersCount--;
									State.monsters[State.monstersCount] = new Monster();	// fix references

									break;
								}
							}
						}

						if (act.param > 0)
						{
							State.wallsMap[mark.y][mark.x] = TextureLoader.BASE_WALLS + act.param - 1;
							State.transpMap[mark.y][mark.x] = 0;
							State.decorationsMap[mark.y][mark.x] = 0;
							State.passableMap[mark.y][mark.x] = PASSABLE_IS_WALL;
						}
						else
						{
							State.wallsMap[mark.y][mark.x] = 0;
							State.transpMap[mark.y][mark.x] = 0;
							State.decorationsMap[mark.y][mark.x] = 0;
							State.passableMap[mark.y][mark.x] = 0;
						}

						if (Level.doorsMap[mark.y][mark.x] != null)
						{
							for (int i = Level.doorsMap[mark.y][mark.x].index; i < State.doorsCount - 1; i++) {
								State.doors[i] = State.doors[i + 1];
							}

							Level.doorsMap[mark.y][mark.x] = null;
							State.doorsCount--;
							State.doors[State.doorsCount] = new Door(); 	// fix references
						}
					}
				}
				break;

				case ACTION_NEXT_LEVEL:
					Game.nextLevel(false);
					break;

				case ACTION_NEXT_TUTOR_LEVEL:
					Game.nextLevel(true);
					break;

				case ACTION_DISABLE_PISTOL:
					State.heroHasWeapon[Weapons.WEAPON_PISTOL] = false;
					State.heroWeapon = Weapons.WEAPON_HAND;
					Weapons.updateWeapon();
					break;

				case ACTION_ENABLE_PISTOL:
					State.reInitPistol();
					State.heroWeapon = Weapons.WEAPON_PISTOL;
					Weapons.updateWeapon();
					break;

				case ACTION_WEAPON_HAND:
					State.heroWeapon = Weapons.WEAPON_HAND;
					Weapons.updateWeapon();
					break;

				case ACTION_RESTORE_HEALTH:
					State.heroHealth = 100;
					break;

				case ACTION_SECRET:
					if ((State.foundSecretsMask & act.param) == 0)
					{
						State.foundSecretsMask |= act.param;
						State.foundSecrets++;
						Overlay.showLabel(Labels.LABEL_SECRET_FOUND);
					}
					break;

				case ACTION_ENSURE_WEAPON:
					if (act.param > 0 && act.param < Weapons.WEAPON_LAST) {
						State.heroHasWeapon[act.param] = true;

						if (act.param == Weapons.WEAPON_PISTOL || act.param == Weapons.WEAPON_CHAINGUN || act.param == Weapons.WEAPON_DBLCHAINGUN) {
							if (State.heroAmmo[Weapons.AMMO_PISTOL] < Weapons.ENSURED_PISTOL_AMMO) {
								State.heroAmmo[Weapons.AMMO_PISTOL] = Weapons.ENSURED_PISTOL_AMMO;
							}
						} else if (act.param == Weapons.WEAPON_SHOTGUN || act.param == Weapons.WEAPON_DBLSHOTGUN) {
							if (State.heroAmmo[Weapons.AMMO_SHOTGUN] < Weapons.ENSURED_SHOTGUN_AMMO) {
								State.heroAmmo[Weapons.AMMO_SHOTGUN] = Weapons.ENSURED_SHOTGUN_AMMO;
							}
						}

						Weapons.updateWeapon();
					}
					break;

				case ACTION_BTN_ON:
					State.highlightedControlTypeMask |= act.param;
					break;

				case ACTION_BTN_OFF:
					State.highlightedControlTypeMask = 0;
					break;

				case ACTION_MSG_ON:
					State.shownMessageId = act.param;
					break;

				case ACTION_MSG_OFF:
					State.shownMessageId = 0;
					break;
			}
		}

		return true;
	}

	public static void setPassable(float x, float y, float wallDist, int mask)
	{
		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(State.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(State.levelHeight - 1, (int)(y + wallDist));

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				State.passableMap[j][i] |= mask;
			}
		}
	}

	public static void clearPassable(float x, float y, float wallDist, int mask)
	{
		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(State.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(State.levelHeight - 1, (int)(y + wallDist));

		mask = ~mask;

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				State.passableMap[j][i] &= mask;
			}
		}
	}

	private static boolean[] wasAlreadyInWall = null;
	public static boolean quickReturnFromFillInitialInWall = false; // set to false before using fillInitialInWallMap

	public static void fillInitialInWallMap(float x, float y, float wallDist, int mask)
	{
		if (quickReturnFromFillInitialInWall) {
			return;
		}

		if (wasAlreadyInWall == null) {
			wasAlreadyInWall = new boolean[9];
		}

		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(State.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(State.levelHeight - 1, (int)(y + wallDist));

		int cnt = 0;
		boolean atLeastOneAtWall = false;

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				boolean inWall = ((State.passableMap[j][i] & mask) != 0);
				wasAlreadyInWall[cnt++] = inWall;
				atLeastOneAtWall = atLeastOneAtWall || inWall;

				if (cnt >= 9) {
					return;
				}
			}
		}

		while (cnt < 9) {
			wasAlreadyInWall[cnt++] = false;
		}

		if (!atLeastOneAtWall) {
			quickReturnFromFillInitialInWall = true;
		}
	}

	// call fillInitialInWallMap before using isPassable
	public static boolean isPassable(float x, float y, float wallDist, int mask)
	{
		// level always have 1-cell wall border, so we can skip border checks (like x>=0 && x<width)
		// but just for case limit coordinates

		int fx = Math.max(0, (int)(x - wallDist));
		int tx = Math.min(State.levelWidth - 1, (int)(x + wallDist));
		int fy = Math.max(0, (int)(y - wallDist));
		int ty = Math.min(State.levelHeight - 1, (int)(y + wallDist));

		int cnt = 0;

		for (int i = fx; i <= tx; i++) {
			for (int j = fy; j <= ty; j++) {
				if (((State.passableMap[j][i] & mask) != 0) && ((cnt >= 9) || !wasAlreadyInWall[cnt])) {
					return false;
				}

				cnt++;
			}
		}

		return true;
	}
}
