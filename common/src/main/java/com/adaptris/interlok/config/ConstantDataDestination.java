package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link DataDestination} is used when you want to configure data directly in the Interlok configuration.
 * </p>
 * <p>
 * An example might be configuring the XPath expression directly in Interlok configuration used for the {@link XPathService}.
 * </p>
 * 
 * @author amcgrath
 * @config constant-data-destination
 * @license BASIC
 */
@XStreamAlias("constant-data-destination")
public class ConstantDataDestination implements DataDestination {

  private String value;
  
  public ConstantDataDestination() {
  }
  
  @Override
  public Object getData(InterlokMessage message) throws InterlokException {
    return this.getValue();
  }

  @Override
  public void setData(InterlokMessage message, Object data) throws InterlokException {
    throw new InterlokException("setData not supported for " + this.getClass().getName());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
