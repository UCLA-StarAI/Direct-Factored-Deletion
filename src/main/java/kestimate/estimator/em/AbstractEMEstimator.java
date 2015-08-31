/*
 * Copyright 2015 Guy Van den Broeck and Arthur Choi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kestimate.estimator.em;

import il2.model.BayesianNetwork;
import kestimate.data.DataSet;
import kestimate.estimator.IEstimator;
import kestimate.util.JointEngineFactory;

public abstract class AbstractEMEstimator implements IEstimator {


	protected final JointEngineFactory jointEngineFactory;
	protected final boolean anytime;

	public AbstractEMEstimator(JointEngineFactory jointEngineFactory, boolean anytime) {
		// I want to add a prior parameter here, but the Prior class depends on a specific BN
		this.jointEngineFactory = jointEngineFactory;
		this.anytime = anytime;
	}

	@Override
	public abstract BayesianNetwork estimate(BayesianNetwork bn, Prior prior, DataSet data, long timeout);

	protected BayesianNetwork estimateOnce(BayesianNetwork bn, Prior prior,
			DataSet data, long timeout) {
		BayesianNetwork seedBn = getSeed(bn,prior,data,timeout);
		// logger
		Options opts = new Options();
		opts.setTimeout(timeout);
		opts.setJointEngineFactory(jointEngineFactory);
		opts.setAnytime(anytime);
		Task task = new Task(seedBn,data.instancesArray(),data.countsArray(),prior);
		//	        task.setLogger(writer,"EM");
		EM em = new EM(opts,task);
		BayesianNetwork learnedBn = em.em();
		System.out.println("  iterations: " + em.iterations);
		return learnedBn;
	}

	protected abstract BayesianNetwork getSeed(BayesianNetwork bn, Prior prior, DataSet data, long timeout);

}
