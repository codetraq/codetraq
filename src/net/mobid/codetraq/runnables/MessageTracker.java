/*
 * MessageTracker.java
 *
 * This class tracks the message database and sends (or try to send) any message
 * left in the database.
 */

package net.mobid.codetraq.runnables;

import java.util.List;
import net.mobid.codetraq.ConnectionType;
import net.mobid.codetraq.ITalker;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.talkers.EmailTalker;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;

/**
 *
 * @author viper
 */
public class MessageTracker implements Runnable {

	private ITalker _xmppTalker = null;

	private ITalker _msnTalker = null;

	private ITalker _mailTalker = null;

	private DbUtility _db = null;

	public MessageTracker(DbUtility db) {
		_db = db;
	}

	public void setXMPPTalker(ITalker value) {
		_xmppTalker = value;
	}

	public void setMSNTalker(ITalker value) {
		_msnTalker = value;
	}

	public void setEmailTalker(ITalker value) {
		_mailTalker = value;
	}

	public void run() {
		Thread currentThread = Thread.currentThread();
		try {
			while (true) {
				Thread.yield();
				if (currentThread.isInterrupted()) {
					throw new InterruptedException("Time to pack up and go home");
				}
				List<MessageDTO> list = _db.getAllUnsentMessages();
				for (MessageDTO m : list) {
					if (m.isSent()) {
						_db.deleteMessage(m);
						_db.logCheckDelete(m.getServerName(), m.getTimestamp());
						continue;
					}
					if (m.getRecipient().getNotificationType() == ConnectionType.GOOGLE_TALK ||
						m.getRecipient().getNotificationType() == ConnectionType.JABBER) {
						boolean status = _xmppTalker.talk(m.getRecipient().getNotificationId(),
							m.generateMessage());
						if (!status) {
							LogService.writeMessage("Sending message to " + m.getRecipient().getNotificationId() + " failed.");
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
							_db.updateMessageRetries(m);
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
						} else {
							// delete the message if successfuly sent
							_db.updateMessageSent(m);
							LogService.writeMessage("Sending message to " + m.getRecipient().getNotificationId() + " successful");
							_db.deleteMessage(m);
							_db.logCheckDelete(m.getServerName(), m.getTimestamp());
						}
					} else if (m.getRecipient().getNotificationType() == ConnectionType.MSN) {
						boolean status = _msnTalker.talk(m.getRecipient().getNotificationId(),
							m.generateMessage());
						if (!status) {
							LogService.writeMessage("Sending message to " + m.getRecipient().getNotificationId() + " failed.");
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
							_db.updateMessageRetries(m);
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
						} else {
							// delete the message if successfuly sent
							_db.updateMessageSent(m);
							LogService.writeMessage("Sending message to " + m.getRecipient().getNotificationId() + " successful");
							_db.deleteMessage(m);
							_db.logCheckDelete(m.getServerName(), m.getTimestamp());
						}
					} else if (m.getRecipient().getNotificationType() == ConnectionType.EMAIL) {
						((EmailTalker)_mailTalker).setMessage(m);
						boolean status = _mailTalker.talk(m.getRecipient().getNotificationId(),
							m.generateMessage());
						if (!status) {
							LogService.writeMessage("Sending email to " + m.getRecipient().getNotificationId() +
								" failed.");
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
							_db.updateMessageRetries(m);
							_db.logCheckRetries(m.getServerName(), m.getTimestamp());
						} else {
							// delete the message if successfuly sent
							_db.updateMessageSent(m);
							LogService.writeMessage("Sending message to " + m.getRecipient().getNotificationId() +
								" successful.");
							_db.deleteMessage(m);
							_db.logCheckDelete(m.getServerName(), m.getTimestamp());
						}
					}
				}
				Thread.sleep(3* 60 * 1000);
			}
		} catch (InterruptedException ex) {
			LogService.writeMessage("MessageTracker interrupted");
			return;
		}
	}

}
