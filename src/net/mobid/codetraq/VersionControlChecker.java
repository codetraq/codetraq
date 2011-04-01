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
			LogService.writeMessage("Adding server " + _server.getServerAddress() +
				" with owner " + _server.getOwnerId() + " to user database");
			_db.addServerToUserRecord(_server.getServerAddress(), _user.getId());
		}
		return false;
	}

	public abstract boolean compareLatestRevisionHistory() throws Exception;

	protected void sendRevisionMessage(ServerRevision sr) {
		MessageDTO message = new MessageDTO();
		message.setAuthor(sr.getLastAuthor());
		message.setMessage(sr.getLastMessage());
		message.setRecipient(_user);
		message.setServerName(_server.getShortName());
		message.setTimestamp(sr.getLastRevisionTimestamp());
		message.setFiles(sr.getFiles());
		if (sr.getVersionControlType() == VersionControlType.SVN) {
			message.setRevisionNumber(sr.getLastRevision());
			message.setSubject("New revision detected for " + _server.getShortName() +
				" (" + sr.getLastRevision() + ")");
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
