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

package kestimate.data;

import java.util.Random;

import il2.model.BayesianNetwork;

public abstract class MissingnessClass {
	
	public abstract MissingnessGraph generateInstance(BayesianNetwork bn, Random r);

}
