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

package cascading.operation;

import cascading.tuple.Fields;
import cascading.tuple.TupleCollector;
import cascading.tuple.TupleEntry;

/**
 * Class Cut should be used to narrow and optionally reorder the fields passed to the outgoing stream.
 * <p/>
 * This is especially useful if the current stream has many values unused downstream.
 * <p/>
 * Cut is similar to {@link Identity} in that it only copies values from the current stream, but it does
 * not allow for renaming of fields.
 *
 * @see Identity
 */
public class Cut extends BaseOperation implements Function
  {
  /** Field fieldSelector */
  private final Fields fieldSelector;

  /**
   * Constructs a new instance that returns the fields declared in fieldDeclaration and accepts any number of arguments.
   *
   * @param fieldDeclaration of type Fields
   */
  public Cut( Fields fieldDeclaration )
    {
    super( Fields.asDeclaration( fieldDeclaration ) );
    this.fieldSelector = fieldDeclaration;
    }

  /** @see Function#operate(TupleEntry, TupleCollector) */
  public void operate( TupleEntry input, TupleCollector outputCollector )
    {
    outputCollector.add( input.selectEntry( fieldSelector ) );
    }
  }
