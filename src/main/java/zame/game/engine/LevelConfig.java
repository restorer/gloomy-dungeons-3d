package zame.game.engine;

import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class LevelConfig
{
	public static final int HIT_TYPE_EAT = 0;
	public static final int HIT_TYPE_PIST = 1;
	public static final int HIT_TYPE_SHTG = 2;

	public static class MonsterConfig
	{
		int texture;
		int health;
		int hits;
		int hitType;

		public MonsterConfig(int texture, int health, int hits, int hitType)
		{
			this.texture = texture;
			this.health = health;
			this.hits = hits;
			this.hitType = hitType;
		}
	}

	public int levelNum;
	public int floorTexture;
	public int ceilTexture;
	public MonsterConfig[] monsters;

	public LevelConfig(int levelNum)
	{
		this.levelNum = levelNum;
		this.floorTexture = 2;
		this.ceilTexture = 2;

		this.monsters = new MonsterConfig[] {
			new MonsterConfig(1, 4, 4, HIT_TYPE_PIST),
			new MonsterConfig(2, 8, 8, HIT_TYPE_SHTG),
			new MonsterConfig(3, 32, 32, HIT_TYPE_EAT),
			new MonsterConfig(4, 64, 64, HIT_TYPE_EAT),
		};
	}

	public static LevelConfig read(AssetManager assetManager, int levelNum)
	{
		LevelConfig res = new LevelConfig(levelNum);

		try {
			InputStreamReader isr = new InputStreamReader(
				assetManager.open(String.format(Locale.US, "config/level-%d.txt", levelNum)),
				"UTF-8"
			);

			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();

			if (line != null) {
				String[] spl = line.split(" ");

				if (spl.length == 2) {
					res.floorTexture = Integer.parseInt(spl[0]);
					res.ceilTexture = Integer.parseInt(spl[1]);

					for (int i = 0; i < 4; i++) {
						line = br.readLine();

						if (line == null) {
							break;
						}

						spl = line.split(" ");

						if (spl.length != 4) {
							break;
						}

						res.monsters[i].texture = Integer.parseInt(spl[0]);
						res.monsters[i].health = Integer.parseInt(spl[1]);
						res.monsters[i].hits = Integer.parseInt(spl[2]);
						res.monsters[i].hitType = Integer.parseInt(spl[3]);
					}
				}
			}

			br.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (NumberFormatException ex) {
			throw new RuntimeException(ex);
		}

		return res;
	}
}
