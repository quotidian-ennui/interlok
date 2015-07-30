/*
 * $RCSfile: FixedIntervalPoller.java,v $
 * $Revision: 1.9 $
 * $Date: 2008/10/28 13:34:11 $
 * $Author: lchan $
 */
package com.adaptris.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Poller</code> which polls at a configurable fixed interval.
 * </p>
 * 
 * @config fixed-interval-poller
 * @license BASIC
 */
@XStreamAlias("fixed-interval-poller")
public class FixedIntervalPoller extends PollerImp {

  private static final TimeInterval DEFAULT_POLL_INTERVAL = new TimeInterval(20L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES);

  @Valid
  @AutoPopulated
  private TimeInterval pollInterval;
  private TimeInterval shutdownWaitTime;

  protected transient ScheduledExecutorService executor;
  protected transient ScheduledFuture pollerTask;

  /**
   * <p>
   * Creates a new instance.  Default poll interval is 20 seconds.
   * </p>
   */
  public FixedIntervalPoller() {
  }

  public FixedIntervalPoller(TimeInterval interval) {
    setPollInterval(interval);
  }

  public void init() throws CoreException {
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  public void start() throws CoreException {
    executor = Executors.newSingleThreadScheduledExecutor(new ManagedThreadFactory());
    scheduleTask();

  }

  protected void scheduleTask() {
    pollerTask = executor.scheduleWithFixedDelay(new PollerTask(), 100L, pollInterval(), TimeUnit.MILLISECONDS);
    log.trace("Scheduled {}", pollerTask);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  public void stop() {
    cancelTask();
    shutdownExecutor();
  }

  private void shutdownExecutor() {
    if (executor != null) {
      executor.shutdown();
      boolean success = false;
      try {
        success = executor.awaitTermination(shutdownWaitTimeMs(), TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {
      }
      if (!success) {
        log.trace("Pool failed to shutdown in {}ms, forcing shutdown", shutdownWaitTimeMs());
        executor.shutdownNow();
      }
      executor = null;
    }
  }

  private void cancelTask() {
    if (pollerTask != null) {
      pollerTask.cancel(true);
      log.trace("Poller {} cancelled", pollerTask);
      pollerTask = null;
    }
  }

  public void close() {
    cancelTask();
    shutdownExecutor();
  }

  /** @see com.adaptris.core.AdaptrisComponent#isEnabled
   *   (com.adaptris.util.license.License) */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }


  /**
   * <p>
   * Implementation of <code>TimerTask</code> to do the actual polling.
   * </p>
   */
  private class PollerTask implements Runnable {

    /** @see java.lang.Runnable#run() */
    @Override
    public void run() {
      processMessages();
    }
  }

  // properties...

  long pollInterval() {
    return getPollInterval() != null ? getPollInterval().toMilliseconds() : DEFAULT_POLL_INTERVAL.toMilliseconds();
  }

  public TimeInterval getPollInterval() {
    return pollInterval;
  }

  /**
   * Set the poll interval.
   *
   * @param pollInterval
   */
  public void setPollInterval(TimeInterval pollInterval) {
    this.pollInterval = pollInterval;
  }
  
  
  long shutdownWaitTimeMs() {
    return getShutdownWaitTime() != null ? getShutdownWaitTime().toMilliseconds() : DEFAULT_SHUTDOWN_WAIT.toMilliseconds();
  }
  

  public TimeInterval getShutdownWaitTime() {
    return shutdownWaitTime;
  }

  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When <code>stop()</code> is invoked, this causes a emptying and shutdown of the pool. The specified value is the amount of time
   * to wait for a clean shutdown. If this timeout is exceeded then a forced shutdown ensues, which may mean messages are in an
   * inconsistent state.
   * </p>
   *
   * @param interval the shutdown time (default is 60 seconds)
   * @see #stop()
   */
  public void setShutdownWaitTime(TimeInterval interval) {
    shutdownWaitTime = interval;
  }
}
