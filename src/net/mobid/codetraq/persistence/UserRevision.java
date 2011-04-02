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

/**
 * RevisionDTO.java
 *
 * This class holds the record for revision data for a certain pairing of server
 * and user.
 * @author Ronald Kurniawan
 */
public class UserRevision {

	private String _serverAddress = null;

	public String getServerAddress() {
		return _serverAddress;
	}

	public void setServerAddress(String value) {
		if (_serverAddress != null) {
			System.out.printf("Server Address cannot be changed.%n");
			return;
		}
		_serverAddress = value;
	}

	private String _owner = null;

	public String getOwner() {
		return _owner;
	}

	public void setOwner(String value) {
		_owner = value;
	}

	private String _lastRevisionId = null;

	public String getLastRevisionId() {
		return _lastRevisionId;
	}

	public void setLastRevisionId(String value) {
		_lastRevisionId = value;
	}

}
