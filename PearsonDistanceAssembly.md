Here is an example on calculating the Pearson Distance for ranking data. You can read about the Pearson Distance algorithm in [Programming Collective Intelligence](http://www.amazon.com/Programming-Collective-Intelligence-Building-Applications/dp/0596529325). The comments will speak to that particular example.

From the book, we have a list of critics, and we want to calculate their distance. The field names are "name" "movie" "rate", where "movie" and "rate" are repeated in the row for each critic name. For example:
```
Toby	Snakes on a Plane	4.5	You, Me and Dupree	1.0	Superman Returns	4.0
```

To process a text file of critic rates, we simply create the following Flow.

```
    Tap source = new Dfs( new TextLine(), inputFileCritics );
    Tap sink = new Dfs( new TextLine(), outputPathPearson + "/composite", true );

    // unknown number of tab delimited elements of the format: name movie1 rate1 movie2 rate2 movieN rateN
    Pipe pipe = new Each( "pearson", new Fields( "line" ), Regexes.TAB_SPLITTER );

    // break out into name, movie, rate
    pipe = new Each( pipe, new UnGroup( new Fields( "name", "movie", "rate" ), Fields.FIRST, 2 ) );

    // name and rate against others of same movie
    pipe = new PearsonDistance( pipe, new Fields( "name", "movie", "rate" ), new Fields( "name1", "name2", "distance" ) );

    Flow flow = new FlowConnector().connect( source, sink, pipe );
```

To make that work, we must have a PearsonDistance Pipe class. But for that to work, we will implement a simple CrossTab base class than be re-used by different algorithms like Euclidean Distance.

Since it's re-usable, the field names are generic, but for clarity they have been annotated in the comments.

```
/**
 * Here we create a base pipe assembly. It is expected to overridden by specific implementations of the Aggregator function.
 */
public class CrossTab extends PipeAssembly
  {
  public CrossTab( Pipe previous, Fields argumentFieldSelector, CrossTabOperation crossTabOperation, Fields fieldDeclaration )
    {
    // assert size of input
    Pipe pipe = new Each( previous, argumentFieldSelector, new Identity( new Fields( "n", "l", "v" ) ) );

    // name and rate against others of same movie
    pipe = new Group( pipe, new Fields( "l" ), 2, new Fields( "n1", "l", "v1", "n2", "l2", "v2" ) );

    // remove useless fields
    pipe = new Each( pipe, new Cut( new Fields( "l", "n1", "v1", "n2", "v2" ) ) );

    // remove lines if the names are the same
    pipe = new Each( pipe, new RegexFilter( "^[^\\t]*\\t([^\\t]*)\\t[^\\t]*\\t\\1\\t.*", true ) );

    // transpose values in fields by natural sort order
    pipe = new Each( pipe, new SortElements( new Fields( "n1", "v1" ), new Fields( "n2", "v2" ) ) );

    // unique the pipe
    pipe = new Uniq( pipe );

    // out: name1, name2, movie, name1, rate1, name2, rate2
    pipe = new Group( pipe, new Fields( "n1", "n2" ) );

    // out: movie, name1, rate1, name2, rate2, score
    pipe = new Every( pipe, new Fields( "v1", "v2" ), crossTabOperation );

    pipe = new Each( pipe, new Identity( fieldDeclaration ) );

    setTails( pipe );
    }

  /** Class CrossTabOperation */
  public abstract static class CrossTabOperation extends Operation implements Aggregator
    {
    protected CrossTabOperation( Fields fields )
      {
      super( fields );
      }
    }
```

Lastly we extend the CrossTab PipeAssembly and make it our own.

```
public class PearsonDistance extends CrossTab
  {
  public PearsonDistance( Pipe previous, Fields argumentFieldSelector, Fields fieldDeclaration )
    {
    super( previous, argumentFieldSelector, new Pearson(), fieldDeclaration );
    }
  
  // implement the pearson score calculation
  private static class Pearson extends CrossTabOperation
    {
    private static final String COUNT = "count";
    private static final String SUM1 = "sum1";
    private static final String SUM2 = "sum2";
    private static final String SUMSQRS1 = "sumsqrs1";
    private static final String SUMSQRS2 = "sumsqrs2";
    private static final String SUMPROD = "sumprod";

    public Pearson()
      {
      super( new Fields( "pearson" ) );
      }

    public void start( Map context )
      {
      context.put( COUNT, 0d );
      context.put( SUM1, 0d );
      context.put( SUM2, 0d );
      context.put( SUMSQRS1, 0d );
      context.put( SUMSQRS2, 0d );
      context.put( SUMPROD, 0d );
      }

    public void aggregate( Map context, TupleEntry entry )
      {
      context.put( COUNT, ( (Double) context.get( COUNT ) ) + 1d );

      context.put( SUM1, ( (Double) context.get( SUM1 ) ) + entry.getTuple().getDouble( 0 ) );
      context.put( SUM2, ( (Double) context.get( SUM2 ) ) + entry.getTuple().getDouble( 1 ) );

      context.put( SUMSQRS1, ( (Double) context.get( SUMSQRS1 ) ) + Math.pow( entry.getTuple().getDouble( 0 ), 2 ) );
      context.put( SUMSQRS2, ( (Double) context.get( SUMSQRS2 ) ) + Math.pow( entry.getTuple().getDouble( 1 ), 2 ) );

      context.put( SUMPROD, ( (Double) context.get( SUMPROD ) ) + ( entry.getTuple().getDouble( 0 ) * entry.getTuple().getDouble( 1 ) ) );
      }

    public void complete( Map context, TupleEntryListIterator outputCollector )
      {
      Double count = (Double) context.get( COUNT );
      Double sum1 = (Double) context.get( SUM1 );
      Double sum2 = (Double) context.get( SUM2 );

      double num = (Double) context.get( SUMPROD ) - ( sum1 * sum2 / count );
      double den = Math.sqrt( ( (Double) context.get( SUMSQRS1 ) - Math.pow( sum1, 2 ) / count ) * ( (Double) context.get( SUMSQRS2 ) - Math.pow( sum2, 2 ) / count ) );

      if( den == 0 )
        outputCollector.add( new Tuple( 0 ) );
      else
        outputCollector.add( new Tuple( num / den ) );
      }
    }
  }
```