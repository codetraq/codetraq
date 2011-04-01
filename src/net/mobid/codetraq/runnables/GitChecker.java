/*
 * GitChecker.java
 *
 * This is a worker class whose purpose is to check changes on a git server.
 */
package net.mobid.codetraq.runnables;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import net.mobid.codetraq.VersionControlChecker;
import net.mobid.codetraq.VersionControlType;
import net.mobid.codetraq.persistence.ServerDTO;
import net.mobid.codetraq.persistence.ServerRevision;
import net.mobid.codetraq.persistence.UserDTO;
import net.mobid.codetraq.persistence.UserRevision;
import net.mobid.codetraq.utils.DbUtility;
import net.mobid.codetraq.utils.LogService;
import net.mobid.codetraq.utils.Utilities;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;

/**
 *
 * @author viper
 */
public class GitChecker extends VersionControlChecker implements Runnable {

	private FileRepository repo = null;

	private Git mGit = null;

	public GitChecker(ServerDTO server, UserDTO user, DbUtility db) {
		super(server, user, db);
	}

	public void run() {
		// we need to make sure that a project repository dir can be created or
		// already exists
		Utilities.createGitProjectDir(_server.getShortName());
		File gitProjectDir = new File("gitrepos/" + _server.getShortName());
		if (!gitProjectDir.exists()) {
			System.out.printf("Cannot create local repositories for server %s. Please check read/write access daemon directory.%n",
				_server.getServerAddress());
			return;
		}
		// try to perform clone if it's a fresh repo, otherwise do a pull
		File dotGitPath = new File("gitrepos/" + _server.getShortName() + "/.git");
		if (!dotGitPath.exists()) {
			clone("gitrepos/" + _server.getShortName());
		} else {
			pull("gitrepos/" + _server.getShortName() + "/.git");
		}
		try {
			compareLatestRevisionHistory();
		} catch (Exception ex) {
			LogService.getLogger(SvnChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}

	@Override
	public boolean compareLatestRevisionHistory() throws Exception {
		checkServerInUserRecord();
		UserRevision ur = _db.getUserLatestRevision(_server.getServerAddress(), _user.getId());
		ServerRevision sr = _db.getServerRevisionByAddress(_server.getServerAddress());
		if (sr == null) {
			throw new Exception("Cannot find server " + _server.getServerAddress() +
				" in the server revision list.");
		}
		if (sr.getVersionControlType() == VersionControlType.GIT) {
			if (ur.getLastRevisionId() == null || (!ur.getLastRevisionId().equals(sr.getLastRevisionId()))) {
				// update the UserRevision object
				ur.setLastRevisionId(sr.getLastRevisionId());
				_db.updateUserLatestRevision(ur);
				sendRevisionMessage(sr);
				return true;
			}
		}
		return false;
	}

	private void pull(String path) {
		LogService.writeMessage("GitChecker is trying to do a pull from " +
			_server.getServerAddress());
		System.out.printf("GitChecker is trying to do a pull from %s%n", _server.getServerAddress());
		try {
			File gitDir = new File(path);
			repo = new FileRepository(gitDir);
			if (mGit == null) {
			 mGit = new Git(repo);
			}
			if (mGit.getRepository().getFullBranch() == null ||
				Utilities.isHexString(mGit.getRepository().getFullBranch())) {
				attachHead(mGit, _server.getServerBranch());
			}
			PullCommand puller = mGit.pull();
			puller.setTimeout(60);
			puller.setProgressMonitor(new TextProgressMonitor());
			PullResult pullResult = puller.call();
			if (pullResult != null) {
				LogService.writeMessage("GitChecker has something to pull from " +
					_server.getServerAddress());
				FetchResult result = pullResult.getFetchResult();
				if (result.getTrackingRefUpdates().isEmpty()) {
					return;
				}
				showFetchResult(result);
			} else {
				LogService.writeMessage("GitChecker did not find anything to pull from " +
					_server.getServerAddress());
			}
		} catch(Exception ex) {
			LogService.getLogger(GitChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}

	private void clone(String path) {
		LogService.writeMessage("GitChecker is trying to do a clone from " +
			_server.getServerAddress());
		System.out.printf("GitChecker is trying to do a clone from %s%n", _server.getServerAddress());
		try {
			File gitDir = new File(path);
			CloneCommand cloner = new CloneCommand();
			cloner.setBare(false);
			cloner.setDirectory(gitDir);
			cloner.setProgressMonitor(new TextProgressMonitor());
			cloner.setRemote("origin");
			cloner.setURI(_server.getServerAddress());
			mGit = cloner.call();
			// for some reason, repository cloned with jgit always has HEAD detached.
			// we need to create a "temporary" branch, then create a "master" branch.
			// we then merge the two...
			if (!isMasterBranchDefined(mGit.getRepository())) {
				// save the remote and merge config values
				mGit.getRepository().getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION,
					_server.getServerBranch(), ConfigConstants.CONFIG_KEY_REMOTE, "origin");
				mGit.getRepository().getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION,
					_server.getServerBranch(), ConfigConstants.CONFIG_KEY_MERGE, _server.getServerBranch());
				mGit.getRepository().getConfig().save();
			}
			if (mGit.getRepository().getFullBranch() == null ||
				Utilities.isHexString(mGit.getRepository().getFullBranch())) {
				// HEAD is detached and we need to reattach it
				attachHead(mGit, _server.getServerBranch());
			}
		} catch(Exception ex) {
			LogService.getLogger(GitChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}

	private boolean isMasterBranchDefined(Repository r) {
		if (r.getConfig().getString(ConfigConstants.CONFIG_BRANCH_SECTION,
			_server.getServerBranch(), ConfigConstants.CONFIG_KEY_REMOTE) == null) {
			return false;
		}
		return true;
	}

	private void attachHead(Git g, String branch) {
		LogService.writeMessage("Trying to attach HEAD to " + g.getRepository().toString());
		try {
			CheckoutCommand temp = g.checkout();
			temp.setCreateBranch(true);
			temp.setName("temp");
			Ref tRef = temp.call();
			CheckoutCommand b = g.checkout();
			b.setName(branch);
			b.setCreateBranch(true);
			b.call();
			MergeCommand merge = g.merge();
			merge.include(tRef);
			merge.call();
		} catch (Exception ex) {
			LogService.getLogger(GitChecker.class.getName()).log(Level.SEVERE, null, ex);
			LogService.writeLog(Level.SEVERE, ex);
		}
	}

	protected void showFetchResult(final FetchResult r) {
		ObjectReader reader = repo.newObjectReader();
		PrintWriter out = new PrintWriter(System.out);
		try {
			boolean shownURI = false;
			for (final TrackingRefUpdate u : r.getTrackingRefUpdates()) {
				if (u.getResult() == RefUpdate.Result.NO_CHANGE) {
					continue;
				}
				final char type = shortTypeOf(u.getResult());
				final String longType = longTypeOf(reader, u);
				final String src = abbreviateRef(u.getRemoteName(), false);
				final String dst = abbreviateRef(u.getLocalName(), true);

				if (!shownURI) {
					out.println("jGIT::from " + r.getURI());
					shownURI = true;
				}

				out.format(" %c %-17s %-10s -> %s", type, longType, src, dst);
				out.println();
			}
		} finally {
			reader.release();
		}
		showRemoteMessages(r.getMessages());
	}

	static void showRemoteMessages(String pkt) {
		PrintWriter writer = new PrintWriter(System.err);
		while (0 < pkt.length()) {
			final int lf = pkt.indexOf('\n');
			final int cr = pkt.indexOf('\r');
			final int s;
			if (0 <= lf && 0 <= cr)
				s = Math.min(lf, cr);
			else if (0 <= lf)
				s = lf;
			else if (0 <= cr)
				s = cr;
			else {
				writer.print("jGIT::remote reply: " + pkt);
				writer.println();
				break;
			}

			if (pkt.charAt(s) == '\r') {
				writer.print("jGIT::remote reply: " + pkt.substring(0, s));
				writer.print('\r');
			} else {
				writer.print("jGIT::remote reply: " + pkt.substring(0, s));
				writer.println();
			}

			pkt = pkt.substring(s + 1);
		}
		writer.flush();
	}

	private String longTypeOf(ObjectReader reader, final TrackingRefUpdate u) {
		final RefUpdate.Result r = u.getResult();
		if (r == RefUpdate.Result.LOCK_FAILURE)
			return "[lock fail]";
		if (r == RefUpdate.Result.IO_FAILURE)
			return "[i/o error]";
		if (r == RefUpdate.Result.REJECTED)
			return "[rejected]";
		if (ObjectId.zeroId().equals(u.getNewObjectId()))
			return "[deleted]";

		if (r == RefUpdate.Result.NEW) {
			if (u.getRemoteName().startsWith(Constants.R_HEADS))
				return "[new branch]";
			else if (u.getLocalName().startsWith(Constants.R_TAGS))
				return "[new tag]";
			return "[new]";
		}

		if (r == RefUpdate.Result.FORCED) {
			final String aOld = safeAbbreviate(reader, u.getOldObjectId());
			final String aNew = safeAbbreviate(reader, u.getNewObjectId());
			return aOld + "..." + aNew;
		}

		if (r == RefUpdate.Result.FAST_FORWARD) {
			final String aOld = safeAbbreviate(reader, u.getOldObjectId());
			final String aNew = safeAbbreviate(reader, u.getNewObjectId());
			return aOld + ".." + aNew;
		}

		if (r == RefUpdate.Result.NO_CHANGE)
			return "[up to date]";
		return "[" + r.name() + "]";
	}

	private String safeAbbreviate(ObjectReader reader, ObjectId id) {
		try {
			return reader.abbreviate(id).name();
		} catch (IOException cannotAbbreviate) {
			return id.name();
		}
	}

	String abbreviateRef(String dst, boolean abbreviateRemote) {
		if (dst.startsWith(Constants.R_HEADS))
			dst = dst.substring(Constants.R_HEADS.length());
		else if (dst.startsWith(Constants.R_TAGS))
			dst = dst.substring(Constants.R_TAGS.length());
		else if (abbreviateRemote && dst.startsWith(Constants.R_REMOTES))
			dst = dst.substring(Constants.R_REMOTES.length());
		return dst;
	}

	private static char shortTypeOf(final RefUpdate.Result r) {
		if (r == RefUpdate.Result.LOCK_FAILURE)
			return '!';
		if (r == RefUpdate.Result.IO_FAILURE)
			return '!';
		if (r == RefUpdate.Result.NEW)
			return '*';
		if (r == RefUpdate.Result.FORCED)
			return '+';
		if (r == RefUpdate.Result.FAST_FORWARD)
			return ' ';
		if (r == RefUpdate.Result.REJECTED)
			return '!';
		if (r == RefUpdate.Result.NO_CHANGE)
			return '=';
		return ' ';
	}
	
}
