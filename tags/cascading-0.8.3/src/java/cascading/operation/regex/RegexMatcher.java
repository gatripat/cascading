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

package cascading.operation.regex;

import java.util.regex.Matcher;

import cascading.tuple.Tuple;
import org.apache.log4j.Logger;

/** Class RegexMatcher is the base class for common regular expression operations. */
public class RegexMatcher extends RegexOperation
  {
  /** Field LOG */
  private static final Logger LOG = Logger.getLogger( RegexMatcher.class );
  /** Field removeMatch */
  protected final boolean negateMatch;

  protected RegexMatcher( String patternString )
    {
    super( patternString );
    this.negateMatch = false;
    }

  protected RegexMatcher( String patternString, boolean negateMatch )
    {
    super( patternString );
    this.negateMatch = negateMatch;
    }

  /**
   * Method matchWholeTuple ...
   *
   * @param input of type Tuple
   * @return boolean
   */
  protected boolean matchWholeTuple( Tuple input )
    {
    Matcher matcher = getPattern().matcher( input.toString( "\t" ) );

    if( LOG.isDebugEnabled() )
      LOG.debug( "pattern: " + getPattern() + ", matches: " + matcher.matches() );

    return matcher.matches() == negateMatch;
    }

  /**
   * Method matchEachElement ...
   *
   * @param input of type Tuple
   * @return boolean
   */
  protected boolean matchEachElement( Tuple input )
    {
    return matchEachElementPos( input ) != -1;
    }

  protected int matchEachElementPos( Tuple input )
    {
    int pos = 0;
    for( Object value : input )
      {
      if( value == null )
        value = "";

      Matcher matcher = getPattern().matcher( value.toString() );

      if( LOG.isDebugEnabled() )
        LOG.debug( "pattern: " + getPattern() + ", matches: " + matcher.matches() + ", element: '" + value + "'" );

      if( matcher.matches() == negateMatch )
        return pos;

      pos++;
      }

    return -1;
    }
  }