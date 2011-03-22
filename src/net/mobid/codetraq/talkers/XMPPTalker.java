/*
 * XMPPTalker.java
 *
 * This class implements the CodeTraq connector to XMPP-based instant messaging
 * service.
 */

package net.mobid.codetraq.talkers;

import java.util.logging.Level;
import net.mobid.codetraq.ConnectionType;
import net.mobid.codetraq.ITalker;
import net.mobid.codetraq.utils.LogService;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

/**
 *
 * @author viper
 */
public class XMPPTalker implements ITalker {

	private XMPPConnection _conn = null;
	private String _username = null;
	private String _password = null;
	private ConnectionType _cType = null;
	private Roster _roster = null;
	private boolean _connected = false;

	public XMPPTalker(String username, String password, ConnectionType connection) {
		_username = username;
		_password = password;
		_cType = connection;
		ConnectionConfiguration config = new ConnectionConfiguration(_cType.getServer(),
			_cType.getPort(), _cType.getResource());
		config.setCompressionEnabled(true);
		config.setReconnectionAllowed(true);
		config.setRosterLoadedAtLogin(true);
		_conn = new XMPPConnection(config);
		connect();
		// if we don't have a default group "client" we need to create one
		if (_roster != null && _roster.getGroup("client") == null) {
			_roster.createGroup("client");
		}
	}

	public boolean isConnected() {
		return _connected;
	}

	public void setConnected(boolean value) {
		_connected = value;
	}

	public final void connect() {
		if (_conn != null && !_conn.isConnected()) {
			try {
				System.out.printf("Connecting to %s on port %d...%n", _cType.getServer(),
					_cType.getPort());
				_conn.connect();
				SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				Thread.sleep(2000);
				System.out.printf("Logging in...%n");
				_conn.login(_username, _password, _cType.getResource());
				_roster = _conn.getRoster();
				_roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
				while (_roster == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
					}
				}
				System.out.printf("There are %d contacts on the list%n", _roster.getEntryCount());
				setConnected(true);
			} catch (XMPPException ex) {
				System.out.printf("%s%n", ex.getMessage());
				// UnknownHostException 504;
				LogService.getLogger(XMPPTalker.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
				System.out.printf("ERROR %d: %s%n", ex.getXMPPError().getCode(), ex.getXMPPError().getMessage());
				setConnected(false);
			} catch (InterruptedException ex) {
			}
		}
	}

	public boolean talk(String to, String message) {
		if (!isConnected()) {
			connect();
		}
		try {
			if (recipientOnline(to)) {
				ChatManager cm = _conn.getChatManager();
				Chat newMessage = cm.createChat(to, new MessageListener() {
					public void processMessage(Chat chat, Message message) {
					}
				});
				newMessage.sendMessage(message);
				return true;
			}
		} catch (XMPPException ex) {
			LogService.getLogger(XMPPTalker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
		return false;
	}

	public void disconnect() {
		if (_conn != null && _conn.isConnected()) {
			_conn.disconnect();
			setConnected(false);
		}
	}

	public boolean isInContactList(String recipientAddress) {
		if (!isConnected()) {
			connect();
		}
		return _roster.contains(recipientAddress);
	}

	public void addToContactList(String recipientAddress) {
		try {
			if (!isConnected()) {
				connect();
			}
			String[] groups = new String[]{"client"};
			_roster.createEntry(recipientAddress, recipientAddress, groups);
		} catch (XMPPException ex) {
			LogService.getLogger(XMPPTalker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}

	public boolean recipientOnline(String recipientAddress) {
		// Presence object can be null if recipient is offline or if
		// server's identity is not present in recipient's contact list
		if (!isConnected()) {
			connect();
		}
		String[] groups = new String[] { "client" };
		// if recipient is not in our roster, we need to add it first
		if (!_roster.contains(recipientAddress)) {
			try {
				_roster.createEntry(recipientAddress, recipientAddress, groups);
			} catch (XMPPException ex) {
				LogService.getLogger(XMPPTalker.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			}
		}
		Presence p = _roster.getPresence(recipientAddress);
		if (p == null || !p.isAvailable()) {
			return false;
		}
		return true;
	}
}
