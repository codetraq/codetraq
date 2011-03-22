/*
 * UserDTO.java
 *
 * This is a template to persist user-related data into the memory.
 */

package net.mobid.codetraq.persistence;

import net.mobid.codetraq.ConnectionType;

/**
 *
 * @author viper
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
