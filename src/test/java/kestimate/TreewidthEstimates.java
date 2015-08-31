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

import il2.inf.structure.minfill2.Util;
import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.model.SubDomain;
import il2.model.Table;
import il2.util.IntSet;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
//{superfluous} import java.math.BigInteger;

import kestimate.util.BN;

public class TreewidthEstimates{

	public static void main(String[] args) {
		File dir = new File("networks/");
		File[] directoryListing = dir.listFiles();

		System.out.printf(
				"%15s %10s %10s %10s %10s %10s\n", 
				"DATASET","VARS","PARAMS","MAXDEGREE","TREEWIDTH","NORMWIDTH");
	    for (File child : directoryListing) {
	      // Do something with child
	      if(child.toString().endsWith(".uai")){
				BN gen = new BN(child.toString());
				final BayesianNetwork bn = gen.getBayesianNetwork();
				System.out.printf(
						"%15s %10d %10d %10d %10d %10.1f\n", 
						child.getName().replaceAll(".uai", ""),vars(bn),params(bn),maxdegree(bn),
						treewidth(bn),normalizedTreewidth(bn));
	      }
	    }
	}
	
	private static int params(BayesianNetwork bn) {
		int count = 0;
		for(Table t: bn.cpts()){
			count += t.values().length;
		}
		return count;
	}

	private static int vars(BayesianNetwork bn) {
		return bn.size();
	}

	private static int maxdegree(BayesianNetwork bn) {
		int degree = 0;
		for(Table t: bn.cpts()){
			if(t.vars().size()>degree) degree = t.vars().size();
		}
		return degree;
	}

	public final static int REPS = 10;

	public static int treewidth(BayesianNetwork bn){
		return treewith( java.util.Arrays.asList(bn.cpts()), REPS, new Random(0) );
	}

    public static int treewith( Collection subDomains, int reps, Random seed ){
    	return treewidth( subDomains, new IntSet[]{ variables(subDomains) }, reps, seed );
    }
    
	public static double normalizedTreewidth(BayesianNetwork bn){
		return normalizedTreewidth( java.util.Arrays.asList(bn.cpts()), REPS, new Random(0) );
	}

    public static double normalizedTreewidth( Collection subDomains, int reps, Random seed ){
    	return normalizedTreewidth( subDomains, new IntSet[]{ variables(subDomains) }, reps, seed );
    }

    /**
     * A constant for the largest cluster possible.  Minfill will fail if a
     * variable is eliminated that has a cluster bigger.
     */

   public static final int BIGGEST_POSSIBLE_CLUSTER = 2000;
   
    /**
     * Replacing JD's original minfill with a much faster version.
     * @author Mark Chavira
     */
    public static int treewidth( Collection subDomains, IntSet[] vars, int reps, Random seed ){

      if( reps < 1 ) throw new IllegalArgumentException( "reps must be >= 1" );

      // Create the moral graph.

      Domain d = ((SubDomain)subDomains.iterator().next()).domain();
      IntSet[] neighbors = new IntSet[d.size ()];
      for (int i = 0; i < neighbors.length; i++) {
        neighbors[i] = new IntSet ();
      }
      for (Iterator i = subDomains.iterator (); i.hasNext ();) {
        IntSet cptVars = ((SubDomain)i.next ()).vars ();
        for (int j = 0; j < cptVars.size (); j++) {
          int v1 = cptVars.get (j);
          for (int k = j + 1; k < cptVars.size (); k++) {
            int v2 = cptVars.get (k);
            neighbors[v1].add (v2);
            neighbors[v2].add (v1);
          }
        }
      }

      // Package up the parameters to the minfill2 algorithm.

      int[][] g = new int[neighbors.length][];
      int[] cardinalities = new int[neighbors.length];
      for (int i = 0; i < neighbors.length; i++) {
        cardinalities[i] = d.size (i);
        g[i] = neighbors[i].toArray ();
      }
      int[][] partition = new int[vars.length][];
      for (int i = 0; i < partition.length; i++) {
        partition[i] = vars[i].toArray ();
      }

      // Call minfill2 and return the result.

      if( seed == null ) seed = new Random();

      try {
        il2.inf.structure.minfill2.MinfillEoe engine = new il2.inf.structure.minfill2.MinfillEoe();
        int[] ans = null, best = null, worst = null;
        int logmaxclustersize = 0, min = Integer.MAX_VALUE, max = 0;
        for( int i=0; i<reps; i++ ){
          ans = engine.order( seed, cardinalities, g, partition );
          logmaxclustersize = logMaxClusterSize( ans, cardinalities, g );
          if( logmaxclustersize < min ){
            min = logmaxclustersize;
            best = ans;
          }
          if( logmaxclustersize > max ){
            max = logmaxclustersize;
            worst = ans;
          }
        }
//        System.out.println( "minfill2, best of " + reps + ": " + min + ", worst: " + max );
        return min-1; // treewidth is cluster size -1
      }catch( Exception e ){
        System.err.println( e );
        return -1;
      }

    }

    
    /**
     * Returns the maximum cluster size of the given
     * elimination order when applied to the given graph.
     *
     * @param eo the given elimination order.
     * @param cardinalities a map from each node to its cardinality.
     * @param g the given graph.
     * @return the maximum cluster size.
     */
    
    public static int logMaxClusterSize (
     int[] eo, int[] cardinalities, int[][] g) {
      int N = g.length;
      boolean[] marked = new boolean[N];
      boolean[][] marked2 = new boolean[BIGGEST_POSSIBLE_CLUSTER - 1][N];
      int[][] adj = new int[N][];
      int[] adjSize = new int[N];
      initGraph (g, adj, adjSize);
      int ans = -1;
      for (int eoIndex = 0; eoIndex < eo.length; eoIndex++) {
        int removed = eo[eoIndex];
        int clusterSize = 1;
        for (int j = 0; j < adjSize[removed]; j++) {
          int oneHop = adj[removed][j];
          clusterSize += 1;
        }
        ans = Math.max (ans, clusterSize);
        Util.prepareForUpdate (adj, adjSize, removed, marked, marked2);
        Util.update (adj, adjSize, removed, marked, marked2);
      }
      return ans;
    }


    /**
     * Initializes a graph in the internal representation from a graph in the
     * external representation.
     * 
     * @param g the given external graph.
     * @param on entry, an array of the same dimension as ext; on exit, a map
     *   from each node to its adjacency list.
     * @param adjSize on entry, an array of the same dimension as ext; on
     *   exit, a map from each node to the size of its adjacency list.
     */
    
    public static void initGraph (int[][] g, int[][] adj, int[] adjSize) {
      for (int n = 0; n < adj.length; n++) {
        adj[n] = new int[g[n].length * 2];
        adjSize[n] = g[n].length;
        System.arraycopy (g[n], 0, adj[n], 0, adjSize[n]);
      }
    }
    
    /**
     * Returns the set of vairables in the given subdomains.
     * @author Mark Chavira
     */

    private static IntSet variables (Collection subDomains) {
      Domain d = ((SubDomain)subDomains.iterator().next()).domain();
      boolean[] marked = new boolean[d.size ()];
      IntSet ans = new IntSet ();
      for (Iterator i = subDomains.iterator (); i.hasNext ();) {
        IntSet vars = ((SubDomain)i.next ()).vars ();
        for (int j = 0; j < vars.size (); j++) {
          int v1 = vars.get (j);
          if (!marked[v1]) {
            marked[v1] = true;
            ans.add (v1);
          }
        }
      }
      return ans;
    }
    

    public static double normalizedTreewidth( Collection subDomains, IntSet[] vars, int reps, Random seed ){

      if( reps < 1 ) throw new IllegalArgumentException( "reps must be >= 1" );

      // Create the moral graph.

      Domain d = ((SubDomain)subDomains.iterator().next()).domain();
      IntSet[] neighbors = new IntSet[d.size ()];
      for (int i = 0; i < neighbors.length; i++) {
        neighbors[i] = new IntSet ();
      }
      for (Iterator i = subDomains.iterator (); i.hasNext ();) {
        IntSet cptVars = ((SubDomain)i.next ()).vars ();
        for (int j = 0; j < cptVars.size (); j++) {
          int v1 = cptVars.get (j);
          for (int k = j + 1; k < cptVars.size (); k++) {
            int v2 = cptVars.get (k);
            neighbors[v1].add (v2);
            neighbors[v2].add (v1);
          }
        }
      }

      // Package up the parameters to the minfill2 algorithm.

      int[][] g = new int[neighbors.length][];
      int[] cardinalities = new int[neighbors.length];
      for (int i = 0; i < neighbors.length; i++) {
        cardinalities[i] = d.size (i);
        g[i] = neighbors[i].toArray ();
      }
      int[][] partition = new int[vars.length][];
      for (int i = 0; i < partition.length; i++) {
        partition[i] = vars[i].toArray ();
      }

      // Call minfill2 and return the result.

      if( seed == null ) seed = new Random();

      try {
        il2.inf.structure.minfill2.MinfillEoe engine = new il2.inf.structure.minfill2.MinfillEoe();
        int[] ans = null, best = null, worst = null;
        double logmaxclustersize = Double.NaN, min = Double.MAX_VALUE, max = (double)0;
        for( int i=0; i<reps; i++ ){
          ans = engine.order( seed, cardinalities, g, partition );
          logmaxclustersize = il2.inf.structure.minfill2.Util.logMaxClusterSize( ans, cardinalities, g );
          if( logmaxclustersize < min ){
            min = logmaxclustersize;
            best = ans;
          }
          if( logmaxclustersize > max ){
            max = logmaxclustersize;
            worst = ans;
          }
        }
        return min-1; // treewidth is cluster size -1;
      }catch( Exception e ){
        System.err.println( e );
        return -1;
      }

    }
}
