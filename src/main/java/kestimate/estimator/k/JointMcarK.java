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

package kestimate.estimator.k;

import il2.model.Domain;
import il2.util.IntMap;
import il2.util.IntSet;
import kestimate.data.DataSet;

public class JointMcarK extends AbstractK{

	public final int settings;
	private final AbstractK backupEstimator;
	
	public JointMcarK(DataSet data, int settings, AbstractK backupEstimator) {
		super(data);
		this.settings = settings;
		this.backupEstimator = backupEstimator;
//		System.out.println("Number of MCAR instances: " + dataSet.numUniqueInstances());
	}
	
	@Override
	public double estimate(IntMap state, IntMap externalCondition, Domain domain) {
		if(state.size()==0) return 1; // query is empty
		int nbObserved = 0;
		int nbMatching = 0;
		IntSet vars = state.keys();
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			if(isObserved(vars,trainingInstance)){
				int count = dataSet.counts[i];
				nbObserved += count;
				if(matches(state, trainingInstance)){
					nbMatching += count;
				}
			}
		}
		if(nbObserved > 0) return nbMatching*1.0/nbObserved;
		else {
			// do something sensible here?
			// we should also check if x_m is even possible...
			if(settings == 0){
				return 0; 
			}else if(settings == 1){
				return 1.0 / domain.size(state.keys());
			}else if(settings == 2){
				// estimate fraction of data that can have state for some way to complete the data
				for(int i=0;i<dataSet.numUniqueInstances();i++){
					byte[] trainingInstance = dataSet.instances[i];
					int count = dataSet.counts[i];
					nbObserved += count;
					if(canMatch(state, trainingInstance)){
						nbMatching += count;
					}
				}
				return (nbMatching*1.0/nbObserved)/domain.size(state.keys());
			}else if(settings == 4){
				return Double.NaN;
			}else if(settings == 5){
				IntMap extJointState = externalCondition.combine(state);
				return backupEstimator.estimate(extJointState, null, domain)
											/backupEstimator.estimate(externalCondition, null, domain);
			}else{
				throw new IllegalArgumentException("No setting " + settings);
			}
		}
	}
	
	public static class Factory extends KFactory {

		protected final int settings;

		public Factory(int settings) {
			this.settings = settings;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new JointMcarK(data,settings,backupEstimator);
		}

		@Override
		public String toString() {
			return "McarK(Joint,"+settings+")";
		}
		
	}

}
