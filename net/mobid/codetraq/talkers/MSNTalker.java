/*
 * MSNTalker.java
 *
 * This is the implementation of ITalker interface for MSN Protocol.
 * We are using JML for implementation.
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
 *
 * @author viper
 */
public class MSNTalker implements ITalker {

	private String _username = null;
	private String _password = null;
	private ConnectionType _cType = null;
	private MsnMessenger _messenger = null;
	private List<String> _pendingCheckUser = new ArrayList<String>();
	private final Object swid = "CODETRAQ";
	private boolean _switchboardOn = false;

	public MSNTalker(String username, String password, ConnectionType cType) {
		_username = username;
		_password = password;
		_cType = cType;
		_messenger = MsnMessengerFactory.createMsnMessenger(_username, _password);
		initMessenger();
		connect();
	}

	public List<String> getPendingCheckUser() {
		return _pendingCheckUser;
	}

	public final void connect() {
		if (_messenger != null) {
			_messenger.login();
			System.out.printf("Connecting to %s on port %d...%n", _messenger.getConnection().getRemoteIP(),
					_messenger.getConnection().getRemotePort());
		}
	}

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

	public void disconnect() {
		if (_messenger != null) {
			_messenger.logout();
		}
	}

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

	public void addToContactList(String recipientAddress) {
		if (_messenger != null) {
			_messenger.addFriend(Email.parseStr(recipientAddress), recipientAddress);
		}
	}

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
