# Developer FAQ #

Also see our evolving [Cook Book](http://www.cascading.org/documentation/cook-book.html) on the main site.

### How do I convert String tuple values to their primitive types for more efficient storage? ###

> Use the `c.o.Identity` class to coerce values to new types. If a tuple value is "1234", you could coerce it to a Integer. For example:
```
  pipe = new Each( pipe, new Fields("index_string"), new Identity( new Fields("index"), Integer.class), Fields.ALL );
```

### What do all these constants on the c.t.Fields class mean? ###

> See the javadoc for details. In short, they represent logical groups of fields, so that pipe assemblies don't need to hard code field names if they perform common operations. For example, `Fields.ALL` represents all the fields in the current stream, i.e. a wild card. `Fields.KEYS` represent the grouping fields used by a previous `c.p.Group` pipe.

### Can I reference tuple values by position instead of by field name? ###

> Yes. Indexing starts at 0. Thus new Fields( 3 ) would select the 4th item in a Tuple. You can also use relative positions. -1 would return the last, and -2 the second to last value in a Tuple. Field names and positions can be used together, new Fields( "name" , -1 ).

### How do I create reusable assemblies? ###

> Subclass `c.p.SubAssembly`. In the constructor assemble your reusable assembly using parameters passed in. Common parameters are the 'previous' pipe you are attaching to, or simply the 'name' of this assembly. Before your constructor returns, you must pass all the tail ends of your assembly to `setTails( Pipe... tails )`. This allows your `SubAssembly` class to be attached to by subsequent assemblies. For example:
```
  public SomeAssembly( Pipe previous, Fields argumentFieldSelector, Fields fieldDeclaration )
    {
    // expects 3 argument values, should check argumentFieldSelector is the correct size
    // rename all fields for internal use
    Pipe pipe = new Each( previous, argumentFieldSelector, new Identity( new Fields( "n", "l", "v" ) ) );

    ...

    // rename fields to expected return values
    pipe = new Each( pipe, new Identity( fieldDeclaration ) );

    setTails( pipe );
    }
```


### How do I print my tuple stream for debugging? ###

> Use the c.o.Debug filter. By default it will print your tuple stream to stderr.

### Can I enable DEBUG logging without updating every log4j.properties file in my cluster? ###

> Yes. Set this property on your `JobConf`.
```
jobConf.set("log4j.logger","logger1=LEVEL,logger2=LEVEL");
```

> Where logger1 could be `cascading` and LEVEL could be `DEBUG`. Note only the loggers in the task mappers or task reducers will be configured.

### Can I print out my Flow? ###

> Yes. You can print your Flow instance to a .dot file for import into a graphics package like OmniGraffle. What results is a graph representation of the underlying pipeline, and the fields being passed to each pipe element. After the Group element, you can see the grouping fields, and the value fields expected by the next Operator.

### How do I filter out duplicate Tuples in a stream? ###

> In order to 'unique' the stream to get distinct tuples, use the following assembly.
```
// group on all values
pipe = new GroupBy( pipe, Fields.ALL );
// only take the first value in the grouping, ignore the rest
pipe = new Every( pipe, Fields.ALL, new First(), Fields.RESULTS );
```

> It's important to note that the `Each` pipe always replaces the values in the stream with `Function` results, by default. The `Every` pipe always appends `Aggregator` results. Using `Fields.RESULTS` tells `Every` to replace the tuple values. `Fields.ALL` would append them, and is the defualt for `Every`.

## Not yet released, available via svn ##

### None ###