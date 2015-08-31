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

public class Estimate {

	public final double mean;
	public final double variance;
	
	public Estimate(double mean, double variance) {
//		if(Double.isNaN(mean) || variance < 0 || Double.isNaN(variance)) 
//			throw new IllegalArgumentException("Mean: " + mean + ", variance: "+variance);
		this.mean = mean;
		this.variance = variance;
	}
	
	public Estimate multiply(Estimate other) {
		double newMean = mean * other.mean;
		if(this.variance == Double.POSITIVE_INFINITY || other.variance == Double.POSITIVE_INFINITY) {
			return new Estimate(newMean,Double.POSITIVE_INFINITY);
		}
		double newVariance = variance * other.variance 
				+ variance * other.mean * other.mean 
				+ other.variance * mean * mean;
		return new Estimate(newMean,newVariance);
	}
	
	// The fixed effect model provides a weighted average of a series of study estimates. 
	// The inverse of the estimates' variance is commonly used as study weight, 
	// such that larger studies tend to contribute more than smaller studies to the weighted average.
	public Estimate inverseVarianceWeighting(Estimate other) {
		if(this.variance == Double.POSITIVE_INFINITY) return other;
		if(other.variance == Double.POSITIVE_INFINITY) return this;
		double w1 = 1/variance;
		double w2 = 1/other.variance;
		double norm = w1 + w2;
		w1 = w1 / norm;
		w2 = w2 / norm;
		double newMean = w1 * mean + w2 * other.mean;
		double newVariance = w1 * w1 * variance + w2 * w2 * other.variance; // incorrectly assume no covariance!
		return new Estimate(newMean,newVariance);
	}
	
}
