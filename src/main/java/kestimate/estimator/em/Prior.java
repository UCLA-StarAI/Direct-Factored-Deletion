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
import il2.model.Index;
import il2.model.Table;

import java.util.Arrays;

public class Prior {
    Domain d;
    double[][] cpt_psis;
    double network_psi;

    /*
    public Prior(Domain d) {
        this(d,null,2);
    }
    */

    /** 
     * domains is an array of CPT domains (can be an array of Table)
     * psi represents a Dirichlet prior for all network parameter sets
     */
    public Prior(Index[] domains, double psi) {
        this(domains[0].domain(),null,psi);
        cpt_psis = new double[d.size()][];
        for (int i = 0; i < cpt_psis.length; i++) {
            cpt_psis[i] = new double[domains[i].sizeInt()];
            Arrays.fill(cpt_psis[i],psi);
        }
    }

    public Prior(Domain d, double[][] psis) {
        this(d,psis,2);
    }

    Prior(Domain d, double[][] psis, double psi) {
        this.d = d;
        this.cpt_psis = psis;
        this.network_psi = psi;
    }

    /**
     * returns the Dirichlet prior for variable v and parent
     * instantiation index uindex
     */
    public double[] getPsis(int var, int uindex) {
        double[] psis;
        int size = d.size(var);
        if ( cpt_psis == null ) {
            psis = new double[size];
            Arrays.fill(psis,network_psi);
        } else {
            psis = EmUtil.cptColumn(cpt_psis[var],size,uindex);
        }
        return psis;
    }

    public double[] getTablePsis(int var) {
        return cpt_psis[var];
    }

	public double logPrior(BayesianNetwork bn) {
		Domain d = bn.cpts()[0].domain();
		double logp = 0.0;
		for (int var = 0; var < d.size(); var++) {
			Table cpt = bn.cpts()[var];
			int usize = cpt.values().length/d.size(var);
			for (int uindex = 0; uindex < usize; uindex++) {
				double[] pr = EmUtil.cptColumn(cpt,uindex);
				double[] psis = getPsis(var,uindex);
				for (int x = 0; x < pr.length; x++) {
					if ( psis[x] == 1.0 ) continue;
					logp += (psis[x]-1) * Math.log(pr[x]);
				}
			}
		}
		return logp;
	}
	
}
