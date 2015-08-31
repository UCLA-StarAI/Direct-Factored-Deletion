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

package kestimate.data;

import il2.inf.JointEngine;
import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntMap;
import il2.util.IntSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import kestimate.util.Global;
import kestimate.util.JointEngineFactory;

public final class DataSet {

	public final byte[][] instances;
	public final int[] counts;
	public final int size;
	public final int numVars;
	public IntSet separator = null; // parents of the R-variables that generated the missing data
	public final boolean complete;

	public DataSet(int numVars, byte[][] instances, int[] counts, boolean complete) {
//		if(instancesAr.length != instances.length || instances.length != counts.length) throw new IllegalArgumentException();
		this.instances = instances;
		this.numVars = numVars;
		this.counts = counts;
		int size = 0;
		for(int count:counts) size += count;
		this.size = size;
		this.complete = complete;
	}
	
	public int numUniqueInstances(){
		return counts.length;
	}

	public double logLikelihood(BayesianNetwork bn) {
		return logLikelihood(bn,null);
	}
	
	public double logLikelihood(BayesianNetwork bn, JointEngineFactory engineFactorie) {
		double ll = 0.0;
		if(complete){
			// don't run inference
			for (int di = 0; di < numUniqueInstances(); di++) {
				Table[] condTables = Table.shrink(bn.cpts(), worldToEvidence(instances[di]));
				for(Table t: condTables){
					if(t.values().length!=1) throw new IllegalStateException();
					ll += counts[di]*Math.log(t.values()[0]);
					if(Double.isNaN(ll)) throw new IllegalStateException();
				}
			}
		}else{
			if(engineFactorie == null) engineFactorie = new JointEngineFactory.Jointree();
			JointEngine ie = engineFactorie.create(bn);
			for (int di = 0; di < numUniqueInstances(); di++) {
				ie.setEvidence(worldToEvidence(instances[di]));
				ll += counts[di]*Math.log(ie.prEvidence());
				if(Double.isNaN(ll)) throw new IllegalStateException();
			}
		}
		return ll;
	}
	
	// the price of mutable data structures
	public DataSet copy() {
		DataSet dataSet = new DataSet(numVars, Global.deepClone(instances), counts.clone(), complete);
		dataSet.separator = separator;
		return dataSet;
	}
	
	
	// does EM require copies?
	public IntMap[] instancesArray() {
		return dataToEvidence(instances);
	}

	// does EM require copies?
	public int[] countsArray() {
		return counts.clone();
	}
	

	public static DataSet nextDataSet(BayesianNetwork bn, int size, Random random) {
		byte[][] fullTestData = DataSet.nextArrayDataSet(bn,size,random);
		return uniqueDataSet(fullTestData, true);
	}

    public static int[][] nextArrayDataSet(BayesianNetwork bn, int N) {
    	return nextArrayDataSet(bn,N);
    }
	
    public static byte[][] nextArrayDataSet(BayesianNetwork bn, int N, Random r) {
    	byte[][] data = new byte[N][];
        for (int i = 0; i < N; i++)
            data[i] = simulateWorld(bn,r);
        return data;
    }


    /**
     * draw sample w (a world) from Pr() induced by network bn
     */
    public static byte[] simulateWorld(BayesianNetwork bn, Random r) {
        Domain d = bn.domain();
        Table[] cpts = bn.cpts();
        byte[] w = new byte[d.size()];
		
        for (int var = 0; var < d.size(); var++) {
            Table cpt = cpts[var];
            int[] xu = project(w,cpt.vars());
            xu[xu.length-1] = 0;
            // the following only works since x = 0
            int uindex = cpt.getIndexFromFullInstance(xu);
            double[] vals = cptColumn(cpt,uindex);

			w[var] = (byte) (vals.length-1); //default
			double sum = 0.0;
			double rand = r.nextDouble();
			for ( int i = 0; i < vals.length; i++ ) {
                sum += vals[i];
				if ( rand <= sum ) {
                    w[var] = (byte) i;
                    break;
                }
			}
		}
		return w;
	}

    private static double[] cptColumn(Table cpt, int uindex) {
        int var = familyChild(cpt.vars());
        int size = cpt.domain().size(var);

        double[] vals = cpt.values();
        int usize = vals.length/size;
        //int usize = parents[var].sizeInt();

        double[] xvals = new double[size];
        for (int state = 0; state < size; state++)
            xvals[state] = vals[state*usize + uindex];
        return xvals;
    }

    private static int familyChild(IntSet vars) {
        int size = vars.size();
        return vars.get(size-1);
    }

    /**
     * project world w onto parents u, where vars=xu (assumes last
     * index of vars is child x)
     */
    protected static int[] project(byte[] w, IntSet vars) {
        int[] x = new int[vars.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = w[vars.get(i)];
        }
        return x;
    }

    static class WorldComparator implements java.util.Comparator<byte[]> {
        public WorldComparator() {}
        public int compare(byte[] w1, byte[] w2) {
            for (int i = 0; i < w1.length; i++) {
                if      ( w1[i] < w2[i] ) return -1;
                else if ( w1[i] > w2[i] ) return 1;
            }
            return 0;
        }
    }
    
    public static DataSet uniqueDataSet(byte[][] data, boolean complete) {
        LinkedHashMap<byte[],Integer> unique = 
            new LinkedHashMap<byte[],Integer>();
        WorldComparator wc = new WorldComparator();
        java.util.Arrays.sort(data,wc);
        int i = 0;
        while ( i < data.length ) {
        	byte[] di = data[i];
            int count = 1;
            i++;
            while ( i < data.length && wc.compare(di,data[i]) == 0 ) {
                count++;
                i++;
            }
            unique.put(di,new Integer(count));
        }
        int n = unique.size();
        byte[][] udata = new byte[n][];
        int[] counts = new int[n];
        i = 0;
        for (byte[] di : unique.keySet()) {
            udata[i] = di;
            counts[i] = unique.get(di);
            i++;
        }
        return new DataSet(data[0].length,udata,counts,complete);
    }

//    private static IntMap[] dataToEvidence(int[][] data) {
//        IntMap[] evid = new IntMap[data.length];
//        for (int i = 0; i < data.length; i++)
//            evid[i] = worldToEvidence(data[i]);
//        return evid;
//    }
    
//    private static IntMap worldToEvidence(int[] w) {
//        IntMap e = new IntMap();
//        for (int var = 0; var < w.length; var++)
//            if ( w[var] >= 0 ) e.put(var,w[var]);
//        return e;
//    }
    
    private static IntMap[] dataToEvidence(byte[][] data) {
        IntMap[] evid = new IntMap[data.length];
        for (int i = 0; i < data.length; i++)
            evid[i] = worldToEvidence(data[i]);
        return evid;
    }
    
    private static IntMap worldToEvidence(byte[] w) {
        IntMap e = new IntMap();
        for (int var = 0; var < w.length; var++)
            if ( w[var] >= 0 ) e.put(var,w[var]);
        return e;
    }
    
	public IntSet getObservedVariables() {
		ArrayList<Integer> observedIndicesList = new ArrayList<Integer>();
		for(int var=0;var<this.numVars;var++){
			boolean fullObserve = true;
			for(int i=0;i<this.numUniqueInstances();i++){
				if(this.instances[i][var] == -1){
					fullObserve = false;
					break;
				}
			}
			if(fullObserve) observedIndicesList.add(var);
		}
//		System.out.print("Fully observed variables: ");
//		for(int var:observedIndicesList) System.out.print(var + " ");
//		System.out.println();
		return new IntSet(Global.toIntArray(observedIndicesList));
	}
	

	public double getMissingRate() {
		long numMissing = 0;
		for(int i=0;i<this.numUniqueInstances();i++){
			for(int var=0;var<this.numVars;var++){
				if(this.instances[i][var] == -1){
					numMissing += this.counts[i];
				}
			}
		}
		return numMissing*1.0/(size*numVars);
	}
	
}
