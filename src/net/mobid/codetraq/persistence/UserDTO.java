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
 * UserDTO.java
 *
 * This is a template to persist user-related data into the memory.
 * @author Ronald Kurniawan
 */
public class UserDTO implements Comparable<UserDTO> {

	private String _nickname = null;

	public String getNickname() {
		return _nickname;
	}

	public void setNickname(String value) {
		_nickname = value;
	}

	private String _uid = null;

	public String getId() {
		return _uid;
	}

	public void setId(String value) {
		_uid = value;
	}

	private ConnectionType _type = null;

	public ConnectionType getNotificationType() {
		return _type;
	}

	public void setNotificationType(ConnectionType value) {
		_type = value;
	}

	private String _notificationId = null;

	public String getNotificationId() {
		return _notificationId;
	}

	public void setNotificationId(String value) {
		_notificationId = value;
	}

	public int compareTo(UserDTO o) {
		return getId().compareTo(o.getId());
	}
}
