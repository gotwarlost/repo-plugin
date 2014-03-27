/*
 * The MIT License
 *
 * Copyright (c) 2014, Krishnan Anantheswaran
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.repo;

import hudson.Util;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Queue;
import hudson.model.Queue.QueueAction;
import hudson.model.queue.FoldableAction;

import java.io.Serializable;
import java.util.List;


/**
 * Used as a build parameter to specify the revision to be built.
 * Code closely follows the implementation in the git plugin.
 */
public class RevisionParameterAction extends InvisibleAction
		implements Serializable, QueueAction, FoldableAction {

	private static final long serialVersionUID = 1L;

	private final String manifest;
	private final String manifestRevision;
	private final boolean combineCommits;

	/**
	 * constructs a parameterized action from a revision state.
	 * @param state the revision state
	 * @param combineCommits true to indicate that commits should be combined
	 */
	public RevisionParameterAction(final RevisionState state,
								   final boolean combineCommits) {
		this.manifest = state.getManifest();
		this.manifestRevision = state.getManifestRevision();
		this.combineCommits = combineCommits;
	}

	/**
	 * returns the local manifest associated with the revision state.
	 */
	public String getManifest() {
		return manifest;
	}

	/**
	 * returns the revision of the manifest as a SHA.
	 */
	public String getManifestRevision() {
		return manifestRevision;
	}

	@Override
	public String toString() {
		return super.toString()
				+ "[rev=" + manifestRevision + ";manifest=" + manifest + "]";
	}

	/**
	 * Returns whether the new item should be scheduled.
	 * An action should return true if the associated task is
	 * 'different enough' to warrant a separate execution.
	 * from {@link QueueAction}
	 * @param actions the list of actions
	  */
	public boolean shouldSchedule(final List<Action> actions) {
		/* Called in two cases
		1. On the action attached to an existing queued item
		2. On the action attached to the new item to add.
		Behaviour
		If actions contain a RevisionParameterAction with a matching commit
		to this one, we do not need to schedule
		in all other cases we do.
		*/
		final List<RevisionParameterAction> otherActions =
				Util.filter(actions, RevisionParameterAction.class);
		if (combineCommits) {
			// we are combining commits so we never need to schedule
			// another run.
			// unless other job does not have a RevisionParameterAction
			// (manual build)
			if (otherActions.size() != 0) {
				return false;
			}
		} else {
			for (RevisionParameterAction action : otherActions) {
				if (this.manifest.equals(action.manifest)
						&& this.manifestRevision.equals(
							action.manifestRevision)) {
					return false;
				}
			}
		}
		// if we get to this point there were no matching actions
		// so a new build is required
		return true;
	}

	/**
	 * Folds this Action into another action already associated with item.
	 * from {@link FoldableAction}
	 * @param item the queue item
	 * @param owner the task owner
	 * @param otherActions the list of other actions with which ti potentially
	 *					   fold this one
	 */
	public void foldIntoExisting(final Queue.Item item, final Queue.Task owner,
								 final List<Action> otherActions) {
		// only do this if we are asked to.
		if (combineCommits) {
			final RevisionParameterAction existing =
					item.getAction(RevisionParameterAction.class);
			if (existing != null) {
				//because we cannot modify the commit in the existing action
				// remove it and add self
				item.getActions().remove(existing);
				item.getActions().add(this);
				return;
			}
			// no CauseAction found, so add a copy of this one
			item.getActions().add(this);
		}
	}

}

