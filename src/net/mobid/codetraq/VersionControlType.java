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
 * VersionControlType.java
 *
 * Enumeration containing the types of Version Control System we support.<br/>
 * Currently we support Subversion and Git.
 * @author Ronald Kurniawan
 */
public enum VersionControlType {

	/**
	 * Subversion type.
	 */
	SVN("subversion"),
	/**
	 * Git type.
	 */
	GIT("git");

	private final String _longName;

	VersionControlType(String longName) {
		_longName = longName;
	}

	/**
	 * This is a getter method to return the long name of a particular Version Control System.
	 * @return
	 */
	public String getLongName() {
		return _longName;
	}
}
