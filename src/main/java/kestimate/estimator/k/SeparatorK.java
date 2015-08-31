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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kestimate.data.DataSet;
import kestimate.util.ByteArrayWrapper;
import kestimate.util.Global;
import kestimate.util.TimeoutException;

/**
 * Learns parameters given a set of fully observed variables that separate the R-variables from the rest.
 */
public class SeparatorK extends AbstractK{

	private final IntSet separator;
	private final ArrayList<SeparatorState> separatorStates;
	private final KFactory mcarFactory;
	private final AbstractK backupEstimator;

	public SeparatorK(final DataSet data, KFactory mcarFactory, KFactory defaultFactory, IntSet separator, long deadline) {
		super(data);
		this.mcarFactory = mcarFactory;
		backupEstimator = (defaultFactory!=null)? defaultFactory.create(data,deadline) : null;
		this.separator = separator;
		int[] separatorIndices = separator.toArray();
		System.out.print("Separator variables: ");
		for(int var:separatorIndices) System.out.print(var + " ");
		System.out.println();
		Map<ByteArrayWrapper,SeparatorState> observedStates = new HashMap<ByteArrayWrapper,SeparatorState>();
		for(int i=0;i<data.numUniqueInstances();i++){
			if(i%500 == 0 && System.currentTimeMillis()>deadline) throw new TimeoutException(deadline);
			byte[] instance = data.instances[i];
			byte[] keyContent = new byte[separatorIndices.length];
			for(int j=0;j<separatorIndices.length;j++){
				keyContent[j] = instance[separatorIndices[j]];
			}
			ByteArrayWrapper key = new ByteArrayWrapper(keyContent);
//			System.out.println("Instance: "+Arrays.toString(instance));
//			System.out.println("Observed: "+Arrays.toString(keyContent));
			SeparatorState obsState = observedStates.get(key);
			if(obsState == null) {
				obsState = new SeparatorState(instance);
				observedStates.put(key, obsState);
			}
			obsState.addInstances(instance,data.counts[i]);
		}
		this.separatorStates = new ArrayList<SeparatorState>(observedStates.values());
		System.out.println("Collapsed " + dataSet.numUniqueInstances() + " unique instances into " 
							+ observedStates.size() + " unique observed states");
		for(SeparatorState state: this.separatorStates) state.doneAddingInstances(deadline);
	}

	@Override
	public double estimate(IntMap state, IntMap externalCondition, Domain domain) {
		double sum = 0;
		IntMap x_o = state.subMap(separator.intersection(state.keys()));
		IntMap missingVarState = state.subMap(state.keys().diff(separator));
		double defaultConditional;
		defaultConditional =  (backupEstimator!=null)? 
				backupEstimator.estimate(state, null, domain)/backupEstimator.estimate(x_o, null, domain) : 0;
		for(SeparatorState observedState: separatorStates){
			if(observedState.matchesOnObserved(x_o)){
				// P(v_o) > 0
				double p_v_o = observedState.prob();
				double p_x_m_given_v_o;
				p_x_m_given_v_o = observedState.prob(missingVarState,x_o,domain);
				if(Double.isNaN(p_x_m_given_v_o)) p_x_m_given_v_o = defaultConditional;
				sum += p_x_m_given_v_o * p_v_o;
			}
		}
		return sum;
	}

	private class SeparatorState{

		private final byte[] representative;
		private final ArrayList<byte[]> matchingInstances = new ArrayList<byte[]>();
		private final ArrayList<Integer> matchingCounts = new ArrayList<Integer>(); 
		private AbstractK mcar = null;
		private DataSet matchingDataSet;

		public SeparatorState(byte[] representative) {
			this.representative = representative;
		}

		public void addInstances(byte[] instance, int c) {
			if(mcar != null) throw new IllegalStateException();
			matchingInstances.add(instance);
			matchingCounts.add(c);
		}
		
		public void doneAddingInstances(long deadline){
			if(mcar != null) throw new IllegalStateException();
			this.matchingDataSet = new DataSet(dataSet.numVars, 
					matchingInstances.toArray(new byte[matchingInstances.size()][]),
					Global.toIntArray(matchingCounts),false);
			mcar = mcarFactory.create(matchingDataSet,deadline,backupEstimator);
		}

		public boolean matchesOnObserved(IntMap instance) {
			for(int i=0;i<instance.size();i++){
				if(instance.value(i) != representative[instance.key(i)]) return false;
			}
			return true;
		}

		public double prob() {
			return matchingDataSet.size*1.0/dataSet.size;
		}

		public double prob(IntMap missingVarState, IntMap obsVarState, Domain domain) {
			return mcar.estimate(missingVarState, obsVarState, domain);
		}

	}

	public static class Factory extends KFactory {

		private final KFactory mcarFactory;
		private final KFactory defaultFactory;

		public Factory(KFactory mcarFactory,KFactory defaultFactory) {
			this.mcarFactory = mcarFactory;
			this.defaultFactory = defaultFactory;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			if(data.separator == null){
				throw new IllegalArgumentException();
			}
			return new SeparatorK(data,mcarFactory,defaultFactory,data.separator,timeout);
		}

		@Override
		public String toString() {
			return "SeparatorK("+mcarFactory+","+defaultFactory+")";
		}
		
	}
	
}
