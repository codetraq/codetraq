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
package net.mobid.codetraq.persistence;

import java.util.ArrayList;
import java.util.List;
import net.mobid.codetraq.utils.Utilities;

/**
 * This is the Message Object that can be saved into db4o. Unsent Message(s) go
 * into the database and the main process will query into the database every so
 * often to try to send them.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class MessageDTO {

	// Timestamp of the change
	private long _timestamp = -1;

	/**
	 * Gets the timestamp of the latest revision (miliseconds from epoch).
	 * @return timestamp of the latest revision
	 */
	public long getTimestamp() {
		return _timestamp;
	}

	/**
	 * Sets the timestamp of the latest revision.
	 * @param value - timestamp in the form of miliseconds from epoch
	 */
	public void setTimestamp(long value) {
		_timestamp = value;
	}

	// author of change
	private String _author = null;

	/**
	 * Returns the name of the author of the latest revision.
	 * @return author name
	 */
	public String getAuthor() {
		return _author;
	}

	/**
	 * Sets the name of the author of the latest revision.
	 * @param value - author name
	 */
	public void setAuthor(String value) {
		_author = value;
	}

	// Subject of the message
	private String _subject = null;

	/**
	 * Returns the subject of the message.
	 * @return message subject
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * Sets the subject of the message.
	 * @param value - message subject
	 */
	public void setSubject(String value) {
		_subject = value;
	}

	// short server name
	private String _serverName = null;

	/**
	 * Returns the short name (nickname) of the server.
	 * @return server short name
	 */
	public String getServerName() {
		return _serverName;
	}

	/**
	 * Sets the short name (nickname) of the server. This nickname must be unique
	 * throughout the configuration file.
	 * @param value - server short name
	 */
	public void setServerName(String value) {
		_serverName = value;
	}

	// revision id for git/svn
	private String _strRev = null;

	/**
	 * Returns the revision id (git) or revision number (svn) of the latest revision.
	 * @return latest revision id / number
	 */
	public String getRevisionId() {
		return _strRev;
	}

	/**
	 * Sets the revision id (git) or revision number (svn) of the latest revision.
	 * @param value - latest revision id / number
	 */
	public void setRevisionId(String value) {
		_strRev = value;
	}

	// message sent by the submitter
	private String _message = null;

	/**
	 * Returns the message sent by revision submitter.
	 * @return submission message
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * Sets the message sent by revision submitter.
	 * @param value - submission message
	 */
	public void setMessage(String value) {
		_message = value;
	}

	// list of changed files; filenames separated by semicolon
	private List<String> _files = null;

	/**
	 * Returns the list of files modified (changed/deleted/added) on the latest revision.
	 * @return a <code>List</code> containing the modified files on the latest revision
	 */
	public List<String> getFiles() {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		return _files;
	}

	/**
	 * Sets the list of files modified (changed/deleted/added) on the latest revision.
	 * @param value - <code>List</code> containing the modified files on the latest revision.
	 */
	public void setFiles(List<String> value) {
		_files = value;
	}

	// status: sent? Y/N
	private boolean _isSent = false;

	/**
	 * Returns the sending status of the message.
	 * @return <code>true</code> if the message has been sent, <code>false</code> otherwise.
	 */
	public boolean isSent() {
		return _isSent;
	}

	/**
	 * Sets the sending status of the message.
	 * @param value - <code>true</code> if the message has been sent,
	 * <code>false</code> otherwise.
	 */
	public void setSent(boolean value) {
		_isSent = value;
	}

	// number of sending tries
	private int _retries = 0;

	/**
	 * Returns the number of times the server has tried to send this message.
	 * @return number of retries
	 */
	public int getRetries() {
		return _retries;
	}

	/**
	 * Appends the number of times the server has tried to send this message by 1.
	 */
	public void appendRetries() {
		_retries += 1;
	}

	// recipient
	private UserDTO _recipient = null;

	/**
	 * Gets the recipient data of this message.
	 * @return a <code>UserDTO</code> object.
	 */
	public UserDTO getRecipient() {
		return _recipient;
	}

	/**
	 * Sets the recipient data of this message.
	 * @param value - a <code>UserDTO</code> object.
	 */
	public void setRecipient(UserDTO value) {
		_recipient = value;
	}

	/**
	 * Generates a message to be sent to the user. The content is extracted from
	 * the properties of this class.
	 * @return The content of the message to be sent
	 */
	public String generateMessage() {
		String cr = System.getProperty("line.separator");
		String liner = "--------------------------------------------------------------";
		StringBuilder sb = new StringBuilder();
		sb.append(getSubject()).append(cr);
		sb.append(liner).append(cr);
		if (getRevisionId() != null) {
			sb.append("revision: ").append(getRevisionId()).append(cr);
		}
		sb.append("author: ").append(getAuthor()).append(cr);
		sb.append("date: ").append(Utilities.getFormattedTime(getTimestamp())).append(cr);
		sb.append("message: ").append(getMessage()).append(cr);
		sb.append(liner).append(cr);
		if (_files.size() > 0) {
			sb.append("modified files:").append(cr);
			for (String line : _files) {
				sb.append(line).append(cr);
			}
		}
		return sb.toString();
	}
}
