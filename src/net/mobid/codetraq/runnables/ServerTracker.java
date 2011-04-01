/*
 * ServerTracker.java
 *
 * This is a worker class that tracks and updates the revision history of a
 * server. Set an update time that is convenient for your own circumnstance.
 */
package net.mobid.codetraq.runnables;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.mobid.codetraq.VersionControlType;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
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
					if (sr.shoudlUpdate() && sr.getMinutesSinceLastCheck() >= UPDATE_IN_MINUTES) {
						if (sr.getVersionControlType() == VersionControlType.SVN) {
							SVNLogEntry latestLogEntry = getSvnLatestRevisionHistory(sr);
							if (latestLogEntry != null && latestLogEntry.getRevision() > sr.getLastRevision()) {
								LogService.writeMessage("Found latest revision for "
										+ sr.getServerAddress() + " with timestamp "
										+ latestLogEntry.getDate().getTime());
								sr.setLastCheckedTimestamp(System.currentTimeMillis());
								// revision-related changes
								sr.setLastMessage(latestLogEntry.getMessage());
								sr.setLastRevision(latestLogEntry.getRevision());
								sr.setLastAuthor(latestLogEntry.getAuthor());
								sr.setLastCommitter(latestLogEntry.getAuthor());
								sr.setLastRevisionTimestamp(latestLogEntry.getDate().getTime());
								sr.clearFiles();
								if (latestLogEntry.getChangedPaths().size() > 0) {
									Iterator iterator = latestLogEntry.getChangedPaths().keySet().iterator();
									while (iterator.hasNext()) {
										SVNLogEntryPath ep = (SVNLogEntryPath) latestLogEntry.getChangedPaths().get(iterator.next());
										StringBuilder sb = new StringBuilder();
										sb.append(ep.getType()).append(" ").append(ep.getPath());
										sr.addModifiedFile(sb.toString());
									}
								}
								_db.updateServerLatestRevision(sr);
							}
						} else if (sr.getVersionControlType() == VersionControlType.GIT) {
							RevCommit latestLogEntry = getGitLatestRevisionHistory(sr);
							long latestCommitTstamp = ((long) latestLogEntry.getCommitTime()) * 1000;
							if (latestLogEntry != null && (latestCommitTstamp > sr.getLastRevisionTimestamp())) {
								LogService.writeMessage("Found latest revision for "
										+ sr.getServerAddress() + " with timestamp "
										+ latestCommitTstamp);
								sr.setLastCheckedTimestamp(System.currentTimeMillis());
								// revision-related changes
								sr.setLastMessage(latestLogEntry.getFullMessage());
								sr.setLastRevisionId(latestLogEntry.getId().getName());
								sr.setLastAuthor(latestLogEntry.getAuthorIdent().getName() + " ("
										+ (latestLogEntry.getAuthorIdent().getEmailAddress().length() > 0
										? latestLogEntry.getAuthorIdent().getEmailAddress() : "empty email") + ")");
								sr.setLastCommitter(latestLogEntry.getCommitterIdent().getName()
										+ (latestLogEntry.getCommitterIdent().getEmailAddress().length() > 0
										? latestLogEntry.getCommitterIdent().getEmailAddress() : "empty email") + ")");
								sr.setLastRevisionTimestamp(latestCommitTstamp);
								List<String> changedFiles = getChangedFiles(sr);
								sr.clearFiles();
								// add new files
								for (String m : changedFiles) {
									sr.addModifiedFile(m);
								}
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
				sr.setServerShortName(server.getShortName());
				sr.setVersionControlType(server.getServerType());
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

	private RevCommit getGitLatestRevisionHistory(ServerRevision sr) {
		try {
			File gitPath = new File("gitrepos/" + sr.getServerShortName() + "/.git");
			Repository repo = new FileRepository(gitPath);
			RevCommit[] commits = getCommits(1, repo);
			return commits[0];
		} catch (Exception ex) {
			LogService.getLogger(ServerTracker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
		return null;
	}

	private List<String> getChangedFiles(ServerRevision sr) {
		List<String> modifiedFiles = new ArrayList<String>();
		try {
			File gitPath = new File("gitrepos/" + sr.getServerShortName() + "/.git");
			Repository repo = new FileRepository(gitPath);
			String cr = System.getProperty("line.separator");
			DiffFormatter df = new DiffFormatter(new ByteArrayOutputStream());
			RevCommit[] commits = getCommits(2, repo);
			if (commits.length == 2) {
				RevTree aTree = commits[0].getTree();
				RevTree bTree = commits[1].getTree();
				df.setRepository(repo);
				List<DiffEntry> changed = df.scan(aTree, bTree);
				StringBuilder sb = new StringBuilder();
				for (DiffEntry entry : changed) {
					sb.delete(0, sb.length());
					switch (entry.getChangeType()) {
						case ADD:
							sb.append("A ").append(entry.getNewPath()).append(cr);
							break;
						case DELETE:
							sb.append("D ").append(entry.getOldPath()).append(cr);
							break;
						case MODIFY:
							sb.append("M ").append(entry.getNewPath());
							break;
						case COPY:
							sb.append("[Copied] from ").append(entry.getOldPath()).append(" to ").append(entry.getNewPath());
							break;
						case RENAME:
							sb.append("[Renamed] from ").append(entry.getOldPath()).append(" to ").append(entry.getNewPath());
							break;
					}
					if (sb.toString().length() > 0) {
						modifiedFiles.add(sb.toString());
					}
				}
			}
		} catch (IOException ex) {
			LogService.getLogger(ServerTracker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
		return modifiedFiles;
	}

	private RevCommit[] getCommits(int howManyCommits, Repository repo) {
		if (howManyCommits == 0) {
			return null;
		}
		RevWalk rw = new RevWalk(repo);
		for (Ref ref : repo.getAllRefs().values()) {
			try {
				rw.markStart(rw.parseCommit(ref.getObjectId()));
			} catch (Exception notACommit) {
				continue;
			}
		}
		Comparator<RevCommit> commitTimeComparator = new Comparator<RevCommit>() {

			public int compare(RevCommit o1, RevCommit o2) {
				if (o1.getCommitTime() == o2.getCommitTime()) {
					return 0;
				} else if (o1.getCommitTime() > o2.getCommitTime()) {
					return 1;
				}
				return -1;
			}
		};
		ArrayList<RevCommit> commits = new ArrayList<RevCommit>();
		for (RevCommit commit : rw) {
			commits.add(commit);
		}
		Collections.sort(commits, commitTimeComparator);
		Collections.reverse(commits);
		RevCommit[] c = null;
		if (howManyCommits > commits.size()) {
			c = commits.toArray(new RevCommit[howManyCommits]);
		} else {
			c = new RevCommit[howManyCommits];
			for (int i = 0; i
					< howManyCommits; i++) {
				c[i] = commits.get(i);
			}
		}
		rw.dispose();
		return c;
	}
}
