# Direct & Factored Deletion #

Prototype implementation of direct and factored deletion algorithms to learn Bayesian network parameters from incomplete data under the MCAR and MAR assumptions. These algorithms are consistent, yet they only require a single pass over the data, and no inference in the Bayesian network.

![Logo](
http://web.cs.ucla.edu/~guyvdb/code/img/deletion.png)


## Publication ##

Guy Van den Broeck, Karthika Mohan, Arthur Choi, Adnan Darwiche, Judea Pearl. 
**[Efficient Algorithms for Bayesian Network Parameter Learning from Incomplete Data](https://lirias.kuleuven.be/bitstream/123456789/500017/1/deletion-uai15.pdf)**, 
In *Proceedings of the 31st Conference on Uncertainty in Artificial Intelligence (UAI)*, 2015.

## Contact ##

### Website ###
[http://reasoning.cs.ucla.edu/deletion/](http://reasoning.cs.ucla.edu/deletion/)

### Main contact###

Guy Van den Broeck  
Department of Computer Science  
UCLA  
[http://web.cs.ucla.edu/~guyvdb/](http://web.cs.ucla.edu/~guyvdb/)  
[guyvdb@cs.ucla.edu](mailto:guyvdb@cs.ucla.edu)

## Contributors ##

* [Guy Van den Broeck](http://web.cs.ucla.edu/~guyvdb/)
* [Arthur Choi](http://web.cs.ucla.edu/~aychoi/)

## Usage ##

The code takes as input Bayesian networks in the [UAI file format](http://www.hlt.utdallas.edu/~vgogate/uai14-competition/modelformat.html). A simple example called `fire_alarm.uai` is shown below.
```
BAYES
6
2 2 2 2 2 2
6
1 0
1 1
2 0 2
3 0 1 3
2 3 4
2 4 5
2
 0.01 0.99
2
 0.02 0.98
4
 0.9 0.1 0.01 0.99
8
 0.5 0.5 0.99 0.01 0.85 0.15 0.0001 0.9999
4
 0.88 0.12 0.001 0.999
4
 0.75 0.25 0.01 0.99
```

## License ##

This source code is licensed under the Apache License, Version 2.0: 
http://www.apache.org/licenses/LICENSE-2.0

This software uses the inference library inflib.jar, which is
provided by the Automated Reasoning Group at UCLA.  inflib.jar is
licensed only for non-commercial, research and educational use.
