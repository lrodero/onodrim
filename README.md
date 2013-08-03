onodrim
=======

Onodrim is a Java library aimed to ease the configuration and collection of results of generic jobs. Onodrim can be handy when it is required to run a computational tasks against several parameters --where each parameter can have different values-- and to organize the results in tables to ease their analysis.

**The problem:** often, it is not possible to know beforehand which are the job parameters that should be configurable, in fact, many times those are discovered as more executions are run and a deeper analysis of results is required. Adding new configurable parameters to the code and assigning values to them will require changing (once and again) the code.

A pausible solution is just to define parameters values outside the code in a `.properties` file. For example:

    Parameter1=123
    Parameter2=456

Adding new configurable parameters is easy, it is enough to add the parameter in the file and change the code to get the value from it. However, what happens if we want to run the job against several values of each parameter, to compare the results? Creating a different `.properties` value for each different parameter would be cumbersome, and adding new parameters would force us to re-create all `.properties` files again.

**Onodrim to the rescue:** let's see how Onodrim works with the simplest example. Let's assume we want to check the results when `Parameter1` has two different values: 123 and 456. We will create the following `.properties` file

    Parameter1=123;456

The `;` tells Onodrim that those are in fact two different values for the same parameter. If we run the following code

    List<Configuration> confs = org.onodrim.Configuration.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        // Your stuff here
    }

then Onodrim will build _2_ configurations (the `Configuration` class extends `java.util.Properties`) and return them in a `List`. The 2 configurations will contain only `Parameter1`. Of course this is a very simple example. But imagine now that you decide to configure your tasks with two more parameters, and each parameter with several values. You only will need to change the `.properties` file to something like:

    Parameter1=123;456;789
    Parameter2=1;2;3;4;5;6;7;8;9;0
    Parameter3=11.1;12.2;13.3
    
the code will remain the same! You only need to retrieve the parameter values:

    List<Configuration> confs = org.onodrim.Configuration.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        int p2 = conf.getIntParameter("Parameter2");
        double p3 = conf.getDoubleParameter("Parameter3");
        // Your stuff here
    }

as before, Onodrim will generate all the configurations required (90 in this case).

Onodrim also implements several mechanisms (conditional parameter generation, parameters grouping...) that bring a lot of flexibility when defining your experiments configuration. A more detailed description of how to use Onodrim is available in the [Onodrim wiki in github](https://github.com/lrodero/onodrim/wiki).

Requirements, Download & Installation
=====================================

Onodrim requires Java v1.6 (at least). Also, [Ant](http://ant.apache.org/ (v1.6.0 at least) is needed to compile and generate its documentation. 

Onodrim is available through github. A copy of it can be downloaded by running the following code:

    $ git clone git://github.com/lrodero/onodrim.git
    $ cd onodrim
    $ ant

this will compile the source in the `src` folder and write the results in `bin`, it will generate the Javadoc API documentation in `doc`, and it will create three `.jar` files with the class files, documentation and source.


FAQ
===
_What does 'Onodrim' mean?_ I am a fan of Tolkien fictional world :) ! . Onodrim is _"The name given by the Elves to the giant tree-like beings that Men called Ents."_ ([The Encyclopedia or Arda](http://www.glyphweb.com/arda/o/onodrim.html)). As you probably know, and Onodrim/Ent is a shepherd of trees. Similarly, this software is a kind of 'shepherd' of your jobs, it should help you to organize and herd them. 

License
=======
Onodrim is distributed under the [GPL v3 license](http://www.gnu.org/licenses/gpl.html). You should have received a copy of this license along with Onodrim.
