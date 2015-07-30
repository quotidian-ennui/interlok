package com.adaptris.core;

import java.util.Arrays;

import com.adaptris.core.util.LifecycleHelper;

public class DefaultLifecycleStrategyTest extends BaseCase {

  public DefaultLifecycleStrategyTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  protected ChannelLifecycleStrategy createStrategy() {
    return new DefaultChannelLifecycleStrategy();
  }

  protected ChannelList create() {
    ChannelList list = new ChannelList();
    list.setLifecycleStrategy(createStrategy());
    return list;
  }

  public void testChannelInit() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelInit");
    ChannelList list = create();
    try {
      list.add(new Channel());
      list.prepare();
      LifecycleHelper.init(list);
      waitFor(list, InitialisedState.getInstance());
      assertEquals(InitialisedState.getInstance(), list.get(0).retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelStart() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelStart");
    ChannelList list = create();
    try {
      list.add(new Channel());
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), list.get(0).retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelStop() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelStop");
    ChannelList list = create();
    try {
      list.add(new Channel());
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      LifecycleHelper.stop(list);
      waitFor(list, StoppedState.getInstance());
      assertEquals(StoppedState.getInstance(), list.get(0).retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelClose() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelClose");
    ChannelList list = create();
    try {
      list.add(new Channel());
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      stop(list);
      waitFor(list, ClosedState.getInstance());
      assertEquals(ClosedState.getInstance(), list.get(0).retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelAutoStartFalse() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelAutoStartFalse");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setUniqueId("testAutoStart");
      testChannel.setAutoStart(Boolean.FALSE);
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, ClosedState.getInstance());
      assertEquals(ClosedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelAutoStartTrue() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelAutoStartTrue");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setUniqueId("testAutoStart");
      testChannel.setAutoStart(Boolean.TRUE);
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  // No ID means that we always start regardless of auto-start being false.
  public void testChannelAutoStartFalseNoUniqueId() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelAutoStartFalseNoUniqueId");

    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setAutoStart(Boolean.FALSE);
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelAutoStartTrueNoUniqueId() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelAutoStartTrueNoUniqueId");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setAutoStart(Boolean.TRUE);
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  public void testChannelAutoStartNotSpecified() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelAutoStartNotSpecified");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setUniqueId("testAutoStartNotSpecified");
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, StartedState.getInstance());
      assertEquals(StartedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Override
  protected String renameThread(String newName) {
    String name = Thread.currentThread().getName();
    Thread.currentThread().setName(newName);
    return name;
  }

  protected void waitFor(ChannelList cl, ComponentState state) throws Exception {
    long waitTime = 0;
    boolean allCorrect = false;
    while (waitTime < MAX_WAIT && !allCorrect) {
      int matchingState = 0;
      waitTime += DEFAULT_WAIT_INTERVAL;
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      for (Channel c : cl) {
        log.trace("Channel " + c.getUniqueId() + " is " + c.retrieveComponentState());
        if (c.retrieveComponentState().equals(state)) {
          matchingState++;
        }
      }
      if (matchingState == cl.size()) {
        allCorrect = true;
      }
    }
    return;
  }

}
