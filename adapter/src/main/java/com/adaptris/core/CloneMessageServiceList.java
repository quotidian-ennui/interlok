/*
 * $RCSfile: CloneMessageServiceList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.Collection;

import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@linkplain ServiceCollection} that creates a new clone of {@linkplain AdaptrisMessage} for each configured
 * service.
 * <p>
 * The expected use case for this {@linkplain ServiceCollection} is that you have a number of services that have to process exactly
 * the same message (e.g. transforming XML to an HTML representation as well as CSV).
 * </p>
 * <p>
 * If you have a list of services that require the same clone to be used; then use a nested {@link ServiceList} to wrap all the
 * required {@linkplain Service} implementations that require it (e.g. Transforming to HTML and emailing the result of the
 * transform)
 * </p>
 * <p>
 * If there are services configured after this {@linkplain ServiceCollection} implementation then they will process the message in
 * its original form.
 * </p>
 * 
 * @config clone-message-service-list
 * @license STANDARD
 */
@XStreamAlias("clone-message-service-list")
public class CloneMessageServiceList extends ServiceCollectionImp {

  public CloneMessageServiceList() {
    super();
  }

  public CloneMessageServiceList(Collection<Service> list) {
    super(list);
  }

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    for (Service service : getServices()) {
      try {
        service.doService((AdaptrisMessage) msg.clone());
        log.debug("service [" + friendlyName(service) + "] applied");
      }
      catch (CloneNotSupportedException e) {
        throw new ServiceException(e);
      }
      catch (Exception e) {
        this.handleException(service, msg, e);
      }
    }
  }

  @Override
  protected void doClose() {
  }

  @Override
  protected void doInit() throws CoreException {
  }

  @Override
  protected void doStart() throws CoreException {
  }

  @Override
  protected void doStop() {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard) && super.isEnabled(license);
  }

}
