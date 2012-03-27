package {$PKG_CURR};

import java.io.*;

public class AutoWall implements Externalizable
{
	public float fromX;
	public float fromY;
	public float toX;
	public float toY;
	public boolean vert;
	public int type;
	public int doorIndex;	// required for save/load
	public Door door;

	public void copyFrom(AutoWall aw)
	{
		fromX = aw.fromX;
		fromY = aw.fromY;
		toX = aw.toX;
		toY = aw.toY;
		vert = aw.vert;
		type = aw.type;
		doorIndex = aw.doorIndex;
		door = aw.door;
	}

	public void writeExternal(ObjectOutput os) throws IOException
	{
		os.writeFloat(fromX);
		os.writeFloat(fromY);
		os.writeFloat(toX);
		os.writeFloat(toY);
		os.writeBoolean(vert);
		os.writeInt(type);
		os.writeInt(doorIndex);
	}

	public void readExternal(ObjectInput is) throws IOException
	{
		fromX = is.readFloat();
		fromY = is.readFloat();
		toX = is.readFloat();
		toY = is.readFloat();
		vert = is.readBoolean();
		type = is.readInt();
		doorIndex = is.readInt();
	}
}
