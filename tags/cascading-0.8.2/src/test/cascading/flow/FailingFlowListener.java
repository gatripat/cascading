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

package cascading.flow;

/**
 *
 */
public class FailingFlowListener extends LockingFlowListener
  {
  public static enum OnFail
    {
      STARTING, STOPPING, COMPLETED, THROWABLE
    }

  private final OnFail onFail;

  public FailingFlowListener( OnFail onFail )
    {
    this.onFail = onFail;
    }

  public void onStarting( Flow flow )
    {
    super.onStarting( flow );

    if( onFail == OnFail.STARTING )
      throw new RuntimeException( "intentionally failed on: " + onFail );
    }

  public void onStopping( Flow flow )
    {
    super.onStopping( flow );

    if( onFail == OnFail.STOPPING )
      throw new RuntimeException( "intentionally failed on: " + onFail );
    }

  public void onCompleted( Flow flow )
    {
    super.onCompleted( flow );

    if( onFail == OnFail.COMPLETED )
      throw new RuntimeException( "intentionally failed on: " + onFail );
    }

  public boolean onThrowable( Flow flow, Throwable throwable )
    {
    super.onThrowable( flow, throwable );

    if( onFail == OnFail.THROWABLE )
      throw new RuntimeException( "intentionally failed on: " + onFail );

    return false;
    }
  }