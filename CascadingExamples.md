# Introduction #

Details in using the examples provided as separate [downloads](http://code.google.com/p/cascading/downloads/list).

# Example Details #
  * ApacheLogCascade
  * CrawlDataWordCountCascade
  * PearsonDistanceAssembly

If Groovy scripting is more your style, see our [Groovy syntax page](http://www.cascading.org/documentation/groovy.html).

# Running #

Two sample applications, with source are provided on the downloads page.

To give Cascading a quick whirl, download the cascading archive and one of the example applications. Untar them both into a new directory so that you have a 'cascading' and either 'loganalysis' or 'wordcount' directories in the same parent directory.

Next, you must initialize your local Hadoop environment. For example, call
```
export HADOOP_HOME=~/hadoop
export PATH=$HADOOP_HOME/bin/:$PATH
```
where `HADOOP_HOME` points to the root of your Hadoop installation.

Finally, per the README.TXT file in your example application, call
```
ant jar
hadoop jar ./build/example.jar args...
```

Note these examples work great with a cluster enabled. So if some files/directories don't show up in the current directory, check your HDFS user directory.