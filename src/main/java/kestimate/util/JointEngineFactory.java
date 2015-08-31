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

package kestimate.util;

import il2.inf.Algorithm;
import il2.inf.Algorithm.EliminationOrderHeuristic;
import il2.inf.Algorithm.Order2JoinTree;
import il2.inf.Algorithm.Setting;
import il2.inf.JointEngine;
import il2.inf.edgedeletion.EDAlgorithm;
import il2.inf.edgedeletion.EDEdgeDeleter;
import il2.inf.structure.EliminationOrders;
import il2.model.BayesianNetwork;
import il2.model.Table;

import java.util.Map;
import java.util.Random;

public interface JointEngineFactory {

	public JointEngine create(BayesianNetwork bn);

	class Jointree implements JointEngineFactory{

		@Override
		public JointEngine create(BayesianNetwork bn) {
	    	Table[] cpts = bn.cpts();
	        java.util.Collection<Table> sd = java.util.Arrays.asList(cpts);
	        Random r = new Random(0);
	        EliminationOrders.Record record = EliminationOrders.minFill(sd,6,r);
	        // this JT construction has quadratic space complexity
	        EliminationOrders.JT jt = 
	            EliminationOrders.traditionalJoinTree(sd,record.order);
	        // this JT construction has linear space complexity
//	        EliminationOrders.JT jt = 
//	            EliminationOrders.bucketerJoinTree(sd,record.order);
//	        return il2.inf.jointree.NormalizedZCAlgorithm.create(cpts,jt);
	        return il2.inf.jointree.UnindexedZCAlgorithm.create(cpts,jt);
//	        return il2.inf.jointree.NormalizedSSAlgorithm.create(cpts,jt);
		}
		
		@Override
		public String toString() {
			return "Jointree";
		}
		
	}
	
	class Bp implements JointEngineFactory{

		@Override
		public JointEngine create(BayesianNetwork bn) {

			Map<Setting,Object> settings = 
	            new java.util.EnumMap<Setting,Object>(Setting.class);
			settings.put( Setting.eliminationorderheuristic,
	                      EliminationOrderHeuristic.minfill );
			settings.put( Setting.eliminationorderrepetitions, 1 ); // 1 is enough for spanning trees
			settings.put( Setting.order2jointree, Order2JoinTree.bucketer );

	        Random r = new Random(0);
	        int[][] edges = 
	            EDEdgeDeleter.getEdgesToDeleteForRandomSpanningTree(bn.cpts(),r);
	        int maxIterations = 8;
	        int timeoutMillis = 0;
	        double convThreshold = 1e-4;
	        Algorithm alg = Algorithm.zeroconscioushugin;

	        EDAlgorithm ie = new EDAlgorithm(bn,edges,
	                    maxIterations,timeoutMillis,convThreshold,alg,settings);
	        return (JointEngine)ie;
		}

		@Override
		public String toString() {
			return "BP";
		}
	}
	
}
