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

package cascading.pipe;

import java.util.Set;

import cascading.flow.FlowCollector;
import cascading.flow.FlowSession;
import cascading.flow.Scope;
import cascading.operation.Aggregator;
import cascading.operation.AssertionLevel;
import cascading.operation.GroupAssertion;
import cascading.operation.OperationCall;
import cascading.operation.Reducer;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleCollector;
import cascading.tuple.TupleEntry;

/**
 * The Every operator applies an {@link Aggregator} to every grouping. Any number of Every instances may follow other
 * Every or {@link Group} instance.
 * <p/>
 * Every operators create aggregate values for every grouping they encounter. This aggregate value is added to the current
 * grouping Tuple. Subsequent Every instances can continue to append values to the grouping Tuple. When an Each follows
 * and Every, the Each applies its operation to the grouping Tuple (thus all values in the grouping are discarded).
 */
public class Every extends Operator
  {
  /** Field AGGREGATOR_ARGUMENTS */
  private static final Fields AGGREGATOR_ARGUMENTS = Fields.ALL;
  /** Field AGGREGATOR_SELECTOR */
  private static final Fields AGGREGATOR_SELECTOR = Fields.ALL;
  /** Field ASSERTION_SELECTOR */
  private static final Fields ASSERTION_SELECTOR = Fields.RESULTS;

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous   of type Pipe
   * @param aggregator of type Aggregator
   */
  public Every( Pipe previous, Aggregator aggregator )
    {
    super( previous, AGGREGATOR_ARGUMENTS, aggregator, AGGREGATOR_SELECTOR );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous              of type Pipe
   * @param argumentFieldSelector of type Fields
   * @param aggregator            of type Aggregator
   */
  public Every( Pipe previous, Fields argumentFieldSelector, Aggregator aggregator )
    {
    super( previous, argumentFieldSelector, aggregator, AGGREGATOR_SELECTOR );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous              of type Pipe
   * @param argumentFieldSelector of type Fields
   * @param aggregator            of type Aggregator
   * @param outFieldSelector      of type Fields
   */
  public Every( Pipe previous, Fields argumentFieldSelector, Aggregator aggregator, Fields outFieldSelector )
    {
    super( previous, argumentFieldSelector, aggregator, outFieldSelector );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous         of type Pipe
   * @param aggregator       of type Aggregator
   * @param outFieldSelector of type Fields
   */
  public Every( Pipe previous, Aggregator aggregator, Fields outFieldSelector )
    {
    super( previous, AGGREGATOR_ARGUMENTS, aggregator, outFieldSelector );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous of type Pipe
   * @param reducer  of type Reducer
   */
  public Every( Pipe previous, Reducer reducer )
    {
    super( previous, AGGREGATOR_ARGUMENTS, reducer, AGGREGATOR_SELECTOR );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous              of type Pipe
   * @param argumentFieldSelector of type Fields
   * @param reducer               of type Reducer
   */
  public Every( Pipe previous, Fields argumentFieldSelector, Reducer reducer )
    {
    super( previous, argumentFieldSelector, reducer, AGGREGATOR_SELECTOR );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous              of type Pipe
   * @param argumentFieldSelector of type Fields
   * @param reducer               of type Reducer
   * @param outFieldSelector      of type Fields
   */
  public Every( Pipe previous, Fields argumentFieldSelector, Reducer reducer, Fields outFieldSelector )
    {
    super( previous, argumentFieldSelector, reducer, outFieldSelector );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous         of type Pipe
   * @param reducer          of type Reducer
   * @param outFieldSelector of type Fields
   */
  public Every( Pipe previous, Reducer reducer, Fields outFieldSelector )
    {
    super( previous, AGGREGATOR_ARGUMENTS, reducer, outFieldSelector );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous       of type Pipe
   * @param assertionLevel of type AssertionLevel
   * @param assertion      of type ValueAssertion
   */
  public Every( Pipe previous, AssertionLevel assertionLevel, GroupAssertion assertion )
    {
    super( previous, AGGREGATOR_ARGUMENTS, assertionLevel, assertion, ASSERTION_SELECTOR );
    }

  /**
   * Constructor Every creates a new Every instance.
   *
   * @param previous              of type Pipe
   * @param argumentFieldSelector of type Fields
   * @param assertionLevel        of type AssertionLevel
   * @param assertion             of type ValueAssertion
   */
  public Every( Pipe previous, Fields argumentFieldSelector, AssertionLevel assertionLevel, GroupAssertion assertion )
    {
    super( previous, argumentFieldSelector, assertionLevel, assertion, ASSERTION_SELECTOR );
    }

  /**
   * Method isReducer returns true if this Every instance holds a {@link Reducer} operation.
   *
   * @return boolean
   */
  public boolean isReducer()
    {
    return operation instanceof Reducer;
    }

  /**
   * Method isReducer returns true if this Every instance holds a {@link Aggregator} operation.
   *
   * @return boolean
   */
  public boolean isAggregator()
    {
    return operation instanceof Aggregator;
    }

  private Aggregator getAggregator()
    {
    return (Aggregator) operation;
    }

  private Reducer getReducer()
    {
    return (Reducer) operation;
    }

  private GroupAssertion getGroupAssertion()
    {
    return (GroupAssertion) operation;
    }

  @Override
  public Fields resolveIncomingOperationFields( Scope incomingScope )
    {
    if( incomingScope.isEach() || incomingScope.isTap() )
      throw new IllegalStateException( "Every cannot follow a Tap or an Each" );

    return incomingScope.getOutValuesFields();
    }

  @Override
  public Fields resolveFields( Scope scope )
    {
    if( scope.isEach() || scope.isTap() )
      throw new IllegalStateException( "Every cannot follow a Tap or an Each" );

    if( isReducer() )
      return scope.getOutValuesFields();
    else
      return scope.getOutGroupingFields();
    }

  /** @see Operator#outgoingScopeFor(Set<Scope>) */
  public Scope outgoingScopeFor( Set<Scope> incomingScopes )
    {
    Fields argumentSelector = resolveArgumentSelector( incomingScopes );

    verifyArguments( argumentSelector );

    // we currently don't support using result from a previous Every in the current Every
    Scope scope = getFirst( incomingScopes );

    if( scope.isEvery() && argumentSelector.contains( scope.getDeclaredFields() ) )
      throw new OperatorException( "arguments may not select a declared field from a previous Every" );

    Fields declared = resolveDeclared( incomingScopes, argumentSelector );

    verifyDeclared( declared );

    Fields outgoingGroupingSelector = resolveOutgoingGroupingSelector( incomingScopes, argumentSelector, declared );

    verifyOutputSelector( outgoingGroupingSelector );

    Fields outgoingValues = resolveOutgoingValues( incomingScopes );

    return new Scope( getName(), Scope.Kind.EVERY, argumentSelector, declared, outgoingGroupingSelector, outgoingValues );
    }

  Fields resolveOutgoingGroupingSelector( Set<Scope> incomingScopes, Fields argumentSelector, Fields declared )
    {
    try
      {
      return resolveOutgoingSelector( incomingScopes, argumentSelector, declared );
      }
    catch( Exception exception )
      {
      throw new OperatorException( "could not resolve outgoing grouping in: " + this, exception );
      }
    }

  Fields resolveOutgoingValues( Set<Scope> incomingScopes )
    {
    // Every never modifies the value stream, just the grouping stream
    try
      {
      return getFirst( incomingScopes ).getOutValuesFields();
      }
    catch( Exception exception )
      {
      throw new OperatorException( "could not resolve outgoing values selector in: " + this, exception );
      }
    }

  /**
   * Method getHandler returns the {@link EveryHandler} for this instnce.
   *
   * @param outgoingScope of type Scope
   * @return EveryHandler
   */
  public EveryHandler getHandler( Scope outgoingScope )
    {
    if( isAssertion() )
      return new EveryAssertionHandler( outgoingScope );
    else if( isAggregator() )
      return new EveryAggregatorHandler( outgoingScope );
    else
      return new EveryReducerHandler( outgoingScope );
    }

  /** Class EveryHandler is a helper class that wraps Every instances. */
  public abstract class EveryHandler
    {
    /** Field outgoingScope */
    public final Scope outgoingScope;
    /** Field outputCollector */
    public FlowCollector outputCollector;
    /** Field operationCall */
    OperationCall operationCall;

    public EveryHandler( Scope outgoingScope )
      {
      this.outgoingScope = outgoingScope;
      this.operationCall = new OperationCall();
      }

    public abstract void start( FlowSession flowSession, TupleEntry groupEntry );

    public abstract void operate( FlowSession flowSession, TupleEntry inputEntry );

    public abstract void complete( FlowSession flowSession, TupleEntry groupEntry );


    @Override
    public String toString()
      {
      return Every.this.toString();
      }

    public Every getEvery()
      {
      return Every.this;
      }
    }

  public class EveryAggregatorHandler extends EveryHandler
    {
    EveryTupleCollector tupleCollector;

    private abstract class EveryTupleCollector extends TupleCollector
      {
      TupleEntry value;

      public EveryTupleCollector( Fields fields )
        {
        super( fields );
        }
      }

    public EveryAggregatorHandler( final Scope outgoingScope )
      {
      super( outgoingScope );

      tupleCollector = new EveryTupleCollector( outgoingScope.getDeclaredFields() )
      {
      protected void collect( Tuple tuple )
        {
        outputCollector.collect( makeResult( outgoingScope.getOutGroupingSelector(), value, outgoingScope.getDeclaredEntry(), tuple ) );
        }
      };
      }

    public void start( FlowSession flowSession, TupleEntry groupEntry )
      {
      operationCall.setArguments( null );  // zero it out
      operationCall.setOutputCollector( null ); // zero it out
      operationCall.setGroup( groupEntry );
      getAggregator().start( flowSession, operationCall );
      }

    public void operate( FlowSession flowSession, TupleEntry inputEntry )
      {
      TupleEntry arguments = outgoingScope.getArgumentsEntry( inputEntry );

      operationCall.setArguments( arguments );

      try
        {
        getAggregator().aggregate( flowSession, operationCall );
        }
      catch( Throwable throwable )
        {
        throw new OperatorException( "operator Every failed executing aggregator: " + operation, throwable );
        }
      }

    public void complete( FlowSession flowSession, TupleEntry groupEntry )
      {
      tupleCollector.value = groupEntry;

      operationCall.setArguments( null );
      operationCall.setOutputCollector( tupleCollector );

      getAggregator().complete( flowSession, operationCall );
      }
    }

  public class EveryReducerHandler extends EveryHandler
    {
    EveryTupleCollector tupleCollector;

    private abstract class EveryTupleCollector extends TupleCollector
      {
      TupleEntry value;

      public EveryTupleCollector( Fields fields )
        {
        super( fields );
        }
      }

    public EveryReducerHandler( final Scope outgoingScope )
      {
      super( outgoingScope );

      tupleCollector = new EveryTupleCollector( outgoingScope.getDeclaredFields() )
      {
      protected void collect( Tuple tuple )
        {
        outputCollector.collect( makeResult( outgoingScope.getOutGroupingSelector(), value, outgoingScope.getDeclaredEntry(), tuple ) );
        }
      };
      }

    public void start( FlowSession flowSession, TupleEntry groupEntry )
      {
      tupleCollector.value = groupEntry;
      operationCall.setArguments( null );
      operationCall.setOutputCollector( tupleCollector );
      operationCall.setGroup( groupEntry );
      getReducer().start( flowSession, operationCall );
      }

    public void operate( FlowSession flowSession, TupleEntry inputEntry )
      {
      tupleCollector.value = inputEntry;

      TupleEntry arguments = outgoingScope.getArgumentsEntry( inputEntry );

      operationCall.setArguments( arguments );

      try
        {
        getReducer().operate( flowSession, operationCall );
        }
      catch( Throwable throwable )
        {
        throw new OperatorException( "operator Every failed executing reducer: " + operation, throwable );
        }
      }

    public void complete( FlowSession flowSession, TupleEntry groupEntry )
      {
      tupleCollector.value = groupEntry;

      operationCall.setArguments( null );

      getReducer().complete( flowSession, operationCall );
      }
    }

  public class EveryAssertionHandler extends EveryHandler
    {
    public EveryAssertionHandler( Scope outgoingScope )
      {
      super( outgoingScope );
      }

    public void start( FlowSession flowSession, TupleEntry groupEntry )
      {
      operationCall.setArguments( null );
      operationCall.setOutputCollector( null ); // zero it out
      operationCall.setGroup( groupEntry );

      getGroupAssertion().start( flowSession, operationCall );
      }

    public void operate( FlowSession flowSession, TupleEntry inputEntry )
      {
      TupleEntry arguments = outgoingScope.getArgumentsEntry( inputEntry );

      operationCall.setArguments( arguments );

      getGroupAssertion().aggregate( flowSession, operationCall );
      }

    public void complete( FlowSession flowSession, TupleEntry groupEntry )
      {
      operationCall.setArguments( null );
      getGroupAssertion().doAssert( flowSession, operationCall );

      outputCollector.collect( groupEntry.getTuple() );
      }
    }
  }
