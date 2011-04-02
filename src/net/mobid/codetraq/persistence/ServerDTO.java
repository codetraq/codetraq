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

import net.mobid.codetraq.VersionControlType;

/**
 * ServerDTO.java
 *
 * This is a template for storing Server objects in memory
 * @author Ronald Kurniawan
 */
public class ServerDTO {

	private String _owner = null;

	/**
	 * Returns this server's owner's user ID.
	 * @return user ID for this server's owner
	 */
	public String getOwnerId() {
		return _owner;
	}

	/**
	 * Sets this server's owner's user ID.
	 * @param value - user ID for this server's owner
	 */
	public void setOwnerId(String value) {
		_owner = value;
	}

	private String _shortName = null;

	/**
	 * Returns this server's short name (nickname).
	 * @return server's short name
	 */
	public String getShortName() {
		return _shortName;
	}

	/**
	 * Sets this server's short name (nickname). This nickname must be unique
	 * across the configuration file.
	 * @param value - server's short name
	 */
	public void setShortName(String value) {
		_shortName = value;
	}

	private VersionControlType _serverType = null;

	/**
	 * Returns the type of version control this server supports.
	 * @return a value of the type <code>VersionControlType</code>
	 */
	public VersionControlType getServerType() {
		return _serverType;
	}

	/**
	 * Sets the type of version control this server supports.
	 * @param value - <code>VersionControlType</code>
	 */
	public void setServerType(VersionControlType value) {
		_serverType = value;
	}

	private String _serverAddress = null;

	/**
	 * Returns the URL of this server.
	 * @return server's URL
	 */
	public String getServerAddress() {
		return _serverAddress;
	}

	/**
	 * Sets the URL of this server.
	 * @param value - server's URL
	 */
	public void setServerAddress(String value) {
		_serverAddress = value;
	}

	private String _serverUsername = null;

	/**
	 * Returns the username to be used to log into the version control.
	 * @return username for login
	 */
	public String getServerUsername() {
		return _serverUsername;
	}

	/**
	 * Sets the username to be used to log into the version control.
	 * @param value - username for login
	 */
	public void setServerUsername(String value) {
		_serverUsername = value;
	}

	private String _serverPassword = null;

	/**
	 * Returns the password to be used to log into the version control.
	 * @return password for login
	 */
	public String getServerPassword() {
		return _serverPassword;
	}

	/**
	 * Sets the password to be used to log into the version control.
	 * @param value - password for login
	 */
	public void setServerPassword(String value) {
		_serverPassword = value;
	}

	private String _serverBranch = "HEAD";

	/**
	 * Returns the branch of code that should be monitored. Currently, this is
	 * a GIT only property.
	 * @return branch name to be monitored
	 */
	public String getServerBranch() {
		return _serverBranch;
	}

	/**
	 * Sets the branch of code that should be monitored. Currently, this is a
	 * GIT only property.
	 * @param value - branch name to be monitored
	 */
	public void setServerBranch(String value) {
		_serverBranch = value;
	}
}
