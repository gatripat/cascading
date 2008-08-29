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

package cascading.stats;

/**
 * Class CascadingStats is the base class for all Cascading statistics gathering. It also reports the status of
 * core elements that have state.
 * <p/>
 * There are five states the stats object reports; pending, running, completed, failed, stopped, and finished.
 * <ul>
 * <li><code>pending</code> - when the Flow or Cascade has yet to start.</li>
 * <li><code>running</code> - when the Flow or Cascade is executing a workload.</li>
 * <li><code>completed</code> - when the Flow or Cascade naturally completed its workload.</li>
 * <li><code>failed</code> - when the Flow or Cascade threw an error and failed to finish the workload.</li>
 * <li><code>stopped</code> - when the user calls stop() on the Flow or Cascade.</li>
 * <li><code>finished</code> - when the Flow or Cascade is no longer processing a workload and <code>completed</code>,
 * <code>failed</code>, or <code>stopped</code> is true.</li>
 * </ul>
 *
 * @see FlowStats
 * @see CascadeStats
 */
public class CascadingStats
  {
  enum Status
    {
      PENDING, RUNNING, COMPLETED, FAILED, STOPPED;
    }

  /** Field status */
  Status status = Status.PENDING;
  /** Field startTime */
  long startTime;
  /** Field finishedTime */
  long finishedTime;
  /** Field throwable */
  Throwable throwable;

  /** Constructor CascadingStats creates a new CascadingStats instance. */
  CascadingStats()
    {
    }

  /**
   * Method isFinished returns true if the current status show no work currently being executed. This method
   * returns true if {@link #isCompleted()}, {@link #isFailed()}, or {@link #isStopped()} returns true.
   *
   * @return the finished (type boolean) of this CascadingStats object.
   */
  public boolean isFinished()
    {
    return status == Status.COMPLETED || status == Status.FAILED || status == Status.STOPPED;
    }

  /**
   * Method isPending returns true if no work has started.
   *
   * @return the pending (type boolean) of this CascadingStats object.
   */
  public boolean isPending()
    {
    return status == Status.PENDING;
    }

  /**
   * Method isRunning returns true when work has begun.
   *
   * @return the running (type boolean) of this CascadingStats object.
   */
  public boolean isRunning()
    {
    return status == Status.RUNNING;
    }

  /**
   * Method isCompleted returns true when work has completed successfully.
   *
   * @return the completed (type boolean) of this CascadingStats object.
   */
  public boolean isCompleted()
    {
    return status == Status.COMPLETED;
    }

  /**
   * Method isFailed returns true when the work ended with an error.
   *
   * @return the failed (type boolean) of this CascadingStats object.
   */
  public boolean isFailed()
    {
    return status == Status.FAILED;
    }

  /**
   * Method isStopped returns true when the user stopped the work.
   *
   * @return the stopped (type boolean) of this CascadingStats object.
   */
  public boolean isStopped()
    {
    return status == Status.STOPPED;
    }

  /** Method markRunning sets the status to running. */
  public void markRunning()
    {
    if( status != Status.PENDING )
      throw new IllegalStateException( "may not mark flow as " + Status.RUNNING + ", is already " + status );

    status = Status.RUNNING;
    markStartTime();
    }

  protected void markStartTime()
    {
    startTime = System.currentTimeMillis();
    }

  /** Method markCompleted sets the status to completed. */
  public void markCompleted()
    {
    if( status != Status.RUNNING )
      throw new IllegalStateException( "may not mark flow as " + Status.COMPLETED + ", is already " + status );

    status = Status.COMPLETED;
    markFinishedTime();
    }

  private void markFinishedTime()
    {
    finishedTime = System.currentTimeMillis();
    }

  /**
   * Method markFailed sets the status to failed.
   *
   * @param throwable of type Throwable
   */
  public void markFailed( Throwable throwable )
    {
    if( status != Status.RUNNING )
      throw new IllegalStateException( "may not mark flow as " + Status.FAILED + ", is already " + status );

    status = Status.FAILED;
    markFinishedTime();
    this.throwable = throwable;
    }

  /** Method markStopped sets the status to stopped. */
  public void markStopped()
    {
    if( status != Status.RUNNING )
      throw new IllegalStateException( "may not mark flow as " + Status.STOPPED + ", is already " + status );

    status = Status.STOPPED;
    markFinishedTime();
    }

  /**
   * Method getDuration returns the duration the work executed before being finished.
   *
   * @return the duration (type long) of this CascadingStats object.
   */
  public long getDuration()
    {
    if( finishedTime != 0 )
      return finishedTime - startTime;
    else
      return 0;
    }

  protected String getStatsString()
    {
    String string = "status=" + status + ", startTime=" + startTime;

    if( finishedTime != 0 )
      string += ", duration=" + ( finishedTime - startTime );

    return string;
    }

  @Override
  public String toString()
    {
    return "Cascading{" + getStatsString() + '}';
    }
  }
