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

/** Class GroupClosure ... */
public class GroupClosure
  {
  private static final Logger LOG = Logger.getLogger( GroupClosure.class );

  final Tuple grouping;
  final Iterator values;

  public GroupClosure( Tuple key, Iterator values )
    {
    this.grouping = key;
    this.values = values;
    }

  public int size()
    {
    return 1;
    }

  public Tuple getGrouping()
    {
    return grouping;
    }

  public Iterator getIterator( int pos )
    {
    if( pos != 0 )
      throw new IllegalArgumentException( "invalid group position: " + pos );

    return values;
    }
  }