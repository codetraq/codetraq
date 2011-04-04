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
package net.mobid.codetraq.talkers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.mobid.codetraq.ConnectionType;
import net.mobid.codetraq.ITalker;
import net.mobid.codetraq.utils.LogService;
import net.sf.jml.Email;
import net.sf.jml.MsnContact;
import net.sf.jml.MsnContactList;
import net.sf.jml.MsnContactPending;
import net.sf.jml.MsnList;
import net.sf.jml.MsnMessenger;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.MsnUserStatus;
import net.sf.jml.event.MsnContactListAdapter;
import net.sf.jml.event.MsnMessengerAdapter;
import net.sf.jml.event.MsnSwitchboardAdapter;
import net.sf.jml.impl.MsnMessengerFactory;

/**
 * This is the implementation of ITalker interface for MSN Protocol.
 * We are using JML for implementation.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class MSNTalker implements ITalker {

	private String _username = null;
	private String _password = null;
	private ConnectionType _cType = null;
	private MsnMessenger _messenger = null;
	private List<String> _pendingCheckUser = new ArrayList<String>();
	private final Object swid = "CODETRAQ";
	private boolean _switchboardOn = false;

	/**
	 * Creates a new MSNTalker. You should provide the password as
	 * an encrypted <code>String</code> inside the configuration file. See
	 * <code>PasswordProcessor.java</code> for more information on how we encrypt
	 * or decrypt text. <b>DO NOT</b> store plaintext passwords in your configuration
	 * file.
	 * @param username - User's MSN username
	 * @param password - User's MSN password
	 * @param cType - a <code>ConnectionType</code> object
	 */
	public MSNTalker(String username, String password, ConnectionType cType) {
		_username = username;
		_password = password;
		_cType = cType;
		_messenger = MsnMessengerFactory.createMsnMessenger(_username, _password);
		initMessenger();
		connect();
	}

	/**
	 * Returns the list of users that has not been approved into our contact list.
	 * @return a <code>List</code> of pending contacts
	 */
	public List<String> getPendingCheckUser() {
		return _pendingCheckUser;
	}

	/**
	 * Logs into MSN.
	 */
	public final void connect() {
		if (_messenger != null) {
			_messenger.login();
			System.out.printf("Connecting to %s on port %d...%n", _messenger.getConnection().getRemoteIP(),
					_messenger.getConnection().getRemotePort());
		}
	}

	/**
	 * Sends a message to a contact.
	 * @param recipientAddress - recipient's MSN username
	 * @param message - message to send
	 * @return <code>true</code> if message is sent successfully, <code>false</code> otherwise
	 */
	public boolean talk(String recipientAddress, String message) {
		if (_messenger != null && _switchboardOn) {
			MsnContact c = _messenger.getContactList().getContactByEmail(Email.parseStr(recipientAddress));
			if (c.getStatus() != MsnUserStatus.OFFLINE) {
				_messenger.sendText(Email.parseStr(recipientAddress), message);
				return true;
			}
		}
		return false;
	}

	/**
	 * Logs out from MSN.
	 */
	public void disconnect() {
		if (_messenger != null) {
			_messenger.logout();
		}
	}

	/**
	 * Checks whether a contact is in our contact list.
	 * @param recipientAddress - contact's MSN username
	 * @return <code>true</code> if contact is in our list, <code>false</code> otherwise
	 */
	public boolean isInContactList(String recipientAddress) {
		if (_messenger != null) {
			MsnContactList contactList = _messenger.getContactList();
			MsnContact c = contactList.getContactByEmail(Email.parseStr(recipientAddress));
			if (c != null && c.isInList(MsnList.AL)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a user into our contact list.
	 * @param recipientAddress - contact's MSN username
	 */
	public void addToContactList(String recipientAddress) {
		if (_messenger != null) {
			_messenger.addFriend(Email.parseStr(recipientAddress), recipientAddress);
		}
	}

	/**
	 * Checks whether a contact is online and ready to receive messages.
	 * @param recipientAddress - contact's MSN username
	 * @return <code>true</code> if contact is online, <code>false</code> otherwise
	 */
	public boolean recipientOnline(String recipientAddress) {
		if (_messenger != null) {
			MsnContactList contactList = _messenger.getContactList();
			MsnContact c = contactList.getContactByEmail(Email.parseStr(recipientAddress));
			if (c != null && c.isInList(MsnList.AL)) {
				if (c.getStatus() != MsnUserStatus.OFFLINE) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Clears the pending contact list.
	 */
	private void clearPendingCheckUserList() {
		if (_pendingCheckUser != null && !_pendingCheckUser.isEmpty()) {
			Iterator it = _pendingCheckUser.iterator();
			while (it.hasNext()) {
				String notificationID = it.next().toString();
				if (!isInContactList(notificationID)) {
					addToContactList(notificationID);
				}
			}
			_pendingCheckUser.clear();
		}
	}

	/*
	 * Initialises our MSN object. As a rule we need to add "listeners" to MSN object,
	 * and try to catch every possible situations. Without these "listeners", the
	 * MSN object will not work correctly.
	 */
	private void initMessenger() {
		if (_messenger != null) {
			_messenger.addMessengerListener(new MsnMessengerAdapter() {

				public void loginCompleted(MsnMessenger messenger) {
					LogService.writeMessage(messenger.getOwner().getEmail().getEmailAddress()
							+ " logged in.");
					_messenger.addContactListListener(new MsnContactListAdapter() {

						@Override
						public void contactListSyncCompleted(MsnMessenger messenger) {
							super.contactListSyncCompleted(messenger);
							clearPendingCheckUserList();
							System.out.printf("Syncing contact list completed%n");
							System.out.printf("There are %d contacts on the list.%n",
									messenger.getContactList().getContacts().length);
						}

						@Override
						public void contactListInitCompleted(MsnMessenger messenger) {
						}

						@Override
						public void contactAddedMe(MsnMessenger messenger, MsnContact contact) {
							messenger.addFriend(contact.getEmail(), contact.getDisplayName());
						}

						@Override
						public void contactAddedMe(MsnMessenger messenger, MsnContactPending[] pending) {
							for (int i = 0; i < pending.length; i++) {
								MsnContactPending p = pending[i];
								messenger.addFriend(p.getEmail(), p.getDisplayName());
							}
						}

						@Override
						public void contactStatusChanged(MsnMessenger messenger, MsnContact contact) {
							super.contactStatusChanged(messenger, contact);
						}
					});
					_messenger.addSwitchboardListener(new MsnSwitchboardAdapter() {

						@Override
						public void switchboardStarted(MsnSwitchboard switchboard) {
							if (!switchboard.getAttachment().equals(swid)) {
								return;
							}
							_switchboardOn = true;
							LogService.writeMessage(_cType.toString() + " switchboard started");
						}

						@Override
						public void switchboardClosed(MsnSwitchboard switchboard) {
							_switchboardOn = false;
							LogService.writeMessage(_cType.toString() + " switchboard closed");
							_messenger.newSwitchboard(swid);
						}
					});
					_messenger.newSwitchboard(swid);
				}

				public void logout(MsnMessenger messenger) {
					LogService.writeMessage(messenger.getOwner().getEmail().getEmailAddress()
							+ " logged out.");
				}

				public void exceptionCaught(MsnMessenger messenger, Throwable t) {
					LogService.writeLog(Level.SEVERE, t);
				}
			});
		}
	}
}
