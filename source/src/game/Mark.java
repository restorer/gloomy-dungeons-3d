package {$PKG_CURR};

import java.io.*;

public class Mark implements Externalizable
{
	public int id;
	public int x;
	public int y;

	public void writeExternal(ObjectOutput os) throws IOException
	{
		os.writeInt(id);
		os.writeInt(x);
		os.writeInt(y);
	}

	public void readExternal(ObjectInput is) throws IOException
	{
		id = is.readInt();
		x = is.readInt();
		y = is.readInt();
	}
}
