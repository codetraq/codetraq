/*
 * Copyright 2011 Ronald Kurniawan.
 *
 * This file is part of CodeTraq.
 *
 * CodeTraq is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CodeTraq is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CodeTraq. If not, see <http://www.gnu.org/licenses/>.
 */
package net.mobid.codetraq;

/**
 * ConnectionType.java
 *
 * Enumeration containing the Notification types available for communication.
 * @author Ronald Kurniawan
 */
public enum ConnectionType {

	/**
	 * Google Talk - This is used for all Gmail address.
	 */
	GOOGLE_TALK("talk.google.com", 5222, "gmail.com"),
	/**
	 * Jabber - This is used for all jabber.org subscribers.
	 */
	JABBER("jabber.org", 5222, "jabber.org"),
	/**
	 * MSN - This is used for MSN-related contacts.
	 */
	MSN("", 0, ""),
	/**
	 * Email addresses
	 */
	EMAIL("", 0, "");


	private final String _server;

	private final int _port;

	private final String _resource;

	ConnectionType(String server, int port, String resource) {
		_server = server;
		_port = port;
		_resource = resource;
	}

	/**
	 * Getter method for server address for a particular Connection Type
	 * @return server address
	 */
	public String getServer() {
		return _server;
	}

	/**
	 * Getter method for port number for a particular Connection Type
	 * @return port number
	 */
	public int getPort() {
		return _port;
	}

	/**
	 * Getter method for additional information for a particular Connection Type
	 * @return additional information
	 */
	public String getResource() {
		return _resource;
	}
}
