/*
 * SvnChecker.java
 *
 * This class serves as a worker class that checks for changes on a given
 * subversion server.
 */
package net.mobid.codetraq.runnables;

import java.util.Iterator;
import java.util.logging.Level;
import net.mobid.codetraq.VersionControlChecker;
import net.mobid.codetraq.VersionControlType;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.persistence.UserRevision;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.DbUtility.DbException;
import net.mobid.codetraq.utils.LogService;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

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
	public boolean compareLatestRevisionHistory() throws Exception {
		checkServerInUserRecord();
		UserRevision ur = _db.getUserLatestRevision(_server.getServerAddress(), _user.getId());
		ServerRevision sr = _db.getServerRevisionByAddress(_server.getServerAddress());
		if (sr == null) {
			throw new Exception("Cannot find server " + _server.getServerAddress() +
				" in the server revision list.");
		}
		if (sr.getVersionControlType() == VersionControlType.SVN) {
			if (ur.getLastRevision() < sr.getLastRevision()) {
				// we update the UserRevision object...
				ur.setLastRevision(sr.getLastRevision());
				_db.updateUserLatestRevision(ur);
				sendRevisionMessage(sr);
				return true;
			}
		}
		return false;
	}

	protected void sendRevisionMessage(ServerRevision sr) {
		MessageDTO message = new MessageDTO();
		if (sr.getVersionControlType() == VersionControlType.SVN) {
			SVNLogEntry latestEntry = (SVNLogEntry)sr.getLastMessage();
			message.setAuthor(latestEntry.getAuthor());
			if (latestEntry.getChangedPaths().size() > 0) {
				Iterator i = latestEntry.getChangedPaths().keySet().iterator();
				while (i.hasNext()) {
					SVNLogEntryPath entry = (SVNLogEntryPath) latestEntry.getChangedPaths().get(i.next());
					StringBuilder sb = new StringBuilder();
					sb.append(entry.getType());
					sb.append(" ");
					sb.append(entry.getPath());
					message.addModifiedFile(sb.toString());
				}
			}
			message.setMessage(latestEntry.getMessage());
			message.setRecipient(_user);
			message.setRevisionNumber(latestEntry.getRevision());
			message.setServerName(_server.getShortName());
			message.setSubject("New revision detected for " + _server.getShortName() +
				" (" + latestEntry.getRevision() + ")");
			message.setTimestamp(latestEntry.getDate().getTime());
		}
		
		try {
			// add into the database
			_db.saveMessage(message);
			LogService.writeMessage("Adding Message object for " + _user.getNickname() + " to database.");
		} catch (DbException ex) {
			LogService.getLogger(SvnChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}

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
