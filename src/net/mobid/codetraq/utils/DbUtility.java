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

	public void saveMessage(MessageDTO value) throws DbException {
		if (getMessageByTime(value.getTimestamp()).hasNext()) {
			throw new DbException("Duplicate timestamp found");
		}
		_db.store(value);
		_db.commit();
	}

	public void updateMessageRetries(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		MessageDTO found = (MessageDTO)result.next();
		found.appendRetries();
		_db.store(found);
		_db.commit();
	}

	public List<MessageDTO> getAllUnsentMessages() {
		return _db.query(MessageDTO.class);
	}

	public void deleteMessage(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		while(result.hasNext()) {
			MessageDTO m = (MessageDTO)result.next();
			_db.delete(m);
			_db.commit();
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
				found.getServerName() + " (rev#" + found.getRevisionNumber() + ") has " +
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
				found.getServerName() + " (rev#" + found.getRevisionNumber() + ")" +
				" still in database.");
		}
	}
	
	private void dbClose() {
		if (_db != null) {
			_db.close();
		}
	}

	// REVISION database
	private void openUserRevisionDb() {
		_userRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "user.db");
	}

	private void userRevisionDbClose() {
		if (_userRevDb != null) {
			_userRevDb.close();
		}
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
		_userRevDb.store(r);
		_userRevDb.commit();
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
			_userRevDb.commit();
		}
	}

	private void openServerRevisionDb() {
		_serverRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "revision.db");
	}

	private void serverRevisionDbClose() {
		if (_serverRevDb != null) {
			_serverRevDb.close();
		}
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
		_serverRevDb.store(sr);
		_serverRevDb.commit();
	}

	public void updateServerLatestRevision(ServerRevision sr) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(sr.getServerAddress());
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			ServerRevision found = (ServerRevision)result.next();
			found.setLastCheckedTimestamp(sr.getLastCheckedTimestamp());
			found.setLastMessage(sr.getLastMessage());
			found.setLastRevision(sr.getLastRevision());
			_serverRevDb.store(found);
			_serverRevDb.commit();
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
			_serverRevDb.commit();
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
			_serverRevDb.commit();
		}
	}

	public void turnAllServerUpdateOff() {
		ServerRevision template = new ServerRevision();
		ObjectSet result = _serverRevDb.queryByExample(template);
		while(result.hasNext()) {
			ServerRevision item = (ServerRevision)result.next();
			item.setShouldUpdate(false);
			_serverRevDb.store(item);
			_serverRevDb.commit();
		}
	}

	public class DbException extends Exception {

		public DbException(String message) {
			super(message, null);
		}
	}

}
