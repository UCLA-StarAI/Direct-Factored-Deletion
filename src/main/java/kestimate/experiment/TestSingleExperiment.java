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

public class TestSingleExperiment {

	public static void main(String[] args) throws IOException {
		
		final boolean tractable = false;
		
//		String name = "spect";
//		String name = "alarm2";   
//		String name = "tcc4e";           
//		String name = "diagnose_a";
//		String name = "emdec6g";
//		String name = "andes";
//				String name = "win95pts";
//				String name = "barley";
//				String name = "cpcs54";
		String name = "munin1";
//		String name = "water";
//		String name = "link";
//		String name = "barley";
//		String name = "90-20-1";

//		IEstimator estimator = new RestartEMEstimator(new JointEngineFactory.Jointree(),5,false);
		IEstimator estimator = new RestartEMEstimator(new JointEngineFactory.Bp(),1,false);
//		IEstimator estimator = (new MarK.Factory(new FactorMcarK.MetaFactory(0))).estimator();
//		IEstimator estimator = (new MarK.Factory(new FactorMcarK.MetaFactory(1))).estimator();
//		IEstimator estimator = new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator();
//		IEstimator estimator = new MarK.Factory(new FactorMcarK.MetaFactory(4), new FactorMcarK.MetaFactory(1)).estimator();
//		IEstimator estimator = new MarK.Factory(new FactorMcarK.MetaFactory(0),null).estimator();
//		IEstimator estimator = new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator();
//		IEstimator estimator = (new FactorMcarK.MetaFactory(1)).estimator();

		int size = 10;
		int repetitions = 10;
//		MissingnessClass miss = new NeighborMar(0.75, 2, 0.5, 0.5);
		MissingnessClass miss = new NeighborMar(0.9, 2, 0.5, 0.5);
//		MissingMech miss = new NeighborMar(0.3, 2, 1, 0.5);
		long timeout = 10*60*1000;

		System.out.println("Loading network");
		String network_filename = "networks/"+name+".uai";
		BN gen = new BN(network_filename);
		BayesianNetwork bn = gen.getBayesianNetwork();
		Prior prior = new Prior(BN.emptyNetwork(bn).cpts(),2); // laplace smoothing

		double avgKld = 0, avgTest = 0;
		for(int i=0;i<repetitions;i++){
			System.out.println("Generating data of size "+size);
			ExpDataSets dataSets = miss.generateInstance(bn, new Random(0)).generate(size, 0);

			System.out.println("Learning with " + estimator);
			long time = -System.currentTimeMillis();
			long deadline = System.currentTimeMillis() + timeout;
			BayesianNetwork learnedBn = estimator.estimate(BN.emptyNetwork(bn), prior, dataSets.partial.copy(), deadline);
			time += System.currentTimeMillis();
			System.out.println("Learning took: " + time + "ms");

			time = -System.currentTimeMillis();
			double partialLl = Double.NaN;
			if(tractable){
				partialLl = dataSets.partial.logLikelihood(learnedBn)/dataSets.partial.size;
				System.out.println("Partial Log Likelihood: " + partialLl);
			}
		
			double fullLl = dataSets.full.logLikelihood(learnedBn)/dataSets.full.size;
			System.out.println("Full Log Likelihood: " + fullLl);

			double testLl = dataSets.test.logLikelihood(learnedBn)/dataSets.test.size;
			System.out.println("Test Log Likelihood: " + testLl);
			avgTest += testLl/repetitions;

			double kld = Double.NaN;
			if(tractable){
				kld = BN.kl_bn(bn, learnedBn);
				System.out.println("KL Divergence: " + kld);
			}
			avgKld += kld/repetitions;

			time += System.currentTimeMillis();
			System.out.println("Evaluation took: " + time + "ms");

		}

		System.out.println("Avg Test LL: " + avgTest);
		System.out.println("Avg KL Divergence: " + avgKld);

		System.out.println("done");

	}

}
