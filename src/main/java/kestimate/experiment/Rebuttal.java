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

package kestimate.experiment;

import il2.model.BayesianNetwork;

import java.io.IOException;
import java.util.Random;

import kestimate.data.ExpDataSets;
import kestimate.data.MissingnessClass;
import kestimate.data.NeighborMar;
import kestimate.estimator.IEstimator;
import kestimate.estimator.em.Prior;
import kestimate.estimator.em.RestartEMEstimator;
import kestimate.util.BN;
import kestimate.util.JointEngineFactory;

public class Rebuttal {

	public static void main(String[] args) throws IOException {
		
		
//		String name = "munin1";
//		String name = "water";
//		String name = "link";
//		String name = "barley";
		String name = "90-20-1";


		int size = 100;
		int repetitions = 10;
		MissingnessClass miss = new NeighborMar(0.9, 2, 0.5, 0.5);
		
		System.out.println("Loading network");
		String network_filename = "networks/"+name+".uai";
		BN gen = new BN(network_filename);
		BayesianNetwork bn = gen.getBayesianNetwork();

		for(int i=0;i<repetitions;i++){
			ExpDataSets dataSets = miss.generateInstance(bn, new Random(0)).generate(size, 0);
//			System.out.println(dataSets.partial.getMissingRate());
		}

		System.out.println("done");

	}

}
