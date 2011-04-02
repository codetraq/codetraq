/*
 * SvnChecker.java
 *
 * This class serves as a worker class that checks for changes on a given
 * subversion server.
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
 *
 * @author viper
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
