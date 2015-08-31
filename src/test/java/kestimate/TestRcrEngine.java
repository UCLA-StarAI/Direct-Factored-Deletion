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

package kestimate;

import il2.inf.Algorithm;
import il2.inf.Algorithm.EliminationOrderHeuristic;
import il2.inf.Algorithm.Order2JoinTree;
import il2.inf.Algorithm.Setting;
import il2.inf.JointEngine;
import il2.inf.edgedeletion.EDAlgorithm;
import il2.inf.edgedeletion.EDEdgeDeleter;
import il2.model.BayesianNetwork;
import il2.model.Table;

import java.util.Map;
import java.util.Random;

import kestimate.util.UaiConverter;

public class TestRcrEngine {
	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: main UAI-NETWORK\n");
            System.exit(1);
        }

        String network_filename = args[0];
        BayesianNetwork bn = UaiConverter.uaiToBayesianNetwork(network_filename);
        JointEngine ie = new_rcr_engine(bn);


        for (int table_index = 0; table_index < bn.size(); table_index++) {
            Table cpt = bn.cpts()[table_index];
            Table prxu = ie.tableConditional(table_index);
            System.out.printf("== table index %d ==\n",table_index);
            System.out.println(cpt);
            System.out.println(prxu);
        }

    }

    public static JointEngine new_rcr_engine(BayesianNetwork bn) {
        return new_rcr_engine(bn,null);
    }

    public static JointEngine new_rcr_engine(BayesianNetwork bn, Random r) {
        if ( r == null ) r = new Random(0);

		Map<Setting,Object> settings = 
            new java.util.EnumMap<Setting,Object>(Setting.class);
		settings.put( Setting.eliminationorderheuristic,
                      EliminationOrderHeuristic.minfill );
		settings.put( Setting.eliminationorderrepetitions, 6 );
		settings.put( Setting.order2jointree, Order2JoinTree.bucketer );

        int[][] edges = 
            EDEdgeDeleter.getEdgesToDeleteForRandomSpanningTree(bn.cpts(),r);
        int maxIterations = 100;
        int timeoutMillis = 0;
        double convThreshold = 1e-4;
        Algorithm alg = Algorithm.zcnormalized;

        EDAlgorithm ie = new EDAlgorithm(bn,edges,
                    maxIterations,timeoutMillis,convThreshold,alg,settings);

        return (JointEngine)ie;
    }


}
