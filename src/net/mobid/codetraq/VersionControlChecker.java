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

import java.util.logging.Level;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.DbUtility.DbException;
import net.mobid.codetraq.utils.LogService;

/**
 * This is an abstract class that defines the behaviour of a Version Control
 * checker.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public abstract class VersionControlChecker {

	protected ServerDTO _server = null;
	protected UserDTO _user = null;
	protected DbUtility _db = null;

	/**
	 * Creates a new VersionControlChecker. This constructor requires a Server, a
	 * User and an instance of DbUtility
	 * @param server - A <code>ServerDTO</code> instance
	 * @param user - A <code>UserDTO</code> instance
	 * @param db - An instance of <code>DbUtility</code>
	 */
	public VersionControlChecker(ServerDTO server, UserDTO user, DbUtility db) {
		_server = server;
		_user = user;
		_db = db;
	}

	/**
	 * Checks whether this pair of user and server is already saved in internal
	 * database.
	 * @return <code>true</code> if user and server pair is saved in database,
	 * <code>false</code> otherwise.
	 */
	protected boolean checkServerInUserRecord() {
		if (_db.isServerInUserRecord(_server.getServerAddress(), _user.getId())) {
			return true;
		} else {
			LogService.writeMessage("Adding server " + _server.getServerAddress() +
				" with owner " + _server.getOwnerId() + " to user database");
			_db.addServerToUserRecord(_server.getServerAddress(), _user.getId());
		}
		return false;
	}

	/**
	 * Checks if user already has the latest revision on records. Each implementation
	 * must override this method.
	 * @return <code>true</code> if user already has latest revision, <code>false</code>
	 * otherwise
	 * @throws Exception - Depends on implementation
	 */
	public abstract boolean compareLatestRevisionHistory() throws Exception;

	/**
	 * Adds a new revision message into the database to be picked up and sent later
	 * by the <code>MessageTracker</code>.
	 * @param sr - a <code>ServerRevision</code> object
	 */
	protected void sendRevisionMessage(ServerRevision sr) {
		MessageDTO message = new MessageDTO();
		message.setAuthor(sr.getLastAuthor());
		message.setMessage(sr.getLastMessage());
		message.setRecipient(_user);
		message.setServerName(_server.getShortName());
		message.setTimestamp(sr.getLastRevisionTimestamp());
		message.setFiles(sr.getFiles());
		if (sr.getVersionControlType() == VersionControlType.SVN) {
			message.setRevisionId(sr.getLastRevisionId());
			message.setSubject("New revision detected for " + _server.getShortName() +
				" (" + sr.getLastRevisionId() + ")");
		} else if (sr.getVersionControlType() == VersionControlType.GIT) {
			message.setRevisionId(sr.getLastRevisionId());
			message.setSubject("New revision detected for " + _server.getShortName() +
				" (" + sr.getLastRevisionId() + ")");
		}

		try {
			// add into the database
			_db.saveMessage(message);
			LogService.writeMessage("Adding Message object for " + _user.getNickname() + " to database.");
		} catch (DbException ex) {
			LogService.getLogger(VersionControlChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}
}
