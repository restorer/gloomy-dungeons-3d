package zame.game.engine;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@SuppressWarnings("WeakerAccess")
public class Action implements Externalizable {
    private static final long serialVersionUID = 0L;

    public int type;
    public int mark;
    public int param;

    @Override
    public void writeExternal(ObjectOutput os) throws IOException {
        os.writeInt(type);
        os.writeInt(mark);
        os.writeInt(param);
    }

    @Override
    public void readExternal(ObjectInput is) throws IOException {
        type = is.readInt();
        mark = is.readInt();
        param = is.readInt();
    }
}
