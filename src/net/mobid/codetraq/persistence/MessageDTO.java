/*
 * MessageDTO.java
 *
 * This is the Message Object that can be saved into db4o. Unsent Message(s) go
 * into the database and the main process will query into the database every so
 * often to try to send them.
 */

package net.mobid.codetraq.persistence;

import java.util.ArrayList;
import java.util.List;
import net.mobid.codetraq.utils.Utilities;

/**
 *
 * @author viper
 */
public class MessageDTO {

	// Timestamp of the change
	private long _timestamp = -1;

	public long getTimestamp() {
		return _timestamp;
	}

	public void setTimestamp(long value) {
		_timestamp = value;
	}

	// author of change
	private String _author = null;

	public String getAuthor() {
		return _author;
	}

	public void setAuthor(String value) {
		_author = value;
	}

	// Subject of the message
	private String _subject = null;

	public String getSubject() {
		return _subject;
	}

	public void setSubject(String value) {
		_subject = value;
	}

	// short server name
	private String _serverName = null;

	public String getServerName() {
		return _serverName;
	}

	public void setServerName(String value) {
		_serverName = value;
	}

	// revision id for git/svn
	private String _strRev = null;

	public String getRevisionId() {
		return _strRev;
	}

	public void setRevisionId(String value) {
		_strRev = value;
	}

	// message sent by the submitter
	private String _message = null;

	public String getMessage() {
		return _message;
	}

	public void setMessage(String value) {
		_message = value;
	}

	// list of changed files; filenames separated by semicolon
	private List<String> _files = null;

	public List<String> getFiles() {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		return _files;
	}

	public void setFiles(List<String> value) {
		_files = value;
	}

	// status: sent? Y/N
	private boolean _isSent = false;

	public boolean isSent() {
		return _isSent;
	}

	public void setSent(boolean value) {
		_isSent = value;
	}

	// number of sending tries
	private int _retries = 0;

	public int getRetries() {
		return _retries;
	}

	public void appendRetries() {
		_retries += 1;
	}

	// recipient
	private UserDTO _recipient = null;

	public UserDTO getRecipient() {
		return _recipient;
	}

	public void setRecipient(UserDTO value) {
		_recipient = value;
	}

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
