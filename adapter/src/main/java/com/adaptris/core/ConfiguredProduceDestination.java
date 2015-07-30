/*
 * $RCSfile: ConfiguredProduceDestination.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>ProduceDestination</code> that has a configured <code>String</code> destination.
 * </p>
 * 
 * @config configured-produce-destination
 */
@XStreamAlias("configured-produce-destination")
public final class ConfiguredProduceDestination implements ProduceDestination {

  private String destination;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ConfiguredProduceDestination() {
    // default...
    this.setDestination(""); // null protection
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param s the destination name to use
   */
  public ConfiguredProduceDestination(String s) {
    this.setDestination(s);
  }

  /**
   * <p>
   * Semantic equality is based on the equality of the underlying
   * <code>String</code> destination names.
   * </p>
   *
   * @param obj the <code>Object</code> to test for equality
   * @return true if <code>obj</code> is semantically equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfiguredProduceDestination) {
      return this.getDestination().equals(((ConfiguredProduceDestination) obj).getDestination());
    }
    return false;
  }

  /**
   * <p>
   * The hash code of instances of this class is the hash code of the underlying
   * <code>String</code> destination name.
   * </p>
   *
   * @return this instance's hash code
   */
  @Override
  public int hashCode() {
    return destination.hashCode();
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return "destination [" + destination + "]";
  }

  /**
   * @see com.adaptris.core.ProduceDestination
   *      #getDestination(com.adaptris.core.AdaptrisMessage)
   */
  public String getDestination(AdaptrisMessage msg) {
    return destination;
  }

  /**
   * <p>
   * Returns the name of the destination.
   * </p>
   *
   * @return the name of the destination
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the name of the destination.
   * </p>
   *
   * @param s the name of the destination
   */
  public void setDestination(String s) {
    if (s == null) {
      throw new IllegalArgumentException("param may not be null");
    }
    destination = s;
  }
}
