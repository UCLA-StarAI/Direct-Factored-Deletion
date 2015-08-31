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

package kestimate.util;

/**
 * Class to calculate running mean and variance from a set of samples 
 * without keeping them all in memory.
 */
public final class RunningStats {
	
	private int n = 0;
	private double oldM, newM, oldS, newS;

	public void add(double value) {
		n++;

		// See Knuth TAOCP vol 2, 3rd edition, page 232
		if (n == 1) {
			oldM = newM = value;
			oldS = 0.0;
		} else {
			newM = oldM + (value - oldM) / n;
			newS = oldS + (value - oldM) * (value - newM);

			// set up for next iteration
			oldM = newM;
			oldS = newS;
		}
	}
	
	public void add(double value, int count){
		//TODO optimize to closed form
		for(int i=0; i<count;i++) add(value);
	}

	public int getNbSamples() {
		return n;
	}

	public double getMean() {
		return (n > 0) ? newM : Double.NaN;
	}

	public double getVariance() {
		return ((n > 1) ? newS / (n - 1) : Double.NaN);
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double getEVStdDev() {
		return Math.sqrt(getVariance()/getNbSamples());
	}
	
	public double getEVVar() {
		return getVariance()/getNbSamples();
	}

	public Estimate getMeanEstimate(){
		return new Estimate(getMean(), getEVVar());
	}
	
	public void reset() {
		n = 0;
	}

}
