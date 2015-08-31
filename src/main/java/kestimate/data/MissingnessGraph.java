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

import java.util.Random;

import kestimate.util.Global;

public abstract class MissingnessGraph {
	
	private final BayesianNetwork bn;

	public MissingnessGraph(BayesianNetwork bn) {
		this.bn = bn; 
	}
	
	public abstract IntSet getSeparator();

	protected abstract void hideAtRandom(byte[][] data, Random random);
	
	public ExpDataSets generate(int size, int seed) {

		byte[][] fullTrainData = DataSet.nextArrayDataSet(bn,size,new Random(seed));
		DataSet fullTrainDataSet = DataSet.uniqueDataSet(fullTrainData, true);

		byte[][] partialData = Global.deepClone(fullTrainData);
		// this makes sure that the missing data mechanisms is the same for equal seeds, regardless of size
		hideAtRandom(partialData,new Random(seed+1));
		DataSet partialDataSet = DataSet.uniqueDataSet(partialData, false);
		partialDataSet.separator = getSeparator();
		
		DataSet fullTestDataSet = DataSet.nextDataSet(bn, size, new Random(seed+2));

		System.out.println("Generated datasets with " + (partialDataSet.numVars-partialDataSet.getObservedVariables().size())+"/"+partialDataSet.numVars
					+" missing variables and " + (float)(partialDataSet.getMissingRate()*100)+"% missing data");
		
		return new ExpDataSets(fullTrainDataSet,partialDataSet,fullTestDataSet);
	}

}
