package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This service will cause the message to not be processed any further and will also request that the Workflows producer not be
 * called.
 * 
 * <p>
 * What happens will be dependent on the parent workflow and parent service collection implementation. See
 * {@link CoreConstants#STOP_PROCESSING_KEY} and {@link CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER} for more information.
 * </p>
 * 
 * @config stop-processing-service
 * @license BASIC
 */
@XStreamAlias("stop-processing-service")
public class StopProcessingService extends ServiceImp {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.addMetadata(CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
    msg.addMetadata(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER, CoreConstants.STOP_PROCESSING_VALUE);
    log.info("Message will now stop processing");
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

}
