package connection;

import java.net.InetAddress;

public abstract class Connection {

	protected int port = 4945;
	
	public abstract void startThread ();
	public abstract int getNextPort ();
	public abstract boolean isConnected ();
	public abstract InetAddress getIPAddress ();
	public boolean isServerUp () {return false;}
	public boolean getServerDown () {return false;}
	public abstract void setConnected (boolean connected);
	public boolean isGraphicsReady () {return false;}
}
