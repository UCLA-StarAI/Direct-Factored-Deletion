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

import java.util.ArrayList;

import il2.util.IntSet;
import kestimate.data.DataSet;
import kestimate.util.Global;

/**
 * Learns parameters given a set of MCAR variables that separate the R-variables from the rest.
 */
public class MnarK extends SeparatorK{

	public MnarK(DataSet data, KFactory mcarFactory, KFactory defaultFactory, IntSet separator,
			long deadline) {
		super(data, mcarFactory, defaultFactory, separator, deadline);
	}

	public static class Factory extends KFactory {

		private final KFactory mcarFactory;
		private final KFactory defaultFactory;

		public Factory(KFactory mcarFactory, KFactory defaultFactory) {
			this.mcarFactory = mcarFactory;
			this.defaultFactory = defaultFactory;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout,AbstractK backupEstimator) {
			if(data.separator == null){
				throw new IllegalArgumentException();
			}
			DataSet filterObserved = filterObserved(data);
			System.out.println("MNAR filtering reduces number of unique instances from " 
					+ data.numUniqueInstances() + " to " + filterObserved.numUniqueInstances());
			System.out.println("MNAR filtering reduces number of instances from " 
					+ data.size + " to " + filterObserved.size);
			return new MnarK(filterObserved,mcarFactory,defaultFactory,data.separator,timeout);
		}
		
		private DataSet filterObserved(DataSet data) {
			ArrayList<byte[]> filteredInstances = new ArrayList<byte[]>();
			ArrayList<Integer> counts = new ArrayList<Integer>();
			int[] separator = data.separator.toArray();
			instances: for(int i=0;i<data.numUniqueInstances();i++){
				for(int v:separator){
					if(data.instances[i][v]==-1) continue instances;
				}
				filteredInstances.add(data.instances[i]);
				counts.add(data.counts[i]);
			}
			return new DataSet(data.numVars, 
					filteredInstances.toArray(new byte[filteredInstances.size()][]),
						Global.toIntArray(counts),false);
		}

		@Override
		public String toString() {
			return "MnarK("+mcarFactory+","+defaultFactory+")";
		}
		
	}
	
}
