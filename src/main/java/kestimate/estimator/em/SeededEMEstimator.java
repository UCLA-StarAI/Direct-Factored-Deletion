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

public class SeededEMEstimator extends AbstractEMEstimator {

	private final IEstimator seeder;

	public SeededEMEstimator(JointEngineFactory jointEngineFactory, IEstimator seeder, boolean anytime) {
		super(jointEngineFactory,anytime);
		// I want to add a prior parameter here, but the Prior class depends on a specific BN
		this.seeder = seeder;
	}

	@Override
	public BayesianNetwork estimate(BayesianNetwork bn, Prior prior, DataSet data, long timeout) {
		return estimateOnce(bn, prior, data, timeout);
	}

	protected BayesianNetwork getSeed(BayesianNetwork bn, Prior prior, DataSet data, long timeout) {
		return seeder.estimate(bn, prior, data, timeout);
	}
	
	@Override
	public String toString() {
		return seeder+"+EM"+"-"+jointEngineFactory;
	}

}
