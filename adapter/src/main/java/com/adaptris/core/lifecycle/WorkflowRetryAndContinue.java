package com.adaptris.core.lifecycle;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultWorkflowLifecycleStrategy;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Strategy for starting workflows that retries a workflow init or start a number of times.
 * <p>
 * This implementation allows you to retry the init and start for a given workflow a configurable number of times. Each workflow is
 * attempted in sequence, and the failing workflow is retried until either it is successful or the max number of retry attempts has
 * been reached. If the workflow has still failed at this point, it is simply skipped and any remaining workflows initialised or
 * started.
 * </p>
 * 
 * <p>
 * Note that if you use this strategy then any configured {@link AdapterLifecycleEvent} may be inaccurate.
 * </p>
 * 
 * @config workflow-retry-and-continue
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("workflow-retry-and-continue")
public class WorkflowRetryAndContinue extends DefaultWorkflowLifecycleStrategy {

  enum WorkflowAction {
    Initialise {
      @Override
      void execute(Workflow wf) throws CoreException {
        LifecycleHelper.init(wf);
      }
    },
    Start {
      @Override
      void execute(Workflow wf) throws CoreException {
        LifecycleHelper.start(wf);
      }
    };
    abstract void execute(Workflow wf) throws CoreException;
  }

  static final int DEFAULT_MAX_RETRIES = 5;
  static final int INFINITE_RETRIES = -1;

  static final long DEFAULT_WAIT_BETWEEN_RETRIES = 10L;

  private Integer maxRetries;
  private TimeInterval waitBetweenRetries;

  private transient static final TimeInterval DEFAULT_INTERVAL_BETWEEN_RETRIES = new TimeInterval(DEFAULT_WAIT_BETWEEN_RETRIES,
      TimeUnit.SECONDS.name());

  public WorkflowRetryAndContinue() {

  }

  public WorkflowRetryAndContinue(Integer max, TimeInterval interval) {
    this();
    setMaxRetries(max);
    setWaitBetweenRetries(interval);
  }

  @Override
  public void start(List<Workflow> workflowList) throws CoreException {
    for (Workflow w : workflowList) {
      execute(WorkflowAction.Start, w);
    }
  }

  @Override
  public void init(List<Workflow> workflowList) throws CoreException {
    for (Workflow w : workflowList) {
      execute(WorkflowAction.Initialise, w);
    }
  }

  void execute(WorkflowAction action, Workflow wf) throws CoreException {
    int attempts = 0;
    while (!exceedsMaxRetries(attempts)) {
      attempts++;
      try {
        action.execute(wf);
        break;
      }
      catch (CoreException e) {
        waitQuietly();
      }
    }
  }

  void waitQuietly() {
    try {
      Thread.sleep(waitInterval());
    }
    catch (InterruptedException e) {
    }
  }

  private boolean exceedsMaxRetries(int attempts) {
    if (maxRetries() == INFINITE_RETRIES) {
      return false;
    }
    if (attempts > maxRetries()) {
      return true;
    }
    return false;
  }

  public Integer getMaxRetries() {
    return maxRetries;
  }

  /**
   * Set the maximum number of retries.
   *
   * <p>
   * Note that setting the maximum number of retries to be infinite will cause failling workflows to block other workflows from
   * starting until they are successfully actioned.
   * </p>
   *
   * @param maxRetries the max number of retries, if not set, defaults to 5. -1 means infinite retries.
   */
  public void setMaxRetries(Integer maxRetries) {
    this.maxRetries = maxRetries;
  }

  int maxRetries() {
    return getMaxRetries() != null ? getMaxRetries().intValue() : DEFAULT_MAX_RETRIES;
  }

  public TimeInterval getWaitBetweenRetries() {
    return waitBetweenRetries;
  }

  /**
   * Set the wait between each retry attempt.
   *
   * @param tu the wait between each retry attempt; if not explicitly set then defaults to 10 seconds.
   */
  public void setWaitBetweenRetries(TimeInterval tu) {
    waitBetweenRetries = tu;
  }

  long waitInterval() {
    return getWaitBetweenRetries() != null ? getWaitBetweenRetries().toMilliseconds() : DEFAULT_INTERVAL_BETWEEN_RETRIES
        .toMilliseconds();
  }
}
