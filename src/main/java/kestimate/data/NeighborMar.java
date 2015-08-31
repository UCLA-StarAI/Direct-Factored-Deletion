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

import il2.model.BayesianNetwork;
import il2.model.Table;
import il2.util.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * Generate MAR data where the parents in the missingness mechanism are 
 * the fully observed parents and children of the variable being hidden.
 */
public class NeighborMar extends MissingnessClass{

	protected final double pAttrMissing;
	protected final double alpha;
	protected final double beta;
	protected final BetaDistribution prior;
	protected final int nbParents;

	/**
	 * When the prior (alpha and beta) is closer to 1, then more data is missing.
	 */
	public NeighborMar(double pAttrMissing, int nbParents, double alpha, double beta) {
		this.pAttrMissing = pAttrMissing;
		this.nbParents = nbParents;
		this.alpha = alpha;
		this.beta = beta;
		this.prior = new BetaDistribution(alpha, beta);
	}
	
	@Override
	public MissingnessGraph generateInstance(final BayesianNetwork bn, final Random r1) {
		int numVars = bn.size();
		ArrayList<Integer> vars = new ArrayList<Integer>(numVars);
		for (int var = 0; var < numVars; var++) {
			vars.add(var);
		}
		Collections.shuffle(vars, r1);
		int numMissing = (int)(numVars * pAttrMissing);

		final List<Integer> missingVars = vars.subList(0, numMissing);
		final List<Integer> observedVars = vars.subList(numMissing, numVars);

		final IntSet[] parents = new IntSet[numVars];
		final Table[] cpts = new Table[numVars];
		
		for (int missingVar: missingVars) {
			createMissingnessGraph(bn, r1, observedVars, parents, cpts,
					missingVar);
		}
		return new MissingnessGraph(bn) {
			
			@Override
			protected void hideAtRandom(byte[][] data, Random r2) {
				for (int missingVar: missingVars) {
					// go over data and make missingVar hidden
					for (byte[] world : data) {
						int[] state = new int[nbParents];
						for(int i=0;i<parents[missingVar].size();i++){
							int parent = parents[missingVar].get(i);
							// it's an observed value
							state[i] = world[parent];
						}
						double probObserved = cpts[missingVar].getCompatibleEntry(state);
						if (r2.nextDouble() <= probObserved ) {
							world[missingVar] = -1;
						}
					}
				}
			}
			
			@Override
			public IntSet getSeparator() {
				return null;
			}
		};
	}

	private void createMissingnessGraph(final BayesianNetwork bn,
			final Random r1, final List<Integer> observedVars,
			final IntSet[] parents, final Table[] cpts, int missingVar) {
		
		IntSet neighbors = parents(bn,missingVar).union(children(bn,missingVar));
		
		List<Integer> rParentNeighbors = new ArrayList<Integer>();
		for(int nb: neighbors.toArray()){
			if(observedVars.contains(nb)){
				rParentNeighbors.add(nb);
			}
		}
		
		List<Integer> otherRParents = new ArrayList<Integer>(observedVars);
		otherRParents.removeAll(rParentNeighbors);
		
		parents[missingVar] = new IntSet();
		for(int i=0; i<nbParents; i++) {
			int parent;
			if(rParentNeighbors.isEmpty()){
				int index = r1.nextInt(otherRParents.size());
				parent = otherRParents.remove(index);
			}else{
				int index = r1.nextInt(rParentNeighbors.size());
				parent = rParentNeighbors.remove(index);
			}
			parents[missingVar].add(parent);
		}
		cpts[missingVar] = new Table(bn.domain(),parents[missingVar]);
		double[] entries = cpts[missingVar].values();
		for(int i=0;i<entries.length;i++){
			entries[i] = prior.sample();
		}
	}
	
	protected IntSet parents(BayesianNetwork bn, int var) {
		IntSet vars = new IntSet(bn.forVariable(var).vars());
		vars.remove(var);
		return vars;
	}

	protected IntSet children(BayesianNetwork bn, int var) {
		IntSet vars = new IntSet();
		for(int i=0;i<bn.size();i++){
			if(bn.forVariable(i).vars().contains(var)) {
				vars.add(i);
			}
		}
		vars.remove(var);
		return vars;
	}

	@Override
	public String toString() {
		return "NeighborMar-" + pAttrMissing + "-" + nbParents+ "-" + alpha + "-"+beta;
	}

}
