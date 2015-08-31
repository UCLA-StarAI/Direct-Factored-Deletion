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
import il2.model.Table;

import java.util.Random;

import kestimate.data.DataSet;
import kestimate.util.JointEngineFactory;
import kestimate.util.TimeoutException;

public class RestartEMEstimator extends AbstractEMEstimator {

	private final int numStarts;

	public RestartEMEstimator(JointEngineFactory jointEngineFactory,int numStarts, boolean anytime) {
		super(jointEngineFactory,anytime);
		// I want to add a prior parameter here, but the Prior class depends on a specific BN
		this.numStarts = numStarts;
	}

	@Override
	public BayesianNetwork estimate(BayesianNetwork bn, Prior prior, DataSet data, long timeout) {
		
		BayesianNetwork bestModel = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		
		//EM with random restarts
		for(int i=0;i<numStarts;i++){
			
			System.out.println("Starting EM time " + (i+1)+"/"+numStarts);
			BayesianNetwork learnedBn;
			try {
				learnedBn = estimateOnce(bn, prior, data, timeout);
			} catch (TimeoutException e) {
				if(bestModel==null || !anytime) throw e;
				else return bestModel;
			}
			double logLikelihood = (data.logLikelihood(learnedBn,jointEngineFactory) 
										+ prior.logPrior(learnedBn))/data.size;
			System.out.printf(" estimated logLikelihood: %f\n",logLikelihood);
			
			if(logLikelihood > bestScore){
				bestScore = logLikelihood;
				bestModel = learnedBn;
			}
		}
		
		return bestModel;
	}

	protected BayesianNetwork getSeed(BayesianNetwork bn, Prior prior, DataSet data, long timeout) {
		Table[] seedTables = EmUtil.randomNetwork(bn,new Random());
		BayesianNetwork seedBn = new BayesianNetwork(seedTables);
		return seedBn;
	}
	
	@Override
	public String toString() {
		return "EM-"+numStarts+"-"+jointEngineFactory;
	}

}
