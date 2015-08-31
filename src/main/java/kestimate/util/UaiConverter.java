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

import il2.model.BayesianNetwork;
import il2.util.IntMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import edu.ucla.belief.BeliefNetwork;
import edu.ucla.belief.FiniteVariable;
import edu.ucla.belief.Table;
import edu.ucla.belief.TableShell;

public class UaiConverter {

	public static void main(String[] args) {
		if ( args.length != 1 ) { 
			System.err.println("usage: program net1");
			return;
		}
		String base = args[0].substring(0,args[0].lastIndexOf("."));
		String ext1 = args[0].substring(args[0].lastIndexOf(".")+1);
		String ext2 = null;
		if (ext1.equals("net")) ext2 = "uai"; else ext2 = "net";

		String input_filename =  base+"."+ext1, 
   			   output_filename = base+"."+ext2;

		if (ext1.equals("uai")) {
			BeliefNetwork bn = uaiToBeliefNetwork(input_filename);
			//BayesianNetwork bn2 = uaiToBayesianNetwork(input_filename);
			//Map<String,String> evidence = evidToInst(input_filename+".evid");
			saveNetwork(bn, output_filename);
			//saveInst(evidence,output_filename+".inst");
		} else {
			BeliefNetwork bn = openNetwork(input_filename);
			List vars = sortedTopologicalOrder(bn);
			netToUai(vars,output_filename);
			//Map<String,String> evidence = openInst(input_filename+".inst");
			//instToEvid(bn,vars,evidence,output_filename+".evid");
		}
	}

	//////////////////////////////////////////////////
	// conversion: SamIam -> Irvine
	//////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
	public static void netToUai(List vars, String filename) {
		System.err.println("Saving network  : " + filename);
		java.io.PrintWriter pw = getPrintWriter(filename);
		
		// preamble
		Map<FiniteVariable,Integer> var2index = getVarToIndexMap(vars);
		pw.println("BAYES");
		pw.println(vars.size());
		
		for (int i = 0; i < vars.size(); i++) {
			FiniteVariable var = (FiniteVariable)vars.get(i);
			pw.print((i==0?"":" ") + var.size());
		}
		pw.println();
		pw.println(vars.size());
		
		for (int i = 0; i < vars.size(); i++) {
			FiniteVariable var = (FiniteVariable)vars.get(i);
			Table t = var.getCPTShell().getCPT();
			List tvars = permute(t.variables(),vars);
			pw.print(tvars.size());
			for (int j = 0; j < tvars.size(); j++) {
				FiniteVariable tvar = (FiniteVariable)tvars.get(j);
				pw.print(" " + var2index.get(tvar));
			}
			pw.println();
		}

		// function tables
		pw.println();
		for (int i = 0; i < vars.size(); i++) {
			FiniteVariable var = (FiniteVariable)vars.get(i);
			int size = var.size();
            Table t = permute(var.getCPTShell().getCPT(),vars);
			double[] values = t.dataclone();
			pw.println(values.length);
			for (int row = 0; row < values.length/size; row++) {
				for (int col = 0; col < size; col++)
					pw.print(" " + values[row*size+col]);
				pw.println();
			}
			pw.println();
		}

		pw.close();
	}

	public static void instToEvid(BeliefNetwork bn, List vars, 
								  Map evidence, String filename) {
		System.err.println("Saving evidence : " + filename);
		java.io.PrintWriter pw = getPrintWriter(filename);

		Map<FiniteVariable,Integer> var2index = getVarToIndexMap(vars);
		pw.println(evidence.keySet().size());
		for (Iterator it = evidence.keySet().iterator(); it.hasNext(); ) {
			String varID = (String)it.next();
			FiniteVariable var = (FiniteVariable)bn.forID(varID);
			String instance = (String)evidence.get(varID);
			int varIndex = var2index.get(var);
			int instanceIndex = var.index(instance);
			pw.println(" " + varIndex + " " + instanceIndex);
		}

		pw.close();
	}

	//////////////////////////////////////////////////
	// conversion: UAI -> SamIam
	//////////////////////////////////////////////////

	public static UaiNetwork openUaiNetwork
		(String filename, double[] return_time) {
		long time = System.currentTimeMillis();
		UaiNetwork uaibn = openUaiNetwork(filename);
		time = System.currentTimeMillis() - time;
		if ( return_time != null && return_time.length > 0 )
			return_time[0] = time/1000.0;
		return uaibn;
	}

	public static UaiNetwork openUaiNetwork(String filename) {
		java.util.StringTokenizer uai 
			= new java.util.StringTokenizer(loadFile(filename));

		// preamble
		UaiNetwork.Network type 
			= Enum.valueOf(UaiNetwork.Network.class,uai.nextToken());
		int numVars = Integer.parseInt( uai.nextToken() );
		int[] sizes = new int[numVars];
		for (int i = 0; i < numVars; i++)
			sizes[i] = Integer.parseInt( uai.nextToken() );
		int numPots = Integer.parseInt( uai.nextToken() );
		int[][] potVars = new int[numPots][];
		for (int i = 0; i < numPots; i++) {
			int potSize = Integer.parseInt( uai.nextToken() );
			potVars[i] = new int[potSize];
			for (int j = 0; j < potSize; j++)
				potVars[i][j] = Integer.parseInt( uai.nextToken() );
		}

		// function tables
		double[][] pots = new double[numPots][];
		for (int i = 0; i < numPots; i++) {
			int potLength = Integer.parseInt( uai.nextToken() );
			pots[i] = new double[potLength];
			for (int j = 0; j < potLength; j++)
				pots[i][j] = Double.parseDouble( uai.nextToken() );
		}

		return new UaiNetwork(numVars, numPots, sizes, potVars, pots, type);
	}

	public static BeliefNetwork uaiToBeliefNetwork(String filename) {
		UaiNetwork uaibn = openUaiNetwork(filename);
		int numVars = uaibn.numVars;
		int numPots = uaibn.numPots;
		int[] sizes = uaibn.sizes;
		int[][] potVars = uaibn.potVars;
		double[][] pots = uaibn.pots;

		if ( numVars != numPots ) {
			System.err.println("expecting Bayesian network, quitting");
			System.err.println("# of vars != # of pots");
			System.exit(1);
		}

		// to BeliefNetwork
		Map<FiniteVariable,TableShell> varsToPots = 
			new LinkedHashMap<FiniteVariable,TableShell>(numVars);
		FiniteVariable[] index2var = getIndexToVarMap(sizes);
		for (int i = 0; i < numPots; i++) {
			FiniteVariable var = index2var[i];
			int num = potVars[i].length;
			List<FiniteVariable> vars = new ArrayList<FiniteVariable>(num);
			for (int j = 0; j < num; j++) vars.add(index2var[potVars[i][j]]);
			TableShell cpt = new TableShell(new Table(vars,pots[i]));
			varsToPots.put(var,cpt);
		}

		return new edu.ucla.belief.BeliefNetworkImpl(varsToPots);
	}

	public static BNPair uaiToBeliefNetworkWithIndex(String filename) {
		UaiNetwork uaibn = openUaiNetwork(filename);
		int numVars = uaibn.numVars;
		int numPots = uaibn.numPots;
		int[] sizes = uaibn.sizes;
		int[][] potVars = uaibn.potVars;
		double[][] pots = uaibn.pots;

		if ( numVars != numPots ) {
			System.err.println("expecting Bayesian network, quitting");
			System.err.println("# of vars != # of pots");
			System.exit(1);
		}

		// to BeliefNetwork
		Map<FiniteVariable,TableShell> varsToPots = 
			new LinkedHashMap<FiniteVariable,TableShell>(numVars);
		FiniteVariable[] index2var = getIndexToVarMap(sizes);
		for (int i = 0; i < numPots; i++) {
			FiniteVariable var = index2var[i];
			int num = potVars[i].length;
			List<FiniteVariable> vars = new ArrayList<FiniteVariable>(num);
			for (int j = 0; j < num; j++) vars.add(index2var[potVars[i][j]]);
			TableShell cpt = new TableShell(new Table(vars,pots[i]));
			varsToPots.put(var,cpt);
		}

        BeliefNetwork bn = new edu.ucla.belief.BeliefNetworkImpl(varsToPots);
		return new BNPair(bn,index2var);
	}

	/*
	public static BayesianNetwork uaiToBayesianNetwork(String filename) {
		BeliefNetwork bn = uaiToBeliefNetwork(filename);
		il2.bridge.Converter c = new il2.bridge.Converter();
		return c.convert(bn);
	}
	*/

	private static void fixPots(UaiNetwork uaibn) {
		FiniteVariable[] index2var = getIndexToVarMap(uaibn.sizes);
		for (int i = 0; i < uaibn.numPots; i++) {
			int num = uaibn.potVars[i].length;
			List<FiniteVariable> vars1 = new ArrayList<FiniteVariable>(num);
			for (int j = 0; j < num; j++)
				vars1.add( index2var[uaibn.potVars[i][j]] );
			int[] potVars = uaibn.potVars[i].clone();
			java.util.Arrays.sort(potVars);
			FiniteVariable[] vars2 = new FiniteVariable[num];
			for (int j = 0; j < num; j++) vars2[num-1-j]=index2var[potVars[j]];
			Table cpt = new Table(vars1,uaibn.pots[i]);
			uaibn.pots[i] = cpt.permute(vars2).dataclone();
		}
	}

	public static BayesianNetwork uaiToBayesianNetwork
		(String filename, double[] return_time) {

		UaiNetwork uaibn = openUaiNetwork(filename, return_time);
		BayesianNetwork bn = uaiToBayesianNetwork(uaibn);

		return bn;
	}
		
	public static BayesianNetwork uaiToBayesianNetwork(String filename) {
		return uaiToBayesianNetwork(filename,null);
	}

	public static BayesianNetwork uaiToBayesianNetwork(UaiNetwork uaibn) {
		fixPots(uaibn);
		int numVars = uaibn.numVars;
		int numPots = uaibn.numPots;
		int[] sizes = uaibn.sizes;
		int[][] potVars = uaibn.potVars;
		double[][] pots = uaibn.pots;

		il2.model.Domain d = new il2.model.Domain(numVars);
		for (int i = 0; i < numVars; i++) {
			String[] vals = new String[sizes[i]];
			for (int j=0;j<sizes[i];j++) vals[j]="s"+j;
			d.addDim("x"+i,vals);
		}
		il2.model.Table[] tables = new il2.model.Table[numPots];
		for (int i = 0; i < numPots; i++) {
			il2.model.Index index = 
				new il2.model.Index(d, new il2.util.IntSet(potVars[i]));
			// pots[i] must be shuffled for il2 variable ordering
			tables[i] = new il2.model.Table(index,pots[i]);
			//System.out.println(tables[i]);
		}

		return new BayesianNetwork(tables);
	}
	
	public static Map<String,String> evidToInst(String filename) {
		String file = loadFile(filename);
		if ( file == null ) return null;
		java.util.StringTokenizer evid = new java.util.StringTokenizer(file);
		int numKeys = Integer.parseInt( evid.nextToken() );
		Map<String,String> evidence = new LinkedHashMap<String,String>(numKeys);
		for (int i = 0; i < numKeys; i++) {
			int varIndex = Integer.parseInt( evid.nextToken() );
			int instanceIndex = Integer.parseInt( evid.nextToken() );
			String var = "x"+varIndex;
			String instance = "s"+instanceIndex;
			evidence.put(var,instance);
		}
		return evidence;
	}

    public static IntMap[] evidToIntMaps(String filename) {
        StringTokenizer evid = new StringTokenizer(loadFile(filename));
        int numEvid = Integer.parseInt( evid.nextToken() );
        IntMap[] data = new IntMap[numEvid];
        for (int i = 0; i < numEvid; i++) {
            int numKeys = Integer.parseInt( evid.nextToken() );
            data[i] = new IntMap(numKeys);
            for (int k = 0; k < numKeys; k++) {
                int var = Integer.parseInt( evid.nextToken() );
                int val = Integer.parseInt( evid.nextToken() );
                data[i].put(var,val);
            }
		}
		return data;
    }

    public static int[][] evidToArrays(String filename, int size) {
        StringTokenizer evid = new StringTokenizer(loadFile(filename));
        int numEvid = Integer.parseInt( evid.nextToken() );
        int[][] data = new int[numEvid][size];
        for (int i = 0; i < numEvid; i++) {
            java.util.Arrays.fill(data[i],-1);
            int numKeys = Integer.parseInt( evid.nextToken() );
            for (int k = 0; k < numKeys; k++) {
                int var = Integer.parseInt( evid.nextToken() );
                int val = Integer.parseInt( evid.nextToken() );
                data[i][var] = val;
            }
		}
		return data;
    }

	private static Map<FiniteVariable,Integer> getVarToIndexMap(List vars) {
		Map<FiniteVariable,Integer> var2index = 
			new LinkedHashMap<FiniteVariable,Integer>(vars.size());
		for (int i = 0; i < vars.size(); i++)
			var2index.put((FiniteVariable)vars.get(i),new Integer(i));
		return var2index;
	}

	private static FiniteVariable[] getIndexToVarMap(int[] sizes) {
		FiniteVariable[] index2var = new FiniteVariable[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			String id = "x"+i;
			List<String> instances = new ArrayList<String>(sizes[i]);
			for (int j = 0; j < sizes[i]; j++) instances.add(new String("s"+j));
			index2var[i] = new edu.ucla.belief.FiniteVariableImpl(id,instances);
		}
		return index2var;
	}

    @SuppressWarnings("unchecked")
    public static List<FiniteVariable> sortedTopologicalOrder(BeliefNetwork bn){
        int size = bn.size();
        List<FiniteVariable> order = new ArrayList<FiniteVariable>(size);
        Map<Object,Integer> counts = new LinkedHashMap<Object,Integer>(size);

        List<FiniteVariable> cur = new ArrayList<FiniteVariable>();
        List<FiniteVariable> next = new ArrayList<FiniteVariable>();
        for ( Object var : bn.topologicalOrder() ) {
            int degree = bn.inDegree(var);
            if ( degree == 0 )
                cur.add((FiniteVariable)var);
            else 
                counts.put(var,new Integer(degree));
        }

        while ( !cur.isEmpty() ) {
            java.util.Collections.sort(cur);
            for ( FiniteVariable var : cur ) {
                order.add(var);
                for ( Object child : bn.outGoing(var) ) {
                    int count = counts.get(child).intValue() - 1;
                    counts.put(child,new Integer(count));
                    if ( count == 0 )
                        next.add((FiniteVariable)child);
                }
            }
            cur = next;
            next = new ArrayList<FiniteVariable>();
        }

        return order;
    }

    @SuppressWarnings("unchecked")
    private static edu.ucla.belief.Table permute(edu.ucla.belief.Table t, 
                                                 List<FiniteVariable> order) {
        List<FiniteVariable> vars = permute(t.variables(),order);
        return t.permute(vars.toArray(new FiniteVariable[]{}));
    }

    @SuppressWarnings("unchecked")
    private static List<FiniteVariable> permute(List<FiniteVariable> vars,
                                                List<FiniteVariable> order) {
        Set set = new java.util.HashSet(vars);
        List<FiniteVariable> list = new ArrayList<FiniteVariable>(vars.size());
        for (FiniteVariable var : order) {
            if ( set.contains(var) )
                list.add(var);
        }
        return list;
    }

	//////////////////////////////////////////////////
	// network i/o
	//////////////////////////////////////////////////

	private static java.io.PrintWriter getPrintWriter(String filename) {
		java.io.PrintWriter pw = null;
		try {
			pw = new java.io.PrintWriter(new java.io.BufferedWriter
										 (new java.io.FileWriter(filename)));
		} catch (Exception e) {
			System.err.println("Error saving net:" + e);
			return null;
		}
		return pw;
	}

	private static String loadFile(String filename) {
		//System.out.println("Opening file    : " + filename);
		StringBuilder uai = new StringBuilder();
		try {
			java.io.BufferedReader input = new java.io.BufferedReader
				(new java.io.FileReader(filename));
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					uai.append(line);
					uai.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		}
		catch (Exception e){
			System.err.println("Error loading uai:" + e);
			return null;
		}
		return uai.toString();
	}

	private static BeliefNetwork openNetwork(String filename) {
		//System.err.println("Opening network : " + filename);
		BeliefNetwork bn = null;
		try {
			bn = edu.ucla.belief.io.NetworkIO.read(filename);
		} catch ( Exception e ) {
			System.err.println("Failed loading : " + filename + ", " + e);
		}
		return bn;
	}

	private static void saveNetwork(BeliefNetwork bn, String filename) {
		System.err.println("Saving network  : " + filename);
		try {
			edu.ucla.belief.io.NetworkIO.writeNetwork
				(bn,new java.io.File(filename));
		} catch (Exception e) {
			System.err.println("Error saving net: " + e);
		}
	}

	//////////////////////////////////////////////////
	// evidence i/o
	//////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	private static Map<String,String> openInst(String filename) {
		//System.err.println("Opening evidence: " + filename);
		edu.ucla.belief.io.InstantiationXmlizer xml = 
			new edu.ucla.belief.io.InstantiationXmlizer();
		Map<String,String> evidence = null;
		try {
			evidence = xml.getMap(new java.io.File(filename));
		} catch ( Exception e ) {
			System.err.println("Failed loading : " + filename + ", " + e);
		}
		return evidence;
	}

	private static void saveInst(Map<String,String> evidence, String filename) {
		if ( evidence == null ) return;
		System.err.println("Saving network  : " + filename);
		edu.ucla.belief.io.InstantiationXmlizer xml = 
			new edu.ucla.belief.io.InstantiationXmlizer();
		try {
			xml.save(evidence,new java.io.File(filename));
		} catch (Exception e) {
			System.err.println("Error saving inst: " + e);
		}
	}

	public static Map<FiniteVariable,Object> convert
		(Map<String,String> e, BeliefNetwork bn) {
		Map<FiniteVariable,Object> e2 = 
			new LinkedHashMap<FiniteVariable,Object>(e.keySet().size());
		for ( String varId : e.keySet() ) {
			FiniteVariable var = (FiniteVariable)bn.forID(varId);
			Object instance = var.instance(e.get(varId));
			e2.put(var,instance);
		}
		return e2;
	}

	public static IntMap convert(Map<String,String> e, BayesianNetwork bn) {
		IntMap e2 = new IntMap(e.keySet().size());
		il2.model.Domain d = bn.domain();
		for ( String varId : e.keySet() ) {
			int var = d.index(varId);
			int instance = d.instanceIndex(var,e.get(varId));
			e2.put(var,instance);
		}
		return e2;
	}

}


class BNPair {
    BeliefNetwork bn;
    FiniteVariable[] i2v;
    public BNPair(BeliefNetwork bn, FiniteVariable[] i2v) {
        this.bn = bn;
        this.i2v = i2v;
    }
}

	class UaiNetwork {
		public enum Network { BAYES, MARKOV }

		int numVars;
		int numPots;
		int[] sizes;
		int[][] potVars;
		double[][] pots;
		Network type;

		public UaiNetwork(int nv, int np, int[] s, int[][] pv, double[][] p,
						  Network t) {
			numVars = nv;
			numPots = np;
			sizes = s;
			potVars = pv;
			pots = p;
			type = t;
		}
	}
