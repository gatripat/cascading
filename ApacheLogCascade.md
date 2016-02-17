A simple example showing how to import an apache log file into a Hadoop DFS and then create two files, one showing the number of requests per second and a second showing the number of requests per minute.

There are two Flow instances here. The first to import the log file and store it as a native to Hadoop sequence file after being parsed into its constituent fields. The second shows a how to apply two grouping functions to the same stream and output those values to two different files.

Internally, the second flow is actually two map/reduce jobs since there are two GroupBy elements. But the whole flow is treated as one atomic unit, and if any temporary files are needed to support the flow, they are transparent and do not need to be managed.

Since these flow instances were connected into a Cascade instance, they can be executed atomically (but they can be reused in other Cascade instances). The first time the Cascade is run, all the flows will execute and all the files will be generated. If the Cascade is run a second time, and the original log file is the same and has not changed, no flows will be executed, as there is no need.

```
    FlowConnector flowConnector = new FlowConnector();
    CascadeConnector cascadeConnector = new CascadeConnector();

    String inputPath = args[ 0 ];
    String logsPath = args[ 1 ] + "/logs/";
    String arrivalRatePath = args[ 1 ] + "/arrivalrate/";
    String arrivalRateSecPath = arrivalRatePath + "sec";
    String arrivalRateMinPath = arrivalRatePath + "min";

    // create an assembly to import an Apache log file and store on DFS
    // declares: "time", "method", "event", "status", "size"
    Pipe importPipe = new Each( "import", new Fields( "line" ), Regexes.APACHE_PARSER );

    // create tap to read a resource from the local file system
    Tap localLogTap = new Lfs( new TextLine(), inputPath );
    // create a tap to read/write from a Hadoop distributed filesystem
    Tap parsedLogTap = new Dfs( Regexes.APACHE_GROUP_FIELDS, logsPath );

    // connect the assembly to source and sink taps
    Flow importLogFlow = flowConnector.connect( localLogTap, parsedLogTap, importPipe );

    // create an assembly to parse out the time field into a timestamp
    // then count the number of requests per second and per minute

    // apply a text parser to create a timestamp with 'second' granularity
    // declares field "ts"
    Pipe tsPipe = new Each( "arrival rate", new Fields( "time" ), Texts.APACHE_DATE_PARSER );

    // name the per second assembly and split on tsPipe
    Pipe tsCountPipe = new Pipe( "tsCount", tsPipe );
    tsCountPipe = new GroupBy( tsCountPipe, new Fields( "ts" ) );
    tsCountPipe = new Every( tsCountPipe, Fields.KEYS, new Count() );

    // apply expression to create a timestamp with 'minute' granularity
    // declares field "tm"
    Pipe tmPipe = new Each( tsPipe, new ExpressionFunction( new Fields( "tm" ), "ts - (ts % (60 * 1000))", long.class ) );

    // name the per minute assembly and split on tmPipe
    Pipe tmCountPipe = new Pipe( "tmCount", tmPipe );
    tmCountPipe = new GroupBy( tmCountPipe, new Fields( "tm" ) );
    tmCountPipe = new Every( tmCountPipe, Fields.KEYS, new Count() );

    // create taps to write the results to a Hadoop distributed file system, using the given fields
    Tap tsSinkTap = new Dfs( new TextLine( new Fields( "ts", "count" ) ), arrivalRateSecPath );
    Tap tmSinkTap = new Dfs( new TextLine( new Fields( "tm", "count" ) ), arrivalRateMinPath );

    // a convenience method for binding taps and pipes, order is significant
    Map<String, Tap> sinks = Cascades.tapsMap( Pipe.pipes( tsCountPipe, tmCountPipe ), Tap.taps( tsSinkTap, tmSinkTap ) );

    // connect the assembly to the source and sink taps
    Flow arrivalRateFlow = flowConnector.connect( parsedLogTap, sinks, tsCountPipe, tmCountPipe );

    // optionally print out the arrivalRateFlow to a graph file for import into a graphics package
    //arrivalRateFlow.writeDOT( "arrivalrate.dot" );

    // connect the flows by their dependencies, order is not significant
    Cascade cascade = cascadeConnector.connect( importLogFlow, arrivalRateFlow );

    // execute the cascade, which in turn executes each flow in dependency order
    cascade.complete();
```