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

import il2.inf.JointEngine;
import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kestimate.estimator.em.EmUtil;
import edu.ucla.belief.BeliefNetwork;
import edu.ucla.belief.FiniteVariable;

public class BN {
    BayesianNetwork bn;
    BeliefNetwork bn1;
    BayesianNetwork bn2;
    FiniteVariable[] index2var;
    il2.util.Graph g;
    List<Integer> roots;
    List<Integer> internal;
    List<Integer> leaves;
    List<Integer> nodes;

    public BN(String filename) {
        BNPair pair = UaiConverter.uaiToBeliefNetworkWithIndex(filename);
        this.bn1 = pair.bn;
        this.bn2 = UaiConverter.uaiToBayesianNetwork(filename);
        this.index2var = pair.i2v;
        this.bn = bn2;
        g = bn.generateGraph();
        IntSet rts = g.roots(), lvs = g.leaves(), ins = g.vertices();
        this.roots = toList(rts);
        this.leaves = toList(lvs);
        this.internal = toList(ins.diff(rts).diff(lvs));
        this.nodes = toList(g.vertices());
    }

    public BeliefNetwork getBeliefNetwork() { return bn1; }
    public BayesianNetwork getBayesianNetwork() { return bn2; }
    public int numRoots()     { return roots.size(); }
    public int numInternal()  { return internal.size(); }
    public int numLeaves()    { return leaves.size(); }
    public int numNodes()     { return nodes.size(); }

    public FiniteVariable[] ordering() { return index2var; }

    public static int sum(int[] list) {
        int sum = 0;
        for (int i = 0; i < list.length; i++)
            sum += list[i];
        return sum;
    }

    /**
     * converts an il2 data set to an il1 data set
     */
    public List<Map<FiniteVariable,Object>> convertData(int[][] data2) {
        int N = data2.length;
        List<Map<FiniteVariable,Object>> data1 = 
            new ArrayList<Map<FiniteVariable,Object>>(N);
        for (int[] d2 : data2) {
            Map<FiniteVariable,Object> d1 = 
                new HashMap<FiniteVariable,Object>(d2.length);
            for (int var2 = 0; var2 < d2.length; var2++) {
                int val2 = d2[var2];
                if ( val2 < 0 ) continue;
                FiniteVariable var1 = index2var[var2];
                Object val1 = var1.instance(val2);
                d1.put(var1,val1);
            }
            data1.add(d1);
        }
        return data1;
    }

	//////////////////////////////////////////////////
	// generating incomplete data
	//////////////////////////////////////////////////

    /**
     * hide k elements from set in data
     */
    public static void hideSet(int[][] data, List<Integer> inputSet, int k, 
                               Random r) {
        List<Integer> set = new ArrayList<Integer>(inputSet);

        if ( k > set.size() )
            k = set.size();
        Collections.shuffle(set,r);
        for (int[] world : data) {
            for (int i = 0; i < k; i++) {
                int var = set.get(i);
                world[var] = -1;
            }
        }
    }

    /**
     * hide k elements from set in data
     */
    /*
    public void hideSetAtRandom(int[][] data, List<Integer> set, int k) {
        double p = (double)k/(double)set.size();
        for (int[] world : data) {
            for (int i = 0; i < set.size(); i++) {
                int var = set.get(i);
                if ( r.nextDouble() <= p )
                    world[var] = -1;
            }
        }
    }
    public void hideLeavesAtRandom(int[][] data, int k) 
    { hideSetAtRandom(data,leaves,k); }
    */

    public void hideRoots(int[][] d, int k, Random r) {hideSet(d,roots,k,r);}
    public void hideInternal(int[][] d, int k, Random r) {hideSet(d,internal,k,r);}
    public void hideLeaves(int[][] d, int k, Random r) {hideSet(d,leaves,k,r);}
    public void hideNodes(int[][] d, int k, Random r) {hideSet(d,nodes,k,r);}

    /**
     * make each cell "missing" with probability p = k/n
     */
    public static void hideAtRandom(int[][] data, int k, Random r) {
        if (data.length == 0) return;
        double p = (double)k/(double)data[0].length;
        for (int[] world : data) {
            for (int var = 0; var < world.length; var++) {
                if ( r.nextDouble() <= p )
                    world[var] = -1;
            }
        }
    }

	//////////////////////////////////////////////////
	// sampling
	//////////////////////////////////////////////////

    /*
	public int[] randomWorld() {
		Domain d = bn.domain();
        int[] w = new int[d.size()];
		for (int var=0; var<d.size(); var++) 
            w[var] = r.nextInt(d.size(var));
		return w;
	}
    */

	//////////////////////////////////////////////////
	// stuff
	//////////////////////////////////////////////////

    List<Integer> toList(IntSet vars) {
        List<Integer> list = new ArrayList<Integer>(vars.size());
        for (int i = 0; i < vars.size(); i++)
            list.add(vars.get(i));
        return list;
    }
    
    static class UniqueSoftData {
        int N;
        double[][] lambdas;
        int[] counts;
        public UniqueSoftData(int N, double[][] lambdas, int[] counts) {
            this.N = N;
            this.lambdas = lambdas;
            this.counts = counts;
        }
    }
    
    public static java.io.PrintWriter getPrintWriter(String filename, boolean append) {
        try {
            java.io.File file = new java.io.File(filename);
            java.io.PrintWriter pw = new java.io.PrintWriter
                (new java.io.OutputStreamWriter
                 (new java.io.BufferedOutputStream
                  (new java.io.FileOutputStream(file,append))), true);
            return pw;
        } catch ( Exception e ) {
            throw new IllegalStateException(e);
        }
    }
    
	public static BayesianNetwork emptyNetwork(BayesianNetwork bn) {
        Table[] cpts = bn.cpts();
        Table[] empty = new Table[cpts.length];
        for (int x = 0; x < cpts.length; x++) {
            Table cpt = cpts[x];
            double[] vals = new double[cpt.values().length];
            empty[x] = new Table(cpt,vals);
        }
        return new BayesianNetwork(empty);
    }
	

    public static double kl(double[] v1, double[] v2) {
        double kl = 0.0;
        for (int i = 0; i < v1.length; i++) {
            if ( v1[i] == 0.0 ) continue;
            kl += v1[i] * ( Math.log( v1[i] / v2[i] ) );
        }
        return kl;
    }

    public static double kl_bn(BayesianNetwork bn1, BayesianNetwork bn2) {
        Domain d = bn1.domain();
        Table[] cpts1 = bn1.cpts();
        Table[] cpts2 = bn2.cpts();

        JointEngine ie = (new JointEngineFactory.Jointree()).create(bn1);
        double kl = 0.0;
        for (int var = 0; var < cpts1.length; var++) {
            Table cpt1 = cpts1[var];
            Table cpt2 = cpts2[var];

            IntSet parents = new IntSet(cpt1.vars());
            parents.remove(var);
            Table pru = new Table(d,parents);
            Table prxu = ie.tableConditional(var);
            pru.projectInto(prxu);
//            System.out.println(prxu);
//            System.out.println(pru);

            int usize = cpt1.values().length/d.size(var);
            for (int uindex = 0; uindex < usize; uindex++) {
                double[] pr1 = EmUtil.cptColumn(cpt1,uindex);
                double[] pr2 = EmUtil.cptColumn(cpt2,uindex);

                kl += pru.values()[uindex] * kl(pr1,pr2);
            }
        }
        return kl;
    }
}
