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

package cascading.pipe.cogroup;

import java.util.Iterator;

import cascading.flow.FlowSession;
import cascading.tuple.Fields;
import cascading.tuple.IndexTuple;
import cascading.tuple.SpillableTupleList;
import cascading.tuple.Tuple;
import org.apache.log4j.Logger;

/** Class CoGroupClosure ... */
public class CoGroupClosure extends GroupClosure
  {
  public static final String SPILL_THRESHOLD = "cascading.cogroup.spill.threshold";
  private static final int defaultThreshold = 10 * 1000;

  /** Field LOG */
  private static final Logger LOG = Logger.getLogger( CoGroupClosure.class );

  /** Field groups */
  SpillableTupleList[] groups;

  public CoGroupClosure( FlowSession flowSession, int repeat, Fields[] groupingFields, Fields[] valueFields, Tuple key, Iterator values )
    {
    super( groupingFields, valueFields, key, values );
    build( flowSession, repeat );
    }

  @Override
  public int size()
    {
    return groups.length;
    }

  @Override
  public Iterator<Tuple> getIterator( int pos )
    {
    if( pos < 0 || pos >= groups.length )
      throw new IllegalArgumentException( "invalid group position: " + pos );

    return makeIterator( pos, groups[ pos ].iterator() );
    }

  public SpillableTupleList getGroup( int pos )
    {
    return groups[ pos ];
    }

  public void build( FlowSession flowSession, int repeat )
    {
    int numPipes = groupingFields.length;
    groups = new SpillableTupleList[Math.max( numPipes, repeat )];

    for( int i = 0; i < numPipes; i++ ) // use numPipes not repeat, see below
      groups[ i ] = new SpillableTupleList( getLong( flowSession, SPILL_THRESHOLD, defaultThreshold ) );

    while( values.hasNext() )
      {
      IndexTuple current = (IndexTuple) values.next();
      int pos = current.getIndex();

      if( LOG.isDebugEnabled() )
        {
        LOG.debug( "group pos: " + pos );

        if( repeat != 1 )
          LOG.debug( "repeating: " + repeat );
        }

      groups[ pos ].add( (Tuple) current.getTuple() ); // get the value tuple for this cogroup
      }

    for( int i = 1; i < repeat; i++ )
      groups[ i ] = groups[ 0 ];
    }

  private long getLong( FlowSession flowSession, String key, long defaultValue )
    {
    String value = (String) flowSession.getCurrentProcess().getProperty( key );

    if( value == null || value.length() == 0 )
      return defaultValue;

    return Long.parseLong( value );
    }
  }
