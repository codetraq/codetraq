/*
 * ServerRevision.java
 *
 * Records the latest revision number for a server.
 */

package net.mobid.codetraq.persistence;

import java.util.ArrayList;
import java.util.List;
import net.mobid.codetraq.VersionControlType;
import org.joda.time.Period;

/**
 *
 * @author viper
 */
public class ServerRevision {

	private boolean _shouldUpdate = false;

	public boolean shoudlUpdate() {
		return _shouldUpdate;
	}

	private VersionControlType _versionControl = null;

	public VersionControlType getVersionControlType() {
		return _versionControl;
	}

	public void setVersionControlType(VersionControlType value) {
		_versionControl = value;
	}

	public void setShouldUpdate(boolean value) {
		_shouldUpdate = value;
	}

	private String _serverShortName = null;

	public String getServerShortName() {
		return _serverShortName;
	}

	public void setServerShortName(String value) {
		_serverShortName = value;
	}

	private String _serverAddress = null;

	public String getServerAddress() {
		return _serverAddress;
	}

	public void setServerAddress(String value) {
		if (_serverAddress != null) {
			System.out.printf("Server addres cannot be changed.%n");
			return;
		}
		_serverAddress = value;
	}

	private String _serverUsername = null;

	public String getServerUsername() {
		return _serverUsername;
	}

	public void setServerUsername(String value) {
		_serverUsername = value;
	}

	private String _serverPassword = null;

	public String getServerPassword() {
		return _serverPassword;
	}

	public void setServerPassword(String value) {
		_serverPassword = value;
	}

	public long _lastCheckedTimestamp = 0;

	public long getLastCheckedTimestamp() {
		return _lastCheckedTimestamp;
	}

	public void setLastCheckedTimestamp(long value) {
		_lastCheckedTimestamp = value;
	}

	public int getMinutesSinceLastCheck() {
		if (getLastCheckedTimestamp() > 0)  {
			Period p = new Period(getLastCheckedTimestamp(), System.currentTimeMillis());
			return p.getMinutes();
		}
		return Integer.MAX_VALUE;
	}
	
	// These properties are linked to a particular Revision --------------------------------------
	// Needed to construct a Message
	private String _rLastMessage = null;

	public String getLastMessage() {
		return _rLastMessage;
	}

	public void setLastMessage(String value) {
		_rLastMessage = value;
	}

	private long _rLastRevision = -1;

	public long getLastRevision() {
		return _rLastRevision;
	}

	public void setLastRevision(long value) {
		_rLastRevision = value;
	}

	private String _rLastRevisionId = null;

	public String getLastRevisionId() {
		return _rLastRevisionId;
	}

	public void setLastRevisionId(String value) {
		_rLastRevisionId = value;
	}

	private long _rLastTimestamp = 0;

	public long getLastRevisionTimestamp() {
		return _rLastTimestamp;
	}

	public void setLastRevisionTimestamp(long value) {
		_rLastTimestamp = value;
	}

	private String _rLastAuthor = null;

	public String getLastAuthor() {
		return _rLastAuthor;
	}

	public void setLastAuthor(String value) {
		_rLastAuthor = value;
	}

	private String _rLastCommitter = null;

	public String getLastCommitter() {
		return _rLastCommitter;
	}

	public void setLastCommitter(String value) {
		_rLastCommitter = value;
	}

	private List<String> _files = null;

	public List<String> getFiles() {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		return _files;
	}

	public void addModifiedFile(String value) {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		_files.add(value);
	}

	public void clearFiles() {
		if (_files != null && !_files.isEmpty()) {
			_files.clear();
		}
	}
}
