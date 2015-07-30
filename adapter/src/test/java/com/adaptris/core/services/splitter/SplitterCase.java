/*
 * $RCSfile: SplitterCase.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/05/01 08:37:21 $
 * $Author: lchan $
 */
package com.adaptris.core.services.splitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.stubs.StubMessageFactory;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class SplitterCase extends SplitterServiceExample {

  public static final String XML_MESSAGE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.getProperty("line.separator")
      + "<envelope>" + System.getProperty("line.separator")
      + "<document>one</document>" + System.getProperty("line.separator")
      + "<document>two</document>" + System.getProperty("line.separator") + "<document>three</document>"
      + System.getProperty("line.separator") + "</envelope>";
  public static final String LINE = "The quick brown fox jumps over the lazy dog";

  public SplitterCase(String name) {
    super(name);
  }

  static BasicMessageSplitterService createBasic(MessageSplitter ms) {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    service.setConnection(new NullConnection());
    service.setProducer(new NullMessageProducer());
    service.setSplitter(ms);
    return service;
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms,
                                                       StandaloneProducer p) {
    return createAdvanced(ms, new Service[]
    {
      p
    });
  }

  static AdvancedMessageSplitterService createAdvanced(MessageSplitter ms,
                                                       Service[] services) {
    AdvancedMessageSplitterService service = new AdvancedMessageSplitterService();
    ServiceList sl = new ServiceList(services);
    service.setSplitter(ms);
    service.setService(sl);
    return service;
  }


  static List<Service> createExamples(MessageSplitter ms) {
    List<Service> services = new ArrayList<Service>();
    services.add(createBasic(ms));

    AdvancedMessageSplitterService ams = new AdvancedMessageSplitterService();
    ServiceList sl = new ServiceList();
    sl.addService(new WaitService());
    sl.addService(new StandaloneProducer());
    ams.setSplitter(ms);
    ams.setService(sl);

    services.add(ams);

    return services;
  }

  public static AdaptrisMessage createLineCountMessageInput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter print = new PrintWriter(out);
    for (int i = 0; i < 50; i++) {
      print.println(LINE);
      print.println("");
    }
    print.flush();
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(
        out.toByteArray());
  }
  
  public static AdaptrisMessage createLineCountMessageInputWithHeader(String[] header) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter print = new PrintWriter(out);
    for(String h: header) {
      print.println(h);
    }
    for (int i = 0; i < 50; i++) {
      print.println(LINE);
      print.println("");
    }
    print.flush();
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(
        out.toByteArray());
  }

  public void testSetMessageFactory() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new StubMessageFactory());
    assertEquals(StubMessageFactory.class, splitter.getMessageFactory().getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(new DefaultMessageFactory());
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());

    splitter.setMessageFactory(null);
    assertEquals(DefaultMessageFactory.class, splitter.selectFactory(new DefaultMessageFactory().newMessage()).getClass());
    assertEquals(StubMessageFactory.class, splitter.selectFactory(new StubMessageFactory().newMessage()).getClass());
  }

  public void testSetCopyMetadata() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getCopyMetadata());
    assertTrue(splitter.copyMetadata());
    splitter.setCopyMetadata(Boolean.FALSE);
    assertNotNull(splitter.getCopyMetadata());
    assertEquals(Boolean.FALSE, splitter.getCopyMetadata());
    assertFalse(splitter.copyMetadata());
    splitter.setCopyMetadata(null);
    assertNull(splitter.getCopyMetadata());
    assertTrue(splitter.copyMetadata());
  }

  public void testSetCopyObjectMetadata() throws Exception {
    MessageSplitterImp splitter = createSplitterForTests();
    assertNull(splitter.getCopyObjectMetadata());
    assertFalse(splitter.copyObjectMetadata());
    splitter.setCopyObjectMetadata(Boolean.TRUE);
    assertNotNull(splitter.getCopyObjectMetadata());
    assertEquals(Boolean.TRUE, splitter.getCopyObjectMetadata());
    assertTrue(splitter.copyObjectMetadata());
    splitter.setCopyObjectMetadata(null);
    assertNull(splitter.getCopyObjectMetadata());
    assertFalse(splitter.copyObjectMetadata());
  }

  protected abstract MessageSplitterImp createSplitterForTests();
  
  /**
   * Convert the Iterable into a List. If it's already a list, just return it. If not, 
   * it will be iterated and the resulting list returned.
   */
  protected List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if(iter instanceof List) {
      return (List<AdaptrisMessage>)iter;
    }
    
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    
    try(CloseableIterable<AdaptrisMessage> messages = CloseableIterable.FACTORY.ensureCloseable(iter)) {
      for(AdaptrisMessage msg: messages) {
        result.add(msg);
      }
    } catch (IOException e) {
      log.warn("Could not close Iterable!", e);
    }
    
    return result;
  }

}
