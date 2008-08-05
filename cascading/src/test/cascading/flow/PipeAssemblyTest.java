/*
 * Copyright (c) 2007-2008 Concurrent, Inc. All Rights Reserved.
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

package cascading.flow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cascading.CascadingTestCase;
import cascading.operation.Identity;
import cascading.operation.regex.RegexParser;
import cascading.pipe.Each;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.PipeAssembly;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;

/** A planner test only, does not execute */
public class PipeAssemblyTest extends CascadingTestCase
  {
  public PipeAssemblyTest()
    {
    super( "pipe assembly tests" );
    }

  private static class TestAssembly extends PipeAssembly
    {
    public TestAssembly( String name )
      {
      Pipe pipe = new Pipe( name );

      pipe = new Each( pipe, new Fields( "line" ), new RegexParser( new Fields( "ip" ), "^[^ ]*" ), new Fields( "ip" ) );

      setTails( pipe );
      }
    }

  /** Tests that proper pipe graph is assembled without throwing an internal error */
  public void testPipeAssembly()
    {
    Pipe pipe = new TestAssembly( "test" );
    pipe = new GroupBy( pipe, new Fields( "ip" ) );

    Tap source = new Hfs( new TextLine( new Fields( "offset", "line" ) ), "foo" );
    Tap sink = new Hfs( new TextLine(), "foo/split1", true );

    List<FlowStep> steps = new FlowConnector().connect( source, sink, pipe ).getSteps();

    assertEquals( "not equal: steps.size()", 1, steps.size() );
    }

  public void testPipeAssemblySplit()
    {
    Pipe pipe = new TestAssembly( "test" );
    Pipe pipe1 = new GroupBy( "left", pipe, new Fields( "ip" ) );
    Pipe pipe2 = new GroupBy( "right", pipe, new Fields( "ip" ) );

    Tap source = new Hfs( new TextLine( new Fields( "offset", "line" ) ), "foo" );
    Tap sink1 = new Hfs( new TextLine(), "foo/split1", true );
    Tap sink2 = new Hfs( new TextLine(), "foo/split2", true );

    Map sources = new HashMap();
    sources.put( "test", source );

    Map sinks = new HashMap();
    sinks.put( "left", sink1 );
    sinks.put( "right", sink2 );

    List<FlowStep> steps = new FlowConnector().connect( sources, sinks, pipe1, pipe2 ).getSteps();

    assertEquals( "not equal: steps.size()", 2, steps.size() );
    }

  private static class FirstAssembly extends PipeAssembly
    {
    public FirstAssembly( Pipe previous )
      {
      Pipe pipe = new Pipe( "first", previous );

      pipe = new Each( pipe, new Identity() );

      setTails( pipe );
      }
    }

  private static class SecondAssembly extends PipeAssembly
    {
    public SecondAssembly( Pipe previous )
      {
      Pipe pipe = new Pipe( "second", previous );

      pipe = new Each( pipe, new Identity() );

      pipe = new FirstAssembly( pipe );

      setTails( pipe );
      }
    }

  public void testNestedAssembliesAccessors() throws IOException
    {
    Pipe pipe = new Pipe( "test" );

    pipe = new SecondAssembly( pipe );

    Pipe[] allPrevious = pipe.getPrevious();

    assertEquals( "wrong number of previous", 1, allPrevious.length );

    for( Pipe previous : allPrevious )
      assertFalse( previous instanceof PipeAssembly );

    Pipe[] heads = pipe.getHeads();

    assertEquals( "wrong number of heads", 1, heads.length );

    for( Pipe head : heads )
      assertFalse( head instanceof PipeAssembly );

    }

  public void testNestedAssemblies() throws IOException
    {
    Tap source = new Hfs( new TextLine(), "input/path" );
    Tap sink = new Hfs( new TextLine(), "output/path", true );

    Pipe pipe = new Pipe( "test" );

    pipe = new SecondAssembly( pipe );

    pipe = new GroupBy( pipe, Fields.size( 1 ) );

    try
      {
      Flow flow = new FlowConnector().connect( source, sink, pipe );

//      flow.writeDOT( "nestedassembly.dot" );

      List<FlowStep> steps = flow.getSteps();

      assertEquals( "wrong size", 1, steps.size() );
      }
    catch( FlowException exception )
      {
//      exception.writeDOT( "nestedassembly.dot" );

      throw exception;
      }
    }
  }
