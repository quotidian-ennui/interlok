package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PlainIdGeneratorTest {

  @Test
  public void testCreateId() throws Exception {
    IdGenerator guid = new PlainIdGenerator();
    assertNotNull(guid.create(guid));
  }

  @Test
  public void testCreateIdWithNull() throws Exception {
    IdGenerator guid = new PlainIdGenerator();
    assertNotNull(guid.create(null));
  }

  @Test
  public void testSeparator() throws Exception {
    PlainIdGenerator guid = new PlainIdGenerator("-");
    assertEquals("-", guid.getSeparator());
    assertNotNull(guid.create(guid));
    assertTrue(guid.create(guid).contains("-"));
  }
}
