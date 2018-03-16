/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.core.services.splitter;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.impl.GenericObjectPool;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.ServiceWorkerPool.Worker;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension to {@link AdvancedMessageSplitterService} that uses a underlying thread and object pool to execute the service on each
 * split message.
 * <p>
 * Note that using this splitter may mean that messages become un-ordered; if the order of the split messages is critical, then you
 * probably shouldn't use this service. Additionally, individual split-message failures will only be reported on after all the split
 * messages have been processed, so unlike {@link AdvancedMessageSplitterService}; {@link #setIgnoreSplitMessageFailures(Boolean)}
 * will not halt the processing of the subsequent split messages
 * </p>
 * 
 * @config pooling-message-splitter-service
 */
@XStreamAlias("pooling-message-splitter-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and execute an arbitary number of services on the split message", tag = "service,splitter", since = "3.7.1")
@DisplayOrder(order =
{
    "splitter", "service", "maxThreads", "ignoreSplitMessageFailures", "sendEvents"
})
public class PoolingMessageSplitterService extends AdvancedMessageSplitterService {

  private static final long EVICT_RUN = new TimeInterval(60L, TimeUnit.SECONDS).toMilliseconds();

  @InputFieldDefault(value = "10")
  private Integer maxThreads;

  private transient ExecutorService executor;
  private transient ServiceExceptionHandler exceptionHandler;
  private transient ServiceWorkerPool workerFactory;
  private transient GenericObjectPool<ServiceWorkerPool.Worker> objectPool;

  @Override
  public Future<?> handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    exceptionHandler.clearExceptions();
    return executor.submit(new ServiceExecutor(exceptionHandler, msg));
  }

  protected void initService() throws CoreException {
    workerFactory = new ServiceWorkerPool(getService(), eventHandler, maxThreads());
    objectPool = workerFactory.createObjectPool();
    executor = workerFactory.createExecutor(this.getClass().getSimpleName());
    exceptionHandler = new ServiceExceptionHandler();
    super.initService();
  }

  protected void closeService() {
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval());
    ServiceWorkerPool.closeQuietly(objectPool);
    super.closeService();
  }

  protected void waitForCompletion(List<Future> tasks) throws ServiceException {
    super.waitForCompletion(tasks);
    exceptionHandler.throwFirstException();
  }

  public Integer getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(Integer maxThreads) {
    this.maxThreads = maxThreads;
  }

  int maxThreads() {
    return getMaxThreads() != null ? getMaxThreads().intValue() : 10;
  }

  private class ServiceExecutor implements Callable<AdaptrisMessage> {
    private ServiceExceptionHandler handler;
    private AdaptrisMessage msg;

    ServiceExecutor(ServiceExceptionHandler ceh, AdaptrisMessage msg) {
      handler = ceh;
      this.msg = msg;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      Worker w = objectPool.borrowObject();
      try {
        w.doService(msg);
      } catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      } finally {
        sendEvents(msg);
        objectPool.returnObject(w);
      }
      return msg;
    }
  }

}
