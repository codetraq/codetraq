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
package net.mobid.codetraq;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.runnables.GitChecker;
import net.mobid.codetraq.runnables.MessageTracker;
import net.mobid.codetraq.runnables.ServerTracker;
import net.mobid.codetraq.runnables.SvnChecker;
import net.mobid.codetraq.talkers.EmailTalker;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;
import net.mobid.codetraq.utils.PasswordProcessor;
import net.mobid.codetraq.utils.Utilities;
import net.mobid.codetraq.talkers.MSNTalker;
import net.mobid.codetraq.talkers.XMPPTalker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is the entry point for the CodeTraq daemon program. Setup all necessary
 * component, then run all the runnables.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class Main {

	private static boolean _isRunning = true;
	private static ITalker _xmppTalker = null;
	private static ITalker _msnTalker = null;
	private static ITalker _mailTalker = null;
	private static Document _configuration = null;
	private static List<UserDTO> _users = null;
	private static List<ServerDTO> _servers = null;
	private volatile Thread _messageChecker = null;
	private volatile Thread _serverChecker = null;
	private static DbUtility _traqdb = null;
	private final int USER_UPDATE_IN_MINUTES = 8;

	/**
	 * Read and parse CodeTraq configuration file (ctraq.xml) in current directory.
	 * @return <code>Document</code> object containing the parsed configuration
	 */
	public Document getConfiguration() {
		if (_configuration == null) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				_configuration = builder.parse(new File("ctraq.xml"));
				_configuration.getDocumentElement().normalize();
			} catch (SAXException ex) {
				LogService.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			} catch (IOException ex) {
				LogService.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			} catch (ParserConfigurationException pae) {
				LogService.getLogger(Main.class.getName()).log(Level.SEVERE, null, pae);
				LogService.writeLog(Level.SEVERE, pae);
			}
		}
		return _configuration;
	}

	/**
	 * Returns all the users in the configuration file.
	 * @return a <code>List</code> containing all the users that will receive notifications.
	 */
	public List<UserDTO> getUsers() {
		if (_users == null) {
			_users = new ArrayList<UserDTO>();
		}
		return _users;
	}

	/**
	 * Returns all the servers in the configuration file.
	 * @return a <code>List</code> containing all the servers that should be monitored.
	 */
	public List<ServerDTO> getServers() {
		if (_servers == null) {
			_servers = new ArrayList<ServerDTO>();
		}
		return _servers;
	}

	public static void main(String[] args) {
		Main m = new Main();
	}

	/**
	 * Constructor for Main class.
	 * <p>Initialises the application, reads the configuration file and call run().</p>
	 */
	public Main() {
		System.out.printf("CodeTraq daemon v0.1%n");
		// we need to have a config file. Check for existing one or create a new one...
		// and also, we need to read the configuration file for any errors....
		boolean setupStatus = true;
		if (!isConfigFileExists()) {
			setupStatus = false;
			createConfigFile();
		}
		if (!readConfigFile() && setupStatus) {
			setupStatus = false;
		}

		if (!setupStatus) {
			System.out.printf("Your daemon has not been configured properly. Please populate "
					+ "the configuration file (ctraq.xml) with correct values and restart the daemon.%n");
			return;
		}
		_traqdb = new DbUtility();
		ShutdownHook hook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(hook);
		System.out.printf("CodeTraq started on %s%n", Utilities.getFormattedTime());
		LogService.writeMessage("CodeTraq started");
		run();
	}

	/*
	 * Creates an empty configuration file.
	 */
	private void createConfigFile() {
		Utilities.createNewConfigFile("ctraq.xml");
	}

	/*
	 * Checks whether configuration file exists.
	 * @return <code>true</code> if configuration file exists, <code>false</code> otherwise.
	 */
	private boolean isConfigFileExists() {
		File cfg = new File("ctraq.xml");
		if (cfg.exists()) {
			return true;
		}
		return false;
	}

	/*
	 * Reads the configuration file and sets up internal variables.
	 *
	 * @return <code>true</code> if configuration file has the right syntax,
	 * <code>false</code> if reader encounters any errors.
	 */
	private boolean readConfigFile() {
		try {
			// get codetraq notification id
			NodeList traqs = getConfiguration().getElementsByTagName("traq");
			if (traqs.getLength() > 0) {
				for (int i = 0; i < traqs.getLength(); i++) {
					Node traq = traqs.item(i);
					if (!Utilities.checkNode("traq type", traq.getAttributes().getNamedItem("type"))
							|| !Utilities.checkNode("traq notification id", traq.getAttributes().getNamedItem("notificationid"))
							|| !Utilities.checkNode("traq notification password", traq.getAttributes().getNamedItem("password"))) {
						System.out.printf("Please review your configuration file%n");
						return false;
					}
					String type = traq.getAttributes().getNamedItem("type").getTextContent();
					String notificationId = traq.getAttributes().getNamedItem("notificationid").getTextContent();
					String password = PasswordProcessor.decryptString(traq.getAttributes().
							getNamedItem("password").getTextContent());
					if (!Utilities.checkValue("daemon notification type", type)
							|| !Utilities.checkValue("daemon notification password", password)
							|| !Utilities.checkValue("daemon notification id", notificationId)) {
						return false;
					}
					String smtpHost = "";
					int smtpPort = 0;
					boolean smtpSsl = false;
					boolean smtpTls = false;
					if (type.equalsIgnoreCase("email")) {
						if (!Utilities.checkNode("traq smtp host", traq.getAttributes().getNamedItem("host"))
								|| !Utilities.checkNode("traq smtp port", traq.getAttributes().getNamedItem("port"))
								|| !Utilities.checkNode("traq using ssl", traq.getAttributes().getNamedItem("ssl"))
								|| !Utilities.checkNode("traq using tls", traq.getAttributes().getNamedItem("tls"))) {
							System.out.printf("Please review your configuration file%n");
							return false;
						}
						smtpHost = traq.getAttributes().getNamedItem("host").getTextContent();
						if (!Utilities.checkValue("daemon smtp host", smtpHost)) {
							return false;
						}
						String tPort = traq.getAttributes().getNamedItem("port").getTextContent();
						try {
							smtpPort = Integer.parseInt(tPort);
						} catch (NumberFormatException nfe) {
							LogService.getLogger(Main.class.getName()).log(Level.SEVERE, null, nfe);
							LogService.writeLog(Level.SEVERE, nfe);
							System.out.printf("traq smtp port should be a number between 1 - 65535. Please review your configuration file.%n");
							return false;
						}
						String tSsl = traq.getAttributes().getNamedItem("ssl").getTextContent();
						if (tSsl.equalsIgnoreCase("true") || tSsl.equalsIgnoreCase("yes")) {
							smtpSsl = true;
						}
						String tTls = traq.getAttributes().getNamedItem("tls").getTextContent();
						if (tTls.equalsIgnoreCase("true") || tTls.equalsIgnoreCase("yes")) {
							smtpTls = true;
						}
					}
					// it is possible that a second Talker implementing another notification type
					// (e.g. "msn") might be implemented in the future. In that case, add another
					// "else if" block below...
					if (type.equalsIgnoreCase("gtalk") || type.equalsIgnoreCase("jabber")) {
						if (_xmppTalker == null) {
							ConnectionType cType = ConnectionType.JABBER;
							if (type.equalsIgnoreCase("gtalk")) {
								cType = ConnectionType.GOOGLE_TALK;
							}
							_xmppTalker = new XMPPTalker(notificationId, password, cType);
						}
					} else if (type.equalsIgnoreCase("msn")) {
						if (_msnTalker == null) {
							ConnectionType cType = ConnectionType.MSN;
							_msnTalker = new MSNTalker(notificationId, password, cType);
						}
					} else if (type.equalsIgnoreCase("email")) {
						if (_mailTalker == null) {
							_mailTalker = new EmailTalker(notificationId, password, smtpHost, smtpPort, smtpSsl, smtpTls);
						}
					} else {
						return false;
					}
				}
			}
			// get list of users and servers
			NodeList users = getConfiguration().getElementsByTagName("user");
			for (int i = 0; i < users.getLength(); i++) {
				Node user = users.item(i);
				if (!Utilities.checkNode("user name", user.getAttributes().getNamedItem("name"))
						|| !Utilities.checkNode("user id", user.getAttributes().getNamedItem("id"))
						|| !Utilities.checkNode("user notification type", user.getAttributes().getNamedItem("type"))
						|| !Utilities.checkNode("user notification id", user.getAttributes().getNamedItem("notificationid"))) {
					System.out.printf("Please review your configuration file.%n");
					return false;
				}
				String name = user.getAttributes().getNamedItem("name").getTextContent();
				String id = user.getAttributes().getNamedItem("id").getTextContent();
				String type = user.getAttributes().getNamedItem("type").getTextContent();
				String notificationId = user.getAttributes().getNamedItem("notificationid").getTextContent();
				if (!Utilities.checkValue("user nickname", name)
						|| !Utilities.checkValue("user id", id)
						|| !Utilities.checkValue("user notification type", type)
						|| !Utilities.checkValue("user notification id", notificationId)) {
					return false;
				}
				UserDTO u = new UserDTO();
				u.setId(id);
				u.setNickname(name);
				u.setNotificationId(notificationId);
				if (type.equalsIgnoreCase("gtalk") || type.equalsIgnoreCase("jabber")) {
					u.setNotificationType(type.equalsIgnoreCase("gtalk") ? ConnectionType.GOOGLE_TALK : ConnectionType.JABBER);
					getUsers().add(u);
					// check whether this contact is already in our contact list
					if (!_xmppTalker.isInContactList(notificationId)) {
						_xmppTalker.addToContactList(notificationId);
					}
				} else if (type.equalsIgnoreCase("msn")) {
					u.setNotificationType(ConnectionType.MSN);
					getUsers().add(u);
					// check if contact is in our contact list
					// because we need to wait for the messenger to be available,
					// we pack this into a pending list
					((MSNTalker) _msnTalker).getPendingCheckUser().add(notificationId);
				} else if (type.equalsIgnoreCase("email")) {
					u.setNotificationType(ConnectionType.EMAIL);
					getUsers().add(u);
				}
			}
			NodeList servers = getConfiguration().getElementsByTagName("server");
			for (int i = 0; i < servers.getLength(); i++) {
				Node server = servers.item(i);
				if (!Utilities.checkNode("server owner", server.getAttributes().getNamedItem("owner"))
						|| !Utilities.checkNode("server nickname", server.getAttributes().getNamedItem("sname"))
						|| !Utilities.checkNode("server address", server.getAttributes().getNamedItem("address"))
						|| !Utilities.checkNode("server type", server.getAttributes().getNamedItem("type"))
						|| !Utilities.checkNode("server username", server.getAttributes().getNamedItem("username"))
						|| !Utilities.checkNode("server password", server.getAttributes().getNamedItem("password"))) {
					System.out.printf("Please review your configuration file.%n");
					return false;
				}
				String owner = server.getAttributes().getNamedItem("owner").getTextContent();
				String shortName = server.getAttributes().getNamedItem("sname").getTextContent();
				String sAddress = server.getAttributes().getNamedItem("address").getTextContent();
				String sType = server.getAttributes().getNamedItem("type").getTextContent();
				String sUsername = server.getAttributes().getNamedItem("username").getTextContent();
				String sPassword = PasswordProcessor.decryptString(server.getAttributes().
						getNamedItem("password").getTextContent());
				if (!Utilities.checkValue("owner id", owner) || !Utilities.checkValue("server name", shortName)
						|| !Utilities.checkValue("server address", sAddress)
						|| !Utilities.checkValue("server type", sType)
						|| !Utilities.checkValue("server username", sUsername)
						|| !Utilities.checkValue("server password", sPassword)) {
					return false;
				}
				// need to check that server's short name is unique, as we need it to
				// create git local repo (if type is 'git')
				if (!Utilities.checkServerShortName(getServers(), shortName)) {
					System.out.printf("Server short name must be unique. '%s' is already used for another server.%n",
							shortName);
					return false;
				}
				// if this is a GIT repo, we need a branch name
				String sBranch = null;
				if (sType.equalsIgnoreCase("git")) {
					if (!Utilities.checkNode("git branch", server.getAttributes().getNamedItem("branch"))) {
						System.out.printf("GIT server %s does not have a specified branch. Please review your configuration file.%n",
								sAddress);
						return false;
					}
					sBranch = server.getAttributes().getNamedItem("branch").getTextContent();
					if (!Utilities.checkValue("git branch", sBranch)) {
						return false;
					}
				}
				ServerDTO s = new ServerDTO();
				s.setOwnerId(owner);
				s.setShortName(shortName);
				if (sType.equalsIgnoreCase("svn")) {
					s.setServerType(VersionControlType.SVN);
				} else if (sType.equalsIgnoreCase("git")) {
					s.setServerType(VersionControlType.GIT);
					s.setServerBranch(sBranch);
				}
				s.setServerAddress(sAddress);
				s.setServerUsername(sUsername);
				s.setServerPassword(sPassword);
				getServers().add(s);
			}
		} catch (Exception ex) {
			LogService.writeLog(Level.SEVERE, ex);
		}
		// if encountering any error return false
		return true;
	}

	/**
	 * Starts all the runnables that monitor the servers and notify the users.
	 */
	public final void run() {
		MessageTracker tracker = new MessageTracker(_traqdb);
		tracker.setXMPPTalker(_xmppTalker);
		tracker.setMSNTalker(_msnTalker);
		tracker.setEmailTalker(_mailTalker);
		_messageChecker = new Thread(tracker);
		_messageChecker.start();
		ServerTracker st = new ServerTracker(_traqdb, _servers);
		_serverChecker = new Thread(st);
		_serverChecker.start();
		while (_isRunning) {
			for (int i = 0; i < _servers.size(); i++) {
				ServerDTO s = _servers.get(i);
				UserDTO user = getUserById(s.getOwnerId());
				if (user == null) {
					System.out.printf("Cannot find user %s%n.", s.getOwnerId());
					LogService.writeMessage("Cannot find user " + s.getOwnerId());
					continue;
				}
				if (s.getServerType() == VersionControlType.SVN) {
					SvnChecker svnChecker = new SvnChecker(s, user, _traqdb);
					Thread t = new Thread(svnChecker);
					t.start();
				} else if (s.getServerType() == VersionControlType.GIT) {
					GitChecker gitChecker = new GitChecker(s, user, _traqdb);
					Thread t = new Thread(gitChecker);
					t.start();
				}
			}
			try {
				Thread.sleep(USER_UPDATE_IN_MINUTES * 60 * 1000);
			} catch (InterruptedException ex) {
				LogService.writeLog(Level.SEVERE, ex);
			}
		}
	}

	/*
	 * Search the internal list _users for a User with specified ID.
	 * @param id - user specified ID
	 * @return a <code>UserDTO</code> object.
	 */
	private UserDTO getUserById(String id) {
		UserDTO key = new UserDTO();
		key.setId(id);
		Collections.sort(_users);
		int x = Collections.binarySearch(_users, key);
		if (x < 0) {
			return null;
		}
		return _users.get(x);
	}

	/*
	 * Stops the message checker thread. Should only be called during shutdown.
	 */
	private synchronized void messageTrackerStop() {
		Thread tmpChecker = _messageChecker;
		_messageChecker = null;
		if (tmpChecker != null) {
			tmpChecker.interrupt();
		}
	}

	/*
	 * Stops the server tracker thread. Should only be called during shutdown.
	 */
	private synchronized void serverTrackerStop() {
		Thread tmpChecker = _serverChecker;
		_serverChecker = null;
		if (tmpChecker != null) {
			tmpChecker.interrupt();
		}
	}

	/*
	 * ShutdownHook class is a class that is responsible for "cleaning up" during
	 * shutdown process. It stops every running thread and cleans up internal variables.
	 *
	 */
	class ShutdownHook extends Thread {
		// we run "cleaning process" here

		@Override
		public void run() {
			System.out.printf("Shutting down CodeTraq...%n");
			if (_xmppTalker != null) {
				_xmppTalker.disconnect();
				_xmppTalker = null;
			}
			if (_msnTalker != null) {
				_msnTalker.disconnect();
				_msnTalker = null;
			}
			if (_mailTalker != null) {
				_mailTalker = null;
			}
			_isRunning = false;
			_traqdb.closeDbs();
			messageTrackerStop();
			serverTrackerStop();
			_users.clear();
			_users = null;
			_servers.clear();
			_servers = null;
			_configuration = null;
			LogService.writeMessage("CodeTraq shut down");
		}
	}
}
