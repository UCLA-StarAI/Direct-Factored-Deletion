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
import kestimate.data.DataSet;
import kestimate.estimator.IEstimator;
import kestimate.estimator.em.Prior;

public abstract class KFactory {

	public abstract AbstractK create(DataSet matchingDataSet, long deadline,
			AbstractK backupEstimator);

	public final AbstractK create(DataSet matchingDataSet,
			long deadline){
		return create(matchingDataSet,deadline,null);
	}
	
	public IEstimator estimator(){
		return new IEstimator() {
			
			@Override
			public BayesianNetwork estimate(BayesianNetwork bn, Prior prior, DataSet data, long timeout) {
				AbstractK kest = KFactory.this.create(data,timeout);
				return new BayesianNetwork(kest.estimate(bn,prior,timeout));
			}

			@Override
			public String toString() {
				return KFactory.this.toString();
			}
			
		};
	}
	
}
