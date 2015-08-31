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

import il2.model.BayesianNetwork;
import il2.model.Domain;
import il2.util.IntMap;

/**
 * This class maintains a learning task for EM and EDML
 *
 * TODO:
 * - list of CPTs to learn (AC)
 */

public class Task {

    // network
    BayesianNetwork seed; // also used for structure
    int[] cptsToLearn;

    // data
    IntMap[] data;
    int[] counts;
    boolean useUnique;    // use compressed representation of data

    // prior
    Prior prior;

    // logging
    Logger logger;
	final public double nbInstances;

    public Task(BayesianNetwork seed, IntMap[] data) {
        this(seed,data,null);
    }

    public Task(BayesianNetwork seed, IntMap[] data, int[] counts) {
        // Dirichlet prior with exponent 2, for all parameter set
        this(seed,data,counts,new Prior(seed.cpts(),2));
    }

    public Task(BayesianNetwork seed, 
                IntMap[] data, int[] counts, 
                Prior prior) {
        this.seed = seed;

        this.data = data;
        this.counts = counts;
        this.useUnique = (counts != null);

        this.prior = prior;
        this.logger = new Logger();
        
        int countSum = 0;
        for(int c: counts) countSum += c;
        nbInstances = countSum;
    }

    void initializeCptsToLearn() {
        cptsToLearn = new int[seed.size()];
        for (int i = 0; i < seed.size(); i++)
            cptsToLearn[i] = i;
    }
    
    public Domain getDomain() { return seed.domain(); }
    public BayesianNetwork getSeed() { return seed; }
    public IntMap[] getData() { return data; }
    public int[] getCounts() { return counts; }
    public boolean getUseUnique() { return useUnique; }
    public Prior getPrior() { return prior; }
    public Logger getLogger() { return logger; }
    public int[] getCptsToLearn() { return cptsToLearn; }

    public void setCptsToLearn(int[] opt) { this.cptsToLearn = opt; }
    public void setLogger(java.io.PrintWriter writer, String name) {
        logger.setLogger(writer,name);
    }
}
