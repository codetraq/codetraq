/*
 * ConnectionType.java
 *
 * Enumeration containing the XMPP servers available for communication.
 */

package net.mobid.codetraq;

/**
 *
 * @author viper
 */
public enum ConnectionType {

	GOOGLE_TALK("talk.google.com", 5222, "gmail.com"),
	JABBER("jabber.org", 5222, "jabber.org"),
	MSN("", 0, ""),
	EMAIL("", 0, "");


	private final String _server;

	private final int _port;

	private final String _resource;

	ConnectionType(String server, int port, String resource) {
		_server = server;
		_port = port;
		_resource = resource;
	}

	public String getServer() {
		return _server;
	}

	public int getPort() {
		return _port;
	}

	public String getResource() {
		return _resource;
	}
}
