/*
 * ServerDTO.java
 *
 * This is a template for storing Server objects in memory
 */

package net.mobid.codetraq.persistence;

import net.mobid.codetraq.VCSType;

/**
 *
 * @author viper
 */
public class ServerDTO {

	private String _owner = null;

	public String getOwnerId() {
		return _owner;
	}

	public void setOwnerId(String value) {
		_owner = value;
	}

	private String _shortName = null;

	public String getShortName() {
		return _shortName;
	}

	public void setShortName(String value) {
		_shortName = value;
	}

	private VCSType _serverType = null;

	public VCSType getServerType() {
		return _serverType;
	}

	public void setServerType(VCSType value) {
		_serverType = value;
	}

	private String _serverAddress = null;

	public String getServerAddress() {
		return _serverAddress;
	}

	public void setServerAddress(String value) {
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
}
