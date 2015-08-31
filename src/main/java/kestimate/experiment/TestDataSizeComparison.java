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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kestimate.data.MissingnessClass;
import kestimate.data.NeighborMar;
import kestimate.estimator.IEstimator;
import kestimate.estimator.em.Prior;
import kestimate.estimator.k.CachedJointMcarK;
import kestimate.estimator.k.FactorMcarK;
import kestimate.estimator.k.JointMcarK;
import kestimate.estimator.k.MarK;
import kestimate.util.BN;

public class TestDataSizeComparison {

	public static void main(String[] args) throws IOException, InterruptedException {

		final String expClass= "test";
		
		ArrayList<IEstimator> estimators = new ArrayList<IEstimator>();

//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),1));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Bp(),1));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10));

//		estimators.add(new SeededEMEstimator(new JointEngineFactory.Jointree(),new FactorMcarK.MetaFactory(1).estimator()));
//		estimators.add(new SeededEMEstimator(new JointEngineFactory.Jointree(),new MarK.Factory(new FactorMcarK.MetaFactory(0)).estimator()));

//		estimators.add(new JointMcarK.Factory(0).estimator()); // no effect of settings
//		
////		estimators.add(new FactorMcarK.AvgFactory(0).estimator());
//		estimators.add(new FactorMcarK.AvgFactory(1).estimator());
////		estimators.add(new FactorMcarK.AvgFactory(2).estimator());
//		
////		estimators.add(new FactorMcarK.MedianFactory(0).estimator());
//		estimators.add(new FactorMcarK.MedianFactory(1).estimator());
////		estimators.add(new FactorMcarK.MedianFactory(2).estimator());
//		
//		estimators.add(new FactorMcarK.MetaFactory(0).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(0).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(2).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(3).estimator());
//		
////		estimators.add(new FactorMcarK.MaxFactory(0).estimator());
//		estimators.add(new FactorMcarK.MaxFactory(1).estimator());
////		estimators.add(new FactorMcarK.MaxFactory(2).estimator());
//

//		estimators.add(new JointMcarK.Factory(0).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(0).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator());
		
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(0), null).estimator());
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new FactorMcarK.MetaFactory(1)).estimator());
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); // best and fast
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(5), new CachedJointMcarK.Factory(0)).estimator());
		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(0),null).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(1),null).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new CachedJointMcarK.Factory(0)).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(5),new CachedJointMcarK.Factory(0)).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator());
		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(5),new FactorMcarK.MetaFactory(1)).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new JointMcarK.Factory(0)).estimator());
		
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(1)).estimator());
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(1)).estimator());
		
		//List<Integer> sizes = Arrays.asList(2<<6,2<<7,2<<8,2<<9,2<<10,2<<11,2<<12,2<<13,2<<14,2<<15,2<<16,2<<17,2<<18,2<<19,2<<20);
//		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000,10000000);
//		List<Integer> sizes = Arrays.asList(100,1000,10000);
		List<Integer> sizes = Arrays.asList(100);
		
		long timeout = 15*60*1000;

//		Missingness miss = new Mcar(0.5,0.5);
//		Missingness miss = new Mar(0.5, 2, 0.2, 0.2, false);
//		Missingness miss = new NeighborMar(0.5, 1, 1, 1);
//		MissingMech miss = new NeighborMar(0.8, 2, 2, 1);
//		MissingnessClass miss = new NeighborMar(0.3, 2, 1, 0.5);
//		MissingnessClass miss = new NeighborMar(0.75, 2, 1, 0.5);
		MissingnessClass miss = new NeighborMar(0.9, 2, 0.5, 0.5);

		int cores = Runtime.getRuntime().availableProcessors();
		int repetitions = 1;//cores-2;
		DataSizeComparison comp = new DataSizeComparison(expClass,estimators,sizes,miss,repetitions,timeout,cores);

		// load Bayesian network
		final boolean tractable = false;
//		String name = "fire_alarm";
//		String name = "alarm2";
//		String name = "win95pts";
//		String name = "90-20-1";
//		String name = "barley";
		String name = "munin1";
//		String name = "water";
		String network_filename = "networks/"+name+".uai";
		BN gen = new BN(network_filename);
		BayesianNetwork bn = gen.getBayesianNetwork();

		Prior prior = new Prior(BN.emptyNetwork(bn).cpts(),2); // laplace smoothing
//		Prior prior = new Prior(BNMethodMess.emptyNetwork(bn).cpts(),1);   // no prior
		
		comp.compare(bn, prior, name,tractable);
		
		comp.shutDown();
	}
	
}
