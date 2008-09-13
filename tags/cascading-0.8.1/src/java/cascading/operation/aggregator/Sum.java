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

package cascading.operation.aggregator;

import java.util.Map;

import cascading.operation.Aggregator;
import cascading.operation.BaseOperation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleCollector;
import cascading.tuple.TupleEntry;
import cascading.tuple.Tuples;

/** Class Sum is an {@link Aggregator} that returns the sum of all numeric values in the current group. */
public class Sum extends BaseOperation implements Aggregator
  {
  /** Field FIELD_NAME */
  public static final String FIELD_NAME = "sum";

  /** Field type */
  private Class type = double.class;

  /** Constructor Sum creates a new Sum instance that accepts one argument and returns a single field named "sum". */
  public Sum()
    {
    super( 1, new Fields( FIELD_NAME ) );
    }

  /**
   * Constructs a new instance that returns the fields declared in fieldDeclaration and accepts
   * only 1 argument.
   *
   * @param fieldDeclaration of type Fields
   */
  public Sum( Fields fieldDeclaration )
    {
    super( 1, fieldDeclaration );

    if( !fieldDeclaration.isSubstitution() && fieldDeclaration.size() != 1 )
      throw new IllegalArgumentException( "fieldDeclaration may only declare 1 field, got: " + fieldDeclaration.size() );
    }

  /**
   * Constructs a new instance that returns the fields declared in fieldDeclaration and accepts
   * only 1 argument. The return result is coerced into the given Class type.
   *
   * @param fieldDeclaration of type Fields
   * @param type             of type Class
   */
  public Sum( Fields fieldDeclaration, Class type )
    {
    this( fieldDeclaration );
    this.type = type;
    }

  /** @see Aggregator#start(Map, TupleEntry) */
  @SuppressWarnings("unchecked")
  public void start( Map context, TupleEntry groupEntry )
    {
    context.put( FIELD_NAME, 0.0d );
    }

  /** @see Aggregator#aggregate(Map, TupleEntry) */
  @SuppressWarnings("unchecked")
  public void aggregate( Map context, TupleEntry entry )
    {
    context.put( FIELD_NAME, (Double) context.get( FIELD_NAME ) + entry.getTuple().getDouble( 0 ) );
    }

  /** @see Aggregator#complete(Map, TupleCollector) */
  @SuppressWarnings("unchecked")
  public void complete( Map context, TupleCollector outputCollector )
    {
    outputCollector.add( new Tuple( (Comparable) Tuples.coerce( new Tuple( (Comparable) context.get( FIELD_NAME ) ), 0, type ) ) );
    }
  }
