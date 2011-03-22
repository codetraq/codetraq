/*
 * EmailTalker.java
 *
 * This class implements ITalker interface for sending emails to client.
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
 *
 * @author viper
 */
public class EmailTalker implements ITalker {

	private String _username = null;
	private String _password = null;
	private String _host = null;
	private int _port = 0;
	private boolean _useSSL = false;
	private boolean _useTLS = false;
	private MessageDTO _message = null;

	public EmailTalker(String username, String password, String host, int port, boolean ssl, boolean tls) {
		_username = username;
		_password = password;
		_host = host;
		_port = port;
		_useSSL = ssl;
		_useTLS = tls;
	}

	public void setMessage(MessageDTO value) {
		_message = value;
	}

	public void connect() {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

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

	public void disconnect() {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	public boolean isInContactList(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	public void addToContactList(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

	public boolean recipientOnline(String recipientAddress) {
		throw new UnsupportedOperationException("Not supported. Please call talk() directly.");
	}

}
