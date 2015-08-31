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

//package kestimate.data;
//
//import il2.model.BayesianNetwork;
//import il2.model.Domain;
//import il2.model.Table;
//import il2.util.IntSet;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//import kestimate.util.Global;
//
///**
// * Generate MNAR data where the parents in the missingness mechanism are 
// * the fully observed parents and children of the variable being hidden.
// * Then hide the fully observed parents CAR.
// */
//public class MgMnar extends NeighborMar{
//
//	private final int nbCausalVars;
//	private final double pValueMissing;
//
//	/**
//	 * When the prior (alpha and beta) is closer to 1, then more data is missing.
//	 */
//	public MgMnar(double pValueMissing, int nbParents, int nbCausalVars, double alpha, double beta) {
//		super(1,nbParents,alpha,beta);
//		this.nbCausalVars = nbCausalVars;
//		this.pValueMissing = pValueMissing;
//	}
//
//	@Override
//	protected IntSet hideAtRandom(byte[][] data, BayesianNetwork bn, Random r) {
//		int numVars = data[0].length;
//		ArrayList<Integer> vars = new ArrayList<Integer>(numVars);
//		for (int var = 0; var < numVars; var++) {
//			vars.add(var);
//		}
//		Collections.shuffle(vars, r);
//
//		List<Integer> rParentsVars = vars.subList(0, Math.min(numVars, nbCausalVars));
//		List<Integer> missingVars = vars.subList(Math.min(numVars, nbCausalVars), numVars);
//
//		byte[][] missingnesses = new byte[data.length][numVars];
//        for (int i=0;i<missingnesses.length;i++) {
//            for (int j=0;j<numVars;j++) {
//                if (r.nextDouble() <= pValueMissing ){
//                	missingnesses[i][j] = 1;
//                }else{
//                	missingnesses[i][j] = 0;
//                }
//            }
//        }
//
//		for (int missingVar: missingVars) {
//			hideWithRParents(data, bn, r, rParentsVars, missingVar, missingnesses);
//		}
//
//        for (int i=0;i<missingnesses.length;i++) {
//            for (int j=0;j<rParentsVars.size();j++) {
//                if (missingnesses[i][rParentsVars.get(j)] == 1){
//                	data[i][rParentsVars.get(j)] = -1;
//                }
//            }
//        }
//		return new IntSet(Global.toIntArray(rParentsVars));
//	}
//	
//	protected void hideWithRParents(byte[][] data, BayesianNetwork bn, Random r,
//			List<Integer> rParents, int missingVar, byte[][] missingnesses) {
//		Domain domain = bn.domain();
//		int numVars = data[0].length;
//		IntSet neighbors = parents(bn,missingVar).union(children(bn,missingVar));
//		
//		List<Integer> rParentNeighbors = new ArrayList<Integer>();
//		List<Integer> otherRParents = new ArrayList<Integer>(rParents);
//		for(int p: rParents) otherRParents.add(numVars+p);
//		for(int nb: neighbors.toArray()){
//			if(rParents.contains(nb)){
//				rParentNeighbors.add(nb);
//				rParentNeighbors.add(numVars+nb);
//			}
//		}
//		otherRParents.removeAll(rParentNeighbors);
//		
//		// extend the domain with Boolean missingness variables
//		Domain extDomain = new Domain();
//		for(int i=0;i<numVars;i++) {
//			extDomain.addDim(domain.name(i),domain.size(i));
//		}
//		for(int i=0;i<numVars;i++) {
//			extDomain.addDim("R_"+domain.name(i),2);
//		}
//		
//		IntSet parents = new IntSet();
//		for(int i=0; i<nbParents; i++) {
//			int parent;
//			if(rParentNeighbors.isEmpty()){
//				int index = r.nextInt(otherRParents.size());
//				parent = otherRParents.remove(index);
//			}else{
//				int index = r.nextInt(rParentNeighbors.size());
//				parent = rParentNeighbors.remove(index);
//			}
//			parents.add(parent);
//		}
//		Table cpt = new Table(extDomain,parents);
//		double[] entries = cpt.values();
//		for(int i=0;i<entries.length;i++){
//			entries[i] = prior.sample();
//		}
//		// go over data and make missingVar hidden
//		for (int j=0;j<data.length;j++) {
//			byte[] world = data[j];
//			int[] state = new int[nbParents];
//			for(int i=0;i<parents.size();i++){
//				int parent = parents.get(i);
//				if(parent<numVars){
//					state[i] = world[parent];
//				}else{
//					state[i] = missingnesses[j][parent-numVars];
//				}
//			}
//			double probObserved = cpt.getCompatibleEntry(state);
//			if (r.nextDouble() <= probObserved ) {
//				world[missingVar] = -1;
//			}
//		}
//	}
//
//	@Override
//	public String toString() {
//		return "MgMnar-" +pValueMissing + "-" + nbParents+"-"+nbCausalVars+ "-" + alpha + "-"+beta;
//	}
//
//}
