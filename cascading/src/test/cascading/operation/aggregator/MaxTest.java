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

package cascading.operation.aggregator;

import java.util.HashMap;
import java.util.Map;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryCollector;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/** Test class for {@link Max} */
public class MaxTest
  {

  /** class under test */
  private Max max;

  /** @throws java.lang.Exception  */
  @Before
  public void setUp() throws Exception
    {
    max = new Max();
    }

  /** @throws java.lang.Exception  */
  @After
  public void tearDown() throws Exception
    {
    max = null;
    }

  /** Test method for {@link cascading.operation.aggregator.Max#Max()}. */
  @Test
  public final void testMax()
    {
    assertEquals( "Got expected number of args", 1, max.getNumArgs() );
    final Fields fields = new Fields( "max" );
    assertEquals( "Got expected fields", fields, max.getFieldDeclaration() );
    }

  /** Test method for {@link cascading.operation.Aggregator#start(java.util.Map,cascading.tuple.TupleEntry)}. */
  @Test
  public final void testStart()
    {
    Map<String, Double> context = new HashMap<String, Double>();
    max.start( context, null );

    TupleEntryCollector resultEntryCollector = new TupleEntryCollector( new Fields( "field" ) );
    max.complete( context, resultEntryCollector.iterator() );
    Tuple tuple = resultEntryCollector.iterator().next().getTuple();

    assertEquals( "Got expected initial value on start", null, tuple.get( 0 ) );
    }

  /**
   * Test method for {@link cascading.operation.aggregator.Max#aggregate(java.util.Map, cascading.tuple.TupleEntry)}.
   * Test method for {@link cascading.operation.Aggregator#complete(java.util.Map,cascading.tuple.TupleCollector)}.
   */
  @Test
  public final void testAggregateComplete()
    {
    Map<String, Double> context = new HashMap<String, Double>();
    max.start( context, null );
    max.aggregate( context, new TupleEntry( new Tuple( new Double( 1.0 ) ) ) );
    max.aggregate( context, new TupleEntry( new Tuple( new Double( 3.0 ) ) ) );
    max.aggregate( context, new TupleEntry( new Tuple( new Double( 2.0 ) ) ) );
    max.aggregate( context, new TupleEntry( new Tuple( new Double( -4.0 ) ) ) );

    TupleEntryCollector resultEntryCollector = new TupleEntryCollector( new Fields( "field" ) );
    max.complete( context, resultEntryCollector.iterator() );
    Tuple tuple = resultEntryCollector.iterator().next().getTuple();

    assertEquals( "Got expected value after aggregate", 3.0, tuple.getDouble( 0 ), 0.0d );
    }
  }
