/*
 * ServerRevision.java
 *
 * Records the latest revision number for a server.
 */

package net.mobid.codetraq.persistence;

import net.mobid.codetraq.VCSType;
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

	private VCSType _versionControl = null;

	public VCSType getVersionControlType() {
		return _versionControl;
	}

	public void setVersionControlType(VCSType value) {
		_versionControl = value;
	}

	public void setShouldUpdate(boolean value) {
		_shouldUpdate = value;
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

	private long _lastRevision = 0;

	public long getLastRevision() {
		return _lastRevision;
	}

	public void setLastRevision(long value) {
		_lastRevision = value;
	}

	public long _lastCheckedTimestamp = 0;

	public long getLastCheckedTimestamp() {
		return _lastCheckedTimestamp;
	}

	public void setLastCheckedTimestamp(long value) {
		_lastCheckedTimestamp = value;
	}

	private Object _lastMessage = null;

	public Object getLastMessage() {
		return _lastMessage;
	}

	public void setLastMessage(Object value) {
		_lastMessage = value;
	}

	public int getMinutesSinceLastCheck() {
		if (getLastCheckedTimestamp() > 0)  {
			Period p = new Period(getLastCheckedTimestamp(), System.currentTimeMillis());
			return p.getMinutes();
		}
		return -1;
	}
}
