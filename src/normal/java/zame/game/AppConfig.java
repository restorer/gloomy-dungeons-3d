package zame.game;

import zame.game.SoundManager;
import zame.game.engine.TextureLoader;
import zame.game.engine.TextureLoader.TextureToLoad;
import zame.game.engine.Weapons;
import zame.game.engine.Weapons.WeaponParams;

public class AppConfig
{
    public static final int TEXTURE_HAND = 4;       // 4, 5, 6, 7
    public static final int TEXTURE_PIST = 8;       // 8, 9, 10, 11, 12
    public static final int TEXTURE_SHTG = 13;      // 13, 14, 15, 16, 17
    public static final int TEXTURE_CHGN = 18;      // 18, 19, 20, 21
    public static final int TEXTURE_DBLSHTG = 22;   // 22, 23, 24, 25, 26
    public static final int TEXTURE_DBLCHGN = 27;   // 27, 28, 29, 30
    public static final int TEXTURE_SAW = 31;       // 31, 32, 33
    public static final int TEXTURE_LAST = 34;

    public static final TextureToLoad[] TEXTURES_TO_LOAD = new TextureToLoad[] {
        new TextureToLoad(TextureLoader.TEXTURE_MON, 0, TextureToLoad.TYPE_MONSTERS),
        new TextureToLoad(TextureLoader.TEXTURE_MAIN, R.drawable.texmap),
        new TextureToLoad(TextureLoader.TEXTURE_FLOOR, 0, TextureToLoad.TYPE_FLOOR),
        new TextureToLoad(TextureLoader.TEXTURE_CEIL, 0, TextureToLoad.TYPE_CEIL),

        new TextureToLoad(TEXTURE_HAND + 0, R.drawable.hit_hand_1),
        new TextureToLoad(TEXTURE_HAND + 1, R.drawable.hit_hand_2),
        new TextureToLoad(TEXTURE_HAND + 2, R.drawable.hit_hand_3),
        new TextureToLoad(TEXTURE_HAND + 3, R.drawable.hit_hand_4),

        new TextureToLoad(TEXTURE_PIST + 0, R.drawable.hit_pist_1),
        new TextureToLoad(TEXTURE_PIST + 1, R.drawable.hit_pist_2),
        new TextureToLoad(TEXTURE_PIST + 2, R.drawable.hit_pist_3),
        new TextureToLoad(TEXTURE_PIST + 3, R.drawable.hit_pist_4),
        new TextureToLoad(TEXTURE_PIST + 4, R.drawable.hit_pist_5),

        new TextureToLoad(TEXTURE_SHTG + 0, R.drawable.hit_shtg_1),
        new TextureToLoad(TEXTURE_SHTG + 1, R.drawable.hit_shtg_2),
        new TextureToLoad(TEXTURE_SHTG + 2, R.drawable.hit_shtg_3),
        new TextureToLoad(TEXTURE_SHTG + 3, R.drawable.hit_shtg_4),
        new TextureToLoad(TEXTURE_SHTG + 4, R.drawable.hit_shtg_5),

        new TextureToLoad(TEXTURE_CHGN + 0, R.drawable.hit_chgn_1),
        new TextureToLoad(TEXTURE_CHGN + 1, R.drawable.hit_chgn_2),
        new TextureToLoad(TEXTURE_CHGN + 2, R.drawable.hit_chgn_3),
        new TextureToLoad(TEXTURE_CHGN + 3, R.drawable.hit_chgn_4),

        new TextureToLoad(TEXTURE_DBLSHTG + 0, R.drawable.hit_dblshtg_1),
        new TextureToLoad(TEXTURE_DBLSHTG + 1, R.drawable.hit_dblshtg_2),
        new TextureToLoad(TEXTURE_DBLSHTG + 2, R.drawable.hit_dblshtg_3),
        new TextureToLoad(TEXTURE_DBLSHTG + 3, R.drawable.hit_dblshtg_4),
        new TextureToLoad(TEXTURE_DBLSHTG + 4, R.drawable.hit_dblshtg_5),

        new TextureToLoad(TEXTURE_DBLCHGN + 0, R.drawable.hit_dblchgn_1),
        new TextureToLoad(TEXTURE_DBLCHGN + 1, R.drawable.hit_dblchgn_2),
        new TextureToLoad(TEXTURE_DBLCHGN + 2, R.drawable.hit_dblchgn_3),
        new TextureToLoad(TEXTURE_DBLCHGN + 3, R.drawable.hit_dblchgn_4),

        new TextureToLoad(TEXTURE_SAW + 0, R.drawable.hit_saw_1),
        new TextureToLoad(TEXTURE_SAW + 1, R.drawable.hit_saw_2),
        new TextureToLoad(TEXTURE_SAW + 2, R.drawable.hit_saw_3),
    };

    public static final WeaponParams[] WEAPONS = new WeaponParams[] {
        // WEAPON_HAND
        new WeaponParams(new int[] {
            0, 1, 1, 1, 2, 2, 2, -3, 3, 3, 2, 2, 2, 1, 1, 0, 0
        }, -1, 0, 1, 5, TEXTURE_HAND, 1.0f, 0f, 1.5f, SoundManager.SOUND_SHOOT_HAND, true, SoundManager.SOUND_NOWAY),
        // WEAPON_PISTOL
        new WeaponParams(new int[] {
            0, 1, 1, 1, 1, 2, 2, 2, 2, -3, 3, 3, 3, 4, 4, 4, 4, 0, 0, 0, 0, 0
        }, Weapons.AMMO_PISTOL, 1, 2, 5, TEXTURE_PIST, 1.0f, 0.0f, 1.5f, SoundManager.SOUND_SHOOT_PIST, false, 0),
        // WEAPON_SHOTGUN
        new WeaponParams(new int[] {
            0, 0, 0, 0, 0,
            1, 1, 1, 1, 1,
            -2, 2, 2, 2, 2,
            3, 3, 3, 3, 3,
            3, 3, 3, 3, 3,
            4, 4, 4, 4, 4,
            4, 4, 4, 4, 4,
            0, 0, 0, 0, 0,
        }, Weapons.AMMO_SHOTGUN, 1, 8, 10, TEXTURE_SHTG, 0.9f, 0.1f, 1.35f, SoundManager.SOUND_SHOOT_SHTG, false, 0),
        // WEAPON_CHAINGUN
        new WeaponParams(new int[] {
            0,
            1, 1, 1,
            -2, 2, 2,
            -3, 3, 3,
            0, 0
        }, Weapons.AMMO_PISTOL, 1, 2, 5, TEXTURE_CHGN, 0.8f, 0f, 1.2f, SoundManager.SOUND_SHOOT_PIST, false, 0),
        // WEAPON_DBLSHOTGUN
        new WeaponParams(new int[] {
            0,
            1, 1, 1, 1, 1,
            -2, 2, 2, 2, 2,
            3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
        }, Weapons.AMMO_SHOTGUN, 2, 14, 25, TEXTURE_DBLSHTG, 0.8f, 0.0f, 1.2f, SoundManager.SOUND_SHOOT_DBLSHTG, false, 0),
        // WEAPON_DBLCHAINGUN
        new WeaponParams(new int[] {
            0,
            1, 1, 1,
            -2, 2, 2,
            -3, 3, 3,
            0, 0
        }, Weapons.AMMO_PISTOL, 2, 4, 8, TEXTURE_DBLCHGN, 1.2f, -0.1f, 1.8f, SoundManager.SOUND_SHOOT_PIST, false, 0),
        // WEAPON_CHAINSAW
        new WeaponParams(new int[] {
            0, 0, 0, 0, 0,
            1, 1, 1, 1, 1,
            -2,    2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            -1002, 2, 2, 2, -1002, 2, 2, 2, -1002, 2,
            1, 1, 1, 1, 1,
            0, 0, 0, 0, 0
        }, -1, 0, 1, 4 /* was: 3 */, TEXTURE_SAW, 0.8f, 0f, 1.2f, SoundManager.SOUND_SHOOT_SAW, true, 0),
    };
}
