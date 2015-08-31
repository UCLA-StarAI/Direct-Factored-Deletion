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

import java.util.List;

import il2.util.IntMap;


public final class Global {
    
    public static byte[][] int2byte(int[][] src) {
    	byte[][] bytes = new byte[src.length][src[0].length];
    	for(int i=0;i<bytes.length;i++){
        	for(int j=0;j<bytes[0].length;j++){
        		if(src[i][j] < -128 || src[i][j] > 127 ) throw new IllegalStateException(src[i]+" cannot be cast to byte");
        		bytes[i][j] = (byte) src[i][j];
        	}
    	}
    	return bytes;
    }
    
    public static int[][] deepClone(int[][] src) {
        int length = src.length;
        int[][] target = new int[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }

    public static byte[][] deepClone(byte[][] src) {
        int length = src.length;
        byte[][] target = new byte[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }
    
    public static IntMap[] deepClone(IntMap[] src) {
        int length = src.length;
        IntMap[] target = new IntMap[length];
        for (int i = 0; i < length; i++) {
        	target[i] = new IntMap(src[i]);
        }
        return target;
    }
	
    public static int[] toIntArray(List<Integer> list){
    	  int[] ret = new int[list.size()];
    	  for(int i = 0;i < ret.length;i++)
    	    ret[i] = list.get(i);
    	  return ret;
    	}
    
	public static int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;

	   return result;
	}
	
}
