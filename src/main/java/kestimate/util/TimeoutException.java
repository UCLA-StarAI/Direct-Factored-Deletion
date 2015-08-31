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

public class TimeoutException extends Error{

	private static final long serialVersionUID = 3241749938526020982L;
	
	public final long deadline;
	public final long now;

	public TimeoutException(long deadline) {
		this.deadline = deadline;
		this.now = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "Time out: exceeded deadline by " + (now-deadline)/1000.0+"s"; 
	}

}
