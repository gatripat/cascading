/*
 * Copyright (c) 2007-2009 Concurrent, Inc. All Rights Reserved.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import cascading.operation.AssertionLevel;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.Group;
import cascading.pipe.Pipe;
import cascading.pipe.SubAssembly;
import cascading.tap.Tap;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;

/** Class FlowPlanner is the base class for all planner implementations. */
public class FlowPlanner
  {
  /** Field properties */
  protected final Map<Object, Object> properties;

  /** Field assertionLevel */
  protected AssertionLevel assertionLevel;

  FlowPlanner( Map<Object, Object> properties )
    {
    this.properties = properties;
    this.assertionLevel = FlowConnector.getAssertionLevel( properties );
    }

  /**
   * Must be called to determine if all elements of the base pipe assembly are available
   *
   */
  protected void verifyAssembly( Pipe[] pipes, Map<String, Tap> sources, Map<String, Tap> sinks, Map<String, Tap> traps )
    {
    verifyTaps( sources, true, true );
    verifyTaps( sinks, false, true );
    verifyTaps( traps, false, false );

    verifyPipeAssemblyEndPoints( sources, sinks, pipes );
    verifyTraps( traps, pipes, sources, sinks );
    }

  /**
   * Creates a new ElementGraph instance.
   *
   */
  protected ElementGraph createElementGraph( Pipe[] pipes, Map<String, Tap> sources, Map<String, Tap> sinks, Map<String, Tap> traps )
    {
    return new ElementGraph( pipes, sources, sinks, traps, assertionLevel );
    }

  /**
   * Method verifyTaps ...
   *
   * @param taps          of type Map<String, Tap>
   * @param areSources    of type boolean
   * @param mayNotBeEmpty of type boolean
   */
  protected void verifyTaps( Map<String, Tap> taps, boolean areSources, boolean mayNotBeEmpty )
    {
    if( mayNotBeEmpty && taps.isEmpty() )
      throw new PlannerException( ( areSources ? "source" : "sink" ) + " taps are required" );

    for( String tapName : taps.keySet() )
      {
      if( areSources && !taps.get( tapName ).isSource() )
        throw new PlannerException( "tap named: " + tapName + " is not a source: " + taps.get( tapName ) );
      else if( !areSources && !taps.get( tapName ).isSink() )
        throw new PlannerException( "tap named: " + tapName + " is not a sink: " + taps.get( tapName ) );
      }
    }

  /**
   * Method verifyEndPoints ...
   *
   * @param sources of type Map<String, Tap>
   * @param sinks   of type Map<String, Tap>
   * @param pipes   of type Pipe[]
   */
  protected void verifyPipeAssemblyEndPoints( Map<String, Tap> sources, Map<String, Tap> sinks, Pipe[] pipes )
    {
    Set<String> names = new HashSet<String>();

    names.addAll( sources.keySet() );
    names.addAll( sinks.keySet() );

    // handle tails
    for( Pipe pipe : pipes )
      {
      if( pipe instanceof SubAssembly )
        {
        for( String tailName : ( (SubAssembly) pipe ).getTailNames() )
          {
          if( !names.contains( tailName ) )
            throw new PlannerException( pipe, "pipe name not found in either sink or source map: " + tailName );
          }
        }
      else if( !names.contains( pipe.getName() ) )
        {
        throw new PlannerException( pipe, "pipe name not found in either sink or source map: " + pipe.getName() );
        }
      }

    // handle heads
    for( Pipe pipe : pipes )
      {
      for( Pipe head : pipe.getHeads() )
        {
        if( !names.contains( head.getName() ) )
          throw new PlannerException( head, "pipe name not found in either sink or source map: " + head.getName() );
        }
      }
    }

  protected void verifyTraps( Map<String, Tap> traps, Pipe[] pipes, Map<String, Tap> sources, Map<String, Tap> sinks )
    {
    verifyTrapsNotSourcesSinks( traps, sources, sinks );

    Set<String> names = new HashSet<String>();

    collectNames( pipes, names );

    for( String name : traps.keySet() )
      {
      if( !names.contains( name ) )
        throw new PlannerException( "trap name not found in assembly: " + name );
      }
    }

  /**
   * Method collectNames ...
   *
   * @param pipes of type Pipe[]
   * @param names of type Set<String>
   */
  private void collectNames( Pipe[] pipes, Set<String> names )
    {
    for( Pipe pipe : pipes )
      {
      if( pipe instanceof SubAssembly )
        names.addAll( Arrays.asList( ( (SubAssembly) pipe ).getTailNames() ) );
      else
        names.add( pipe.getName() );

      collectNames( SubAssembly.unwind( pipe.getPrevious() ), names );
      }
    }

  private void verifyTrapsNotSourcesSinks( Map<String, Tap> traps, Map<String, Tap> sources, Map<String, Tap> sinks )
    {
    Collection<Tap> sourceTaps = sources.values();
    Collection<Tap> sinkTaps = sinks.values();

    for( Tap tap : traps.values() )
      {
      if( sourceTaps.contains( tap ) )
        throw new PlannerException( "tap may not be used as both a trap and a source in the same Flow: " + tap );

      if( sinkTaps.contains( tap ) )
        throw new PlannerException( "tap may not be used as both a trap and a sink in the same Flow: " + tap );
      }
    }

  /**
   * Verifies that there are not only GroupAssertions following any given Group instance. This will adversely
   * affect the stream entering any subsquent Tap of Each instances.
   */
  protected void failOnLoneGroupAssertion( ElementGraph elementGraph )
    {
    List<Group> groups = elementGraph.findAllGroups();

    // walk Every instances after Group
    for( Group group : groups )
      {
      for( GraphPath<FlowElement, Scope> path : elementGraph.getAllShortestPathsFrom( group ) )
        {
        List<FlowElement> flowElements = Graphs.getPathVertexList( path ); // last element is tail

        int everies = 0;
        int assertions = 0;

        for( FlowElement flowElement : flowElements )
          {
          if( flowElement instanceof Group )
            continue;

          if( !( flowElement instanceof Every ) )
            break;

          everies++;

          Every every = (Every) flowElement;

          if( every.getAssertionLevel() != null )
            assertions++;
          }

        if( everies != 0 && everies == assertions )
          throw new PlannerException( "group assertions must be accompanied by aggregator operations" );
        }
      }
    }

  protected void failOnMissingGroup( ElementGraph elementGraph )
    {
    List<Every> everies = elementGraph.findAllEveries();

    // walk Every instances after Group
    for( Every every : everies )
      {
      for( GraphPath<FlowElement, Scope> path : elementGraph.getAllShortestPathsTo( every ) )
        {
        List<FlowElement> flowElements = Graphs.getPathVertexList( path ); // last element is every
        Collections.reverse( flowElements ); // first element is every

        for( FlowElement flowElement : flowElements )
          {
          if( flowElement instanceof Each )
            throw new PlannerException( (Pipe) flowElement, "Every may only be preceeded by another Every or a Group pipe, found: " + flowElement );

          if( flowElement instanceof Every )
            continue;

          if( flowElement instanceof Group )
            break;
          }
        }
      }
    }

  protected void failOnMisusedBuffer( ElementGraph elementGraph )
    {
    List<Every> everies = elementGraph.findAllEveries();

    // walk Every instances after Group
    for( Every every : everies )
      {
      for( GraphPath<FlowElement, Scope> path : elementGraph.getAllShortestPathsTo( every ) )
        {
        List<FlowElement> flowElements = Graphs.getPathVertexList( path ); // last element is every
        Collections.reverse( flowElements ); // first element is every

        Every last = null;
        boolean foundBuffer = false;
        int foundEveries = -1;

        for( FlowElement flowElement : flowElements )
          {
          if( flowElement instanceof Each )
            throw new PlannerException( (Pipe) flowElement, "Every may only be preceeded by another Every or a Group pipe, found: " + flowElement );

          if( flowElement instanceof Every )
            {
            foundEveries++;

            boolean isBuffer = ( (Every) flowElement ).isBuffer();

            if( foundEveries != 0 && ( isBuffer || foundBuffer ) )
              throw new PlannerException( (Pipe) flowElement, "Only one Every Buffer may follow a Group pipe, found: " + flowElement + " before: " + last );

            if( !foundBuffer )
              foundBuffer = isBuffer;

            last = (Every) flowElement;
            }

          if( flowElement instanceof Group )
            break;
          }
        }
      }
    }
  }
