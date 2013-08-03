onodrim
=======

Onodrim is a Java library aimed to ease the configuration and collection of results of generic jobs. Onodrim can be handy when it is required to run a computational tasks against several parameters --where each parameter can have different values-- and to organize the results in tables to ease their analysis.

**The problem:** often, it is not possible to know beforehand which are the job parameters that should be configurable, in fact, many times those are discovered as more executions are run and a deeper analysis of results is required. Adding new configurable parameters to the code and assigning values to them will require changing (once and again) the code.

A pausible solution is just to define parameters values outside the code in a `.properties file`. For example:

`Parameter1=123
Parameter2=456`

Adding new configurable parameters is easy, it is enough to add the parameter in the file and change the code to get the value from it. However, what happens if we want to run the job against several values of each parameter, to compare the results? Creating a different `.properties` value for each different parameter would be cumbersome, and adding new parameters would force us to re-create all .properties files again.

**Onodrim to the rescue:** let's see how Onodrim works with a simple example. Let's assume we want to check the results when `Parameter1` has two different values: 123 and 456. We will create the following `.properties` file

`Parameter1=123;456`



usage
=====

Onodrim is written in Java and so it can be used from Java programs straight ahead. Using it with other programming languages should be possible, as there are mechanisms that allow to do so (for example JPype or Py4J in Python).

faq
===
_What does 'Onodrim' mean?_ I am a fan of Tolkien fictional world :) ! . Onodrim is "The name given by the Elves to the giant tree-like beings that Men called Ents." ([The Encyclopedia or Arda](http://www.glyphweb.com/arda/o/onodrim.html)). As you probably know, and Onodrim/Ent is a shepherd of trees. Similarly, this software is a kind of 'shepherd' of your jobs, it should help you to organize and herd them. 

license
=======
Onodrim is distributed under the GPL v3 license (http://www.gnu.org/licenses/gpl.html). You should have received a copy of this license along with Onodrim.
