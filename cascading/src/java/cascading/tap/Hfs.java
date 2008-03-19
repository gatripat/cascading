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

package cascading.tap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cascading.scheme.Scheme;
import cascading.scheme.SequenceFile;
import cascading.tuple.Fields;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;

/**
 * Class Hfs is the base class for all Hadoop file system access. Use {@link Dfs}, {@link Lfs}, or {@link S3fs}
 * for resources specific to Hadoop Distributed file system, the Local file system, or Amazon S3, respectively.
 * <p/>
 * Use the Hfs class if the 'kind' of resource is unknown at design time. To use, prefix a scheme to the 'stringPath'. Where
 * <code>hdfs://...</code> will denonte Dfs, <code>file://...</code> will denote Lfs, and <code>s3://aws_id:aws_secret@bucket/...</code> will denote S3fs.
 */
public class Hfs extends Tap
  {
  /** Field LOG */
  private static final Logger LOG = Logger.getLogger( Hfs.class );
  /** Field serialVersionUID */
  private static final long serialVersionUID = 1L;

  /** Field deleteOnSinkInit */
  boolean deleteOnSinkInit = false;
  /** Field stringPath */
  String stringPath;
  /** Field uriScheme */
  transient URI uriScheme;
  /** Field path */
  transient Path path;
  /** Field paths */
  private transient Path[] paths;

  protected Hfs( Scheme scheme )
    {
    super( scheme );
    }

  /**
   * Constructor Hfs creates a new Hfs instance.
   *
   * @param sourceFields of type Fields
   * @param stringPath   of type String
   */
  public Hfs( Fields sourceFields, String stringPath )
    {
    super( new SequenceFile( sourceFields ) );
    this.stringPath = stringPath;
    }

  /**
   * Constructor Hfs creates a new Hfs instance.
   *
   * @param sourceFields     of type Fields
   * @param stringPath       of type String
   * @param deleteOnSinkInit of type boolean
   */
  public Hfs( Fields sourceFields, String stringPath, boolean deleteOnSinkInit )
    {
    super( new SequenceFile( sourceFields ) );
    this.stringPath = stringPath;
    this.deleteOnSinkInit = deleteOnSinkInit;
    }

  /**
   * Constructor Hfs creates a new Hfs instance.
   *
   * @param scheme     of type Scheme
   * @param stringPath of type String
   */
  public Hfs( Scheme scheme, String stringPath )
    {
    super( scheme );
    this.stringPath = stringPath;
    }

  /**
   * Constructor Hfs creates a new Hfs instance.
   *
   * @param scheme           of type Scheme
   * @param stringPath       of type String
   * @param deleteOnSinkInit of type boolean
   */
  public Hfs( Scheme scheme, String stringPath, boolean deleteOnSinkInit )
    {
    super( scheme );
    this.stringPath = stringPath;
    this.deleteOnSinkInit = deleteOnSinkInit;
    }

  protected URI getURIScheme( JobConf jobConf ) throws IOException
    {
    if( uriScheme != null )
      return uriScheme;

    try
      {
      if( LOG.isDebugEnabled() )
        LOG.debug( "handling path: " + stringPath );

      URI uri = new URI( stringPath );
      String schemeString = uri.getScheme();
      String authority = uri.getAuthority();

      if( LOG.isDebugEnabled() )
        {
        LOG.debug( "found scheme: " + schemeString );
        LOG.debug( "found authority: " + authority );
        }

      if( schemeString != null && authority != null )
        uriScheme = new URI( schemeString + "://" + uri.getAuthority() );
      else if( schemeString != null )
        uriScheme = new URI( schemeString + ":///" );
      else
        uriScheme = FileSystem.get( jobConf ).getUri();

      if( LOG.isDebugEnabled() )
        LOG.debug( "using uri scheme: " + uriScheme );

      return uriScheme;
      }
    catch( URISyntaxException exception )
      {
      throw new TapException( "could not determine scheme from path: " + getPath(), exception );
      }
    }

  @Override
  public boolean isUseTapCollector()
    {
    return super.isUseTapCollector() || stringPath != null && stringPath.matches( "(^https?://.*$)|(^s3tp://.*$)" );
    }

  protected FileSystem getFileSystem( JobConf jobConf ) throws IOException
    {
    return FileSystem.get( getURIScheme( jobConf ), jobConf );
    }

  /** @see Tap#getPath() */
  @Override
  public Path getPath()
    {
    if( path != null )
      return path;

    if( stringPath == null )
      throw new IllegalStateException( "path not initialized" );

    path = new Path( stringPath );

    return path;
    }

  @Override
  public Path getQualifiedPath( JobConf conf ) throws IOException
    {
    return getPath().makeQualified( getFileSystem( conf ) );
    }

  /** @see Tap#isDeleteOnSinkInit() */
  @Override
  public boolean isDeleteOnSinkInit()
    {
    return deleteOnSinkInit;
    }

  @Override
  public void sourceInit( JobConf conf ) throws IOException
    {
    conf.addInputPath( getQualifiedPath( conf ) );
    super.sourceInit( conf );
    }

  @Override
  public void sinkInit( JobConf conf ) throws IOException
    {
    if( deleteOnSinkInit )
      deletePath( conf );

    conf.setOutputPath( getQualifiedPath( conf ) );

    super.sinkInit( conf );
    }

  @Override
  public boolean makeDirs( JobConf conf ) throws IOException
    {
    if( LOG.isDebugEnabled() )
      LOG.debug( "making dirs: " + getQualifiedPath( conf ) );

    return getFileSystem( conf ).mkdirs( getPath() );
    }

  @Override
  public boolean deletePath( JobConf conf ) throws IOException
    {
    if( LOG.isDebugEnabled() )
      LOG.debug( "deleting: " + getQualifiedPath( conf ) );

    return getFileSystem( conf ).delete( getPath() );
    }

  @Override
  public boolean pathExists( JobConf conf ) throws IOException
    {
    return getFileSystem( conf ).exists( getPath() );
    }

  @Override
  public long getPathModified( JobConf conf ) throws IOException
    {
    FileStatus fileStatus = getFileSystem( conf ).getFileStatus( getPath() );

    if( !fileStatus.isDir() )
      return fileStatus.getModificationTime();

    makePaths( conf );

    long date = 0;

    for( Path path1 : paths )
      date = Math.max( date, getFileSystem( conf ).getFileStatus( path1 ).getModificationTime() );

    return date;
    }

  @Override
  public boolean containsFile( JobConf conf, String currentFile )
    {
    Path currentFilePath = new Path( currentFile );

    try
      {
      Path qualified = getQualifiedPath( conf );

      if( LOG.isDebugEnabled() )
        LOG.debug( "comparing: " + qualified + " with: " + currentFilePath );

      if( qualified.equals( currentFilePath ) )
        return true;

      makePaths( conf );

      for( Path path1 : paths )
        {
        if( path1.equals( currentFilePath ) )
          return true;
        }
      }
    catch( IOException exception )
      {
      throw new TapException( "could not get FileSystem", exception );
      }

    return false;
    }

  protected Path getDfsTempPath( JobConf conf )
    {
    return new Path( conf.get( "hadoop.tmp.dir" ) );
    }

  protected String makeTemporaryPathDir( String name )
    {
    return name.replaceAll( " ", "_" ).replaceAll( "/", "_" ) + Integer.toString( (int) ( 10000000 * Math.random() ) );
    }

  /**
   * Given a file-system object, it makes an array of paths
   *
   * @param conf of type JobConf
   * @throws IOException on failure
   */
  private void makePaths( JobConf conf ) throws IOException
    {
    if( paths != null )
      return;

    FileSystem fileSystem = getFileSystem( conf );

    paths = fileSystem.listPaths( getPath() );

    for( int i = 0; i < paths.length; i++ )
      paths[ i ] = paths[ i ].makeQualified( fileSystem );
    }

  /** @see Object#toString() */
  @Override
  public String toString()
    {
    if( stringPath != null )
      return getClass().getSimpleName() + "[\"" + stringPath.replaceAll( "(?<=//).*:.*@", "" ) + "\"]"; // sanitize
    else
      return getClass().getSimpleName() + "[not initialized]";
    }

  /** @see Tap#equals(Object) */
  @Override
  public boolean equals( Object object )
    {
    if( this == object )
      return true;
    if( object == null || getClass() != object.getClass() )
      return false;
    if( !super.equals( object ) )
      return false;

    Hfs hfs = (Hfs) object;

    if( stringPath != null ? !stringPath.equals( hfs.stringPath ) : hfs.stringPath != null )
      return false;

    return true;
    }

  /** @see Tap#hashCode() */
  @Override
  public int hashCode()
    {
    int result = super.hashCode();
    result = 31 * result + ( stringPath != null ? stringPath.hashCode() : 0 );
    return result;
    }
  }
