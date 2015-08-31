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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kestimate.data.MissingnessClass;
import kestimate.data.NeighborMar;
import kestimate.estimator.IEstimator;
import kestimate.estimator.em.Prior;
import kestimate.estimator.em.RestartEMEstimator;
import kestimate.estimator.em.SeededEMEstimator;
import kestimate.estimator.k.FactorMcarK;
import kestimate.estimator.k.JointMcarK;
import kestimate.estimator.k.MarK;
import kestimate.util.BN;
import kestimate.util.JointEngineFactory;

public class DataSizeExperiments {


	public static void main(String[] args) throws IOException, InterruptedException {

		String server = java.net.InetAddress.getLocalHost().getHostName();
		final int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Running on "+server+" with "+cores+" cores");       

		ArrayList<String> datasets = new ArrayList<String>();
		ArrayList<IEstimator> estimators = new ArrayList<IEstimator>();
		ArrayList<MissingnessClass> mechanisms = new ArrayList<MissingnessClass>();
		

//		datasets.add("asia");
//		datasets.add("alarm2");
//		datasets.add("win95pts");   
//		datasets.add("emdec6g");  
//		datasets.add("tcc4e");                
//		datasets.add("diagnose_a");
//		datasets.add("cpcs54");
//		datasets.add("andes");
		
		//INTRACTABLE
//		final boolean tractable = false;
//		final boolean anytime = false;

//		datasets.add("90-20-1");
//		datasets.add("water");
//		datasets.add("munin1");
//////		datasets.add("link"); doesnt work
//		datasets.add("barley");
		
//		datasets.add("90-20-2");
//		datasets.add("90-20-3");
//		datasets.add("90-20-4");

		
		//		List<Integer> sizes = Arrays.asList(2<<6,2<<7,2<<8,2<<9,2<<10,2<<11,2<<12,2<<13,
		//				2<<14,2<<15,2<<16,2<<17,2<<18,2<<19,2<<20);
//		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000);

//		//========
//		// EM-based
//		//========
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),1,anytime));
////		estimators.add(new RestartEMEstimator(new JointEngineFactory.Bp(),1));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10,anytime));
////		estimators.add(new RestartEMEstimator(new JointEngineFactory.Bp(),10,anytime));
////
//		estimators.add(new SeededEMEstimator(
//				new JointEngineFactory.Jointree(),
//				new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator(),
//				anytime));
////		
//
//		//========
//		// MCAR
//		//========
////		estimators.add(new FactorMcarK.MetaFactory(0).estimator());
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator()); //BEST
//		estimators.add(new JointMcarK.Factory(0).estimator());  //BEST2
////		estimators.add(new JointMcarK.Factory(1).estimator());
//////		
//
//		//========
//		// MAR
//		//========
////		estimators.add(new MarK.Factory(new JointMcarK.Factory(0), null).estimator());
////		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(0),null).estimator());
////		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(1),null).estimator());
//		
////		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new FactorMcarK.MetaFactory(1)).estimator());
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); //D-MAR
//		
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator()); //F-MAR
////		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new JointMcarK.Factory(0)).estimator());
		
		
//
////		// Using missingness graph
////		estimators.add(new SeparatorK.Factory(new JointMcarK.Factory(0)).estimator());
////		estimators.add(new SeparatorK.Factory(new FactorMcarK.MetaFactory(0)).estimator());
////		estimators.add(new SeparatorK.Factory(new FactorMcarK.MetaFactory(1)).estimator());
//		
//////		// Using missingness graph
//		estimators.add(new MnarK.Factory(new JointMcarK.Factory(0)).estimator());
//		estimators.add(new MnarK.Factory(new FactorMcarK.MetaFactory(0)).estimator());
//		estimators.add(new SeededEMEstimator(new MnarK.Factory(new FactorMcarK.MetaFactory(0)).estimator()));
//		estimators.add(new MnarK.Factory(new FactorMcarK.MetaFactory(1)).estimator());
//		estimators.add(new SeededEMEstimator(new MnarK.Factory(new FactorMcarK.MetaFactory(1)).estimator()));
//		estimators.add(new Mnar2K.Factory().estimator());


//		mechanisms.add(new Mcar(0.5, 0.5));
//		mechanisms.add(new Mcar(0.3, 0.7));
//		mechanisms.add(new Mcar(0.9, 0.5));
		
//		mechanisms.add(new NeighborMar(0.8, 2, 2, 1));
//		mechanisms.add(new NeighborMar(0.8, 1, 2, 1));
//		mechanisms.add(new NeighborMar(0.3, 1, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.3, 1, 0.5, 1));

//		if(server.equals("exclusion.cs.ucla.edu")){
//			mechanisms.add(new NeighborMar(0.9, 2, 0.5, 0.5));
//		}else if(server.equals("inclusion.cs.ucla.edu")){
//			mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
//		}else if(server.equals("gazelle")){
//			mechanisms.add(new NeighborMar(0.9, 2, 0.5, 0.5));
//		}else throw new IllegalStateException("Don't know server "+server);
		
//		mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.6, 2, 0.5, 0.5));
		
//		mechanisms.add(new NeighborMar(0.75, 2, 0.5, 0.5));
//		mechanisms.add(new NeighborMar(0.5, 1, 0.5, 0.5));
//		mechanisms.add(new NeighborMar(0.3, 2, 0.5, 1));
//		mechanisms.add(new NeighborMar(0.3, 2, 0.3, 0.3));
//		mechanisms.add(new NeighborMar(0.7, 2, 0.3, 0.3));
		
//		mechanisms.add(new NeighborMar(0.2, 2, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.2, 2, 0.5, 1));
//		mechanisms.add(new NeighborMar(0.2, 2, 0.4, 0.2));
//		mechanisms.add(new NeighborMar(0.2, 2, 0.2, 0.4));
//		mechanisms.add(new NeighborMar(0.8, 1, 1, 2));
//		mechanisms.add(new NeighborMar(0.8, 2, 0.5, 0.5));
//		mechanisms.add(new NeighborMar(0.5, 2, 0.2, 0.2));
		
//		// Using missingness graph
//		mechanisms.add(new MgMar(0.3, 2, 3, 1.0, 0.5));
//		mechanisms.add(new MgMar(0.3, 2, 3, 0.5, 1.0));
//		mechanisms.add(new MgMar(0.3, 2, 3, 0.7, 0.7));

//		// Using missingness graph -not MAR
//		mechanisms.add(new MgMnar2(0.5, 1));
//		mechanisms.add(new MgMnar2(1, 0.5));
//		mechanisms.add(new MgMnar2(0.2, 0.2));
//		mechanisms.add(new MgMnar(0.7, 2, 3, 0.5, 1));
//		mechanisms.add(new MgMnar(0.7, 4, 3, 0.5, 1));
//		mechanisms.add(new MgMnar(0.9, 2, 3, 0.5, 1));
////		mechanisms.add(new MgMnar(0.1, 2, 3, 0.5, 1.0));
////		mechanisms.add(new MgMnar(0.1, 2, 3, 0.3, 0.3));
////		mechanisms.add(new MgMnar(0.3, 2, 3, 1.0, 0.5));
////		mechanisms.add(new MgMnar(0.3, 2, 3, 0.5, 1.0));
////		mechanisms.add(new MgMnar(0.3, 2, 3, 0.3, 0.3));

//		long[] timeouts = new long[]{1*60*1000,5*60*1000,25*60*1000};
		
//		final String expClass= "MCAR-NIPS";

//		int parallel = 3;

//		// MAR FOR FIREALARM
//		final String expClass= "MAR-NIPS";
//		final boolean tractable = true;
//		final boolean anytime = false;
//		datasets.add("fire_alarm");
//		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000,10000000);
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),1,anytime));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10,anytime));
//		estimators.add(new SeededEMEstimator(
//				new JointEngineFactory.Jointree(),
//				new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator(),
//				anytime));
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator()); //BEST
//		estimators.add(new JointMcarK.Factory(0).estimator());  //BEST2
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); //D-MAR
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator()); 
//		mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.8, 2, 0.5, 0.5));
//		long[] timeouts = new long[]{100000*60*1000}; // no timeout
//		final int parallel = 1;
//		final int repetitions = 2*cores;
		

//		// MAR FOR ALARM2
//		final String expClass= "MAR-NIPS";
//		final boolean tractable = true;
//		final boolean anytime = false;
////		datasets.add("alarm2");
//		datasets.add("win95pts");   
//		datasets.add("emdec6g");  
//		datasets.add("cpcs54");
//		datasets.add("andes");
//		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000,10000000);
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),1,anytime));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10,anytime));
//		estimators.add(new SeededEMEstimator(
//				new JointEngineFactory.Jointree(),
//				new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator(),
//				anytime));
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator()); //BEST
//		estimators.add(new JointMcarK.Factory(0).estimator());  //BEST2
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); //D-MAR
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator()); 
//		mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.9, 2, 0.5, 0.5));
//		long[] timeouts = new long[]{10*60*1000}; // 5 minutes
//		final int parallel = 1;
//		final int repetitions = 1*cores;
		
		
//		// MAR FOR INTRACTABLE NETWORKS
//		final String expClass= "MAR-NIPS";
//		final boolean tractable = false;
//		final boolean anytime = true;
////		datasets.add("alarm2");
//		datasets.add("win95pts");   
//		datasets.add("emdec6g");  
//		datasets.add("cpcs54");
//		datasets.add("andes");
//		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000,10000000);
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),1,anytime));
//		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10,anytime));
//		estimators.add(new SeededEMEstimator(
//				new JointEngineFactory.Jointree(),
//				new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator(),
//				anytime));
//		estimators.add(new FactorMcarK.MetaFactory(1).estimator()); //BEST
//		estimators.add(new JointMcarK.Factory(0).estimator());  //BEST2
//		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); //D-MAR
//		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator()); 
//		mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
//		mechanisms.add(new NeighborMar(0.9, 2, 0.5, 0.5));
//		long[] timeouts = new long[]{10*60*1000}; // 5 minutes
//		final int parallel = 1;
//		final int repetitions = 1*cores;
		

		// MCAR FOR ALARM2
		final String expClass= "MCAR-NIPS";
		final boolean tractable = true;
		final boolean anytime = false;
		datasets.add("90-20-1");
		datasets.add("water");
		datasets.add("munin1");
////		datasets.add("link"); doesnt work
		datasets.add("barley");
		List<Integer> sizes = Arrays.asList(100,1000,10000,100000,1000000,10000000);
		estimators.add(new RestartEMEstimator(new JointEngineFactory.Jointree(),10,anytime));
		estimators.add(new RestartEMEstimator(new JointEngineFactory.Bp(),10,anytime));
		estimators.add(new FactorMcarK.MetaFactory(1).estimator()); //BEST
		estimators.add(new JointMcarK.Factory(0).estimator());  //BEST2
		estimators.add(new MarK.Factory(new JointMcarK.Factory(4), new JointMcarK.Factory(0)).estimator()); //D-MAR
		estimators.add(new MarK.Factory(new FactorMcarK.MetaFactory(4),new FactorMcarK.MetaFactory(1)).estimator()); 
		mechanisms.add(new NeighborMar(0.3, 2, 1, 0.5));
		mechanisms.add(new NeighborMar(0.9, 2, 0.5, 0.5));
//		long[] timeouts = new long[]{1*60*1000,5*60*1000,25*60*1000};
//		final int parallel = 1;
		long[] timeouts = new long[]{125*60*1000};
		final int parallel = 3;
		final int repetitions = cores/parallel;


		ExecutorService executor = Executors.newFixedThreadPool(parallel);

		for(final long timeout: timeouts){
			for(final String name: datasets){
	
				// load Bayesian network
				String network_filename = "networks/"+name+".uai";
				BN gen = new BN(network_filename);
				final BayesianNetwork bn = gen.getBayesianNetwork();
	
				final Prior prior = new Prior(BN.emptyNetwork(bn).cpts(),2); // laplace smoothing
	
				for(final MissingnessClass mechanism: mechanisms){
					
					System.out.println("Learning setting: " + repetitions + " repetitions, " + mechanism + ", and timeout " + timeout );
					final DataSizeComparison comp = new DataSizeComparison(expClass,estimators,sizes,mechanism,
															repetitions,timeout,cores/parallel);
	
					executor.submit(new Runnable() {
	
						@Override
						public void run() {
							try {
								System.out.println("Learning dataset " + name + ",  mechanism " + mechanism + ", "+repetitions + " repetitions, timeout " + timeout );
								comp.compare(bn, prior, name, tractable);
								comp.shutDown();
								System.out.println("Done with dataset " + name + ",  mechanism " + mechanism + ", "+repetitions + " repetitions, timeout " + timeout );
							} catch (Exception e) {
								System.out.println("Error during experiment " + comp + " on " + name + ": " + e);
								e.printStackTrace();
							}
						}
					});
	
				}
	
			}
		}

		executor.shutdown();

	}

}
