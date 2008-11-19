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

package cascading.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cascading.flow.FlowElement;
import cascading.flow.FlowException;
import cascading.flow.Scope;
import cascading.pipe.Pipe;
import cascading.operation.Operation;
import cascading.operation.BaseOperation;
import org.apache.commons.codec.binary.Base64;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.MatrixExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.SimpleDirectedGraph;

/** Class Util provides reusable operations. */
public class Util
  {
  /**
   * This method serializes the given Object instance and retunrs a String Base64 representation.
   *
   * @param object to be serialized
   * @return String
   */
  public static String serializeBase64( Object object ) throws IOException
    {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream( bytes );

    out.writeObject( object );

    return new String( Base64.encodeBase64( bytes.toByteArray() ) );
    }

  /**
   * This method deserializes the Base64 encoded String into an Object instance.
   *
   * @param string
   * @return
   */
  public static Object deserializeBase64( String string ) throws IOException
    {
    if( string == null || string.length() == 0 )
      return null;

    try
      {
      ByteArrayInputStream bytes = new ByteArrayInputStream( Base64.decodeBase64( string.getBytes() ) );
      ObjectInputStream in = new ObjectInputStream( bytes );

      return in.readObject();
      }
    catch( ClassNotFoundException exception )
      {
      throw new FlowException( "unable to deserialize data", exception );
      }
    }

  /**
   * This method joins the values in the given list with the delim String value.
   *
   * @param list
   * @param delim
   * @return String
   */
  public static String join( int[] list, String delim )
    {
    StringBuffer buffer = new StringBuffer();

    for( Object s : list )
      {
      if( s == null )
        continue;

      if( buffer.length() != 0 )
        buffer.append( delim );

      buffer.append( s );
      }

    return buffer.toString();
    }

  public static String join( String delim, String... strings )
    {
    return join( strings, delim );
    }

  /**
   * This method joins the values in the given list with the delim String value.
   *
   * @param list
   * @param delim
   * @return
   */
  public static String join( Object[] list, String delim )
    {
    StringBuffer buffer = new StringBuffer();

    for( Object s : list )
      {
      if( s == null )
        continue;

      if( buffer.length() != 0 )
        buffer.append( delim );

      buffer.append( s );
      }

    return buffer.toString();
    }

  /**
   * This method joins each value in the collection with a tab character as the delimiter.
   *
   * @param collection
   * @return
   */
  public static String join( Collection collection )
    {
    return join( collection, "\t" );
    }

  /**
   * This method joins each valuein the collection with the given delimiter.
   *
   * @param collection
   * @param delim
   * @return
   */
  public static String join( Collection collection, String delim )
    {
    StringBuffer buffer = new StringBuffer();

    join( buffer, collection, delim );

    return buffer.toString();
    }

  /**
   * This method joins each value in the collection with the given delimiter. All results are appended to the
   * given {@link StringBuffer} instance.
   *
   * @param buffer
   * @param collection
   * @param delim
   */
  public static void join( StringBuffer buffer, Collection collection, String delim )
    {
    for( Object s : collection )
      {
      if( buffer.length() != 0 )
        buffer.append( delim );

      buffer.append( s );
      }
    }

  /**
   * This method attempts to remove any username and password from the given url String.
   *
   * @param url
   * @return
   */
  public static String sanitizeUrl( String url )
    {
    if( url == null )
      return null;

    return url.replaceAll( "(?<=//).*:.*@", "" ) + "\"]";
    }

  /**
   * This methdo attempts to remove duplicate consecutive forward slashes from the given url.
   *
   * @param url
   * @return
   */
  public static String normalizeUrl( String url )
    {
    if( url == null )
      return null;

    return url.replaceAll( "([^:]/)/{2,}", "$1/" );
    }

  /**
   * This method returns the {@link Object#toString()} of the given object, or an empty String if the object
   * is null.
   *
   * @param object
   * @return
   */
  public static String toNull( Object object )
    {
    if( object == null )
      return "";

    return object.toString();
    }

  /**
   * This method truncates the given String value to the given size, but appends an ellipse ("...") if the
   * String is larger than maxSize.
   *
   * @param string
   * @param maxSize
   * @return
   */
  public static String truncate( String string, int maxSize )
    {
    string = toNull( string );

    if( string.length() <= maxSize )
      return string;

    return String.format( "%s...", string.subSequence( 0, maxSize - 3 ) );
    }

  public static <A> A getProperty( Map<Object, Object> properties, String key, A defaultValue )
    {
    if( properties == null )
      return defaultValue;

    A value = (A) properties.get( key );

    return value == null ? defaultValue : value;
    }

  public static String printGraph( SimpleDirectedGraph graph )
    {
    StringWriter writer = new StringWriter();

    printGraph( writer, graph );

    return writer.toString();
    }

  public static void printGraph( PrintStream out, SimpleDirectedGraph graph )
    {
    PrintWriter printWriter = new PrintWriter( out );

    printGraph( printWriter, graph );
    }

  public static void printGraph( String filename, SimpleDirectedGraph graph )
    {
    try
      {
      Writer writer = new FileWriter( filename );

      printGraph( writer, graph );

      writer.close();
      }
    catch( IOException exception )
      {
      exception.printStackTrace();
      }
    }

  @SuppressWarnings({"unchecked"})
  private static void printGraph( Writer writer, SimpleDirectedGraph graph )
    {
    DOTExporter dot = new DOTExporter( new IntegerNameProvider(), new VertexNameProvider()
    {
    public String getVertexName( Object object )
      {
      return object.toString().replaceAll( "\"", "\'" );
      }
    }, new EdgeNameProvider<Object>()
    {
    public String getEdgeName( Object object )
      {
      return object.toString().replaceAll( "\"", "\'" );
      }
    } );

    dot.export( writer, graph );
    }

  public static void printMatrix( PrintStream out, SimpleDirectedGraph<FlowElement, Scope> graph )
    {
    new MatrixExporter().exportAdjacencyMatrix( new PrintWriter( out ), graph );
    }

  /**
   * This method removes all nulls from the given List.
   *
   * @param list
   */
  @SuppressWarnings({"StatementWithEmptyBody"})
  public static void removeAllNulls( List list )
    {
    while( list.remove( null ) )
      ;
    }

  public static String formatTrace( Pipe pipe, String message )
    {
    String trace = pipe.getTrace();

    if( trace == null )
      return message;

    return "[" + trace + "] " + message;
    }

  public static String formatTrace( Operation operation, String message )
    {
    if( !( operation instanceof BaseOperation ) )
      return message;

    String trace = ( (BaseOperation) operation ).getTrace();

    if( trace == null )
      return message;

    return "[" + trace + "] " + message;
    }

  public static String captureDebugTrace( Class type )
    {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    for( int i = 3; i < stackTrace.length; i++ )
      {
      StackTraceElement stackTraceElement = stackTrace[ i ];

      if( stackTraceElement.getClassName().startsWith( type.getPackage().getName() ) )
        continue;

      return stackTraceElement.toString();
      }

    return null;
    }
  }
