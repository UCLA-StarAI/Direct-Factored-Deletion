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

import kestimate.data.DataSet;

public class MarK extends SeparatorK{

	public MarK(final DataSet data, KFactory mcarFactory, KFactory defaultFactory, long deadline) {
		super(data,mcarFactory,defaultFactory,data.getObservedVariables(),deadline);
	}

	public static class Factory extends KFactory {

		private final KFactory mcarFactory;
		private final KFactory defaultFactory;

		public Factory(KFactory mcarFactory, KFactory defaultFactory) {
			this.mcarFactory = mcarFactory;
			this.defaultFactory = defaultFactory;
		}
		
		@Override
		public AbstractK create(DataSet data, long timeout, AbstractK backupEstimator) {
			return new MarK(data,mcarFactory,defaultFactory,timeout);
		}

		@Override
		public String toString() {
			return "MarK("+mcarFactory+","+defaultFactory+")";
		}
		
	}
	
}
