Onodrim
=======

(Contact at <onodrim.project@gmail.com>)

Onodrim is a small Java library that eases the task of configuring and collect the results of bunches of jobs. Onodrim can be handy when it is required to run a computational job against several parameters --where each parameter can have different values,and where the set of parameters can change -- and to organize the results to ease their analysis.

**The problem:** often, it is not possible to know beforehand which are the job parameters that should be configurable, in fact, many times those are discovered as more executions are run and a deeper analysis of results is required. Adding new configurable parameters to the code and assigning values to them will require changing (once and again) the code.

A pausible solution is just to define parameters values outside the code in a `.properties` file. For example:

    Parameter1=123

Adding new configurable parameters is easy, it is enough to add the parameter in the file and change the code to get the value from it. However, what happens if we want to run the job against several values of each parameter, to compare the results? Creating a different `.properties` value for each different parameter would be cumbersome, and adding new parameters would force us to re-create all `.properties` files again.

**Onodrim to the rescue:** let's see how Onodrim works with the simplest example. Let's assume we want to check the results when `Parameter1` has two different values: 123 and 456. We will create the following `test.properties` file

    # The ; is used to separate parameter values
    Parameter1=123;456

The `;` tells Onodrim that those are in fact two different values for the same parameter. If we run the following code

    import org.onodrim.Configuration;
    ...
    List<Configuration> confs = Onodrim.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        // Your stuff here
    }

then Onodrim will build 2 configurations (the `Configuration` class extends `java.util.Properties`). Of course this is a very simple example. But imagine now that you decide to configure your tasks with two more parameters, and see the outcome when several values are tried for each parameter. You only will need to change the `test.properties` file to something like:

    Parameter1=123;456;789
    Parameter2=0;1;2;3;4;5;6;7;8;9
    Parameter3=11.1;12.2;13.3
    
your code will remain the same! You only need to retrieve the new parameter values:
    
    import org.onodrim.Configuration;
    ...
    List<Configuration> confs = Onodrim.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        int p2 = conf.getIntParameter("Parameter2");
        double p3 = conf.getDoubleParameter("Parameter3");
        // Your stuff here
    }

just as before, Onodrim will generate all the configurations required (90 in this case).

Onodrim implements several mechanisms (conditional parameter generation, parameters grouping...) that bring a lot of flexibility when defining your experiments configuration. It also allows to organize the results in handy tables for easy analysis. Besides, it helps to keep (well organized) copies of all jobs, their configurations and results. A more detailed description of how to use Onodrim is available in the [Onodrim wiki in github](https://github.com/lrodero/onodrim/wiki).

Requirements, Download & Installation
=====================================

Onodrim requires Java v1.6 (at least). Also, [Ant](http://ant.apache.org/ (v1.6.0 at least) will be handy to compile the source code and generate its Javadoc documentation.

Onodrim is available through [github](https://github.com/lrodero/onodrim). A copy of it can be downloaded by running the following code:

    $ git clone git://github.com/lrodero/onodrim.git
    $ cd onodrim
    $ ant

this will compile the source in the `src` folder and store the compiled classes in `bin`, it will generate the Javadoc API documentation in `doc`, and it will create three `.jar` files with the class files, documentation and source. To use Onodrim it is only needed to add the `onodrim-0.5.jar` file to the `CLASSPATH`.

License & Contact
=================
Onodrim is distributed under the [GPL v3 license](http://www.gnu.org/licenses/gpl.html).

If you use Onodrim, I'd really appreaciate if you let me know! I'm very interested in knowing who (and how) is using Onodrim in order to improve it. Thus, feedback and comments are welcome :) . You can contact me at <onodrim.project@gmail.com>. If you find any bug or have any problem please report the issue [here](https://github.com/lrodero/onodrim/issues) (or, again, contact me by email). 

FAQ
===
**What does 'Onodrim' mean?** I am a fan of Tolkien fictional world :) ! . Onodrim is _"The name given by the Elves to the giant tree-like beings that Men called Ents."_ ([The Encyclopedia or Arda](http://www.glyphweb.com/arda/o/onodrim.html)). As you probably know, and Onodrim/Ent is a shepherd of trees. Similarly, this software is a kind of 'shepherd' of your jobs, it should help you to organize and herd them. 
