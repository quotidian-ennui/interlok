/*
 * $RCSfile: SizeBasedBranchingServiceTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/05/01 16:28:48 $
 * $Author: lchan $
 */
package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.IdGenerator;

public class SizeBasedBranchingServiceTest extends BranchingServiceExample {

  private static final String SMALL_MESSAGE = "SmallMessage";
  private static final String LARGE_MESSAGE = "LargeMessage";
  private static final int DEFAULT_SIZE_CRITERIA = 30;

  public SizeBasedBranchingServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private SizeBasedBranchingService setupForTests() {
    SizeBasedBranchingService s = new SizeBasedBranchingService();
    s.setSizeCriteriaBytes(DEFAULT_SIZE_CRITERIA);
    s.setGreaterThanServiceId(LARGE_MESSAGE);
    s.setSmallerThanServiceId(SMALL_MESSAGE);
    s.setUniqueId("testSize");
    return s;
  }

  public void testInitWithNoServiceIds() throws Exception {
    SizeBasedBranchingService s = new SizeBasedBranchingService();
    try {
      LifecycleHelper.init(s);
      fail("Initialised with no service ids");
    }
    catch (CoreException e) {
      // expected
    }

    try {
      s.setGreaterThanServiceId("ABC");
      LifecycleHelper.init(s);
      fail("Initialised with null Smaller Than");
    }
    catch (CoreException e) {
      // expected
    }

    try {
      s.setGreaterThanServiceId(null);
      s.setSmallerThanServiceId("ABC");
      LifecycleHelper.init(s);
      fail("Initialised with null GreaterThan");
    }
    catch (CoreException e) {
      // expected
    }
  }

  public void testGreaterThanSizeCriteria() throws Exception {
    AdaptrisMessage msg = new SizeMessageFactory(DEFAULT_SIZE_CRITERIA + 1).newMessage();
    System.err.println("testGreaterThanSizeCriteria SizeOfMessage " + msg.getSize());
    SizeBasedBranchingService s = setupForTests();
    execute(s, msg);
    assertEquals(LARGE_MESSAGE, msg.getNextServiceId());
  }

  public void testSmallerThanSizeCriteria() throws Exception {
    AdaptrisMessage msg = new SizeMessageFactory(DEFAULT_SIZE_CRITERIA - 1).newMessage();
    System.err.println("testSmallerThanSizeCriteria SizeOfMessage " + msg.getSize());
    SizeBasedBranchingService s = setupForTests();
    execute(s, msg);
    assertEquals(SMALL_MESSAGE, msg.getNextServiceId());
  }

  public void testEqualToSizeCriteria() throws Exception {
    AdaptrisMessage msg = new SizeMessageFactory(DEFAULT_SIZE_CRITERIA).newMessage();
    System.err.println("testEqualToSizeCriteria SizeOfMessage " + msg.getSize());
    SizeBasedBranchingService s = setupForTests();
    execute(s, msg);
    assertEquals(SMALL_MESSAGE, msg.getNextServiceId());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SizeBasedBranchingService s = setupForTests();
    BranchingServiceCollection sl = new BranchingServiceCollection();
    sl.addService(s);
    sl.setFirstServiceId(s.getUniqueId());
    sl.addService(new LogMessageService(SMALL_MESSAGE));
    sl.addService(new LogMessageService(LARGE_MESSAGE));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return SizeBasedBranchingService.class.getName();
  }

  private class SizeMessageFactory extends DefaultMessageFactory {

    private long msgSize = -1;

    public SizeMessageFactory(long size) {
      super();
      msgSize = size >= 0 ? size : msgSize;
    }

    @Override
    public AdaptrisMessage newMessage() {
      AdaptrisMessage m = null;
      if (msgSize < 0) {
        m = super.newMessage();
      }
      else {
        m = new SizeMessage(uniqueIdGenerator, this, msgSize);
        if (getDefaultCharEncoding() != null && !"".equals(getDefaultCharEncoding())) {
          m.setCharEncoding(getDefaultCharEncoding());
        }
      }
      return m;
    }

  }

  private class SizeMessage extends DefaultAdaptrisMessageImp {

    private long size = -1;

    public SizeMessage(IdGenerator guid, AdaptrisMessageFactory fac, long size) throws RuntimeException {
      super(guid, fac);
      this.size = size;
    }

    @Override
    public long getSize() {
      return size != -1 ? size : super.getSize();
    }

  }
}
