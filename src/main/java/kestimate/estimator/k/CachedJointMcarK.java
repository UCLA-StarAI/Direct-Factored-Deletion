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

import java.util.HashMap;
import java.util.Map;

import kestimate.data.DataSet;
import kestimate.util.IntMapWrapper;

public class CachedJointMcarK extends JointMcarK{

	private final Map<IntMapWrapper,Double> cachedJoints = new HashMap<IntMapWrapper,Double>();
	
	public CachedJointMcarK(DataSet data, int setting, AbstractK backupEstimator) {
		super(data,setting, backupEstimator);
	}
	
	@Override
	public double estimate(IntMap state, IntMap externalCondition, Domain domain) {
		if(state.size()==0) return 1.0; // query is empty, base case, bottom of lattice
		
		IntMapWrapper stateWrapper = new IntMapWrapper(state);
		Double cachedJoint = lookup(stateWrapper);
		if(cachedJoint != null) return cachedJoint;
		
		double estimate = super.estimate(state, externalCondition, domain);
		cache(stateWrapper, estimate);
		return estimate;
	}

	// extracted for profiling
	private final void cache(IntMapWrapper stateWrapper, Double estimate) {
		cachedJoints.put(stateWrapper,estimate);
	}

	// extracted for profiling
	private final Double lookup(IntMapWrapper stateWrapper) {
		Double estimate = cachedJoints.get(stateWrapper);
//		if(estimate ==null) ++miss;
//		else ++hit;
//		if((hit+miss)%100==0) System.out.println(hit+"/"+miss);
		return estimate;
	}
	
	public static class Factory extends JointMcarK.Factory {

		public Factory(int settings) {
			super(settings);
		}

		@Override
		public CachedJointMcarK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new CachedJointMcarK(data,settings,backupEstimator);
		}

		@Override
		public String toString() {
			return "McarK(CachedJoint,"+settings+")";
		}
	}
	
}
