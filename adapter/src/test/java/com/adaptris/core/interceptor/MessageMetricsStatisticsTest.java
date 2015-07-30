package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.List;

import javax.management.JMX;
import javax.management.ObjectName;

import com.adaptris.core.Adapter;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.runtime.BaseComponentMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;

@SuppressWarnings("deprecation")
public class MessageMetricsStatisticsTest extends StatisticsMBeanCase {

  public MessageMetricsStatisticsTest(String name) {
    super(name);
  }

  public void testNoCachesExists() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean metrics = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      assertEquals(0, metrics.getNumberOfTimeSlices());
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgCountNoCacheExists() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      assertEquals(0, stats.getNumberOfMessagesForTimeSliceIndex(1));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgSizeNoCacheExists() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      assertEquals(0, stats.getTotalSizeOfMessagesForTimeSliceIndex(1));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgEndMillisNoCacheExists() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      assertEquals(0, stats.getEndMillisForTimeSliceIndex(1));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgCount() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.injectMessage(msg);
      assertEquals(1, stats.getNumberOfMessagesForTimeSliceIndex(0));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgEndMillis() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      long now = System.currentTimeMillis();
      workflow.injectMessage(msg);
      // We can't check the real time so we check that the time is greater than now.
      assertTrue(now < stats.getEndMillisForTimeSliceIndex(0));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgSize() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    String payload = "SomePayload";

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      workflow.injectMessage(createMessageForInjection(payload));
      assertEquals(1, stats.getNumberOfMessagesForTimeSliceIndex(0));
      assertEquals(payload.getBytes().length, stats.getTotalSizeOfMessagesForTimeSliceIndex(0));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMessageStatistics() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    String payload = "SomePayload";
    MessageMetricsInterceptor interceptor = (MessageMetricsInterceptor) createInterceptor();
    interceptor.setUniqueId(getName());
    Adapter adapter = createSingleChannelAdapter(adapterName, interceptor);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName, getName());
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      workflow.injectMessage(createMessageForInjection(payload));
      
      assertEquals(1, stats.getStatistics().size());
      assertEquals(interceptor.getCacheArray().size(), stats.getStatistics().size());
      MessageStatistic interceptorStat = interceptor.getCacheArray().get(0);
      MessageStatistic mbeanStat = stats.getStatistics().get(0);

      assertNotSame(interceptorStat, mbeanStat); // They're not objectively equals (not clones), and no equals method.

      assertEquals(interceptorStat.getEndMillis(), mbeanStat.getEndMillis());
      assertEquals(interceptorStat.getTotalMessageCount(), mbeanStat.getTotalMessageCount());
      assertEquals(interceptorStat.getTotalMessageErrorCount(), mbeanStat.getTotalMessageErrorCount());
      assertEquals(interceptorStat.getTotalMessageSize(), mbeanStat.getTotalMessageSize());
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetMsgSizeMultipleMessages() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    String payload1 = "SomePayload";
    String payload2 = "SomePayload-blah";
    String payload3 = "SomePayload-blah-blah";
    int bytesExpected = payload1.getBytes().length + payload2.getBytes().length + payload3.getBytes().length;

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      workflow.injectMessage(createMessageForInjection(payload1));
      workflow.injectMessage(createMessageForInjection(payload2));
      workflow.injectMessage(createMessageForInjection(payload3));
      assertEquals(3, stats.getNumberOfMessagesForTimeSliceIndex(0));
      assertEquals(bytesExpected, stats.getTotalSizeOfMessagesForTimeSliceIndex(0));
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetTotalStringStats() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      assertNotNull(stats.getTotalStringStats());
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.injectMessage(msg);
      assertNotNull(stats.getTotalStringStats());
    }
    finally {
      stop(adapter);
    }
  }

  public void testGetNumberOfErrorMessages() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();

    Adapter adapter = createAdapter(adapterName);
    // We need to get the workflow and add a FailService
    ((StandardWorkflow) adapter.getChannelList().get(0).getWorkflowList().get(0)).getServiceCollection().add(
        new ThrowExceptionService(new ConfiguredException("Fail")));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName workflowObj = createWorkflowObjectName(adapterName);
      ObjectName metricsObj = createMetricsObjectName(adapterName);
      MessageMetricsStatisticsMBean stats = JMX.newMBeanProxy(mBeanServer, metricsObj, MessageMetricsStatisticsMBean.class);
      WorkflowManagerMBean workflow = JMX.newMBeanProxy(mBeanServer, workflowObj, WorkflowManagerMBean.class);
      SerializableAdaptrisMessage msg = createMessageForInjection(null);
      workflow.injectMessage(msg);
      assertEquals(1, stats.getNumberOfErrorMessagesForTimeSliceIndex(0));
      assertEquals(0, stats.getNumberOfErrorMessagesForTimeSliceIndex(10));
    }
    finally {
      stop(adapter);
    }

  }

  protected SerializableAdaptrisMessage createMessageForInjection(String payload) {
    if (!isEmpty(payload)) {
      return new SerializableAdaptrisMessage(GUID.getUUID(), payload);
    }
    return new SerializableAdaptrisMessage(GUID.getUUID());
  }

  @Override
  protected WorkflowInterceptorImpl createInterceptor() {
    return new MessageMetricsInterceptor();
  }
}
