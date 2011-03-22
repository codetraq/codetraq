/*
 * VCSType.java
 *
 * Enumeration containing the types of VCS we support.
 */

package net.mobid.codetraq;

/**
 *
 * @author viper
 */
public enum VCSType {

	SVN("subversion");

	private final String _longName;

	VCSType(String longName) {
		_longName = longName;
	}

	public String getLongName() {
		return _longName;
	}
}
