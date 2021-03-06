/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.TimeInterval;

public class PooledConnectionHelper {

  private static Logger log = LoggerFactory.getLogger(PooledConnectionHelper.class);
  private static final TimeInterval DEFAULT_IDLE_TIME = new TimeInterval(1000L, TimeUnit.MILLISECONDS);
  private static final TimeInterval DEFAULT_IDLE_CONNECTION_TEST = new TimeInterval(1100L, TimeUnit.MILLISECONDS);
  private static final TimeInterval DEFAULT_ACQUIRE_WAIT = new TimeInterval(3000L, TimeUnit.MILLISECONDS);
  private static final TimeInterval DEFAULT_RETRY_WAIT = new TimeInterval(1000L, TimeUnit.MILLISECONDS);
  private static final TimeInterval DEFAULT_PAUSE = new TimeInterval(300L, TimeUnit.MILLISECONDS);

  public static void executeTest(List<Service> serviceList, final int iterations, final MessageCreator creator)
      throws Exception {
    MyExceptionHandler handler = new MyExceptionHandler();

    List<Thread> threads = new ArrayList<>();
    for (final Service s : serviceList) {
      Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            for (int j = 0; j < iterations; j++) {
              final AdaptrisMessage msg = creator.createMsgForPooledConnectionTest();
              s.doService(msg);
              TimeUnit.MILLISECONDS.sleep(DEFAULT_PAUSE.toMilliseconds());
            }
          }
          catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
          }
        }
      });
      t.setUncaughtExceptionHandler(handler);
      threads.add(t);
      t.start();
    }
    for (Thread t : threads) {
      if (t.isAlive()) t.join();
    }
    if (handler.hasError()) {
      throw handler.lastCaughtException();
    }
  }

  public static JdbcPooledConnection createPooledConnection(String driver, String url, int poolsize) {
    JdbcPooledConnection conn = new JdbcPooledConnection();
    conn.setConnectUrl(url);
    conn.setDriverImp(driver);
    conn.setMaximumPoolSize(poolsize);
    conn.setMinimumPoolSize(poolsize);
    conn.setMaxIdleTime(DEFAULT_IDLE_TIME);
    conn.setIdleConnectionTestPeriod(DEFAULT_IDLE_CONNECTION_TEST);
    conn.setConnectionAcquireWait(DEFAULT_ACQUIRE_WAIT);
    conn.setConnectionAttempts(1);
    conn.setConnectionRetryInterval(DEFAULT_RETRY_WAIT);
    return conn;
  }
  
  public static AdvancedJdbcPooledConnection createAdvancedPooledConnection(String driver, String url, int poolsize) {
    AdvancedJdbcPooledConnection conn = new AdvancedJdbcPooledConnection();
    conn.setConnectUrl(url);
    conn.setDriverImp(driver);
    KeyValuePairSet poolProps = new KeyValuePairSet();
    poolProps.add(new KeyValuePair(PooledConnectionProperties.maxPoolSize.name(), Integer.toString(poolsize)));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.minPoolSize.name(), Integer.toString(poolsize)));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.maxIdleTime.name(), Long.toString(DEFAULT_IDLE_TIME.toMilliseconds())));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.idleConnectionTestPeriod.name(), Long.toString(DEFAULT_IDLE_CONNECTION_TEST.toMilliseconds())));
    poolProps.add(new KeyValuePair(PooledConnectionProperties.checkoutTimeout.name(), Long.toString(DEFAULT_ACQUIRE_WAIT.toMilliseconds())));
    conn.setConnectionAttempts(1);
    conn.setConnectionPoolProperties(poolProps);
    conn.setConnectionRetryInterval(DEFAULT_RETRY_WAIT);
    return conn;
  }

  public interface MessageCreator {
    AdaptrisMessage createMsgForPooledConnectionTest() throws Exception;

  }

  private static class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Throwable lastCaughtException = null;

    /**
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      log.warn("UnhandledException from {}", t.getName(), e);
      lastCaughtException = e;
    }

    public boolean hasError() {
      return lastCaughtException != null;
    }

    public Exception lastCaughtException() {
      if (lastCaughtException instanceof RuntimeException) {
        throw (RuntimeException) lastCaughtException;
      }
      else if (lastCaughtException instanceof Error) {
        throw (Error) lastCaughtException;
      }
      return (Exception) lastCaughtException;
    }

  }
}
