/*
 * ServerTracker.java
 *
 * This is a worker class that tracks and updates the revision history of a
 * server. Set an update time that is convenient for your own circumnstance.
 */
package net.mobid.codetraq.runnables;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.mobid.codetraq.VCSType;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 *
 * @author viper
 */
public class ServerTracker implements Runnable {

	private final int UPDATE_IN_MINUTES = 8;
	private DbUtility _db = null;

	public ServerTracker(DbUtility db, List<ServerDTO> servers) {
		_db = db;
		setupServers(servers);
	}

	public void run() {
		Thread currentThread = Thread.currentThread();
		try {
			while (true) {
				Thread.yield();
				if (currentThread.isInterrupted()) {
					throw new InterruptedException("Time to pack up and go home");
				}
				// now we need to query the server database and check for changes
				// for all the servers that are marked for update...
				List<ServerRevision> servers = _db.getAllServerRevisions();
				Iterator it = servers.iterator();
				while (it.hasNext()) {
					ServerRevision sr = (ServerRevision) it.next();
					if (sr.shoudlUpdate()) {
						if (sr.getVersionControlType() == VCSType.SVN) {
							SVNLogEntry latestLogEntry = getSvnLatestRevisionHistory(sr);
							if (latestLogEntry.getRevision() > sr.getLastRevision()) {
								sr.setLastMessage(latestLogEntry);
								sr.setLastRevision(latestLogEntry.getRevision());
								sr.setLastCheckedTimestamp(System.currentTimeMillis());
								_db.updateServerLatestRevision(sr);
							}
						}
					}
				}
				Thread.sleep(UPDATE_IN_MINUTES * 60 * 1000);
			}
		} catch (InterruptedException ie) {
			LogService.writeMessage("ServerTracker interrupted");
			return;
		}
	}

	private void setupServers(List<ServerDTO> servers) {
		_db.turnAllServerUpdateOff();
		// now we need to turn ServerRevision.shouldUpdate to true for every server on the list
		Iterator it = servers.iterator();
		while (it.hasNext()) {
			ServerDTO server = (ServerDTO) it.next();
			ServerRevision sr = _db.getServerRevisionByAddress(server.getServerAddress());
			if (sr == null) {
				sr = new ServerRevision();
				sr.setServerAddress(server.getServerAddress());
				sr.setServerUsername(server.getServerUsername());
				sr.setServerPassword(server.getServerPassword());
				sr.setShouldUpdate(true);
				sr.setVersionControlType(server.getServerType());
				_db.addServerRevision(sr);
			} else {
				_db.turnServerUpdateOn(sr);
			}
		}
	}

	private SVNLogEntry getSvnLatestRevisionHistory(ServerRevision server) {
		// check server protocol
		if (!server.getServerAddress().startsWith("http://")
				&& !server.getServerAddress().startsWith("https://")
				&& !server.getServerAddress().startsWith("svn://")
				&& !server.getServerAddress().startsWith("svn+ssh://")
				&& !server.getServerAddress().startsWith("file:///")) {
			System.out.printf("Server URL should start with protocol. Valid protocols are %s,%s,%s and %s%n",
					"http", "https", "svn", "svn+ssh");
			LogService.writeMessage("Wrong protocol for " + server.getServerAddress());
			return null;
		}
		try {
			if (server.getServerAddress().startsWith("http://") || server.getServerAddress().startsWith("https://")) {
				DAVRepositoryFactory.setup();
			} else if (server.getServerAddress().startsWith("svn://") || server.getServerAddress().startsWith("svn+ssh://")) {
				SVNRepositoryFactoryImpl.setup();
			}
			SVNURL svnUrl = SVNURL.parseURIDecoded(server.getServerAddress());
			LogService.writeMessage("Connecting to " + server.getServerAddress() + "...");
			SVNRepository svnRepository = SVNRepositoryFactory.create(svnUrl, null);
			ISVNAuthenticationManager _auth = SVNWCUtil.createDefaultAuthenticationManager(server.getServerUsername(),
					server.getServerPassword());
			svnRepository.setAuthenticationManager(_auth);
			Collection latestLogEntry = svnRepository.log(new String[]{""},
					null, -1, -1, true, true);
			SVNLogEntry latest = null;
			Iterator it = latestLogEntry.iterator();
			while (it.hasNext()) {
				SVNLogEntry entry = (SVNLogEntry) it.next();
				if (latest == null || latest.getRevision() < entry.getRevision()) {
					latest = entry;
				}
			}
			return latest;
		} catch (SVNException ex) {
			LogService.getLogger(SvnChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
		return null;
	}
}
