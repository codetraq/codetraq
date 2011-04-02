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
package net.mobid.codetraq.runnables;

import java.util.logging.Level;
import net.mobid.codetraq.VersionControlChecker;
import net.mobid.codetraq.VersionControlType;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.persistence.UserRevision;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;

/**
 * SvnChecker.java
 *
 * This class serves as a worker class that monitor for changes on a given
 * subversion server.
 * @author Ronald Kurniawan
 */
public class SvnChecker extends VersionControlChecker implements Runnable {

	public SvnChecker(ServerDTO server, UserDTO user, DbUtility db) {
		super(server, user, db);
	}

	/**
	 * compareLatestRevisionHistory
	 *
	 * Compares the latest revision from ServerRevision against the latest revision
	 * recorded for a UserRevision.
	 * @return true if there is a newer revision and UserRevision needs to be updated
	 */
	@Override
	public boolean compareLatestRevisionHistory() throws Exception {
		checkServerInUserRecord();
		UserRevision ur = _db.getUserLatestRevision(_server.getServerAddress(), _user.getId());
		ServerRevision sr = _db.getServerRevisionByAddress(_server.getServerAddress());
		if (sr == null) {
			throw new Exception("Cannot find server " + _server.getServerAddress() +
				" in the server revision list.");
		}
		if (sr.getVersionControlType() == VersionControlType.SVN) {
			// Subversion has a number (integer/long) as a revision id, so we
			// should just compare those to determine the latest revision...
			int urRevision = Integer.parseInt(ur.getLastRevisionId());
			int srRevision = Integer.parseInt(sr.getLastRevisionId());
			if (urRevision < srRevision) {
				// we update the UserRevision object...
				ur.setLastRevisionId(sr.getLastRevisionId());
				_db.updateUserLatestRevision(ur);
				sendRevisionMessage(sr);
				return true;
			}
		}
		return false;
	}

	public void run() {
		try {
			compareLatestRevisionHistory();
		} catch(Exception ex) {
			LogService.getLogger(SvnChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}
}
