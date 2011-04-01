/*
 * RevisionDTO.java
 *
 * This class holds the record for revision data for a certain server.
 */

package net.mobid.codetraq.persistence;

/**
 *
 * @author viper
 */
public class UserRevision {

	private String _serverAddress = null;

	public String getServerAddress() {
		return _serverAddress;
	}

	public void setServerAddress(String value) {
		if (_serverAddress != null) {
			System.out.printf("Server Address cannot be changed.%n");
			return;
		}
		_serverAddress = value;
	}

	private String _owner = null;

	public String getOwner() {
		return _owner;
	}

	public void setOwner(String value) {
		_owner = value;
	}

	private long _lastRevision = 0;

	public long getLastRevision() {
		return _lastRevision;
	}

	public void setLastRevision(long value) {
		_lastRevision = value;
	}

	private String _lastRevisionId = null;

	public String getLastRevisionId() {
		return _lastRevisionId;
	}

	public void setLastRevisionId(String value) {
		_lastRevisionId = value;
	}

}
