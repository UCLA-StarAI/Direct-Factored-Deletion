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

package kestimate.estimator.em;

import il2.inf.JointEngine;
import il2.inf.PartialDerivativeEngine;
import il2.inf.structure.EliminationOrders;
import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntSet;

import java.util.Arrays;
import java.util.Random;

public class EmUtil {

    public static Table[] randomNetwork(BayesianNetwork bn, Random r) {
        Table[] cpts = bn.cpts();
        Table[] rand = new Table[cpts.length];
        for (int x = 0; x < cpts.length; x++) {
            Table cpt = cpts[x];
            double[] vals = new double[cpt.values().length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = r.nextDouble();
            rand[x] = new Table(cpt,vals);
            rand[x] = rand[x].makeCPT2(x);
        }
        return rand;
    }

    public static Table[] randomUndirectedNetwork(BayesianNetwork bn,
                                                  Random r) {
        Table[] cpts = bn.cpts();
        Table[] rand = new Table[cpts.length];
        for (int ti = 0; ti < cpts.length; ti++) {
            Table cpt = cpts[ti];
            double[] vals = new double[cpt.values().length];
            for (int i = 0; i < vals.length; i++)
                vals[i] = r.nextDouble();
            rand[ti] = new Table(cpt,vals);
        }
        return rand;
    }

    public static Table[] uniformNetwork(BayesianNetwork bn) {
        Table[] cpts = bn.cpts();
        Table[] unif = new Table[cpts.length];
        for (int x = 0; x < cpts.length; x++) {
            Table cpt = cpts[x];
            double[] vals = new double[cpt.values().length];
            Arrays.fill(vals,1.0);
            unif[x] = new Table(cpt,vals);
            unif[x] = unif[x].makeCPT2(x);
        }
        return unif;
    }

    public static Table[] uniformUndirectedNetwork(BayesianNetwork bn) {
        Table[] cpts = bn.cpts();
        Table[] unif = new Table[cpts.length];
        for (int ti = 0; ti < cpts.length; ti++) {
            Table cpt = cpts[ti];
            double[] vals = new double[cpt.values().length];
            Arrays.fill(vals,1.0);
            unif[ti] = new Table(cpt,vals);
        }
        return unif;
    }

    static int familyChild(IntSet vars) {
        int size = vars.size();
        return vars.get(size-1);
    }

    static double[] cptColumn(double[] vals, int size, int uindex) {
        int usize = vals.length/size;

        double[] xvals = new double[size];
        for (int state = 0; state < size; state++)
            xvals[state] = vals[state*usize + uindex];
        return xvals;
    }

    public static double[] cptColumn(Table cpt, int uindex) {
        int var = familyChild(cpt.vars());
        int size = cpt.domain().size(var);

        double[] vals = cpt.values();
        int usize = vals.length/size;

        double[] xvals = new double[size];
        for (int state = 0; state < size; state++)
            xvals[state] = vals[state*usize + uindex];
        return xvals;
    }

    static void setCptColumn(Table cpt, int uindex, double[] xvals) {
        int var = familyChild(cpt.vars());
        int size = cpt.domain().size(var);

        double[] vals = cpt.values();
        int usize = vals.length/size;

        for (int state = 0; state < size; state++)
            vals[state*usize + uindex] = xvals[state];
    }

    /** log likelihood computation **/


    static Table[] engineTables(JointEngine ie) { //AC: make this cleaner
        if (ie instanceof il2.inf.jointree.JoinTreeAlgorithm)
            return ((il2.inf.jointree.JoinTreeAlgorithm)ie).getOriginalTables();
        else return null;
    }

    public static double logPrior(Table[] cpts, Prior prior) {
        Domain d = cpts[0].domain();
        double logp = 0.0;
        for (int var = 0; var < d.size(); var++) {
            Table cpt = cpts[var];
            int usize = cpt.values().length/d.size(var);
            for (int uindex = 0; uindex < usize; uindex++) {
                double[] pr = EmUtil.cptColumn(cpt,uindex);
                double[] psis = prior.getPsis(var,uindex);
                for (int x = 0; x < pr.length; x++) {
                    if ( psis[x] == 1.0 ) continue;
                    logp += (psis[x]-1) * Math.log(pr[x]);
                }
            }
        }
        return logp;
    }

}
