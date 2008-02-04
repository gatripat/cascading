/*
 * Copyright (c) 2007-2008 Vinculum Technologies, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.operation.Filter;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.generator.UnGroup;
import cascading.operation.regex.RegexFilter;
import cascading.operation.regex.RegexParser;
import cascading.operation.regex.RegexSplitter;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.Group;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.Join;
import cascading.scheme.TextLine;
import cascading.tap.Dfs;
import cascading.tap.MultiTap;
import cascading.tap.Tap;
import cascading.tap.TapIterator;
import cascading.tuple.Fields;

/** @version $Id: //depot/calku/cascading/src/test/cascading/FieldedPipesTest.java#4 $ */
public class FieldedPipesTest extends ClusterTestCase
  {
  String inputFileApache = "build/test/data/apache.200.txt";
  String inputFileIps = "build/test/data/ips.20.txt";
  String inputFileNums = "build/test/data/nums.20.txt";
  String inputFileCritics = "build/test/data/critics.txt";

  String inputFileUpper = "build/test/data/upper.txt";
  String inputFileLower = "build/test/data/lower.txt";
  String inputFileJoined = "build/test/data/lower+upper.txt";

  String inputFileLhs = "build/test/data/lhs.txt";
  String inputFileRhs = "build/test/data/rhs.txt";
  String inputFileCross = "build/test/data/lhs+rhs-cross.txt";

  String outputPath = "build/test/output/fields/";

  public FieldedPipesTest()
    {
    super( "fielded pipes", true );
    }

  public void testSimpleGroup() throws Exception
    {
    if( !new File( inputFileApache ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileApache );

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileApache );

    Pipe pipe = new Pipe( "test" );

    pipe = new Each( pipe, new Fields( "line" ), new RegexParser( new Fields( "ip" ), "^[^ ]*" ), new Fields( "ip" ) );

    pipe = new Group( pipe, new Fields( "ip" ) );

    pipe = new Every( pipe, new Count(), new Fields( "ip", "count" ) );

    Tap sink = new Dfs( new TextLine(), outputPath + "/simple", true );

    Flow flow = new FlowConnector( jobConf ).connect( source, sink, pipe );

//    flow.writeDOT( "groupcount.dot" );

    flow.complete();

    validateLength( flow, 131, null );
    }

  public void testSimpleChain() throws Exception
    {
    if( !new File( inputFileApache ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileApache );

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileApache );

    Pipe pipe = new Pipe( "test" );

    pipe = new Each( pipe, new Fields( "line" ), new RegexParser( new Fields( "ip" ), "^[^ ]*" ), new Fields( "ip" ) );

    pipe = new Group( pipe, new Fields( "ip" ) );

    pipe = new Every( pipe, new Count( new Fields( "count1" ) ) );
    pipe = new Every( pipe, new Count( new Fields( "count2" ) ) );

    Tap sink = new Dfs( new TextLine(), outputPath + "/simplechain", true );

    Flow flow = new FlowConnector( jobConf ).connect( source, sink, pipe );

//    flow.writeDOT( "chainedevery.dot" );

    flow.complete();

    validateLength( flow, 131, null );
    }

  public void testCoGroup() throws Exception
    {
    if( !new File( inputFileLower ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileLower );
    copyFromLocal( inputFileUpper );

    Tap sourceLower = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileLower );
    Tap sourceUpper = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileUpper );

    Map sources = new HashMap();

    sources.put( "lower", sourceLower );
    sources.put( "upper", sourceUpper );

    Function splitter = new RegexSplitter( new Fields( "num", "char" ), " " );

    // using null pos so all fields are written
    Tap sink = new Dfs( new TextLine(), outputPath + "/complex/cogroup/", true );

    Pipe pipeLower = new Each( new Pipe( "lower" ), new Fields( "line" ), splitter );
    Pipe pipeUpper = new Each( new Pipe( "upper" ), new Fields( "line" ), splitter );

    Pipe splice = new CoGroup( pipeLower, new Fields( "num" ), pipeUpper, new Fields( "num" ), Fields.size( 4 ) );

    Flow countFlow = new FlowConnector( jobConf ).connect( sources, sink, splice );

//    countFlow.writeDOT( "cogroup.dot" );
//    System.out.println( "countFlow =\n" + countFlow );

    countFlow.complete();

    validateLength( countFlow, 5, null );

    TapIterator iterator = countFlow.openSink();

    assertEquals( "not equal: tuple.get(1)", "1\ta\t1\tA", iterator.next().get( 1 ) );
    assertEquals( "not equal: tuple.get(1)", "2\tb\t2\tB", iterator.next().get( 1 ) );

    iterator.close();
    }

  public void testCoGroupDiffFields() throws Exception
    {
    if( !new File( inputFileLower ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileLower );
    copyFromLocal( inputFileUpper );

    Tap sourceLower = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileLower );
    Tap sourceUpper = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileUpper );

    Map sources = new HashMap();

    sources.put( "lower", sourceLower );
    sources.put( "upper", sourceUpper );

    Function splitterLower = new RegexSplitter( new Fields( "numA", "lower" ), " " );
    Function splitterUpper = new RegexSplitter( new Fields( "numB", "upper" ), " " );

    // using null pos so all fields are written
    Tap sink = new Dfs( new TextLine(), outputPath + "/complex/cogroup/", true );

    Pipe pipeLower = new Each( new Pipe( "lower" ), new Fields( "line" ), splitterLower );
    Pipe pipeUpper = new Each( new Pipe( "upper" ), new Fields( "line" ), splitterUpper );

    Pipe cogroup = new Group( pipeLower, new Fields( "numA" ), pipeUpper, new Fields( "numB" ) );

    Flow flow = new FlowConnector( jobConf ).connect( sources, sink, cogroup );

//    System.out.println( "flow =\n" + flow );

    flow.complete();

    validateLength( flow, 5, null );

    TapIterator iterator = flow.openSink();

    assertEquals( "not equal: tuple.get(1)", "1\ta\t1\tA", iterator.next().get( 1 ) );
    assertEquals( "not equal: tuple.get(1)", "2\tb\t2\tB", iterator.next().get( 1 ) );

    iterator.close();
    }

  public void testCoGroupSamePipe() throws Exception
    {
    if( !new File( inputFileLower ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileLower );

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileLower );

    Map sources = new HashMap();

    sources.put( "lower", source );

    Function splitter = new RegexSplitter( new Fields( "num", "char" ), " " );

    // using null pos so all fields are written
    Tap sink = new Dfs( new TextLine(), outputPath + "/complex/cogroup/", true );

    Pipe pipeLower = new Each( new Pipe( "lower" ), new Fields( "line" ), splitter );

    Pipe cogroup = new Group( pipeLower, new Fields( "num" ), 2, new Fields( "num1", "char1", "num2", "char2" ) );

    Flow flow = new FlowConnector( jobConf ).connect( sources, sink, cogroup );

//    System.out.println( "flow =\n" + flow );

    flow.complete();

    validateLength( flow, 5, null );

    TapIterator iterator = flow.openSink();

    assertEquals( "not equal: tuple.get(1)", "1\ta\t1\ta", iterator.next().get( 1 ) );
    assertEquals( "not equal: tuple.get(1)", "2\tb\t2\tb", iterator.next().get( 1 ) );

    iterator.close();
    }

  public void testUnGroup() throws Exception
    {
    if( !new File( inputFileJoined ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileJoined );

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileJoined );
    Tap sink = new Dfs( new TextLine(), outputPath + "/ungrouped", true );

    Pipe pipe = new Pipe( "test" );

    pipe = new Each( pipe, new Fields( "line" ), new RegexSplitter( new Fields( "num", "lower", "upper" ) ) );

    pipe = new Each( pipe, new UnGroup( new Fields( "num", "char" ), new Fields( "num" ), Fields.fields( new Fields( "lower" ), new Fields( "upper" ) ) ) );

    Flow flow = new FlowConnector( jobConf ).connect( source, sink, pipe );

//    flow.writeDOT( "ungroup.dot" );

    flow.complete();

    validateLength( flow, 10, null );
    }

  public void testFilter() throws Exception
    {
    if( !new File( inputFileApache ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileApache );

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileApache );
    Tap sink = new Dfs( new TextLine(), outputPath + "/filter", true );

    Pipe pipe = new Pipe( "test" );

    Filter filter = new RegexFilter( "^68.*" );

    pipe = new Each( pipe, new Fields( "line" ), filter );

    Flow flow = new FlowConnector( jobConf ).connect( source, sink, pipe );

//    flow.writeDOT( "flow.dot" );

    flow.complete();

    validateLength( flow, 12, null );
    }

  public void testCross() throws Exception
    {
    if( !new File( inputFileLhs ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileLhs );
    copyFromLocal( inputFileRhs );

    Map sources = new HashMap();

    sources.put( "lhs", new Dfs( new TextLine(), inputFileLhs ) );
    sources.put( "rhs", new Dfs( new TextLine(), inputFileRhs ) );

    Pipe pipeLower = new Each( "lhs", new Fields( "line" ), new RegexSplitter( new Fields( "numLHS", "charLHS" ), " " ) );
    Pipe pipeUpper = new Each( "rhs", new Fields( "line" ), new RegexSplitter( new Fields( "numRHS", "charRHS" ), " " ) );

    Pipe cross = new Group( pipeLower, new Fields( "numLHS" ), pipeUpper, new Fields( "numRHS" ), new Join() );

    // using null pos so all fields are written
    Tap sink = new Dfs( new TextLine(), outputPath + "/complex/cross/", true );

    Flow flow = new FlowConnector( jobConf ).connect( sources, sink, cross );

//    System.out.println( "flow =\n" + flow );

    flow.complete();

    validateLength( flow, 37, null );

    TapIterator iterator = flow.openSink();

    assertEquals( "not equal: tuple.get(1)", "1\ta\t1\tA", iterator.next().get( 1 ) );
    assertEquals( "not equal: tuple.get(1)", "1\ta\t1\tB", iterator.next().get( 1 ) );

    iterator.close();
    }

  public void testSplit() throws Exception
    {
    if( !new File( inputFileApache ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileApache );

    // 46 192

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileApache );
    Tap sink1 = new Dfs( new TextLine(), outputPath + "/split1", true );
    Tap sink2 = new Dfs( new TextLine(), outputPath + "/split2", true );

    Pipe pipe = new Pipe( "split" );

    pipe = new Each( pipe, new Fields( "line" ), new RegexFilter( "^68.*" ) );

    Pipe left = new Each( new Pipe( "left", pipe ), new Fields( "line" ), new RegexFilter( ".*46.*" ) );
    Pipe right = new Each( new Pipe( "right", pipe ), new Fields( "line" ), new RegexFilter( ".*192.*" ) );

    Map sources = new HashMap();
    sources.put( "split", source );

    Map sinks = new HashMap();
    sinks.put( "left", sink1 );
    sinks.put( "right", sink2 );

    Flow flow = new FlowConnector( jobConf ).connect( sources, sinks, left, right );

//    flow.writeDOT( "split.dot" );

    flow.complete();

    validateLength( flow, 1, "left" );
    validateLength( flow, 2, "right" );
    }

  public void testSplitComplex() throws Exception
    {
    if( !new File( inputFileApache ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileApache );

    // 46 192

    Tap source = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileApache );
    Tap sink1 = new Dfs( new TextLine(), outputPath + "/splitcomp1", true );
    Tap sink2 = new Dfs( new TextLine(), outputPath + "/splitcomp2", true );

    Pipe pipe = new Pipe( "split" );

    pipe = new Each( pipe, new Fields( "line" ), new RegexParser( new Fields( "ip" ), "^[^ ]*" ), new Fields( "ip" ) );

    pipe = new Group( pipe, new Fields( "ip" ) );

    pipe = new Every( pipe, new Fields( "ip" ), new Count(), new Fields( "ip", "count" ) );

    pipe = new Each( pipe, new Fields( "ip" ), new RegexFilter( "^68.*" ) );

    Pipe left = new Each( new Pipe( "left", pipe ), new Fields( "ip" ), new RegexFilter( ".*46.*" ) );

    Pipe right = new Each( new Pipe( "right", pipe ), new Fields( "ip" ), new RegexFilter( ".*192.*" ) );

    Map sources = Cascades.tapsMap( "split", source );
    Map sinks = Cascades.tapsMap( Pipe.pipes( left, right ), Tap.taps( sink1, sink2 ) );

    Flow flow = new FlowConnector( jobConf ).connect( sources, sinks, left, right );

//    flow.writeDOT( "splitcomplex.dot" );

    flow.complete();

    validateLength( flow, 1, "left" );
    validateLength( flow, 1, "right" );
    }

  public void testConcatentation() throws Exception
    {
    if( !new File( inputFileLower ).exists() )
      fail( "data file not found" );

    copyFromLocal( inputFileLower );
    copyFromLocal( inputFileUpper );

    Tap sourceLower = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileLower );
    Tap sourceUpper = new Dfs( new TextLine( new Fields( "offset", "line" ) ), inputFileUpper );

    Tap source = new MultiTap( sourceLower, sourceUpper );

    Function splitter = new RegexSplitter( new Fields( "num", "char" ), " " );

    // using null pos so all fields are written
    Tap sink = new Dfs( new TextLine(), outputPath + "/complex/concat/", true );

    Pipe pipe = new Each( new Pipe( "concat" ), new Fields( "line" ), splitter );

    Pipe splice = new GroupBy( pipe, new Fields( "num" ) );

    Flow countFlow = new FlowConnector( jobConf ).connect( source, sink, splice );

//    countFlow.writeDOT( "cogroup.dot" );
//    System.out.println( "countFlow =\n" + countFlow );

    countFlow.complete();

    validateLength( countFlow, 10, null );
    }

  private void validateLength( Flow flow, int length, String name ) throws IOException
    {
    TapIterator iterator = name == null ? flow.openSink() : flow.openSink( name );
    int count = 0;
    while( iterator.hasNext() )
      {
      iterator.next();
      count++;
      }

    iterator.close();

    assertEquals( "wrong number of items", length, count );
    }

  }
