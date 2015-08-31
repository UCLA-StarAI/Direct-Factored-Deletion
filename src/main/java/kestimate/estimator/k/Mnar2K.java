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

public class Mnar2K extends AbstractK{

	private final int x;
	private final int y;
	private final IntSet ySet;
	private final IntSet xSet;

	public Mnar2K(final DataSet data, IntSet separator, long deadline) {
		super(data);
		this.x = separator.get(0);
		this.y = separator.get(1);
		this.ySet = new IntSet();
		ySet.add(y);
		this.xSet = new IntSet();
		xSet.add(x);
	}


	@Override
	public double estimate(IntMap state, IntMap externalCondition, Domain domain) {
		if(state.keys().contains(x)){
			return estimateWithX(state,domain);
		}else if(state.keys().contains(y)){
			double sum=0;
			for(int val = 0; val< domain.size(x); val++){
				IntMap state2 = new IntMap(state);
				state2.put(x, val);
				sum += estimateWithXY(state2,domain);
			}
			return sum;
		}else{
			return estimateMCAR(state,domain);
		}
	}

	public double estimateMCAR(IntMap state, Domain domain) {
		if(state.size()==0) return 1; // query is empty
		double nbObserved = 0;
		double nbMatching = 0;
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			int count = dataSet.counts[i];
			nbObserved += count;
			if(matches(state, trainingInstance)){
				nbMatching += count;
			}
		}
		if(nbObserved > 0) return nbMatching/nbObserved;
		else return 0; 
	}
	
	public double estimateWithX(IntMap state, Domain domain) {
		if(state.keys().contains(y)){
			return estimateWithXY(state,domain);
		}else{
			double sum=0;
			for(int val = 0; val< domain.size(y); val++){
				IntMap state2 = new IntMap(state);
				state2.put(y, val);
				sum += estimateWithXY(state2,domain);
			}
			return sum;
		}
	}
	
	public double estimateWithXY(IntMap state, Domain domain) {
		double denom = denomX(state)*denomY(state);
		if(denom>0){
			return numerator(state)/denom;
		}else{
			return 0.5;
		}
		
	}

	private double numerator(IntMap state) {
		if(state.size()==0) return 1; // query is empty
		final double nbObserved = dataSet.size;
		double nbMatching = 0;
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			int count = dataSet.counts[i];
			if(matches(state, trainingInstance)){
				nbMatching += count;
			}
		}
		if(nbObserved > 0) return nbMatching/nbObserved;
		else {
				return 0; 
		}
	}
	
	private double denomX(IntMap state) {
		int yval = state.get(y);
		double nbObserved = 0;
		double nbMatching = 0;
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			if(trainingInstance[y] == yval){
				int count = dataSet.counts[i];
				nbObserved += count;
				if(trainingInstance[x] != -1){
					nbMatching += count;
				}
			}
		}
		if(nbObserved > 0) return nbMatching/nbObserved;
		else return 1;
	}
	
	private double denomY(IntMap state) {
		int xval = state.get(x);
		double nbObserved = 0;
		double nbMatching = 0;
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			if(trainingInstance[x] == xval){
				int count = dataSet.counts[i];
				nbObserved += count;
				if(trainingInstance[y] != -1){
					nbMatching += count;
				}
			}
		}
		if(nbObserved > 0) return nbMatching/nbObserved;
		else return 1;
	}

	public static class Factory extends KFactory {


		public Factory() {
		}

		@Override
		public AbstractK create(DataSet data, long timeout,AbstractK backupEstimator) {
			if(data.separator == null){
				throw new IllegalArgumentException();
			}
			return new Mnar2K(data,data.separator,timeout);
		}

		@Override
		public String toString() {
			return "Mnar2K";
		}

	}

}
