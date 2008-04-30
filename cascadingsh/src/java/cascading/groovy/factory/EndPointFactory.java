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

import java.util.Map;

import cascading.scheme.Scheme;
import cascading.tap.Tap;
import groovy.util.FactoryBuilderSupport;

/**
 *
 */
public class EndPointFactory extends BaseFactory
  {
  public Object newInstance( FactoryBuilderSupport builder, Object type, Object value, Map attributes ) throws InstantiationException, IllegalAccessException
    {
    Scheme sourceScheme = null;

    if( type.equals( "sink" ) && builder.getCurrent() instanceof FlowFactory.FlowHolder ) // find source
      {
      TapMap tapMap = ( (FlowFactory.FlowHolder) builder.getCurrent() ).map;

      if( tapMap.sources.size() == 1 )
        sourceScheme = tapMap.getSource().getScheme();
      }

    if( value != null )
      value = value.toString();

    if( !attributes.containsKey( "path" ) ) // value is path, not name
      return new EndPointHolder( (String) type, (String) value, sourceScheme );
    else
      return new EndPointHolder( (String) type, (String) value, sourceScheme );
    }

  public class EndPointHolder extends BaseHolder
    {
    String argValue;
    String name = TapMap.DEFAULT_NAME;
    String path;
    Comparable[] fields;
    Scheme sourceScheme;
    Scheme scheme;
    boolean delete = false;
    Tap tap;

    public EndPointHolder( String type, String argValue, Scheme sourceScheme )
      {
      super( type );
      this.argValue = argValue;
      this.sourceScheme = sourceScheme;
      }

    public void setTap( Tap tap )
      {
      this.tap = tap;
      }

    public void setChild( Object child )
      {
      if( child instanceof Tap )
        setTap( (Tap) child );
      }

    public void handleParent( Object parent )
      {
      // add name/tap pair to parent
      if( !( parent instanceof TapMap ) )
        return;

      if( scheme == null && fields == null )
        scheme = sourceScheme;

      if( tap == null )
        {
        if( path == null )
          {
          path = argValue;
          argValue = null;
          }

        new TapFactory.TapHolder( "hfs", path, scheme, fields, delete ).handleParent( this );
        }

      if( argValue != null )
        name = argValue;

      if( tap == null )
        throw new RuntimeException( "no tap specified in " + getType() + "endpoint named " + name );

      TapMap tapMap = (TapMap) parent;

      if( getType().equalsIgnoreCase( "source" ) )
        tapMap.addSource( name, tap );
      else if( getType().equalsIgnoreCase( "sink" ) )
        tapMap.addSink( name, tap );
      else
        throw new RuntimeException( "unknown endpoint type: " + getType() );
      }
    }

  }