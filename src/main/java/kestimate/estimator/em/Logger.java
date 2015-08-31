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

import il2.util.IntMap;

import java.io.PrintWriter;


public class Logger {
    PrintWriter writer;
    String name;
    long start_time;

    Task task;

    public Logger() {
        this.writer = null;
        this.name = "no_name";
    }

    public void setLogger(PrintWriter writer, String name) {
        this.writer = writer;
        this.name = name;
    }

    public void startLogger(Task task) {
        this.task = task;
        start_time = System.nanoTime();
    }
    public long stopLogger() {
        return System.nanoTime()-start_time;
    }

    public void startLogger(IntMap[] data, int[] counts, Prior prior) {
        startLogger(new Task(null,data,counts,prior));
    }

    void logData(double map, int iteration) {
        double time = (System.nanoTime()-start_time)*1e-6;
        logData(name,map,iteration,time);
    }

    /*
    void logData(Table[] cpts, int iterations) {
        if ( this.writer == null ) return;
        double time = (System.nanoTime()-start_time)*1e-6;
        double ll = EmUtil.logLikelihood(cpts,task.data,task.counts);
        double pr = EmUtil.logPrior(cpts,task.prior);
        logData(name,ll+pr,iterations,time);
    }

    void logData(BayesianNetwork bn) {
        if ( this.writer == null ) return;
        Table[] cpts = bn.cpts();
        double ll = EmUtil.logLikelihood(cpts,task.data,task.counts);
        double pr = EmUtil.logPrior(cpts,task.prior);
        logData("BN",ll+pr,0,0.0);
    }
    */

    void logData(String name, double map, int iteration, double time) {
        System.out.printf("%s,MAP,%d,%.12g,%.12g\n",name,iteration,time,map);
        if ( this.writer == null ) return;
        writer.printf("%s,MAP,%d,%.12g,%.12g\n",name,iteration,time,map);
    }

}
