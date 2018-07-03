package zame.game.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import zame.game.App;
import zame.game.Common;

@SuppressWarnings("WeakerAccess")
public final class State {
    public static int levelNum;
    public static float heroX;
    public static float heroY;
    public static float heroA;
    public static int heroKeysMask;
    public static int heroWeapon;
    public static int heroHealth;
    public static int heroArmor;
    public static boolean[] heroHasWeapon;
    public static int[] heroAmmo;

    public static int totalItems;
    public static int totalMonsters;
    public static int totalSecrets;
    public static int pickedItems;
    public static int killedMonsters;
    public static int foundSecrets;
    public static int foundSecretsMask;

    // *much* better way: reload level, than replace all changed data with data from state
    // (instead of have all these maps in state. but I'm to lazy :)

    public static int levelWidth;
    public static int levelHeight;

    public static int[][] wallsMap;
    public static int[][] transpMap;
    public static int[][] objectsMap;
    public static int[][] decorationsMap;
    public static int[][] passableMap;

    public static int doorsCount;
    public static Door[] doors;

    public static int monstersCount;
    public static Monster[] monsters;

    public static int marksCount;
    public static Mark[] marks;

    public static ArrayList<ArrayList<Action>> actions;

    public static int[][] drawedAutoWalls;
    public static int autoWallsCount;
    public static AutoWall[] autoWalls;

    public static boolean showAutoMap;
    public static long tempElapsedTime;
    public static long tempLastTime;
    public static boolean godMode;

    public static int sfcBlockerTimeout;

    public static int highlightedControlTypeMask;
    public static int shownMessageId;

    public static boolean tmpReloadLevel;

    private State() {
    }

    public static void setHeroA(float angle) {
        heroA = angle;
        Common.heroAngleUpdated();
    }

    public static void reInit() {
        heroWeapon = 1;
        heroHealth = 100;
        heroArmor = 0;
        heroHasWeapon = new boolean[Weapons.WEAPON_LAST];
        heroAmmo = new int[Weapons.AMMO_LAST];
        highlightedControlTypeMask = 0;
        shownMessageId = 0;

        for (int i = 0; i < Weapons.WEAPON_LAST; i++) {
            heroHasWeapon[i] = false;
        }

        for (int i = 0; i < Weapons.AMMO_LAST; i++) {
            heroAmmo[i] = 0;
        }

        heroHasWeapon[Weapons.WEAPON_HAND] = true;
        reInitPistol();

        Weapons.updateWeapon();
    }

    public static void reInitPistol() {
        heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
        heroAmmo[Weapons.AMMO_PISTOL] = Weapons.ENSURED_PISTOL_AMMO;
    }

    @SuppressWarnings("MagicNumber")
    public static void init() {
        levelNum = 1;
        reInit();

        drawedAutoWalls = new int[Level.MAX_HEIGHT][Level.MAX_WIDTH];
        autoWalls = new AutoWall[LevelRenderer.MAX_AUTO_WALLS];

        for (int i = 0; i < LevelRenderer.MAX_AUTO_WALLS; i++) {
            autoWalls[i] = new AutoWall();
        }

        showAutoMap = false;
        godMode = false;
        sfcBlockerTimeout = 40 * 60 * 3; // 40 updates per second, 40*60*3 = 3 minutes, used only in TYPE_SFC
    }

    public static void setStartValues() {
        heroKeysMask = 0;
        autoWallsCount = 0;
        totalItems = 0;
        totalMonsters = 0;
        totalSecrets = 0;
        pickedItems = 0;
        killedMonsters = 0;
        foundSecrets = 0;
        foundSecretsMask = 0;

        highlightedControlTypeMask = 0;
        shownMessageId = 0;

        for (int i = 0; i < Level.MAX_HEIGHT; i++) {
            for (int j = 0; j < Level.MAX_WIDTH; j++) {
                drawedAutoWalls[i][j] = 0;
            }
        }
    }

    public static void writeTo(ObjectOutputStream os) throws IOException {
        os.writeUTF("GloomyDungeons.7");

        os.writeUTF(App.self.getVersionName());
        os.writeInt(levelNum);
        os.writeFloat(heroX);
        os.writeFloat(heroY);
        os.writeFloat(heroA);
        os.writeInt(heroKeysMask);
        os.writeInt(heroWeapon);
        os.writeInt(heroHealth);
        os.writeInt(heroArmor);
        Common.writeBooleanArray(os, heroHasWeapon);
        Common.writeIntArray(os, heroAmmo);
        os.writeInt(totalItems);
        os.writeInt(totalMonsters);
        os.writeInt(totalSecrets);
        os.writeInt(pickedItems);
        os.writeInt(killedMonsters);
        os.writeInt(foundSecrets);
        os.writeInt(foundSecretsMask);
        os.writeInt(levelWidth);
        os.writeInt(levelHeight);
        Common.writeInt2dArray(os, wallsMap);
        Common.writeInt2dArray(os, transpMap);
        Common.writeInt2dArray(os, objectsMap);
        Common.writeInt2dArray(os, decorationsMap);
        Common.writeInt2dArray(os, passableMap);
        Common.writeObjectArray(os, doors, doorsCount);
        Common.writeObjectArray(os, monsters, monstersCount);
        Common.writeObjectArray(os, marks, marksCount);

        os.writeInt(actions.size());

        for (ArrayList<Action> items : actions) {
            os.writeInt(items.size());

            for (Action act : items) {
                act.writeExternal(os);
            }
        }

        Common.writeInt2dArray(os, drawedAutoWalls);
        Common.writeObjectArray(os, autoWalls, autoWallsCount);

        os.writeBoolean(showAutoMap);
        os.writeLong(tempElapsedTime);
        os.writeLong(tempLastTime);
        os.writeBoolean(godMode);
        os.writeInt(sfcBlockerTimeout);

        os.writeInt(highlightedControlTypeMask);
        os.writeInt(shownMessageId);
        os.writeBoolean(false); // onScreenControlsSelector
        os.writeBoolean(false); // changeControlsDialog
    }

    @SuppressWarnings("MagicNumber")
    public static void readFrom(ObjectInputStream is) throws IOException, ClassNotFoundException {
        int saveFileVersion = 0;
        String ver = is.readUTF();

        if ("GloomyDungeons.1".equals(ver)) {
            saveFileVersion = 1;
        } else if ("GloomyDungeons.2".equals(ver)) {
            saveFileVersion = 2;
        } else if ("GloomyDungeons.3".equals(ver)) {
            saveFileVersion = 3;
        } else if ("GloomyDungeons.4".equals(ver)) {
            saveFileVersion = 4;
        } else if ("GloomyDungeons.5".equals(ver)) {
            saveFileVersion = 5;
        } else if ("GloomyDungeons.6".equals(ver)) {
            saveFileVersion = 6;
        } else if ("GloomyDungeons.7".equals(ver)) {
            saveFileVersion = 7;
        }

        if (saveFileVersion < 1) {
            throw new ClassNotFoundException(String.format(Locale.US, "Save from newer game version (%s)", ver));
        }

        if (saveFileVersion >= 2) {
            is.readUTF();
        }

        levelNum = is.readInt();
        heroX = is.readFloat();
        heroY = is.readFloat();
        setHeroA(is.readFloat());
        heroKeysMask = is.readInt();
        heroWeapon = is.readInt();
        heroHealth = is.readInt();
        heroArmor = is.readInt();
        heroHasWeapon = Common.readBooleanArray(is);
        heroAmmo = Common.readIntArray(is);
        totalItems = is.readInt();
        totalMonsters = is.readInt();
        totalSecrets = is.readInt();
        pickedItems = is.readInt();
        killedMonsters = is.readInt();
        foundSecrets = is.readInt();
        foundSecretsMask = is.readInt();
        levelWidth = is.readInt();
        levelHeight = is.readInt();
        wallsMap = Common.readInt2dArray(is);
        transpMap = Common.readInt2dArray(is);
        objectsMap = Common.readInt2dArray(is);
        decorationsMap = Common.readInt2dArray(is);
        passableMap = Common.readInt2dArray(is);
        doorsCount = Common.readObjectArray(is, doors, Door.class);
        monstersCount = Common.readObjectArray(is, monsters, Monster.class);

        if (saveFileVersion == 1) {
            for (int i = 0; i < monstersCount; i++) {
                monsters[i].texture -= 0xA0;
            }
        }

        marksCount = Common.readObjectArray(is, marks, Mark.class);

        actions = new ArrayList<ArrayList<Action>>();

        for (int i = 0; i < Level.MAX_ACTIONS; i++) {
            actions.add(new ArrayList<Action>());
        }

        int actionsCount = is.readInt();

        for (int i = 0; i < actionsCount; i++) {
            ArrayList<Action> items = actions.get(i);
            int itemsCount = is.readInt();

            for (int j = 0; j < itemsCount; j++) {
                Action act = new Action();
                act.readExternal(is);

                items.add(act);
            }
        }

        drawedAutoWalls = Common.readInt2dArray(is);
        autoWallsCount = Common.readObjectArray(is, autoWalls, AutoWall.class);

        if (saveFileVersion >= 3) {
            showAutoMap = is.readBoolean();
            tempElapsedTime = is.readLong();
            tempLastTime = is.readLong();
        }

        if (saveFileVersion >= 4) {
            godMode = is.readBoolean();
        }

        if (saveFileVersion >= 5) {
            sfcBlockerTimeout = is.readInt();
        }

        if (saveFileVersion >= 6) {
            highlightedControlTypeMask = is.readInt();
            shownMessageId = is.readInt();
            is.readBoolean(); // onScreenControlsSelector
        }

        if (saveFileVersion >= 7) {
            is.readBoolean(); // changeControlsDialog
        }

        // post-load updates

        if (heroHasWeapon.length < Weapons.WEAPON_LAST) {
            boolean[] newHeroHasWeapon = new boolean[Weapons.WEAPON_LAST];

            //noinspection ManualArrayCopy
            for (int i = 0; i < heroHasWeapon.length; i++) {
                newHeroHasWeapon[i] = heroHasWeapon[i];
            }

            for (int i = heroHasWeapon.length; i < Weapons.WEAPON_LAST; i++) {
                newHeroHasWeapon[i] = false;
            }

            heroHasWeapon = newHeroHasWeapon;
        }

        Level.updateMaps();
        LevelRenderer.updateAutoWalls();
        Weapons.updateWeapon();

        tmpReloadLevel = false;

        if (saveFileVersion < 6) {
            if (levelNum < 9) {
                levelNum = 1;
                tmpReloadLevel = true;
            } else if (levelNum < 14) {
                levelNum -= 7;
            } else if (levelNum == 14) {
                levelNum = 7;
                tmpReloadLevel = true;
            } else {
                levelNum -= 8;
            }
        }
    }
}
