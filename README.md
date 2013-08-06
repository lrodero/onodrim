Onodrim
=======

(Contact at <onodrim.project@gmail.com>)

Onodrim is a Java library that eases the configuration and collection of results of bunches of jobs (experiments, simulations...). Onodrim is handy when you need to run computations configured using several parameters, where each parameter can have different values, and where even the set of parameters can change. Also it organizes the results to ease their analysis.

**The problem:** often, it is not possible to know beforehand which are the parameters that should be configurable, in fact, many times those are discovered as more executions are run and a deeper analysis of results is required. Adding new configurable parameters and/or assigning values to them will require changing (once and again) the code. A pausible solution is to define the parameters in a `.properties` file. For example:

    Parameter1=123

This way, adding and configuring parameters is easy. However, to iterate through several values to compare the results it would be needed a different `.properties` value for each configuration of parameters values (for two paramers with 10 values each we would need 100 files), and adding new parameters would force us to re-create *all* `.properties` files again and to create new ones.

**Onodrim to the rescue:** let's see how Onodrim works with an example. Let's assume we use two parameters with several values each. We will create the following `test.properties` file

    # The ; is used to separate parameter values
    Parameter1=123;456;789
    Parameter2=0;1;2;3;4;5;6;7;8;9

If we run the following code

    List<Configuration> confs = Onodrim.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        int p2 = conf.getIntParameter("Parameter2");
        // Your stuff here
    }

then Onodrim will build 30 configurations (`org.onodrim.Configuration` extends `java.util.Properties`). Of course this is a very simple example. But imagine now that you decide to configure your tasks with one more parameter. You only will need to change the `test.properties` file to something like:

    Parameter1=123;456;789
    Parameter2=0;1;2;3;4;5;6;7;8;9
    Parameter3=11.1;12.2;13.3
    
just as before, Onodrim will generate all the configurations required (90 in this case), but your code will remain (almost) the same! You only need to retrieve the new parameter values

    List<Configuration> confs = Onodrim.buildConfigurations(new File("test.properties"));
    for(Configuration conf: confs) {
        int p1 = conf.getIntParameter("Parameter1");
        int p2 = conf.getIntParameter("Parameter2");
        double p3 = conf.getDoubleParameter("Parameter3");
        // Your stuff here
    }

**Is that all?** No :) .
- Onodrim implements several mechanisms (conditional parameter generation, parameters grouping...) that bring a lot of flexibility when defining your experiments configuration.
- It also allows to organize the results in handy tables for easy analysis.
- Besides, it helps to keep (well organized) copies of all jobs, their configurations and results.

A more detailed description of how to use Onodrim is available in the [Onodrim wiki in github](https://github.com/lrodero/onodrim/wiki).

Requirements, Download & Installation
=====================================

Onodrim requires Java v1.6 (at least). Also, [Ant](http://ant.apache.org/ (v1.6.0 at least) will be handy to compile the source code and generate its Javadoc documentation.

**Cloning through git** A git repository of Onodrim is available in [github](https://github.com/lrodero/onodrim). You can clone to get Onodrim sources and then compile them by running the following commands:

    $ git clone git://github.com/lrodero/onodrim.git
    $ cd onodrim
    $ ant

this will compile the source in the `src` folder and store the compiled classes in `bin`, it will generate the Javadoc API documentation in `doc`, and it will create three `.jar` files with the class files, documentation and source.

**In a .zip archive** You can also download a `.zip` file containing Onodrim sources. Just run the following commands:

    $ wget https://github.com/lrodero/onodrim/archive/master.zip
    $ unzip master.zip
    $ cd onodrim-master
    $ ant

calling to `ant` will have the same effect as above: compiling sources, generating Javadocs and creating `.jar` files.

**Installation** To use Onodrim it is only needed to add the `onodrim-<version>.jar` file to the `CLASSPATH`.

License & Contact
=================
Onodrim is distributed under the [GPL v3 license](http://www.gnu.org/licenses/gpl.html).

Also, if you use Onodrim, I'd really appreaciate if you let me know! I'm very interested in knowing who (and how) is using Onodrim in order to improve it. Thus, feedback and comments are welcome :) . You can contact me at <onodrim.project@gmail.com>. If you find any bug or have any problem please report the issue [here](https://github.com/lrodero/onodrim/issues) (or, again, contact me by email). 

FAQ
===
**What does 'Onodrim' mean?** I am a fan of Tolkien works :) ! . Onodrim is _"The name given by the Elves to the giant tree-like beings that Men called Ents."_ ([The Encyclopedia or Arda](http://www.glyphweb.com/arda/o/onodrim.html)). As you probably know, and Onodrim/Ent is a shepherd of trees. Similarly, Onodrim is a kind of 'shepherd' of your jobs, as it should help you to organize and herd them. 
