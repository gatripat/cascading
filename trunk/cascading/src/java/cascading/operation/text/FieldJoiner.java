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

package cascading.operation.text;

import cascading.operation.Function;
import cascading.operation.Operation;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryListIterator;

/** Class FieldJoiner joins the values in a Tuple with a given delimiter and stuffs the result into a new field. */
public class FieldJoiner extends Operation implements Function
  {
  /** Field FIELD_NAME */
  public static final String FIELD_NAME = "joined";

  /** Field delimiter */
  private String delimiter = "\t";

  /**
   * Constructor FieldJoiner creates a new FieldJoiner instance.
   *
   * @param delimiter of type String
   */
  public FieldJoiner( String delimiter )
    {
    this( new Fields( FIELD_NAME ) );
    this.delimiter = delimiter;
    }

  /**
   * Constructor FieldJoiner creates a new FieldJoiner instance.
   *
   * @param fieldDeclaration of type Fields
   */
  public FieldJoiner( Fields fieldDeclaration )
    {
    super( fieldDeclaration );
    }

  /**
   * Constructor FieldJoiner creates a new FieldJoiner instance.
   *
   * @param delimiter        of type String
   * @param fieldDeclaration of type Fields
   */
  public FieldJoiner( String delimiter, Fields fieldDeclaration )
    {
    super( fieldDeclaration );
    this.delimiter = delimiter;
    }

  /**
   * Method getDelimiter returns the delimiter of this FieldJoiner object.
   *
   * @return the delimiter (type String) of this FieldJoiner object.
   */
  public String getDelimiter()
    {
    return delimiter;
    }

  /** @see cascading.operation.Function#operate(TupleEntry, TupleEntryListIterator) */
  public void operate( TupleEntry input, TupleEntryListIterator outputCollector )
    {
    outputCollector.add( new Tuple( input.getTuple().toString( delimiter ) ) );
    }
  }
