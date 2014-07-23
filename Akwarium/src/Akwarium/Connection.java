package Akwarium;

import java.net.InetAddress;

public abstract class Connection {

	protected abstract void startThread ();
	protected abstract int getNextPort ();
	protected abstract boolean isConnected ();
	protected abstract InetAddress getIPAddress ();
	protected boolean isServerUp () {return false;}
	protected boolean getServerDown () {return false;}
	protected abstract boolean isDisconnected ();
	protected abstract boolean isGraphicsReady ();
}
