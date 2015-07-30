package com.adaptris.core.services.metadata.compare;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.services.metadata.MetadataServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class MetadataComparisonServiceTest extends MetadataServiceExample {

  private static final String EXAMPLE_RESULT_KEY = "metadata key that will contain true or false post service";
  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";

  private enum ComparatorCreator {

    EndsWith {

      @Override
      EndsWith create() {
        return new EndsWith(EXAMPLE_RESULT_KEY);
      }

    },
    EndsWithIgnoreCase {

      @Override
      EndsWithIgnoreCase create() {
        return new EndsWithIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },

    StartsWith {

      @Override
      StartsWith create() {
        return new StartsWith(EXAMPLE_RESULT_KEY);
      }

    },
    StartsWithIgnoreCase {

      @Override
      StartsWithIgnoreCase create() {
        return new StartsWithIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },

    Contains {

      @Override
      Contains create() {
        return new Contains(EXAMPLE_RESULT_KEY);
      }

    },
    ContainsIgnoreCase {

      @Override
      ContainsIgnoreCase create() {
        return new ContainsIgnoreCase(EXAMPLE_RESULT_KEY);
      }

    },
    
    Equals {

      @Override
      Equals create() {
        return new Equals(EXAMPLE_RESULT_KEY);
      }
      
    },
    
    EqualsIgnoreCase {

      @Override
      EqualsIgnoreCase create() {
        return new EqualsIgnoreCase(EXAMPLE_RESULT_KEY);
      }
    };
    abstract ComparatorImpl create();

  };
  
  public MetadataComparisonServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testInit() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService();
    assertNull(s.getFirstKey());
    assertNull(s.getSecondKey());
    assertNull(s.getComparator());
    try {
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setFirstKey(getName());
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setSecondKey(getName());
      try {
        LifecycleHelper.init(s);
      }
      catch (CoreException expected) {

      }
      s.setComparator(new Equals());
      LifecycleHelper.init(s);
    }
    finally {
      stop(s);
    }
  }

  public void testStartsWith() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new StartsWith());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "a");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(StartsWith.class.getCanonicalName()));
  }

  public void testStartsWithIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new StartsWithIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "A");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(StartsWithIgnoreCase.class.getCanonicalName()));
  }

  public void testEndsWith() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EndsWith());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "c");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EndsWith.class.getCanonicalName()));
  }

  public void testEndsWithIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EndsWithIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "C");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EndsWithIgnoreCase.class.getCanonicalName()));
  }

  public void testContains() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new Contains());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "a");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(Contains.class.getCanonicalName()));
  }

  public void testContainsIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new ContainsIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "A");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(ContainsIgnoreCase.class.getCanonicalName()));
  }

  public void testEquals() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new Equals());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "def");
    execute(s, msg);
    assertEquals("false", msg.getMetadataValue(Equals.class.getCanonicalName()));
  }

  public void testEqualsIgnoreCase() throws Exception {
    MetadataComparisonService s = new MetadataComparisonService(KEY_1, KEY_2, new EqualsIgnoreCase());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY_1, "abc");
    msg.addMetadata(KEY_2, "ABC");
    execute(s, msg);
    assertEquals("true", msg.getMetadataValue(EqualsIgnoreCase.class.getCanonicalName()));
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<Service> list = new ArrayList<>();
    for (ComparatorCreator h : ComparatorCreator.values()) {
      list.add(new MetadataComparisonService("first metadata key", "second metadata key", h.create()));
    }
    return list;
  }

  protected String createBaseFileName(Object object) {
    MetadataComparisonService s = (MetadataComparisonService) object;
    return super.createBaseFileName(object) + "-" + s.getComparator().getClass().getSimpleName();
  }
}