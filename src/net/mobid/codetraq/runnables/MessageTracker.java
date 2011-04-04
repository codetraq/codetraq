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
package net.mobid.codetraq.runnables;

import java.util.List;
import net.mobid.codetraq.ConnectionType;
import net.mobid.codetraq.ITalker;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.talkers.EmailTalker;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;

/**
 * This class tracks the message database and sends (or try to send) any message
 * left in the database. The daemon will always try to delete a message after it
 * succesfully being sent. You should set the Message Round (the number of minutes
 * between sending message process) to a number that is suitable for your own
 * circumstances.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class MessageTracker implements Runnable {

	private ITalker _xmppTalker = null;

	private ITalker _msnTalker = null;

	private ITalker _mailTalker = null;

	private DbUtility _db = null;

	private final int MESSAGE_ROUND = 3;

	/**
	 * Creates a new MessageTracker.
	 * @param db - a <code>DbUtility</code> instance
	 */
	public MessageTracker(DbUtility db) {
		_db = db;
	}

	/**
	 * Sets an instance of <code>XMPPTalker</code> for this <code>MessageTracker</code>.
	 * XMPP is the protocol to use if you wish to send messages via Google Talk or Jabber.
	 * @param value - a <code>XMPPTalker</code> instance
	 */
	public void setXMPPTalker(ITalker value) {
		_xmppTalker = value;
	}

	/**
	 * Sets an instance of <code>MSNTalker</code> for this <code>MessageTracker</code>.
	 * MSN is the protocol to use if you wish to send messages via MSN.
	 * @param value - a <code>MSNTalker</code> instance
	 */
	public void setMSNTalker(ITalker value) {
		_msnTalker = value;
	}

	/**
	 * Sets an instance of <code>EmailTalker</code> for this <code>MessageTracker</code>.
	 * Use this if you wish to send messages via email.
	 * @param value
	 */
	public void setEmailTalker(ITalker value) {
		_mailTalker = value;
	}

	/**
	 * Monitors the message database and tries to send any unsent messages. Messages will
	 * be deleted after successfully sent. If a message is unsent, it will be left in the
	 * database for another try in the next round.
	 */
	public void run() {
		Thread currentThread = Thread.currentThread();
		try {
			while (true) {
				Thread.yield();
				if (currentThread.isInterrupted()) {
					throw new InterruptedException("Time to pack up and go home");
				}
				_db.deleteAllSentMessages();
				List<MessageDTO> list = _db.getAllUnsentMessages();
				for (MessageDTO m : list) {
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
				Thread.sleep(MESSAGE_ROUND * 60 * 1000);
			}
		} catch (InterruptedException ex) {
			LogService.writeMessage("MessageTracker interrupted");
			return;
		}
	}

}
