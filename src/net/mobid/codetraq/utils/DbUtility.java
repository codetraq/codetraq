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
 * This class deals with CRUD operation with db4o. We chose db4o because it is
 * compact, easy to use, works with the Java objects directly and self-contained.
 * You are free to modify this class to work with other database providers.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class DbUtility {

	private ObjectContainer _db = null;
	private ObjectContainer _userRevDb = null;
	private ObjectContainer _serverRevDb = null;

	/**
	 * Creates a new instance of DbUtility. This method automatically opens all
	 * the "tables" (so to speak) so we can start work.
	 */
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

	/**
	 * Closes all the "tables" associated with this instance.
	 */
	public void closeDbs() {
		dbClose();
		userRevisionDbClose();
		serverRevisionDbClose();
	}

	/*
	 * Opens the "table" associated with storing message objects.
	 */
	private void openMessageDb() {
		// open the database
		_db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "messages.db");
	}

	/**
	 * Returns the number of messages left in the database.
	 * @return number of messages still in the database
	 */
	public int getMessagePopulations() {
		ObjectSet result = _db.queryByExample(MessageDTO.class);
		return result.size();
	}

	/**
	 * Saves a message into the database for sending at a later time.
	 * @param value - a <code>MessageDTO</code> object
	 * @throws net.mobid.codetraq.utils.DbUtility.DbException - when a duplicate message is found
	 */
	public void saveMessage(MessageDTO value) throws DbException {
		if (getMessageByTime(value.getTimestamp()).hasNext()) {
			throw new DbException("Duplicate timestamp found");
		}
		_db.store(value);
	}

	/**
	 * Updates the number of attempts the server made to send this message.
	 * This could indicate remote notification service is down.
	 * @param value - a <code>MessageDTO</code> object, which should be updated
	 */
	public void updateMessageRetries(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		MessageDTO found = (MessageDTO)result.next();
		found.appendRetries();
		_db.store(found);
	}

	/**
	 * Updates the status of message to "SENT".
	 * @param value - a <code>MessageDTO</code> object, which should be updated
	 */
	public void updateMessageSent(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		MessageDTO found = (MessageDTO)result.next();
		found.setSent(true);
		_db.store(found);
	}

	/**
	 * Retrieves all messages that are still unsent.
	 * @return a <code>List</code> of unsent messages
	 */
	public List<MessageDTO> getAllUnsentMessages() {
		return _db.query(MessageDTO.class);
	}

	/**
	 * Deletes all messages that are marked "SENT".
	 */
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

	/**
	 * Deletes a single message.
	 * @param value - a <code>MessageDTO</code> that should be deleted
	 */
	public void deleteMessage(MessageDTO value) {
		ObjectSet result = _db.queryByExample(value);
		while(result.hasNext()) {
			MessageDTO m = (MessageDTO)result.next();
			_db.delete(m);
		}
	}

	/*
	 * Returns a message that has a specified timestamp.
	 * @param timeStamp - the required timestamp
	 * @return an <code>ObjectSet</code> containing the message object(s)
	 */
	private ObjectSet getMessageByTime(long timeStamp) {
		MessageDTO m = new MessageDTO();
		m.setTimestamp(timeStamp);
		ObjectSet result = _db.queryByExample(m);
		return result;
	}

	/**
	 * Queries the number of attemps to send a specific message has. The output
	 * would only be visible from the console.
	 * @param serverName - server name which the message refers to
	 * @param timestamp - the timestamp of the message
	 */
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

	/**
	 * Checks whether a message has been deleted from the database. The status would
	 * only be visible from the console.
	 * @param serverName - server name which the message refers to
	 * @param timestamp - the timetamp of the message
	 */
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

	/*
	 * Closes the message "table"
	 */
	private void dbClose() {
		if (_db != null) {
			_db.close();
		}
	}

	// USER REVISION database
	/*
	 * Opens the user revision "table"
	 */
	private void openUserRevisionDb() {
		_userRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "user.db");
	}

	/*
	 * Closes the user revision "table"
	 */
	private void userRevisionDbClose() {
		if (_userRevDb != null) {
			_userRevDb.close();
		}
	}

	/**
	 * Returns the number of user revision in the database.
	 * @return the number of user revisions
	 */
	public int getUserRecordPopulations() {
		ObjectSet result = _userRevDb.queryByExample(UserRevision.class);
		return result.size();
	}

	/**
	 * Queries whether a specified server (URL) has been saved into the database.
	 * @param address - server URL
	 * @param owner - user ID who "owns" this server
	 * @return
	 */
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

	/**
	 * Add a new user revision object into the database.
	 * @param address - server URL
	 * @param owner - user ID who "owns" this server
	 */
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

	/**
	 * Returns the user revision object for the specified URL and user ID.
	 * @param address - server URL
	 * @param owner - user ID who "owns" this server
	 * @return a <code>UserRevision</code> object
	 */
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

	/**
	 * Updates a user revision object with new information.
	 * @param ur - <code>UserRevision</code> object to update
	 */
	public void updateUserLatestRevision(UserRevision ur) {
		UserRevision r = new UserRevision();
		r.setServerAddress(ur.getServerAddress());
		r.setOwner(ur.getOwner());
		ObjectSet result = _userRevDb.queryByExample(r);
		if (result.hasNext()) {
			UserRevision found = (UserRevision)result.next();
			found.setLastRevisionId(ur.getLastRevisionId());
			_userRevDb.store(found);
		}
	}

	// SERVER REVISION
	/*
	 * Opens the Server Revision "table".
	 */
	private void openServerRevisionDb() {
		_serverRevDb = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "revision.db");
	}

	/*
	 * Closes the ServerRevision "table".
	 */
	private void serverRevisionDbClose() {
		if (_serverRevDb != null) {
			_serverRevDb.close();
		}
	}

	/**
	 * Returns the number of ServerRevision objects in the database.
	 * @return number of <code>ServerRevision</code> objects
	 */
	public int getServerRevisionPopulations() {
		ObjectSet result = _serverRevDb.queryByExample(ServerRevision.class);
		return result.size();
	}

	/**
	 * Returns a list of all ServerRevision objects in the database.
	 * @return a <code>List</code> of all <code>ServerRevision</code> objects
	 */
	public List<ServerRevision> getAllServerRevisions() {
		List<ServerRevision> list = new ArrayList<ServerRevision>();
		ServerRevision sr = new ServerRevision();
		ObjectSet result = _serverRevDb.queryByExample(sr);
		while(result.hasNext()) {
			list.add((ServerRevision)result.next());
		}
		return list;
	}

	/**
	 * Returns a ServerRevision objects specified by a server URL.
	 * @param address - server URL
	 * @return a <code>ServerRevision</code> object
	 */
	public ServerRevision getServerRevisionByAddress(String address) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(address);
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			return (ServerRevision)result.next();
		}
		return null;
	}

	/**
	 * Adds a ServerRevision object into the database.
	 * @param sr - <code>ServerRevision</code> object to be added
	 */
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

	/**
	 * Updates a ServerRevision object with new information.
	 * @param sr - <code>ServerRevision</code> object to be updated
	 */
	public void updateServerLatestRevision(ServerRevision sr) {
		ServerRevision template = new ServerRevision();
		template.setServerAddress(sr.getServerAddress());
		ObjectSet result = _serverRevDb.queryByExample(template);
		if (result.hasNext()) {
			ServerRevision found = (ServerRevision)result.next();
			LogService.writeMessage("CHECK_BEFORE_SR_UPDATE: rev " + 
				found.getLastRevisionId());
			found.setLastCheckedTimestamp(sr.getLastCheckedTimestamp());
			found.setLastMessage(sr.getLastMessage());
			found.setLastAuthor(sr.getLastAuthor());
			found.setLastCommitter(sr.getLastCommitter());
			found.setLastRevisionTimestamp(sr.getLastRevisionTimestamp());
			found.setLastRevisionId(sr.getLastRevisionId());
			_serverRevDb.store(found);
			LogService.writeMessage("CHECK_AFTER_SR_UPDATE: rev " + 
				found.getLastRevisionId());
		}
	}

	/**
	 * Toggles the "update" flag of a certain ServerRevision to "ON".
	 * @param sr - <code>ServerRevision</code> to be flagged
	 */
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

	/**
	 * Toggles the "update" flag of a certain ServerRevision to "OFF".
	 * @param sr - <code>ServerRevision</code> to be flagged
	 */
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

	/**
	 * Toggles the "update" flag of all ServerRevision objects to "OFF".
	 */
	public void turnAllServerUpdateOff() {
		ServerRevision template = new ServerRevision();
		ObjectSet result = _serverRevDb.queryByExample(template);
		while(result.hasNext()) {
			ServerRevision item = (ServerRevision)result.next();
			item.setShouldUpdate(false);
			_serverRevDb.store(item);
		}
	}

	/**
	 * DbException
	 */
	public class DbException extends Exception {

		public DbException(String message) {
			super(message, null);
		}
	}

}
