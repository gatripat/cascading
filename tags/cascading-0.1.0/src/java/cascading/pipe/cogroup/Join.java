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

package cascading.pipe.cogroup;

import java.util.Iterator;

import cascading.tuple.Tuple;
import org.apache.log4j.Logger;

/** Class Join ... */
public class Join implements CoGrouper
  {
  private static final Logger LOG = Logger.getLogger( Join.class );

  public Iterator<Tuple> getIterator( GroupClosure closure )
    {
    return new JoinIterator( closure );
    }

  private class JoinIterator implements Iterator<Tuple>
    {
    final GroupClosure closure;
    Iterator[] iterators;
    Comparable[] lastValues;

    public JoinIterator( GroupClosure closure )
      {
      this.closure = closure;

      if( LOG.isDebugEnabled() )
        LOG.debug( "cogrouped size: " + ( closure.size() ) );

      init();
      }

    public void init()
      {
      iterators = new Iterator[closure.size()];

      for( int i = 0; i < closure.size(); i++ )
        iterators[ i ] = closure.getIterator( i );
      }

    private Comparable[] initLastValues()
      {
      lastValues = new Comparable[iterators.length];

      for( int i = 0; i < iterators.length; i++ )
        lastValues[ i ] = (Comparable) iterators[ i ].next();

      return lastValues;
      }

    public final boolean hasNext()
      {
      for( Iterator iterator : iterators )
        {
        if( iterator.hasNext() )
          return true;
        }

      return false;
      }

    public Tuple next()
      {
      if( lastValues == null )
        return makeResult( initLastValues() );

      for( int i = iterators.length - 1; i >= 0; i-- )
        {
        if( iterators[ i ].hasNext() )
          {
          lastValues[ i ] = (Comparable) iterators[ i ].next();
          break;
          }

        // reset to first
        iterators[ i ] = closure.getIterator( i );
        lastValues[ i ] = (Comparable) iterators[ i ].next();
        }

      return makeResult( lastValues );
      }

    private Tuple makeResult( Comparable[] lastValues )
      {
//      Tuple result = new Tuple( closure.getGrouping() );
      Tuple result = new Tuple();

      // flatten the results into one Tuple
      for( Comparable lastValue : lastValues )
        result.addAll( lastValue );

      if( LOG.isDebugEnabled() )
        LOG.debug( "tuple: " + result.print() );

      return result;
      }

    public void remove()
      {
      // unsupported
      }
    }
  }