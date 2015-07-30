/*
 * $RCSfile: StandardFileNameCreatorTest.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core;


public class EmptyFilenameCreatorTest extends BaseCase {

  public EmptyFilenameCreatorTest(java.lang.String testName) {
    super(testName);
  }

  public void testCreateName() {
    EmptyFileNameCreator creator = new EmptyFileNameCreator();
    assertEquals("", creator.createName(new DefaultMessageFactory().newMessage("")));
    assertEquals("", creator.createName(null));
  }

  public void testXmlRoundTrip() throws Exception {
    EmptyFileNameCreator input = new EmptyFileNameCreator();
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    EmptyFileNameCreator output = (EmptyFileNameCreator) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }
}
