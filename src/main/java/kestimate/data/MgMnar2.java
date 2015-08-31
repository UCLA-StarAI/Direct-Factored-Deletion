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
//import java.util.Random;
//
//import org.apache.commons.math3.distribution.BetaDistribution;
//import org.apache.commons.math3.random.JDKRandomGenerator;
//
//public class MgMnar2 extends Missingness{
//
//	private final double alpha;
//	private final double beta;
//
//	/**
//	 * When the prior (alpha and beta) is closer to 1, then more data is missing.
//	 */
//	public MgMnar2(double alpha, double beta) {
//		this.alpha = alpha;
//		this.beta = beta;
//	}
//
//	protected IntSet hideAtRandom(byte[][] data, BayesianNetwork bn, Random r) {
//		JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
//		jdkRandomGenerator.setSeed(r.nextInt());
//		BetaDistribution prior = new BetaDistribution(jdkRandomGenerator,alpha, beta, BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY); // add to r to make identical for all reruns with seed!!!
//		
//		Table tmpCpt;
//		do{
//			tmpCpt = bn.cpts()[r.nextInt(bn.cpts().length)];
//		}while(tmpCpt.vars().size()<2);
//		
//		ArrayList<Integer> family = new ArrayList<Integer>();
//		for(int v: tmpCpt.vars().toArray()) family.add(v);
//		Collections.shuffle(family,r);
//		int x = family.get(0);
//		int y = family.get(1);
//		
//		Domain domain = bn.domain();
//
//		boolean[] xmiss = new boolean[data.length];
//		boolean[] ymiss = new boolean[data.length];
//		{
//			IntSet parents = new IntSet();
//			parents.add(y);
//			Table cpt = new Table(domain,parents);
//			double[] entries = cpt.values();
//			for(int i=0;i<entries.length;i++){
//				entries[i] = prior.sample();
//			}
//			// go over data and make missingVar hidden
//			for (int i=0;i<data.length;i++) {
//				int[] state = new int[1];
//				state[0] = data[i][y];
//				double probObserved = cpt.getCompatibleEntry(state);
//				if (r.nextDouble() <= probObserved ) {
//					xmiss[i] = true;
//				}else{
//					xmiss[i] = false;
//				}
//			}
//		}
//		{
//			IntSet parents = new IntSet();
//			parents.add(x);
//			Table cpt = new Table(domain,parents);
//			double[] entries = cpt.values();
//			for(int i=0;i<entries.length;i++){
//				entries[i] = prior.sample();
//			}
//			// go over data and make missingVar hidden
//			for (int i=0;i<data.length;i++) {
//				int[] state = new int[1];
//				state[0] = data[i][x];
//				double probObserved = cpt.getCompatibleEntry(state);
//				if (r.nextDouble() <= probObserved ) {
//					ymiss[i] = true;
//				}else{
//					ymiss[i] = false;
//				}
//			}
//		}
//		for (int i=0;i<data.length;i++) {
//			if (xmiss[i]) {
//				data[i][x] = -1;
//			}
//			if (ymiss[i]) {
//				data[i][y] = -1;
//			}
//		}
//		IntSet sep = new IntSet();
//		sep.add(x);
//		sep.add(y);
//		return sep;
//	}
//
//	@Override
//	public String toString() {
//		return "MgMnar2-" + alpha + "-"+beta;
//	}
//
//}
