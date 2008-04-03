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

package cascading.flow.stack;

import java.io.IOException;
import java.util.Iterator;

import cascading.flow.FlowElement;
import cascading.flow.FlowException;
import cascading.flow.Scope;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import org.apache.hadoop.mapred.OutputCollector;

/**
 *
 */
abstract class ReducerStackElement extends StackElement
  {
  /** Field incomingScope */
  Scope incomingScope; // used by everything but group
  /** Field incomingFields */
  private Fields incomingFields;
  /** Field outGroupingFields */
  private Fields outGroupingFields;
  /** Field groupingTupleEntry */
  private TupleEntry groupingTupleEntry;
  /** Field tupleEntry */
  private TupleEntry tupleEntry;
  /** Field lastOutput */
  OutputCollector lastOutput;

  ReducerStackElement( StackElement previous, Scope incomingScope )
    {
    this.previous = previous;
    this.incomingScope = incomingScope;
    }

  ReducerStackElement( Fields outGroupingFields )
    {
    this.outGroupingFields = outGroupingFields;
    }

  protected abstract FlowElement getFlowElement();

  Fields resolveIncomingFields()
    {
    if( incomingFields == null )
      incomingFields = getFlowElement().resolveFields( incomingScope );

    return incomingFields;
    }

  public void setLastOutput( OutputCollector lastOutput )
    {
    this.lastOutput = lastOutput;
    }

  Scope getIncomingScope()
    {
    return incomingScope;
    }

  Fields resolveIncomingOperationFields()
    {
    return getFlowElement().resolveIncomingOperationFields( incomingScope );
    }

  public void collect( Tuple key, Iterator values )
    {
    throw new FlowException( "no next stack element" );
    }

  public void collect( Tuple tuple )
    {
    throw new FlowException( "no next stack element" );
    }

  public Fields getOutGroupingFields()
    {
    if( outGroupingFields == null )
      outGroupingFields = incomingScope.getOutGroupingFields();

    return outGroupingFields;
    }

  TupleEntry getGroupingTupleEntry( Tuple tuple )
    {
    if( groupingTupleEntry == null )
      groupingTupleEntry = new TupleEntry( getOutGroupingFields() );

    groupingTupleEntry.setTuple( tuple );

    return groupingTupleEntry;
    }

  TupleEntry getTupleEntry( Tuple tuple )
    {
    if( tupleEntry == null )
      tupleEntry = new TupleEntry( resolveIncomingFields() );

    tupleEntry.setTuple( tuple );

    return tupleEntry;
    }

  public void close() throws IOException
    {
    }
  }
