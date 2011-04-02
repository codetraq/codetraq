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
 * ITalker.java
 *
 * Interface for notification mechanism.
 * @author Ronald Kurniawan
 */
public interface ITalker {

	/**
	 * Connects to notification server.
	 */
	public void connect();

	/**
	 * Sends the message to recipient via notification mechanism.
	 * @param recipientAddress - Notification recipient, the format depends on the implementation
	 * @param message - Message content
	 * @return <code>true</code> if message is sent successfully, <code>false</code> otherwise
	 */
	public boolean talk(String recipientAddress, String message);

	/**
	 * Disconnects the connection to notification server
	 */
	public void disconnect();

	/**
	 * Checks whether recipient is in the internal contact list.
	 * @param recipientAddress - Notification recipient, the format depends on the implementation
	 * @return <code>true</code> if recipient address is in internal contact list,
	 * <code>false</code> otherwise
	 */
	public boolean isInContactList(String recipientAddress);

	/**
	 * Adds the recipient address into internal contact list.
	 * @param recipientAddress - Notification recipient, the format depends on the implementation
	 */
	public void addToContactList(String recipientAddress);

	/**
	 * Checks if a recipient is online and contactable.
	 * @param recipientAddress - Notification recipient, the format depends on the implementation
	 * @return <code>true</code> if recipient is online and contactable,
	 * <code>false</code> otherwise.
	 */
	public boolean recipientOnline(String recipientAddress);

}
