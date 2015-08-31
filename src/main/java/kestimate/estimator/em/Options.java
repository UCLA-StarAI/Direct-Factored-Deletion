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

package kestimate.estimator.em;

import kestimate.util.JointEngineFactory;

/**
 * This class maintains learning options for EM and EDML
 */

public class Options {
    public enum EdmlMode { PSETALT, PSET, CPT } // versions A/B/C
    public enum ConvMode { CHANGE_IN_PARAM, CHANGE_IN_MAP, MAP_LIMIT }

    // global algorithm parameters (EM/EDML)
    int maxIters;
    double threshold;
    double damp; // (EDML only)
    double mapLimit;
    long timeout;
    

    // inference options
    boolean pruneNetwork;

    EdmlMode edmlMode;
    ConvMode convMode;
	JointEngineFactory jointEngineFactory;
	boolean anytime;
    
    

    // default options
    public Options() {
        this.maxIters = 80;
        this.threshold = 1e-4;
        this.damp = 0.5;
        this.mapLimit = Double.NEGATIVE_INFINITY;
        this.pruneNetwork = false;
        this.edmlMode = EdmlMode.PSET;
        this.convMode = ConvMode.CHANGE_IN_PARAM;
        timeout = Long.MAX_VALUE;
        this.jointEngineFactory = null;
        anytime = false;
    }

    // default options
    public Options(Options opts) {
        this.maxIters           = opts.maxIters;
        this.threshold          = opts.threshold;
        this.damp               = opts.damp;
        this.mapLimit           = opts.mapLimit;
        this.pruneNetwork       = opts.pruneNetwork;
        this.edmlMode           = opts.edmlMode;
        this.convMode           = opts.convMode;
        this.timeout 			= opts.timeout;
        this.jointEngineFactory = opts.jointEngineFactory;
        anytime = opts.anytime;
    }

    public void setMaxIters(int opt) { maxIters = opt; }
    public void setThreshold(double opt) { threshold = opt; }
    public void setDamp(double opt) { damp = opt; }
    public void setPruneNetwork(boolean opt) { pruneNetwork = opt; }
    public void setEdmlMode(EdmlMode opt) { edmlMode = opt; }
    public void setConvMode(ConvMode opt) { convMode = opt; }
    public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

    public void setMapLimit(double opt) {
        mapLimit = opt;
        convMode = ConvMode.MAP_LIMIT;
    }

	public void setJointEngineFactory(JointEngineFactory jointEngineFactory) {
		this.jointEngineFactory=jointEngineFactory;
	}

	public void setAnytime(boolean anytime) {
		this.anytime = anytime;
	}
}
