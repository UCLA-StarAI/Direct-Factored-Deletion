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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kestimate.data.DataSet;
import kestimate.util.Estimate;
import kestimate.util.IntMapWrapper;
import kestimate.util.RunningStats;

public abstract class FactorMcarK extends AbstractK{

	private final static Estimate uninformed = new Estimate(0,Double.POSITIVE_INFINITY);
	private final static Estimate certainlyOne = new Estimate(1,0); 
	private final static Estimate unestimable = new Estimate(Double.NaN,Double.NaN); 
	
	
	public final int settings;
	
	private final Map<IntMapWrapper,Estimate> cachedJoints = new HashMap<IntMapWrapper,Estimate>();
	private final AbstractK backupEstimator;
	
	public FactorMcarK(DataSet data, int settings, AbstractK backupEstimator) {
		super(data);
		this.settings = settings;
		this.backupEstimator = backupEstimator;
	}
	
	@Override
	public double estimate(IntMap state, IntMap externalCondition, Domain domain) {
//		System.out.println("Number of cached joints: " + cachedJoints.size());
		return estimateWithVariance(state,externalCondition,domain).mean;
	}
	
	public final Estimate estimateWithVariance(IntMap state, IntMap externalCondition, Domain domain) {
		if(state.size()==0) return certainlyOne; // query is empty, base case, bottom of lattice
		
		IntMapWrapper stateWrapper = new IntMapWrapper(state);
		Estimate cachedJoint = lookup(stateWrapper);
		if(cachedJoint != null) return cachedJoint;
		
		IntSet vars = state.keys();
		int nbVars = vars.size();
		List<Estimate> estimates = new ArrayList<Estimate>(nbVars);
		for(int i=0;i<nbVars;i++){
			int factoredVar = vars.get(i);
			IntMap parentState = getParentState(state, factoredVar);
			Estimate condProb = estimateConditional(factoredVar,state.get(factoredVar),state,
													parentState,externalCondition,domain);
			if(condProb == unestimable){
				cache(stateWrapper, unestimable);
				return unestimable;
			}
			Estimate marginalProb = estimateWithVariance(parentState, externalCondition,domain);
			if(marginalProb == unestimable) throw new IllegalStateException("If conditional is estimable, then so is its parent.");
			Estimate multiplied = condProb.multiply(marginalProb);
			estimates.add(multiplied);
		}
		Estimate aggregateEstimate = aggregate(estimates);
		cache(stateWrapper, aggregateEstimate);
		return aggregateEstimate;
	}

	// extracted for profiling
	private final IntMap getParentState(IntMap state, int factoredVar) {
		IntMap parentState = new IntMap(state);
		parentState.remove(factoredVar);
		return parentState;
	}

	// extracted for profiling
	private final void cache(IntMapWrapper stateWrapper, Estimate aggregateEstimate) {
		cachedJoints.put(stateWrapper,aggregateEstimate);
	}

//	int hit = 0;
//	int miss = 0;
	
	// extracted for profiling
	private final Estimate lookup(IntMapWrapper stateWrapper) {
		Estimate estimate = cachedJoints.get(stateWrapper);
//		if(estimate ==null) ++miss;
//		else ++hit;
//		if((hit+miss)%100==0) System.out.println(hit+"/"+miss);
		return estimate;
	}
	
	
	public abstract Estimate aggregate(List<Estimate> estimates);
	
	public Estimate estimateConditional(int queryVar, int queryState, IntMap jointstate, 
										IntMap condition, IntMap externalCondition, Domain domain){
		// no point in caching: all queries happen once
		RunningStats stats = new RunningStats();
		for(int i=0;i<dataSet.numUniqueInstances();i++){
			byte[] trainingInstance = dataSet.instances[i];
			if(matches(condition, trainingInstance) && trainingInstance[queryVar] != -1){
				int count = dataSet.counts[i];
				if(trainingInstance[queryVar] == queryState){
					stats.add(1.0, count);
				}else{
					stats.add(0.0, count);
				}
			}
		}
		if(stats.getNbSamples()>0){
			double mean = stats.getMean();
			double standardErrorOfMean = stats.getEVVar();
			if(standardErrorOfMean == 0){
				// cannot have zero variance!
				// add a fake data point on the other side, adding variance 1
				standardErrorOfMean = 1.0/(stats.getNbSamples()-1);
			}else if(Double.isNaN(standardErrorOfMean)){
				// we only had one sample, so assume a variance of 1/2
				standardErrorOfMean = 0.5;
			}
			return new Estimate(mean, standardErrorOfMean);
		}else {
			// do something sensible here?
			if(settings == 0){
				return new Estimate(0,1*1);
			}else if(settings == 1){
				// Assume a Bernoulli distribution.
				double p = 1.0/domain.size(queryVar);
				return new Estimate(p,p*(1-p));
			}else if(settings == 2){
				if(condition.size()==0){
					// Assume a Bernoulli distribution.
					double p = 1.0/domain.size(queryVar);
					return new Estimate(p,p*(1-p));
				}else{
					// approximate by marginal without evidence...
					double p = estimate(new IntMap(new int[]{queryVar}, new int[]{queryState}), null, domain);
					return new Estimate(p,1);
				}
			}else if(settings == 3){
				// Assume a Bernoulli distribution.
				double p = 1.0/domain.size(queryVar);
				return new Estimate(p,Double.POSITIVE_INFINITY);
			}else if(settings == 4){
				return unestimable;
			}else if(settings == 5){
				IntMap extJointState = externalCondition.combine(jointstate);
				IntMap extCondState = externalCondition.combine(condition);
				double estimate2 = backupEstimator.estimate(extCondState, null, domain);
				if(estimate2==0) {
//					throw new IllegalStateException();
					double p = 1.0/domain.size(queryVar);
					return new Estimate(p,Double.POSITIVE_INFINITY);
				}
				double estimate1 = backupEstimator.estimate(extJointState, null, domain);
				return new Estimate(estimate1/estimate2, 0.5);
			}else{
				throw new IllegalArgumentException("No setting " + settings);
			}
		}
	}
	
	public static class AvgFactory extends KFactory {

		private final int settings;

		public AvgFactory(int settings) {
			this.settings = settings;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new FactorMcarK(data,settings,backupEstimator){
				
				@Override
				public Estimate aggregate(List<Estimate> estimates) {
					double totalMean = 0;
					for(Estimate estimate: estimates){
						totalMean += estimate.mean;
					}
					return new Estimate(totalMean/estimates.size(),0);
				}
				
			};
		}

		@Override
		public String toString() {
			return "McarK(Factor,Avg,"+settings+")";
		}
		
	}
	
	public static class MedianFactory extends KFactory {

		private final int settings;

		public MedianFactory(int settings) {
			this.settings = settings;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new FactorMcarK(data,settings,backupEstimator){
				
				@Override
				public Estimate aggregate(List<Estimate> estimates) {
				    double[] means = new double[estimates.size()];
				    int i=0;
					for(Estimate estimate: estimates){
						means[i++] = estimate.mean;
					}
					Arrays.sort(means);
					int middle = means.length/2;
				    if (means.length%2 == 1) {
						return new Estimate(means[middle],0);
				    } else {
						return new Estimate((means[middle-1]+means[middle])/2.0,0);
				    }
				}
				
			};
		}

		@Override
		public String toString() {
			return "McarK(Factor,Median,"+settings+")";
		}
		
	}
	
	public static class MetaFactory extends KFactory {

		private final int settings;

		public MetaFactory(int settings) {
			this.settings = settings;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new FactorMcarK(data,settings,backupEstimator){
				
				@Override
				public Estimate aggregate(List<Estimate> estimates) {
					Estimate total = uninformed;
					for(Estimate estimate: estimates){
						total = total.inverseVarianceWeighting(estimate);
					}
					return total;
				}
				
			};
		}

		@Override
		public String toString() {
			return "McarK(Factor,Meta,"+settings+")";
		}
		
	}
	
	public static class MaxFactory extends KFactory {

		private final int settings;
		
		public MaxFactory(int settings) {
			this.settings = settings;
		}

		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new FactorMcarK(data,settings,backupEstimator){
				
				@Override
				public Estimate aggregate(List<Estimate> estimates) {
					Estimate total = uninformed;
					for(Estimate estimate: estimates){
						if(total.variance > estimate.variance)
						total = estimate;
					}
					return total;
				}
				
			};
		}

		@Override
		public String toString() {
			return "McarK(Factor,Max,"+settings+")";
		}
		
	}
	
}
