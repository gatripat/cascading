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

package cascading.flow.stack;

import java.io.IOException;
import java.util.Iterator;

import cascading.CascadingException;
import cascading.flow.FlowElement;
import cascading.flow.FlowException;
import cascading.flow.FlowProcess;
import cascading.flow.Scope;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.tap.Tap;
import cascading.tap.hadoop.TapCollector;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

/**
 *
 */
class TapReducerStackElement extends ReducerStackElement
  {
  private final Tap sink;
  private TapCollector tapCollector;

  public TapReducerStackElement( StackElement previous, FlowProcess flowProcess, Scope incomingScope, Tap sink, boolean useTapCollector ) throws IOException
    {
    super( previous, flowProcess, incomingScope, null );
    this.sink = sink;

    if( useTapCollector )
      this.tapCollector = (TapCollector) sink.openForWrite( getJobConf() );
    }

  public FlowElement getFlowElement()
    {
    return sink;
    }

  public void collect( Tuple key, Iterator values )
    {
    operateSink( key, values );
    }

  public void collect( Tuple tuple )
    {
    operateSink( getTupleEntry( tuple ) );
    }

  private void operateSink( Tuple key, Iterator values )
    {
    while( values.hasNext() )
      operateSink( (TupleEntry) values.next() );
    }

  private void operateSink( TupleEntry tupleEntry )
    {
    try
      {
      if( tapCollector != null )
        {
        ( (HadoopFlowProcess) getFlowProcess() ).getReporter().progress();
        ( (Tap) getFlowElement() ).sink( tupleEntry, tapCollector );
        }
      else
        {
        ( (Tap) getFlowElement() ).sink( tupleEntry, lastOutput );
        }
      }
    catch( OutOfMemoryError error )
      {
      throw new FlowException( "out of memory, try increasing task memory allocation", error );
      }
    catch( Throwable throwable )
      {
      if( throwable instanceof CascadingException )
        throw (CascadingException) throwable;

      throw new FlowException( "internal error: " + tupleEntry.getTuple().print(), throwable );
      }
    }

  public void prepare()
    {
    // do nothing
    }

  public void cleanup()
    {
    // do nothing
    }

  @Override
  public void close() throws IOException
    {
    if( tapCollector != null )
      tapCollector.close();

    super.close();
    }
  }
