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

import java.util.ArrayList;
import java.util.List;
import net.mobid.codetraq.VersionControlType;
import org.joda.time.Period;

/**
 * ServerRevision.java
 *
 * Records the latest revision number for a server.
 * @author Ronald Kurniawan
 */
public class ServerRevision {

	private boolean _shouldUpdate = false;

	/**
	 * Returns the status of whether daemon should update this revision on its next
	 * run.
	 * @return <code>true</code> if daemon should update this revision, <code>false</code>
	 * otherwise
	 */
	public boolean shoudlUpdate() {
		return _shouldUpdate;
	}

	/**
	 * Sets the status of whether daemon should update this revision on its next run.
	 * @param value - <code>true</code> if daemon should update, <code>false</code> otherwise
	 */
	public void setShouldUpdate(boolean value) {
		_shouldUpdate = value;
	}

	private VersionControlType _versionControl = null;

	/**
	 * Returns the type of Version Control this revision uses.
	 * @return a value of the type <code>VersionControlType</code>
	 */
	public VersionControlType getVersionControlType() {
		return _versionControl;
	}

	/**
	 * Sets the type of Version Control this revision uses.
	 * @param value - type <code>VersionControlType</code>
	 */
	public void setVersionControlType(VersionControlType value) {
		_versionControl = value;
	}

	private String _serverShortName = null;

	/**
	 * Returns the server's short name (nickname).
	 * @return server's short name
	 */
	public String getServerShortName() {
		return _serverShortName;
	}

	/**
	 * Sets the server's short name (nickname). This nickname must be unique
	 * throughout the configuration file.
	 * @param value - server's short name
	 */
	public void setServerShortName(String value) {
		_serverShortName = value;
	}

	private String _serverAddress = null;

	/**
	 * Returns the URL for the server to be monitored.
	 * @return server URL
	 */
	public String getServerAddress() {
		return _serverAddress;
	}

	/**
	 * Sets the URL for the server to be monitored.
	 * @param value - server URL
	 */
	public void setServerAddress(String value) {
		if (_serverAddress != null) {
			System.out.printf("Server addres cannot be changed.%n");
			return;
		}
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

	public long _lastCheckedTimestamp = 0;

	/**
	 * Returns the timestamp the server was last monitored.
	 * @return timestamp in miliseconds since epoch
	 */
	public long getLastCheckedTimestamp() {
		return _lastCheckedTimestamp;
	}

	/**
	 * Sets the timestamp the server was last monitored.
	 * @param value - timestamp in miliseconds since epoch
	 */
	public void setLastCheckedTimestamp(long value) {
		_lastCheckedTimestamp = value;
	}

	/**
	 * Calculates and returns the number of minutes since last monitor.
	 * @return number of minutes elapsed since last monitor
	 */
	public int getMinutesSinceLastCheck() {
		if (getLastCheckedTimestamp() > 0)  {
			Period p = new Period(getLastCheckedTimestamp(), System.currentTimeMillis());
			return p.getMinutes();
		}
		return Integer.MAX_VALUE;
	}
	
	// These properties are linked to a particular Revision --------------------------------------
	// Needed to construct a Message
	private String _rLastMessage = null;

	/**
	 * Returns message for last revision.
	 * @return last revision's message
	 */
	public String getLastMessage() {
		return _rLastMessage;
	}

	/**
	 * Sets message for last revision.
	 * @param value - last revision's message
	 */
	public void setLastMessage(String value) {
		_rLastMessage = value;
	}

	private String _rLastRevisionId = null;

	/**
	 * Returns ID for last revision.
	 * @return last revision's ID
	 */
	public String getLastRevisionId() {
		return _rLastRevisionId;
	}

	/**
	 * Sets the ID for last revision.
	 * @param value - last revision's ID
	 */
	public void setLastRevisionId(String value) {
		_rLastRevisionId = value;
	}

	private long _rLastTimestamp = 0;

	/**
	 * Returns the timestamp for last revision.
	 * @return last revision's timestamp
	 */
	public long getLastRevisionTimestamp() {
		return _rLastTimestamp;
	}

	/**
	 * Sets the timestamp for last revision.
	 * @param value - last revision's timestamp
	 */
	public void setLastRevisionTimestamp(long value) {
		_rLastTimestamp = value;
	}

	private String _rLastAuthor = null;

	/**
	 * Returns the author for last revision.
	 * @return last revision's author
	 */
	public String getLastAuthor() {
		return _rLastAuthor;
	}

	/**
	 * Sets the author for last revision.
	 * @param value - last revision's author
	 */
	public void setLastAuthor(String value) {
		_rLastAuthor = value;
	}

	private String _rLastCommitter = null;

	/**
	 * Returns the committer for last revision.
	 * @return last revision's committer
	 */
	public String getLastCommitter() {
		return _rLastCommitter;
	}

	/**
	 * Sets the committer for last revision.
	 * @param value - last revision's committer
	 */
	public void setLastCommitter(String value) {
		_rLastCommitter = value;
	}

	private List<String> _files = null;

	/**
	 * Returns the list of modified files for last revision.
	 * @return a <code>List</code> containing modified files from last revision
	 */
	public List<String> getFiles() {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		return _files;
	}

	/**
	 * Adds a single line describing a modified file into the list of modified files.
	 * @param value - a <code>String</code> describiing a modified file (status and path)
	 */
	public void addModifiedFile(String value) {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		_files.add(value);
	}

	/**
	 * Clears the content of the list of modified files.
	 */
	public void clearFiles() {
		if (_files != null && !_files.isEmpty()) {
			_files.clear();
		}
	}
}
