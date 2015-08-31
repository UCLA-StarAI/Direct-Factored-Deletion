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
import il2.util.IntSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Mcar extends MissingnessClass{

	private final double pAttrMissing;
	private final double pValueMissing;

	public Mcar(double pAttrMissing, double pValueMissing) {
		this.pAttrMissing = pAttrMissing;
		this.pValueMissing = pValueMissing;
	}
	
	@Override
	public MissingnessGraph generateInstance(BayesianNetwork bn, Random r1) {
    	final ArrayList<Integer> missingVars = new ArrayList<Integer>();
        for (int var = 0; var < bn.size(); var++) {
        	missingVars.add(var);
        }
    	Collections.shuffle(missingVars, r1);
        final double numMissing = bn.size() * pAttrMissing;
    	return new MissingnessGraph(bn) {
			
			@Override
			protected void hideAtRandom(byte[][] partialData, Random r2) {
				for (byte[] world : partialData) {
					for (int i=0;i<numMissing; i++) {
		            	int var = missingVars.get(i);
		                if (r2.nextDouble() <= pValueMissing )
		                    world[var] = -1;
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
		return "Mcar-" + pAttrMissing + "-" + pValueMissing;
	}
	
}
