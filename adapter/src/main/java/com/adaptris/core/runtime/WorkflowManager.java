package com.adaptris.core.runtime;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.http.jetty.BasicJettyConsumer;
import com.adaptris.core.http.jetty.JettyPoolingWorkflowInterceptor;
import com.adaptris.core.interceptor.MessageMetricsInterceptor;
import com.adaptris.util.TimeInterval;

/**
 * Base implementation of {@link WorkflowManagerMBean}.
 */
public class WorkflowManager extends ComponentManagerImpl<Workflow> implements WorkflowManagerMBean, WorkflowRuntimeManager {

  private static final TimeInterval MAX_REPLY_WAIT = new TimeInterval(1L, TimeUnit.MINUTES);
  private transient Workflow managedWorkflow;
  private transient ChannelManager parent;
  private transient ObjectName myObjectName = null;
  private transient Set<ChildRuntimeInfoComponent> childRuntimeInfoComponents;
  private transient JmxSubmitMessageInterceptor injectInterceptor;

  private WorkflowManager() {
    super();
    childRuntimeInfoComponents = new HashSet<ChildRuntimeInfoComponent>();
  }

  public WorkflowManager(Workflow w, ChannelManager owner) throws MalformedObjectNameException, CoreException {
    this(w, owner, false);
  }

  WorkflowManager(Workflow w, ChannelManager owner, boolean skipBackRefs) throws MalformedObjectNameException, CoreException {
    this();
    managedWorkflow = w;
    parent = owner;
    initMembers();
    if (!skipBackRefs) {
      parent.addChild(this);
    }
  }

  private void initMembers() throws MalformedObjectNameException, CoreException {
    if (isEmpty(managedWorkflow.getUniqueId())) {
      throw new CoreException("No UniqueID, this workflow cannot be managed");
    }
    // Builds up a name com.adaptris:type=Workflow, adapter=<adapter-id,>, id=<channel-id>, workflow=<workflow-id>
    myObjectName = ObjectName.getInstance(JMX_WORKFLOW_TYPE + ADAPTER_PREFIX + getParent().getParent().getUniqueId()
        + CHANNEL_PREFIX + getParent().getUniqueId() + ID_PREFIX + getWrappedComponent().getUniqueId());
    configureDefaultInterceptors();
    List<WorkflowInterceptor> interceptors = managedWorkflow.getInterceptors();
    for (WorkflowInterceptor interceptor : interceptors) {
      ChildRuntimeInfoComponent comp = (ChildRuntimeInfoComponent) RuntimeInfoComponentFactory.create(this, interceptor);
      if (comp != null) {
        addChildJmxComponent(comp);
      }
    }
    marshalConfig();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

  @Override
  public Workflow getWrappedComponent() {
    return managedWorkflow;
  }

  @Override
  public long requestStartTime() {
    Date startTime = getWrappedComponent().lastStartTime();
    if (startTime != null) {
      return startTime.getTime();
    }
    return 0;
  }

  @Override
  public long requestStopTime() {
    // Should never be null, unlike lastStartTime()
    return getWrappedComponent().lastStopTime().getTime();
  }

  @Override
  public SerializableAdaptrisMessage injectMessageWithReply(SerializableAdaptrisMessage serialisedMessage) throws CoreException {
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    AdaptrisMessage adaptrisMessage = translator.translate(serialisedMessage);
    if(injectInterceptor == null) // only create the interceptor if we need it.
      this.testAndInjectCachingInterceptor();
    
    managedWorkflow.onAdaptrisMessage(adaptrisMessage);
    return translator.translate(this.waitForInjectReply(adaptrisMessage));
  }


  @Override
  public boolean injectMessage(SerializableAdaptrisMessage serialisedMessage) throws CoreException {
    DefaultSerializableMessageTranslator translator = new DefaultSerializableMessageTranslator();
    AdaptrisMessage adaptrisMessage = translator.translate(serialisedMessage);
    managedWorkflow.onAdaptrisMessage(adaptrisMessage);
    return true;
  }

  /**
   * Test if we have a configured caching interceptor.  If we do, great, otherwise create one.
   * @throws CoreException
   */
  private void testAndInjectCachingInterceptor() throws CoreException {
    for(WorkflowInterceptor interceptor : managedWorkflow.getInterceptors()) {
      if(interceptor instanceof JmxSubmitMessageInterceptor) {
        this.injectInterceptor = (JmxSubmitMessageInterceptor) interceptor;
      }
    }
    if(this.injectInterceptor == null) {
      injectInterceptor = new JmxSubmitMessageInterceptor();
      managedWorkflow.getInterceptors().add(injectInterceptor);
      injectInterceptor.init();
      injectInterceptor.start();
    }
  }

  private AdaptrisMessage waitForInjectReply(AdaptrisMessage adaptrisMessage) {
    long startTime = System.currentTimeMillis();
    while(!injectInterceptor.getMessageCache().contains(adaptrisMessage.getUniqueId())) {
      try {
        Thread.sleep(50);
        if((System.currentTimeMillis() - startTime) > MAX_REPLY_WAIT.toMilliseconds())
          return null;
      } catch (InterruptedException e) {
        break;
      }
    }
    return injectInterceptor.getMessageCache().remove(adaptrisMessage.getUniqueId()).getMessage();
  }

  /**
   * Equality is based on the underlying ObjectName.
   *
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    try {
      if (o instanceof WorkflowManagerMBean) {
        WorkflowManagerMBean rhs = (WorkflowManagerMBean) o;
        return new EqualsBuilder().append(myObjectName, rhs.createObjectName()).isEquals();
      }
    }
    catch (MalformedObjectNameException e) {

    }
    return false;
  }

  @Override
  public String toString() {
    return myObjectName.toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(13, 19).append(myObjectName).toHashCode();
  }

  @Override
  protected void checkTransitionTo(ComponentState futureState) throws CoreException {
    ParentStateValidator.checkTransitionTo(futureState, parent.getComponentState());
  }

  @Override
  public ChannelManager getParent() {
    return parent;
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public Collection<ObjectName> getChildRuntimeInfoComponents() throws MalformedObjectNameException {
    Set<ObjectName> result = new TreeSet<ObjectName>();
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      result.add(cmb.createObjectName());
    }
    return result;
  }

  @Override
  public boolean addChildJmxComponent(ChildRuntimeInfoComponent comp) {
    if (comp == null) {
      throw new IllegalArgumentException("Null ChildRuntimeInfoComponent");
    }
    return childRuntimeInfoComponents.add(comp);
  }

  @Override
  public boolean removeChildJmxComponent(ChildRuntimeInfoComponent comp) {
    if (comp == null) {
      throw new IllegalArgumentException("Null ChildRuntimeInfoComponent");
    }
    return childRuntimeInfoComponents.remove(comp);
  }

  @Override
  public String createObjectHierarchyString() {
    return getParent().createObjectHierarchyString() + WORKFLOW_PREFIX + getWrappedComponent().getUniqueId();
  }

  @Override
  public Collection<BaseComponentMBean> getAllDescendants() {
    Set<BaseComponentMBean> result = new HashSet<BaseComponentMBean>();
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      result.add(cmb);
    }
    return result;
  }

  @Override
  public void registerMBean() throws CoreException {
    registerSelf();
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      cmb.registerMBean();
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    unregisterSelf();
    for (ChildRuntimeInfoComponent cmb : childRuntimeInfoComponents) {
      cmb.unregisterMBean();
    }
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    MBeanNotificationInfo adapterLifecycle = new MBeanNotificationInfo(new String[]
    {
        NOTIF_TYPE_WORKFLOW_LIFECYCLE, NOTIF_TYPE_WORKFLOW_CONFIG
    }, Notification.class.getName(), "Workflow Notifications");

    return new MBeanNotificationInfo[]
    {
      adapterLifecycle
    };
  }

  @Override
  protected String getNotificationType(ComponentNotificationType type) {
    String result = null;
    switch (type) {
    case LIFECYCLE: {
      result = NOTIF_TYPE_WORKFLOW_LIFECYCLE;
      break;
    }
    case CONFIG: {
      result = NOTIF_TYPE_WORKFLOW_CONFIG;
      break;
    }
    default:
    }
    return result;
  }

  private void configureDefaultInterceptors() throws CoreException, MalformedObjectNameException {
    configureMessageCounter();
    configureJettyInterceptor();
  }

  private void configureMessageCounter() throws CoreException, MalformedObjectNameException {
    if (managedWorkflow.disableMessageCount()) return;
    if (!hasInterceptorOfType(managedWorkflow.getInterceptors(), MessageMetricsInterceptor.class)) {
      log.trace("Message count interceptor added for [{}], tracks metrics for ~1hr", createObjectName());
      managedWorkflow.getInterceptors().add(
          new MessageMetricsInterceptor(managedWorkflow.getUniqueId(), new TimeInterval(5L, TimeUnit.MINUTES), 12));
    }
  }

  private void configureJettyInterceptor() throws CoreException, MalformedObjectNameException {
    // If we are a PoolingWorkflow, and the consumer is a sub-class of BasicJettyConsumer
    // Then we need to make sure that there is a JettyPoolingWorkflowInterceptor to make sure
    // that object monitors are in place for re-entrant servlet action.
    if (PoolingWorkflow.class.isAssignableFrom(managedWorkflow.getClass())
        && BasicJettyConsumer.class.isAssignableFrom(managedWorkflow.getConsumer().getClass())) {
      if (!hasInterceptorOfType(managedWorkflow.getInterceptors(), JettyPoolingWorkflowInterceptor.class)) {
        log.trace("JettyPoolingWorkflowInterceptor added for [{}]", createObjectName());
        managedWorkflow.getInterceptors().add(new JettyPoolingWorkflowInterceptor());
      }
    }
    return;
  }
  
  private static boolean hasInterceptorOfType(List<WorkflowInterceptor> interceptors, Class<?> clz) {
    boolean found = false;
    for (WorkflowInterceptor wi : interceptors) {
      if (clz.isAssignableFrom(wi.getClass())) {
        found = true;
        break;
      }
    }
    return found;
  }

}
