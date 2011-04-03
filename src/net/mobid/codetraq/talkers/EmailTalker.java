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

import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import net.mobid.codetraq.ITalker;
import net.mobid.codetraq.persistence.MessageDTO;
import net.mobid.codetraq.utils.LogService;

/**
 * EmailTalker.java
 *
 * This class implements ITalker interface for sending emails to client. This class
 * works perfectly with GMail SMTP server. We have trouble testing this with Hotmail
 * SMTP server. You might have some luck getting it to work with your ISP's SMTP server.
 * @author Ronald Kurniawan
 */
public class EmailTalker implements ITalker {

	private String _username = null;
	private String _password = null;
	private String _host = null;
	private int _port = 0;
	private boolean _useSSL = false;
	private boolean _useTLS = false;
	private MessageDTO _message = null;

	/**
	 * Creates a new instance of EmailTalker. You should provide the password as
	 * an encrypted <code>String</code> inside the configuration file. See
	 * <code>PasswordProcessor.java</code> for more information on how we encrypt
	 * or decrypt text. <b>DO NOT</b> store plaintext passwords in your configuration
	 * file.
	 * @param username - Your SMTP username
	 * @param password - Your SMTP password
	 * @param host - Your SMTP host URL
	 * @param port - Your SMTP host port
	 * @param ssl - Does your SMTP host use SSL?
	 * @param tls - Does your SMTP host use TLS?
	 */
	public EmailTalker(String username, String password, String host, int port, boolean ssl, boolean tls) {
		_username = username;
		_password = password;
		_host = host;
		_port = port;
		_useSSL = ssl;
		_useTLS = tls;
	}

	/**
	 * Sets the message to send. Messages are created and saved by <code>MessageTracker</code>.
	 * @param value - a <code>MessageDTO</code> object.
	 */
	public void setMessage(MessageDTO value) {
		_message = value;
	}

	/**
	 * This method is not implemented for <code>EmailTalker</code>. You should call
	 * <code>talk()</code> directly to send messages.
	 */
	public void connect() {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	/**
	 * Sends the message to the recipient.
	 * @param recipientAddress - Email address of the recipient.
	 * @param message - Content of the message
	 * @return <code>true</code> if message is sent successfully, <code>false</code> otherwise
	 */
	public boolean talk(String recipientAddress, String message) {
		if (_message != null) {
			try {
				Properties props = new Properties();
				props.put("mail.transport.protocol", "smtp");
				props.put("mail.smtp.user", _username);
				props.put("mail.smtp.host", _host);
				props.put("mail.smtp.port", _port);
				props.put("mail.smtp.auth", "true");
				if (_useTLS) {
					props.put("mail.smtp.starttls.enable", "true");
				}
				if (_useSSL) {
					props.put("mail.smtp.socketFactory.port", _port);
					props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					props.put("mail.smtp.socketFactory.fallback", "false");
				}
				Session mailSession = Session.getDefaultInstance(props);
				mailSession.setDebug(false);
				Transport transport = mailSession.getTransport();
				MimeMessage content = new MimeMessage(mailSession);
				content.setFrom(new InternetAddress(_username));
				content.setSubject(_message.getSubject());
				content.setContent(message, "text/plain");
				content.addRecipient(Message.RecipientType.TO,
					new InternetAddress(recipientAddress));
				transport.connect(_host, _port, _username, _password);
				transport.sendMessage(content, content.getRecipients(Message.RecipientType.TO));
				transport.close();
				_message = null;
				return true;
			} catch (NoSuchProviderException ex) {
				LogService.getLogger(EmailTalker.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			} catch (MessagingException ex) {
				LogService.getLogger(EmailTalker.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			} finally {
				_message = null;
			}
		}
		return false;
	}

	/**
	 * This method is not implemented for <code>EmailTalker</code>. You should call
	 * <code>talk()</code> directly to send messages.
	 */
	public void disconnect() {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	/**
	 * This method is not implemented for <code>EmailTalker</code>. You should call
	 * <code>talk()</code> directly to send messages.
	 */
	public boolean isInContactList(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	/**
	 * This method is not implemented for <code>EmailTalker</code>. You should call
	 * <code>talk()</code> directly to send messages.
	 */
	public void addToContactList(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	/**
	 * This method is not implemented for <code>EmailTalker</code>. You should call
	 * <code>talk()</code> directly to send messages.
	 */
	public boolean recipientOnline(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

}
