package connection;

import java.net.InetAddress;

public abstract class Connection {

	protected int port = 4945;
	
	public abstract void startThread ();
	public abstract int getNextPort ();
	public abstract boolean isConnected ();
	public abstract InetAddress getIPAddress ();
	public abstract void setConnected (boolean connected);
	public abstract boolean isServerUp ();
	public abstract boolean isGraphicsReady ();
}
