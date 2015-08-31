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
// * Generate MAR data where the parents in the missingness mechanism are 
// * the fully observed parents and children of the variable being hidden.
// * 
// * Use a small set of variables as missingness mechanism parents and 
// * return information about this set.
// */
//public class MgMar extends NeighborMar{
//
//	private final int nbCausalVars;
//
//	/**
//	 * When the prior (alpha and beta) is closer to 1, then more data is missing.
//	 */
//	public MgMar(double pAttrMissing, int nbParents, int nbCausalVars, double alpha, double beta) {
//		super(pAttrMissing,nbParents,alpha,beta);
//		this.nbCausalVars = nbCausalVars;
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
//		int numMissing = (int)(numVars * pAttrMissing);
//
//		List<Integer> missingVars = vars.subList(0, numMissing);
//		List<Integer> rParentsVars = vars.subList(numMissing, Math.min(numVars, numMissing+nbCausalVars));
//
//		for (int missingVar: missingVars) {
//			hideWithRParents(data, bn, r, rParentsVars, missingVar);
//		}
//		return new IntSet(Global.toIntArray(rParentsVars));
//	}
//
//	@Override
//	public String toString() {
//		return "MgMar-" + pAttrMissing + "-" + nbParents+"-"+nbCausalVars+ "-" + alpha + "-"+beta;
//	}
//
//}
