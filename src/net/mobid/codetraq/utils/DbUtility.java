/*
 * DbUtility.java
 *
 * This class deals with CRUD operation with db4o.
 */

package net.mobid.codetraq.utils;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import java.util.ArrayList;
import java.util.List;
import net.mobid.codetraq.VersionControlType;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserRevision;

/**
 *
 * @author viper
 */
public class DbUtility {

	private ObjectContainer _db = null;
	private ObjectContainer _userRevDb = null;
	private ObjectContainer _serverRevDb = null;

	public DbUtility() {
		if (_db == null) {
			openMessageDb();
		}
		if (_userRevDb == null) {
			openUserRevisionDb();
		}
		if (_serverRevDb == null) {
			openServerRevisionDb();
		}
	}

	public void closeDbs() {
		dbClose();
		userRevisionDbClose();
		serverRevisionDbClose();
	}

	private void openMessageDb() {
		// open the database
		_db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "messages.db");
	}

	public int getMessagePopulations() {
		ObjectSet result = _db.queryByExample(MessageDTO.class);
		return result.size();
	}

	public void saveMessage(MessageDTO value) throws DbException {
		if (getMessageByTime(value.getTimestamp()).hasNext()) {
			throw new DbException("Duplicate timestamp found");
		}
		_db.store(value);
	}

	public void updateMessageRetries(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		MessageDTO found = (MessageDTO)result.next();
		found.appendRetries();
		_db.store(found);
	}

	public void updateMessageSent(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		MessageDTO found = (MessageDTO)result.next();
		found.setSent(true);
		_db.store(found);
	}

	public List<MessageDTO> getAllUnsentMessages() {
		return _db.query(MessageDTO.class);
	}

	public void deleteAllSentMessages() {
		MessageDTO template = new MessageDTO();
		template.setSent(true);
		ObjectSet result = _db.queryByExample(template);
		while(result.hasNext()) {
			MessageDTO found = (MessageDTO)result.next();
			deleteMessage(found);
			logCheckDelete(found.getServerName(), found.getTimestamp());
		}
	}

	public void deleteMessage(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		while(result.hasNext()) {
			MessageDTO m = (MessageDTO)result.next();
			_db.delete(m);
		}
	}

	private ObjectSet getMessageByTime(long timeStamp) {
		MessageDTO m = new MessageDTO();
		m.setTimestamp(timeStamp);
		ObjectSet result = _db.queryByExample(m);
		return result;
	}

	public void logCheckRetries(String serverName, long timestamp) {
		MessageDTO m = new MessageDTO();
		m.setServerName(serverName);
		m.setTimestamp(timestamp);
		ObjectSet os = _db.queryByExample(m);
		if (os.hasNext()) {
			MessageDTO found = (MessageDTO)os.next();
			LogService.writeMessage("CHECK retries -> Message for server " +
				found.getServerName() + " (rev#" + found.getRevisionId() + ") has " +
				found.getRetries() + " retries.");
		}
	}

	public void logCheckDelete(String serverName, long timestamp) {
		MessageDTO m = new MessageDTO();
		m.setServerName(serverName);
		m.setTimestamp(timestamp);
		ObjectSet os = _db.queryByExample(m);
		if (os.isEmpty()) {
			LogService.writeMessage("CHECK deleted -> Message for server " +
				serverName + " with timestamp " + timestamp + " has been deleted.");
		} else {
			MessageDTO found = (MessageDTO)os.next();
			LogService.writeMessage("CHECK deleted -> Message for server " +
				found.getServerName() + " (rev#" + found.getRevisionId() + ")" +
				" still in database.");
		}
	}
	
	private void dbClose() {
		if (_db != null) {
			_db.close();
		}
	}

	// USER REVISION database
	private void openUserRevisionDb() {
		_userRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "user.db");
	}

	private void userRevisionDbClose() {
		if (_userRevDb != null) {
			_userRevDb.close();
		}
	}

	public int getUserRecordPopulations() {
		ObjectSet result = _userRevDb.queryByExample(UserRevision.class);
		return result.size();
	}

	public boolean isServerInUserRecord(String address, String owner) {
		UserRevision r = new UserRevision();
		r.setOwner(owner);
		r.setServerAddress(address);
		ObjectSet result = _userRevDb.queryByExample(r);
		if (result.hasNext()) {
			return true;
		}
		return false;
	}

	public void addServerToUserRecord(String address, String owner) {
		UserRevision r = new UserRevision();
		r.setServerAddress(address);
		r.setOwner(owner);
		int records = getUserRecordPopulations();
		_userRevDb.store(r);
		int after = getUserRecordPopulations();
		LogService.writeMessage("CHECK_ADD_UR: before " + records + "/after " + after);
		if (records == after) {
			LogService.writeMessage("WARNING: Adding new server to user database seems to have failed. (address: " +
				address + "; owner: " + owner + ")");
		}
	}

	public UserRevision getUserLatestRevision(String address, String owner) {
		UserRevision r = new UserRevision();
		r.setServerAddress(address);
		r.setOwner(owner);
		ObjectSet result = _userRevDb.queryByExample(r);
		// we only suppose to have one Revision per Server, so grab the first one found...
		if (result.hasNext()) {
			return (UserRevision)result.next();
		}
		return null;
	}

	public void updateUserLatestRevision(UserRevision ur) {
		UserRevision r = new UserRevision();
		r.setServerAddress(ur.getServerAddress());
		r.setOwner(ur.getOwner());
		ObjectSet result = _userRevDb.queryByExample(r);
		if (result.hasNext()) {
			UserRevision found = (UserRevision)result.next();
			found.setLastRevision(ur.getLastRevision());
			_userRevDb.store(found);
		}
	}

	// SERVER REVISION
	private void openServerRevisionDb() {
		_serverRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "revision.db");
	}

	private void serverRevisionDbClose() {
		if (_serverRevDb != null) {
			_serverRevDb.close();
		}
	}

	public int getServerRevisionPopulations() {
		ObjectSet result = _serverRevDb.queryByExample(ServerRevision.class);
		return result.size();
	}

	public List<ServerRevision> getAllServerRevisions() {
		List<ServerRevision> list = new ArrayList<ServerRevision>();
		ServerRevision sr = new ServerRevision();
		ObjectSet result = _serverRevDb.queryByExample(sr);
		while(result.hasNext()) {
			list.add((ServerRevision)result.next());
		}
		return list;
	}

	public ServerRevision getServerRevisionByAddress(String address) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(address);
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			return (ServerRevision)result.next();
		}
		return null;
	}

	public void addServerRevision(ServerRevision sr) {
		int srs = getServerRevisionPopulations();
		_serverRevDb.store(sr);
		int after = getServerRevisionPopulations();
		LogService.writeMessage("CHECK_ADD_SR: before " + srs + "/after " + after);
		if (srs == after) {
			LogService.writeMessage("WARNING: Adding new server revision seems to have failed. (server: " +
				sr.getServerAddress() + ")");
		}
	}

	public void updateServerLatestRevision(ServerRevision sr) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(sr.getServerAddress());
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			ServerRevision found = (ServerRevision)result.next();
			LogService.writeMessage("CHECK_BEFORE_SR_UPDATE: rev " + 
				(found.getVersionControlType() == VersionControlType.GIT ? found.getLastRevisionId() : found.getLastRevision()));
			found.setLastCheckedTimestamp(sr.getLastCheckedTimestamp());
			found.setLastMessage(sr.getLastMessage());
			found.setLastAuthor(sr.getLastAuthor());
			found.setLastCommitter(sr.getLastCommitter());
			found.setLastRevisionTimestamp(sr.getLastRevisionTimestamp());
			if (found.getVersionControlType() == VersionControlType.SVN) {
				found.setLastRevision(sr.getLastRevision());
			} else if (found.getVersionControlType() == VersionControlType.GIT) {
				found.setLastRevisionId(sr.getLastRevisionId());
			}
			_serverRevDb.store(found);
			LogService.writeMessage("CHECK_AFTER_SR_UPDATE: rev " + 
				(found.getVersionControlType() == VersionControlType.GIT ? found.getLastRevisionId() : found.getLastRevision()));
		}
	}

	public void turnServerUpdateOn(ServerRevision sr) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(sr.getServerAddress());
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			ServerRevision found = (ServerRevision)result.next();
			found.setShouldUpdate(true);
			_serverRevDb.store(found);
		}
	}

	public void turnServerUpdateOff(ServerRevision sr) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(sr.getServerAddress());
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			ServerRevision found = (ServerRevision)result.next();
			found.setShouldUpdate(false);
			_serverRevDb.store(found);
		}
	}

	public void turnAllServerUpdateOff() {
		ServerRevision template = new ServerRevision();
		ObjectSet result = _serverRevDb.queryByExample(template);
		while(result.hasNext()) {
			ServerRevision item = (ServerRevision)result.next();
			item.setShouldUpdate(false);
			_serverRevDb.store(item);
		}
	}

	public class DbException extends Exception {

		public DbException(String message) {
			super(message, null);
		}
	}

}
