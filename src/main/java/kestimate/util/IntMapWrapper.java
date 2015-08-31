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

import il2.util.IntMap;

public final class IntMapWrapper{
	
	protected final int hashCode;
	protected final IntMap intMap;
	
	public IntMapWrapper(IntMap intMap) {
		if(intMap == null) throw new IllegalStateException();
		this.intMap = intMap;
		int hashCode = 1;
		for(int i=0;i<intMap.size();i++){
			// hashCode can be order dependent because IntMaps are sorted
			hashCode = 31*hashCode + entryHashCode(intMap.key(i),intMap.value(i));
		}
		this.hashCode = hashCode;
	}
	
	public int entryHashCode(int key, int value) {
		final int prime = 37;
		int result = 17;
		result = prime * result + key;
		result = prime * result + value;
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		IntMapWrapper other = (IntMapWrapper) obj;
		if (this.hashCode != other.hashCode){
			return false;
		} 
		return intMap.equals(other.intMap);
	}
	
}
