/*
 * GitChecker.java
 *
 * This is a worker class whose purpose is to check changes on a git server.
 */

package net.mobid.codetraq.runnables;

import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.utils.DbUtility;

/**
 *
 * @author viper
 */
public class GitChecker implements Runnable {

	private ServerDTO _server = null;
	private UserDTO _user = null;
	private DbUtility _db = null;

	public GitChecker(ServerDTO server, UserDTO user, DbUtility db) {
		_server = server;
		_user = user;
		_db = db;
	}

	public void run() {
		if (getLatestRevisionHistory()) {
			sendRevisionMessage();
		}
	}

	private boolean getLatestRevisionHistory() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void sendRevisionMessage() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
