# Concepts Used in Cascading #

Before reading on, check out the [Cascading presentation](http://www.cascading.org/documentation/Cascading.pdf) (PDF) and [About](http://www.cascading.org//about.html) page.

## Tuple ##
A `Tuple` represents a single set of named values. The names are fields in the `Tuple`. Consider a `Tuple` the same as a data base record where every value is a column in that table. A `Tuple` stream would be a set of `Tuple` instances, which are passed consecutively through a `Pipe` assembly.

## Pipe ##
`Pipes` are chained together through their constructors. To effect a split in the pipe, simply pass a `Pipe` instance to two or more constructors of subsequent `Pipe` instances. A join can be achieved by passing two or more `Pipe` instances to a `Group` pipe.

### Operators ###
`Each`
> The `Each` operator pipe applies an Operation to each entry in the `Tuple` stream. Any number of `Each` operators can follow an `Each`, `Group`, or `Every` operator. `Each` is typically represented in model diagrams as an `E`.

`Every`
> The `Every` operator pipe applies an Operation to every entry within a given grouping. `Every` always becomes immediately after a `Group` pipe. `Every` is typically represented in model diagrams as an `A`.

#### Operations ####
`Function`
> A `Funtion` simply applies some expression to an incoming `Tuple` and returns a new `Tuple`.

`Filter`
> A `Filter` decides if a given `Tuple` should be removed from the stream.

`Generator`
> A `Generator` is effectively the same as a `Function` except it may return many `Tuple` instances for every incoming `Tuple`. `Functions` that create additional results should be typed as a `Generator` so the optimizer can make informed decisions.

`Aggregator`
> An `Aggregator` takes the set of all values associated with a grouping and returns a single value. Max, Min, Count, and Average are good examples.

### Groups ###

`GroupBy`
> `GroupBy` is the simplest grouping `Pipe`. It will create sets of `Tuple` instances based on a given group by field value.

`CoGroup`
> `CoGroup` is the most complex. It provides the ability to join two or more `Tuple` streams, or to cross-tab a single stream.

A group is typically represented in model diagrams as an `G`. It is a `CoGroup` if there are multiple sources.

## Tap ##
A `Tap` represents the physical data source or sink in a connected `Flow`. That is a source `Tap` is the head of a pipe Tuple stream, and a sink `Tap` is the end. Kinds of `Tap` types are used to manage files from a local disk, distributed disk, remote storage like Amazon S3, or via FTP. It simply abstracts out the complexity of connecting to these types of data sources.

A `Tap` takes a `Scheme` instance.

### Scheme ###
A `Scheme` defines what is stored in a `Tap` instance by declaring the `Tuple` field names, and alternately parsing or rendering the incoming or outgoing `Tuple` stream, respectively.

## Flow ##
A `Pipe` assembly is connected to the necessary number of `Tap` sinks and sources into a `Flow`. A `Flow` is then executed to push the incoming source data through the assembly into one or more sinks.

Note that `Pipe` assemblies can be reused in multiple `Flow` instances. They maintain no state regarding the `Flow` execution. Subsequently `Pipe` assemblies can be given parameters through its calling `Flow` so they  can be built in a generic fashion.

When a `Flow` is created, an optimized internal representation is created that is then executed within the cluster. Thus any overhead inherent to a give `Pipe` assembly will be removed once it's placed in context with the actual execution environment.

## Cascade ##
A `Cascade` is simply an assembly of `Flow` instances. `Cascades` allow for incremental builds of complex data processing processes. If a given source `Tap` is newer than a subsequent `Tap` in the assembly, the connecting `Flow`(s) will be executed on the next `Cascade` run. If all the targets (sinks) are up to date, the `Cascade` exits.