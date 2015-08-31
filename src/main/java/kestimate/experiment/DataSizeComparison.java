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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kestimate.data.ExpDataSets;
import kestimate.data.MissingnessGraph;
import kestimate.data.MissingnessClass;
import kestimate.estimator.IEstimator;
import kestimate.estimator.em.Prior;
import kestimate.util.BN;
import kestimate.util.Global;
import kestimate.util.TimeoutException;

public class DataSizeComparison {

	private final List<IEstimator> estimators;
	private final List<Integer> sizes;
	private final int repetitions;
	private final long timeout;
	private final MissingnessClass missClass;
	private ExecutorService executor;
	private final String expClass;
	private final int numCores;

	public DataSizeComparison(String expClass, List<IEstimator> estimators, List<Integer> sizes, 
			MissingnessClass missClass, int repetitions, long timeout, int numCores) {
		this.expClass = expClass;
		this.estimators = estimators;
		this.sizes = new ArrayList<Integer>(sizes);
		this.repetitions = repetitions;
		this.timeout = timeout;
		this.missClass=missClass;
		this.numCores = numCores;
		this.executor = Executors.newFixedThreadPool(numCores);
	}
	
//	@Override
//	public String toString() {
//		return "DataSizeComparison with expClass:"+ expClass+", missClass:"+missClass+", repetitions:"+repetitions
//				+", timeout:"+timeout+", dataTimeout"+dataTimeout+", numCores"+numCores;
//	}

	static class Stats{

		public void invalidate(){
			avgPartialLl = Double.NaN;
			avgFullLl = Double.NaN;
			avgTestLl = Double.NaN;
			avgTime = Double.NaN;
			avgKld = Double.NaN;
		}

		double avgPartialLl = 0, avgFullLl = 0, avgTestLl = 0, avgTime = 0, avgKld = 0;

	}

	public void compare(final BayesianNetwork bn, final Prior prior, String name, final boolean tractable) 
			throws IOException, InterruptedException {

		File experimentDir = new File("experiments/"+expClass+"/"+missClass+"/"+name+"-"+timeout+"/");
		experimentDir.mkdirs();


		for(IEstimator estimator: estimators){
			// write dat file headers
			File dataFile = new File(experimentDir,estimator.toString()+".dat");
			PrintWriter data = new PrintWriter(new FileWriter(dataFile));
			data.printf("%10s %15s %15s %15s %15s %15s\n", 
					"Size", "Time [ms]", "PartialLL", "FullLL", "TestLL", "KL Divergence");
			data.close();
		}

		List<MissingnessGraph> missGraphs = generateMissingnessGraphs(bn);

		final Set<IEstimator> intractableEstimators = new HashSet<IEstimator>();

		for(final int size : sizes){

			if(intractableEstimators.size() == estimators.size()) break;
			
			System.out.println("Dataset size is " + size);
			final ExpDataSets[] allDataSets;
			try {
				allDataSets = generateDataSets(missGraphs, size);
			} catch (TimeoutException e1) {
				return;
			}

			for(final IEstimator estimator: estimators){

				if(!intractableEstimators.contains(estimator)){

					final Stats stats = new Stats();

					List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

					for(final int repeat: Global.range(0,repetitions)){

						tasks.add(new Callable<Void>(){

							@Override
							public Void call() {
								try{
									boolean canceled;
									synchronized(estimator){
										canceled = intractableEstimators.contains(estimator);
									}
									if(!canceled){
										ExpDataSets dataSets = allDataSets[repeat];

										System.out.println("Starting " + estimator);
										long startTime = System.currentTimeMillis();
										long deadline = startTime + timeout;

										try{
											BayesianNetwork learnedBn = estimator.estimate(BN.emptyNetwork(bn), prior, dataSets.partial.copy(),deadline);
											long timeAfter = System.currentTimeMillis();

											long time = timeAfter - startTime;
											System.out.println("Learning Time: " + time + "ms");

											double partialLl = Double.NaN;
											if(tractable){
												partialLl = dataSets.partial.logLikelihood(learnedBn)/dataSets.partial.size;
												System.out.println("Partial Log Likelihood: " + partialLl);
											}

											double fullLl = dataSets.full.logLikelihood(learnedBn)/dataSets.full.size;
											System.out.println("Full Log Likelihood: " + fullLl);

											double testLl = dataSets.test.logLikelihood(learnedBn)/dataSets.test.size;
											System.out.println("Test Log Likelihood: " + testLl);

											double kld = Double.NaN;
											if(tractable){
												kld = BN.kl_bn(bn, learnedBn);
												System.out.println("KL Divergence: " + kld);
											}

											long evaltime = System.currentTimeMillis()-timeAfter;
											System.out.println("Eval Time: " + evaltime + "ms");
											if(evaltime > timeout*3){
												System.out.println("Evaluation took too long: I wont increase data set size any more.");
												sizes.clear();
											}

											synchronized(estimator){
												stats.avgTime += time*1.0/repetitions;
												stats.avgPartialLl += partialLl/repetitions;
												stats.avgFullLl += fullLl/repetitions;
												stats.avgTestLl += testLl/repetitions;
												stats.avgKld += kld/repetitions;
											}
										}catch(TimeoutException e){
											synchronized(estimator){
												intractableEstimators.add(estimator);
												stats.invalidate();
												stats.avgTime = e.now - startTime;
											}
										}catch(OutOfMemoryError|IllegalStateException e){
											// thrown by the jointree implementation when intractable
											e.printStackTrace();
											synchronized(estimator){
												intractableEstimators.add(estimator);
												stats.invalidate();
												stats.avgTime = -1;
											}
										}
									}
								}catch(Throwable e){
									e.printStackTrace();
									System.exit(-1);
								}
								return null;
							}

						});

					}

					executor.invokeAll(tasks);

					// write to dat file
					File dataFile = new File(experimentDir,estimator.toString()+".dat");
					PrintWriter data = new PrintWriter(new FileWriter(dataFile,true));
					data.printf("%10d %15f %15f %15f %15f %15f\n", 
							size, stats.avgTime, stats.avgPartialLl, stats.avgFullLl, stats.avgTestLl, stats.avgKld);
					data.flush(); // should not be needed
					data.close();

				}
			}
		}
	}

	private List<MissingnessGraph> generateMissingnessGraphs(BayesianNetwork bn) {
		long start = System.currentTimeMillis();
		List<MissingnessGraph> graphs = new ArrayList<MissingnessGraph>(repetitions);
		for(int repeat=0; repeat < repetitions; repeat++) {
			graphs.add(missClass.generateInstance(bn, new Random(repeat)));
		}
		System.out.println("Generating missing data mechanisms took " + (System.currentTimeMillis()-start)+"ms");
		return graphs;
	}

	private ExpDataSets[] generateDataSets(final List<MissingnessGraph> missGraphs,
			final int size) throws InterruptedException {
		long start = System.currentTimeMillis();
		System.out.println("Starting to generate "+repetitions+" datasets of size "+size);
		final ExpDataSets[] allDataSets = new ExpDataSets[repetitions];
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		for(int repeat=0; repeat < repetitions; repeat++) {
			final int myrepeat = repeat;
			tasks.add(new Callable<Void>(){
				@Override
				public Void call() {
					allDataSets[myrepeat] = missGraphs.get(myrepeat).generate(size, myrepeat);
					System.out.println("Generated datasets for repeat "+myrepeat);
					return null;
				}
			});
		}
		System.out.println("Starting "+tasks.size()+" tasks");
		executor.invokeAll(tasks);
		System.out.println("Generating datasets of size "+size+" took " + (System.currentTimeMillis()-(start))+"ms");
		return allDataSets;
	}

	public void shutDown() {
		executor.shutdown();
	}

}
