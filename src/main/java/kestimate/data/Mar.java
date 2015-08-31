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
import il2.model.Domain;
import il2.model.Table;
import il2.util.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;

public class Mar extends MissingnessClass{

	private final double pAttrMissing;
	private final double alpha;
	private final double beta;
	private final BetaDistribution prior;
	private final int nbParents;
	private final boolean allowRParents;

	/**
	 * When the prior (alpha and beta) is closer to 1, then more data is missing.
	 */
	public Mar(double pAttrMissing, int nbParents, double alpha, double beta, boolean allowRParents) {
		this.pAttrMissing = pAttrMissing;
		this.alpha = alpha;
		this.beta = beta;
		this.prior = new BetaDistribution(alpha, beta);
		this.nbParents = nbParents;
		this.allowRParents = allowRParents;
	}

	@Override
	public MissingnessGraph generateInstance(BayesianNetwork bn, Random r) {
		Domain domain = bn.domain();
		final int numVars = bn.size();
		ArrayList<Integer> vars = new ArrayList<Integer>(numVars);
		for (int var = 0; var < numVars; var++) {
			vars.add(var);
		}
		Collections.shuffle(vars, r);
		int numMissing = (int)(numVars * pAttrMissing);

		final List<Integer> missingVars = vars.subList(0, numMissing);
		List<Integer> observedVars = vars.subList(numMissing, numVars);
		List<Integer> possibleParents = new ArrayList<Integer>(observedVars);

		// extend the domain with Boolean missingness variables
		Domain extDomain = new Domain();
		for(int i=0;i<numVars;i++) {
			extDomain.addDim(domain.name(i),domain.size(i));
		}
		if(allowRParents){
			for(int i=0;i<numVars;i++) {
				extDomain.addDim("R_"+domain.name(i),2);
			}
		}
		final IntSet[] parents = new IntSet[numVars];
		final Table[] cpts = new Table[numVars];
		for (int missingVar: missingVars) {
			// create a simple missing data mechanism, depending on nbParents observed variables
			parents[missingVar] = new IntSet();
			for(int i=0; i<nbParents; i++) {
				int parent = possibleParents.get(r.nextInt(possibleParents.size()));
				parents[missingVar].add(parent);
			}
			if(allowRParents){
				// add the missingness of missingVar to the set of possible parents
				possibleParents.add(numVars+missingVar);
			}
			cpts[missingVar] = new Table(extDomain,parents[missingVar]);
			double[] entries = cpts[missingVar].values();
			for(int i=0;i<entries.length;i++){
				entries[i] = prior.sample();
			}
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
							if(parent < numVars){
								// it's an observed value
								state[i] = world[parent];
							}else if(world[parent-numVars] == -1){
								// it's a missingness variable - unobserved
								state[i] = 1;
							}else{
								// it's a missingness variable - observed
								state[i] = 0;
							}
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

	@Override
	public String toString() {
		return "Mar-" + pAttrMissing + "-" + nbParents + "-" + alpha + "-"+beta+"-"+allowRParents;
	}

}
