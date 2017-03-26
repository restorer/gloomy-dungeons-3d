package zame.game.engine;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@SuppressWarnings("WeakerAccess")
public class AutoWall implements Externalizable {
    private static final long serialVersionUID = 0L;

    public float fromX;
    public float fromY;
    public float toX;
    public float toY;
    public boolean vert;
    public int type;
    public int doorIndex; // required for save/load
    public Door door;

    public void copyFrom(AutoWall aw) {
        fromX = aw.fromX;
        fromY = aw.fromY;
        toX = aw.toX;
        toY = aw.toY;
        vert = aw.vert;
        type = aw.type;
        doorIndex = aw.doorIndex;
        door = aw.door;
    }

    @Override
    public void writeExternal(ObjectOutput os) throws IOException {
        os.writeFloat(fromX);
        os.writeFloat(fromY);
        os.writeFloat(toX);
        os.writeFloat(toY);
        os.writeBoolean(vert);
        os.writeInt(type);
        os.writeInt(doorIndex);
    }

    @Override
    public void readExternal(ObjectInput is) throws IOException {
        fromX = is.readFloat();
        fromY = is.readFloat();
        toX = is.readFloat();
        toY = is.readFloat();
        vert = is.readBoolean();
        type = is.readInt();
        doorIndex = is.readInt();
    }
}
