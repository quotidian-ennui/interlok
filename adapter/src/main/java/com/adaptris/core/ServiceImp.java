package com.adaptris.core;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.GuidGenerator;

/**
 * <p>
 * Implementation of default / common behaviour for <code>Service</code>s.
 * Includes basic implementation of <code>MessageEventGenerator</code> which
 * returns the fully qualified name of the class.
 * </p>
 */
public abstract class ServiceImp implements Service {
  // protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient ComponentState serviceState;
  
  private String uniqueId;
  private transient boolean isBranching; // defaults to false
  private Boolean continueOnFail;
  private Boolean isTrackingEndpoint;
  private Boolean isConfirmation;

  /**
   * <p>
   * Creates a new instance. Default unique ID is autogenerated using {@link GuidGenerator#getUUID()}.
   * </p>
   */
  public ServiceImp() {
    setUniqueId(new GuidGenerator().getUUID());
    changeState(ClosedState.getInstance());
  }

  @Override
  public void stop() {
    // over-ride if required
  }

  @Override
  public void start() throws CoreException {
    // over-ride if required
  }

  @Override
  public String createName() {
    return this.getClass().getName();
  }

  @Override
  public String createQualifier() {
    return defaultIfEmpty(getUniqueId(), "");
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("[");
    result.append(this.getClass().getName());
    if (!isBlank(getUniqueId())) {
      result.append("] unique ID [");
      result.append(getUniqueId());
    }
    result.append("] continue on fail [");
    result.append(getContinueOnFail());
    result.append("]");

    return result.toString();
  }

  /** @see com.adaptris.core.Service#getUniqueId() */
  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  /** @see com.adaptris.core.Service#setUniqueId(java.lang.String) */
  @Override
  public void setUniqueId(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    uniqueId = s;
  }

  /** @see com.adaptris.core.Service#isBranching() */
  @Override
  public boolean isBranching() {
    return isBranching;
  }

  /**
   *
   * @see com.adaptris.core.Service#continueOnFailure()
   */
  @Override
  public boolean continueOnFailure() {
    if (getContinueOnFail() != null) {
      return getContinueOnFail().booleanValue();
    }
    return false;
  }

  /**
   * @see com.adaptris.core.Service#continueOnFailure()
   * @return whether or not this service is configured to continue on failure.
   */
  public Boolean getContinueOnFail() {
    return continueOnFail;
  }

  /**
   * @see com.adaptris.core.Service#continueOnFailure() param b whether or not
   *      this service is configured to continue on failure.
   */
  public void setContinueOnFail(Boolean b) {
    continueOnFail = b;
  }

  public Boolean getIsTrackingEndpoint() {
    return isTrackingEndpoint;
  }

  public void setIsTrackingEndpoint(Boolean b) {
    isTrackingEndpoint = b;
  }

  public Boolean getIsConfirmation() {
    return isConfirmation;
  }

  public void setIsConfirmation(Boolean b) {
    isConfirmation = b;
  }

  /**
   *
   * @see com.adaptris.core.MessageEventGenerator#isTrackingEndpoint()
   */
  @Override
  public boolean isTrackingEndpoint() {
    if (isTrackingEndpoint != null) {
      return isTrackingEndpoint.booleanValue();
    }
    return false;
  }

  /**
   *
   * @see com.adaptris.core.MessageEventGenerator#isConfirmation()
   */
  @Override
  public boolean isConfirmation() {
    if (isConfirmation != null) {
      return isConfirmation.booleanValue();
    }
    return false;
  }

  protected static void rethrowServiceException(Throwable e) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    throw new ServiceException(e);
  }
  
  /**
   * <p>
   * Updates the state for the component <code>ComponentState</code>.
   * </p>
   */
  public void changeState(ComponentState newState) {
    serviceState = newState;
  }
  
  /**
   * <p>
   * Returns the last record <code>ComponentState</code>.
   * </p>
   * @return the current <code>ComponentState</code>
   */
  public ComponentState retrieveComponentState() {
    return serviceState;
  }
  
  /**
   * <p>
   * Request this component is init'd.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  public void requestInit() throws CoreException {
    serviceState.requestInit(this);
  }

  /**
   * <p>
   * Request this component is started.
   * </p>
   * @throws CoreException wrapping any underlying Exceptions
   */
  public void requestStart() throws CoreException {
    serviceState.requestStart(this);
  }

  /**
   * <p>
   * Request this component is stopped.
   * </p>
   */
  public void requestStop() {
    serviceState.requestStop(this);
  }

  /**
   * <p>
   * Request this component is closed.
   * </p>
   */
  public void requestClose() {
    serviceState.requestClose(this);
  }

}
