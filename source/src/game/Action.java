package {$PKG_CURR};

import java.io.*;

public class Action implements Externalizable
{
	public int type;
	public int mark;
	public int param;

	public void writeExternal(ObjectOutput os) throws IOException
	{
		os.writeInt(type);
		os.writeInt(mark);
		os.writeInt(param);
	}

	public void readExternal(ObjectInput is) throws IOException
	{
		type = is.readInt();
		mark = is.readInt();
		param = is.readInt();
	}
}
