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

package cascading.tap.hadoop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cascading.util.S3Util;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

/**
 * Class S3HttpFileSystem provides a basic {@link FileSystem} for reading and writing remote S3 data.
 * <p/>
 * To use this FileSystem, reference your S3 resources with the following URI pattern:<br/>
 * s3tp://AWS_ACCESS_KEY_ID:AWS_SECRET_ACCESS_KEY@bucketname/key
 * <p/>
 * Optionally these configuration/system properties can be set, instead of stuffing values into the URL authority:
 * "fs.s3tp.awsAccessKeyId" and "fs.s3tp.awsSecretAccessKey".
 */
public class S3HttpFileSystem extends StreamedFileSystem
  {
  public static final String S3TP_SCHEME = "s3tp";

  private URI uri;
  private RestS3Service s3Service;
  private S3Bucket s3Bucket;

  @Override
  public void initialize( URI uri, Configuration conf ) throws IOException
    {
    setConf( conf );

    String key = conf.get( "fs.s3tp.awsAccessKeyId", System.getProperty( "fs.s3tp.awsAccessKeyId" ) );
    String secret = conf.get( "fs.s3tp.awsSecretAccessKey", System.getProperty( "fs.s3tp.awsSecretAccessKey" ) );

    this.s3Service = S3Util.getS3Service( uri, key, secret );
    this.s3Bucket = S3Util.getS3Bucket( uri );
    this.uri = URI.create( uri.getScheme() + "://" + uri.getAuthority() );
    }

  @Override
  public URI getUri()
    {
    return uri;
    }

  @Override
  public FSDataOutputStream create( final Path path, FsPermission permission, boolean overwrite, int bufferSize, short replication, long blockSize, Progressable progress ) throws IOException
    {
    if( !overwrite && exists( path ) )
      throw new IOException( "file already exists: " + path );

    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    final DigestOutputStream digestStream = new DigestOutputStream( stream, getMD5Digest() );

    return new FSDataOutputStream( digestStream )
    {
    @Override
    public void close() throws IOException
      {
      super.close();

      S3Object object = S3Util.getObject( s3Service, s3Bucket, path, S3Util.Request.CREATE );

      object.setContentType( "text/plain" );
      object.setMd5Hash( digestStream.getMessageDigest().digest() );

      byte[] bytes = stream.toByteArray();

      object.setDataInputStream( new ByteArrayInputStream( bytes ) );
      object.setContentLength( bytes.length );

      S3Util.putObject( s3Service, s3Bucket, object );
      }
    };
    }

  @Override
  public FSDataInputStream open( Path path, int i ) throws IOException
    {
    S3Object object = S3Util.getObject( s3Service, s3Bucket, path, S3Util.Request.OBJECT );
    FSDigestInputStream inputStream = new FSDigestInputStream( S3Util.getObjectInputStream( object ), getMD5SumFor( getConf(), path ) );

    // ctor requires Seekable or PositionedReadable stream
    return new FSDataInputStream( inputStream );
    }

  @Override
  public boolean delete( Path path ) throws IOException
    {
    return S3Util.deleteObject( s3Service, s3Bucket, path );
    }

  @Override
  public boolean exists( Path path ) throws IOException
    {
    return S3Util.getObject( s3Service, s3Bucket, path, S3Util.Request.DETAILS ) != null;
    }

  @Override
  public FileStatus getFileStatus( Path path ) throws IOException
    {
    S3Object object = S3Util.getObject( s3Service, s3Bucket, path, S3Util.Request.DETAILS );

    if( LOG.isDebugEnabled() )
      LOG.debug( "returning status for: " + path );

    if( object == null )
      throw new FileNotFoundException( "file does not exist: " + path );

    return new StreamedFileStatus( object.getContentLength(), false, 1, getDefaultBlockSize(), object.getLastModifiedDate().getTime(), path,
      object.getMd5HashAsHex() );
    }

  private MessageDigest getMD5Digest() throws IOException
    {
    try
      {
      return MessageDigest.getInstance( "MD5" );
      }
    catch( NoSuchAlgorithmException exception )
      {
      throw new IOException( "digest not found: " + exception.getMessage() );
      }
    }


  }
