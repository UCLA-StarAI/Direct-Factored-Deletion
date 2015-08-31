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

import kestimate.util.TimeoutException;
import il2.inf.JointEngine;
import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntMap;

public class EM {
    Options opts;  // EM algorithm options
    Task task;     // learning task (network, data, prior)
    Monitor monitor;

//    double logmap;

    // EM statistics
    double residual;
//    double map_residual;
    public int iterations;
    double learn_time;

    public EM(Options opts, Task task) {
        this.opts = opts;
        this.task = task;
        this.monitor = new Monitor(opts);
    }

    /**
     * main loop
     */
    public BayesianNetwork em() {
        BayesianNetwork learnedNetwork = task.getSeed();
//        double lastmap = Double.NEGATIVE_INFINITY;
        residual = Double.POSITIVE_INFINITY;

        task.logger.startLogger(task);
        iterations = 0;
        try {
			while ( iterations < opts.maxIters && residual > opts.threshold ) {
				if(System.currentTimeMillis()>opts.timeout) throw new TimeoutException(opts.timeout);
			    System.out.println("  EM iteration "+iterations);
			    learnedNetwork = emLoop(learnedNetwork);
			    //task.logger.logData(logmap/task.nbInstances,iterations);
			    iterations++;
			}
		} catch (TimeoutException e) {
			if(iterations==0 || !opts.anytime) throw e;
			//else return the learned network below
		}
        //task.logger.logData(expectation,iterations);
        learn_time = task.logger.stopLogger()*1e-6;

        monitor.iterations = iterations; // AC
        return learnedNetwork;
    }

    BayesianNetwork emLoop(BayesianNetwork bn) {
        JointEngine ie = opts.jointEngineFactory.create(bn);
        Domain d = task.getDomain();
//        logmap = EmUtil.logPrior(bn.cpts(),task.prior);
        Table[] expectation = emptyTables(task.seed,task.prior);

        for (int di = 0; di < task.data.length; di++) {
        	if(di%1==0 && System.currentTimeMillis()>opts.timeout) throw new TimeoutException(opts.timeout);
            IntMap example = task.data[di];
            int count = task.useUnique ? task.counts[di] : 1;

            ie.setEvidence(example); // this invalidates engine
//            double pe = ie.prEvidence();
//            assert( pe > 0.0 ) : "P(e) = 0.0";
//            logmap += count * Math.log(pe);

            for (int var = 0; var < d.size(); var++) {
                Table prxu = ie.tableConditional(var);
                for (int i = 0; i < prxu.values().length; i++){
        			if(Double.isNaN(prxu.values()[i])) 
        				throw new IllegalStateException("Computed marginal NaN for var " + var + " with evidence "+example);
                }
                accumulate(expectation[var],prxu,count);
            }
        }
        residual = 0.0;
        for (int var = 0; var < d.size(); var++) {
            Table t = expectation[var].makeCPT2(var);
            expectation[var] = t;
            updateResidual(bn.cpts()[var],expectation[var]);
        }
        return new BayesianNetwork(expectation);
    }

//    /**
//     * sanity check EM updates:
//    	 NOT SUPPORTED BY RCR
//     * updates should monotonically increase the posterior
//     */
//    double checkMap(double lastmap, double curmap) {
//        double map_residual = Double.POSITIVE_INFINITY;
//        if ( ! Double.isInfinite(lastmap) )
//            map_residual = (curmap-lastmap)/Math.abs(lastmap);
//        if ( map_residual < 0.0 && -map_residual > 1e-12 ) {
//            System.err.println("cur map :" + curmap);
//            System.err.println("prev map:" + lastmap);
//            System.err.println("residual:" + map_residual);
//            String errMsg = "EM Assertion failed : email Arthur";
//            throw new IllegalStateException(errMsg);
//        }
//        return curmap;
//    }

    // NOT SUPPORTED BY RCR
    protected void updateResidual(Table t1, Table t2) {
        double[] new_vals = t1.values();
        double[] old_vals = t2.values();
        for (int i = 0; i < new_vals.length; i++) {
            double diff = Math.abs(new_vals[i] - old_vals[i]);
            if ( diff > residual ) residual = diff;
        }
    }

//    /**
//     * Table t1 += t2
//     */
//    static void accumulate(Table t1, Table t2) {
//        double[] v1 = t1.values();
//        double[] v2 = t2.values();
//        for (int i = 0; i < v1.length; i++)
//            v1[i] += v2[i];
//    }

    /**
     * Table t1 += c*t2
     */
    static void accumulate(Table t1, Table t2, int c) {
        double[] v1 = t1.values();
        double[] v2 = t2.values();
        for (int i = 0; i < v1.length; i++){
            v1[i] += c*v2[i];
			if(Double.isNaN(v1[i])) throw new IllegalStateException();
        }
    }

    static Table[] emptyTables(BayesianNetwork bn, Prior prior) {
        Table[] cpts = bn.cpts();
        Table[] empty = new Table[cpts.length];
        for (int var = 0; var < cpts.length; var++) {
            double[] vals = prior.getTablePsis(var).clone();
            for (int i = 0; i < vals.length; i++) {
                vals[i] -= 1;
            }
            empty[var] = new Table(cpts[var],vals);
        }
        return empty;
    }

}
