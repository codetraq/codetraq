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
package net.mobid.codetraq.persistence;

import net.mobid.codetraq.ConnectionType;

/**
 * This is a template to persist user-related data into the memory.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class UserDTO implements Comparable<UserDTO> {

	private String _nickname = null;

	/**
	 * Returns user's nickname.
	 * @return user's nickname
	 */
	public String getNickname() {
		return _nickname;
	}

	/**
	 * Sets user's nickname.
	 * @param value - user's nickname
	 */
	public void setNickname(String value) {
		_nickname = value;
	}

	private String _uid = null;

	/**
	 * Returns user's ID.
	 * @return user's ID
	 */
	public String getId() {
		return _uid;
	}

	/**
	 * Sets user's ID. For convenience, we are using 4-digit integer value in the
	 * configuration file. User IDs must be unique throughout the configuration
	 * file.
	 * @param value - user's ID
	 */
	public void setId(String value) {
		_uid = value;
	}

	private ConnectionType _type = null;

	/**
	 * Returns the <code>ConnectionType</code> for this user.
	 * @return a <code>ConnectionType</code> object
	 */
	public ConnectionType getNotificationType() {
		return _type;
	}

	/**
	 * Sets the <code>ConnectionType</code> for this user.
	 * @param value - <code>ConnectionType</code>
	 */
	public void setNotificationType(ConnectionType value) {
		_type = value;
	}

	private String _notificationId = null;

	/**
	 * Returns the notification ID (e.g Google Talk user id or MSN Messenger username)
	 * for this user.
	 * @return user's notification ID
	 */
	public String getNotificationId() {
		return _notificationId;
	}

	/**
	 * Sets the notification ID for this user.
	 * @param value - user's notification ID
	 */
	public void setNotificationId(String value) {
		_notificationId = value;
	}

	/**
	 * Returns the comparison value of this object against another object.
	 * This is an overridable method from Comparable<T>.
	 * @param o - another <code>UserDTO</code> object
	 * @return a positive integer if user ID is "greater" than other object's user ID,
	 * 0 if equal, negative integer if "less"
	 */
	public int compareTo(UserDTO o) {
		return getId().compareTo(o.getId());
	}
}
