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

package cascading.groovy.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cascading.scheme.Scheme;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Lfs;
import cascading.tap.MultiTap;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import groovy.util.FactoryBuilderSupport;

/**
 *
 */
public class TapFactory extends BaseFactory
  {
  public Object newInstance( FactoryBuilderSupport builder, Object type, Object value, Map attributes ) throws InstantiationException, IllegalAccessException
    {
    if( type != null && type.equals( "tap" ) )
      return value;

    return new TapHolder( (String) type, value );
    }

  public static class TapHolder extends BaseHolder
    {
    List<String> paths = new ArrayList<String>();
    Scheme scheme;
    Comparable[] fields;
    boolean delete = false;

    public TapHolder( String type )
      {
      super( type );
      }

    public TapHolder( String type, Object path )
      {
      super( type );
      setPath( path );
      }

    public TapHolder( String type, Object path, Scheme scheme, Comparable[] fields, boolean delete )
      {
      super( type );
      setPath( path );
      this.scheme = scheme;
      this.fields = fields;
      this.delete = delete;
      }

    public void setPath( Object path )
      {
      if( path == null )
        return;

      if( path instanceof List )
        {
        List values = (List) path;

        for( Object value : values )
          paths.add( value.toString() );

        }
      else
        paths.add( path.toString() );
      }

    public void setScheme( Scheme scheme )
      {
      this.scheme = scheme;
      }

    public void setChild( Object child )
      {
      setScheme( (Scheme) child );
      }

    public void handleParent( Object parent )
      {
      ( (EndPointFactory.EndPointHolder) parent ).setTap( createTap() );
      }

    private Tap createTap()
      {
      Tap[] taps = new Tap[paths.size()];

      for( int i = 0; i < paths.size(); i++ )
        taps[ i ] = createTap( paths.get( i ) );

      if( taps.length == 1 )
        return taps[ 0 ];

      return new MultiTap( taps );
      }

    private Tap createTap( String path )
      {
      if( type.equalsIgnoreCase( "hfs" ) )
        return createHfs( path );
      else if( type.equals( "lfs" ) )
        return createLfs( path );
      else
        throw new RuntimeException( "unkown tap type: " + type );
      }

    private Lfs createLfs( String path )
      {
      if( scheme == null )
        return new Lfs( new TextLine(), path, delete );
      else
        return new Lfs( scheme, path, delete );
      }

    private Hfs createHfs( String path )
      {
      if( scheme == null && path.matches( ".*[.](txt|gz)[^/]?$" ) )
        scheme = SchemeFactory.createDefaultTextLine();

      if( scheme == null && fields == null )
        throw new RuntimeException( "must provide scheme or fields in tap with path: " + path );

      if( scheme == null )
        return new Hfs( new Fields( fields ), path, delete );
      else
        return new Hfs( scheme, path, delete );
      }
    }

  }