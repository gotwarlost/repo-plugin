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

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
//CS IGNORE LineLength FOR NEXT 1 LINES. REASON: class name too long
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters.DontTriggerException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * contributes a build parameter to the parameterized trigger plugin.
 * Code closely follows the implementation in the git plugin.
 */
public class RepoRevisionBuildParameters extends AbstractBuildParameters {

	private final boolean combineQueuedCommits;

	/**
	 * create a build parameters object.
	 * @param combineQueuedCommits indicates if queued commits should be
	 *							   combined
	 */
	@DataBoundConstructor
	public RepoRevisionBuildParameters(final boolean combineQueuedCommits) {
		this.combineQueuedCommits = combineQueuedCommits;
	}

	@Override
	public Action getAction(final AbstractBuild<?, ?> abstractBuild,
							final TaskListener taskListener)
	throws IOException, InterruptedException, DontTriggerException {

		final RevisionState state =
				abstractBuild.getAction(RevisionState.class);

		if (state == null) {
			taskListener.getLogger().println(
					"This project doesn't use repo as SCM."
						+ "Can't pass the revision/ manifest to downstream");
			return null;
		}
		return new RevisionParameterAction(state, combineQueuedCommits);
	}

	/**
	 * Descriptor for this class.
	 */
	@Extension(optional = true)
	public static class DescriptorImpl
			extends Descriptor<AbstractBuildParameters> {
		@Override
		public String getDisplayName() {
			return "Pass-through repo revision and manifest";
		}
	}
}


