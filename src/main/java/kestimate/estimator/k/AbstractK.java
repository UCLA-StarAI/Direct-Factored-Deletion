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

import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntList;
import il2.util.IntMap;
import il2.util.IntSet;
import kestimate.data.DataSet;
import kestimate.estimator.em.Prior;
import kestimate.util.TimeoutException;

public abstract class AbstractK {

	public final DataSet dataSet;
	
	public AbstractK(DataSet data) {
		this.dataSet = data;
	}

	public Table[] estimate(BayesianNetwork bn, Prior prior, long deadline) {
		Table[] cpts = bn.cpts();
		Table[] estimated = new Table[cpts.length];
		for (int x = 0; x < cpts.length; x++) {
			if(System.currentTimeMillis()>deadline) throw new TimeoutException(deadline);
			Table cpt = cpts[x];
			IntSet parents = new IntSet(cpt.vars());
			parents.remove(x);
			estimated[x] = estimate(x, parents, prior, cpt, deadline);
		}
		return estimated;
	}

	public Table estimate(int x, IntSet parents, Prior prior, Table table, long deadline) {
		// fill table with prior
		double[] vals = prior.getTablePsis(x).clone();
		for (int i = 0; i < vals.length; i++) {
			vals[i] -= 1;
		}
		// add posterior
		int[] state = new int[table.vars().size()];
		do{
			if(System.currentTimeMillis()>deadline) throw new TimeoutException(deadline);
			IntMap stateMap = new IntMap(table.vars(),new IntList(state));
			double estimatexy = estimate(stateMap, null, table.domain());
			int index = table.getIndexFromFullInstance(state);
			// instead of nbInstances, we should here have something like number of observed instances
			vals[index] += dataSet.size*estimatexy;
		}while(table.nextSafe(state) >= 0);
		Table normalizedTable = (new Table(table,vals)).makeCPT2(x); // probabilities need to add up to 1
		for(double entry: normalizedTable.values()){
			if(Double.isNaN(entry) || entry < 0 || entry>1) 
				throw new IllegalStateException("Table has entry " + entry);
		}
		return normalizedTable;
	}

	public abstract double estimate(IntMap state, IntMap externalCondition, Domain domain);

	// helper methods
	
	protected final boolean isObserved(IntSet variables, byte[] state){
		for(int i=0;i<variables.size();i++){
			if(state[variables.get(i)] < 0) return false;
		}
		return true;
	}

	
	/**
	 * True if subState agrees with state, including missingness.
	 */
	// accounts for 80% CPU time!!
	protected final boolean matches(IntMap subState, byte[] state){
		for(int i=0; i<subState.size();i++){
			int subStateVar = subState.key(i);
			int subStateVal = subState.value(i);
			if(state[subStateVar] != subStateVal){
				return false;
			}
		}
		return true;
	}

	/**
	 * True if subState is a subset of state, for some instantiation of the missing values in state.
	 */
	protected final boolean canMatch(IntMap subState, byte[] state){
		for(int i=0; i<subState.size();i++){
			int subStateVar = subState.key(i);
			int subStateVal = subState.value(i);
			if(state[subStateVar] != -1 && state[subStateVar] != subStateVal){
				return false;
			}
		}
		return true;
	}

}
