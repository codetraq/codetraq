package net.mobid.codetraq;

import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.utils.DbUtility;

/**
 * VersionControlChecker.java
 *
 * This is an abstract class that defines the behaviour of a Version Control
 * checker.
 * @author viper
 */
public abstract class VersionControlChecker {

	protected ServerDTO _server = null;
	protected UserDTO _user = null;
	protected DbUtility _db = null;

	public VersionControlChecker(ServerDTO server, UserDTO user, DbUtility db) {
		_server = server;
		_user = user;
		_db = db;
	}

	protected boolean checkServerInUserRecord() {
		if (_db.isServerInUserRecord(_server.getServerAddress(), _user.getId())) {
			return true;
		} else {
			_db.addServerToUserRecord(_server.getServerAddress(), _user.getId());
		}
		return false;
	}

	public abstract boolean compareLatestRevisionHistory() throws Exception;

	protected abstract void sendRevisionMessage(ServerRevision sr);
}
