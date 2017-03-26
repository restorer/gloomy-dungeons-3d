package zame.game.engine;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import zame.game.SoundManager;

@SuppressWarnings("WeakerAccess")
public class Door implements Externalizable {
    private static final long serialVersionUID = 0L;

    public static final float OPEN_POS_MAX = 0.9f;
    public static final float OPEN_POS_PASSABLE = 0.7f;

    public int index;
    public int x;
    public int y;
    public int texture;
    public boolean vert;
    public float openPos;
    public int dir;
    public boolean sticked;
    public int requiredKey;

    public long lastTime;
    public Mark mark;

    public void init() {
        openPos = 0.0f;
        dir = 0;
        lastTime = 0;
        sticked = false;
        requiredKey = 0;
        mark = null;
    }

    @Override
    public void writeExternal(ObjectOutput os) throws IOException {
        os.writeInt(x);
        os.writeInt(y);
        os.writeInt(texture);
        os.writeBoolean(vert);
        os.writeFloat(openPos);
        os.writeInt(dir);
        os.writeBoolean(sticked);
        os.writeInt(requiredKey);
    }

    @Override
    public void readExternal(ObjectInput is) throws IOException {
        x = is.readInt();
        y = is.readInt();
        texture = is.readInt();
        vert = is.readBoolean();
        openPos = is.readFloat();
        dir = is.readInt();
        sticked = is.readBoolean();
        requiredKey = is.readInt();

        lastTime = Game.elapsedTime;
    }

    public void stick(boolean opened) {
        sticked = true;
        dir = (opened ? 1 : -1);
        lastTime = 0; // instant open or close
    }

    @SuppressWarnings("MagicNumber")
    private float getVolume() {
        float dx = State.heroX - ((float)x + 0.5f);
        float dy = State.heroY - ((float)y + 0.5f);
        float dist = (float)Math.sqrt((dx * dx) + (dy * dy));

        return (1.0f / Math.max(1.0f, dist * 0.5f));
    }

    public boolean open() {
        if (dir != 0) {
            return false;
        }

        lastTime = Game.elapsedTime;
        dir = 1;

        SoundManager.playSound(SoundManager.SOUND_DOOR_OPEN, getVolume());
        return true;
    }

    @SuppressWarnings("MagicNumber")
    public void tryClose() {
        if (sticked
                || (dir != 0)
                || (openPos < 0.9f)
                || ((Game.elapsedTime - lastTime) < (1000 * 5))
                || ((State.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0)) {

            return;
        }

        SoundManager.playSound(SoundManager.SOUND_DOOR_CLOSE, getVolume());
        lastTime = Game.elapsedTime;
        dir = -1;
    }

    public void update(long elapsedTime) {
        if (dir > 0) {
            State.wallsMap[y][x] = 0; // clear door mark for PortalTracer

            if (openPos >= OPEN_POS_PASSABLE) {
                State.passableMap[y][x] &= ~Level.PASSABLE_IS_DOOR;

                if (openPos >= OPEN_POS_MAX) {
                    openPos = OPEN_POS_MAX;
                    dir = 0;
                }
            }
        } else if (dir < 0) {
            if (openPos < OPEN_POS_PASSABLE) {
                if ((dir == -1) && ((State.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0)) {
                    dir = 1;
                    lastTime = elapsedTime;
                } else {
                    dir = -2;
                    State.passableMap[y][x] |= Level.PASSABLE_IS_DOOR;
                }

                if (openPos <= 0.0f) {
                    State.wallsMap[y][x] = -1; // mark door for PortalTracer
                    openPos = 0.0f;
                    dir = 0;
                }
            }
        }
    }
}
