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
import java.util.Arrays;

import cascading.flow.FlowProcess;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.operation.OperationException;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;

/** Class RegexParser is used to extract a matched regex from an incoming argument value. */
public class RegexParser extends RegexOperation<Matcher> implements Function<Matcher>
  {
  /** Field groups */
  private int[] groups = null;

  /**
   * Constructor RegexParser creates a new RegexParser instance, where the argument Tuple value is matched and returned
   * in a new Tuple.
   * <p/>
   * If the given patternString declares regular expression groups, each group will be returned as a value in the
   * resulting Tuple. If no groups are declared, the match will be returned as the only value in the resulting Tuple.
   * <p/>
   * The fields returned will be {@link Fields#UNKNOWN}, so a variable number of values may be emitted based on the
   * regular expression given.
   *
   * @param patternString of type String
   */
  public RegexParser( String patternString )
    {
    super( 1, patternString );
    }

  /**
   * Constructor RegexParser creates a new RegexParser instance, where the argument Tuple value is matched and returned
   * as the given Field.
   * <p/>
   * If the given patternString declares regular expression groups, each group will be returned as a value in the
   * resulting Tuple. If no groups are declared, the match will be returned as the only value in the resulting Tuple.
   * <p/>
   * If the number of fields in the fieldDeclaration does not match the number of groups matched, an {@link OperationException}
   * will be thrown during runtime.
   * <p/>
   * To overcome this, either use the constructors that take an array of groups, or use the {@code (?: ...)} sequence
   * to tell the regular expression matcher to not capture the group.
   *
   * @param fieldDeclaration of type Fields
   * @param patternString    of type String
   */
  public RegexParser( Fields fieldDeclaration, String patternString )
    {
    super( 1, fieldDeclaration, patternString );
    }

  /**
   * Constructor RegexParser creates a new RegexParser instance, where the patternString is a regular expression
   * with match groups and whose groups designated by {@code groups} are stored in the appropriate number of new fields.
   * <p/>
   * The number of resulting fields will match the number of groups given ({@code groups.length}).
   *
   * @param patternString of type String
   * @param groups        of type int[]
   */
  public RegexParser( String patternString, int[] groups )
    {
    super( 1, Fields.size( groups.length ), patternString );
    this.groups = Arrays.copyOf( groups, groups.length );
    }

  /**
   * Constructor RegexParser creates a new RegexParser instance, where the patternString is a regular expression
   * with match groups and whose groups designated by {@code groups} are stored in the named fieldDeclarations.
   *
   * @param fieldDeclaration of type Fields
   * @param patternString    of type String
   * @param groups           of type int[]
   */
  public RegexParser( Fields fieldDeclaration, String patternString, int[] groups )
    {
    super( 1, fieldDeclaration, patternString );
    this.groups = Arrays.copyOf( groups, groups.length );

    if( !fieldDeclaration.isUnknown() && fieldDeclaration.size() != groups.length )
      throw new IllegalArgumentException( "fieldDeclaration must equal number of groups to be captured, fields: " + fieldDeclaration.print() );
    }

  @Override
  public void prepare( FlowProcess flowProcess, OperationCall<Matcher> operationCall )
    {
    operationCall.setContext( getPattern().matcher( "" ) );
    }

  /** @see Function#operate(cascading.flow.FlowProcess,cascading.operation.FunctionCall) */
  public void operate( FlowProcess flowProcess, FunctionCall<Matcher> functionCall )
    {
    String value = functionCall.getArguments().getString( 0 );

    if( value == null )
      value = "";

    Matcher matcher = functionCall.getContext().reset( value );

    if( !matcher.find() )
      throw new OperationException( "could not match pattern: [" + getPattern() + "] with value: [" + value + "]" );

    Tuple output = new Tuple();

    if( groups != null )
      onGivenGroups( functionCall, matcher, output );
    else
      onFoundGroups( functionCall, matcher, output );
    }

  private final void onFoundGroups( FunctionCall<Matcher> functionCall, Matcher matcher, Tuple output )
    {
    int count = matcher.groupCount();

    if( count == 0 )
      {
      output.add( matcher.group( 0 ) );
      }
    else
      {
      for( int i = 0; i < count; i++ )
        output.add( matcher.group( i + 1 ) ); // skip group 0
      }

    functionCall.getOutputCollector().add( output );
    }

  private final void onGivenGroups( FunctionCall<Matcher> functionCall, Matcher matcher, Tuple output )
    {
    for( int pos : groups )
      output.add( matcher.group( pos ) );

    functionCall.getOutputCollector().add( output );
    }
  }
