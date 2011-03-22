/*
 * ITalker.java
 *
 * Interface for notification mechanism.
 */

package net.mobid.codetraq;

/**
 *
 * @author viper
 */
public interface ITalker {

	public void connect();

	public boolean talk(String recipientAddress, String message);

	public void disconnect();

	public boolean isInContactList(String recipientAddress);

	public void addToContactList(String recipientAddress);

	public boolean recipientOnline(String recipientAddress);

}
