package com.adaptris.interlok.types;

import java.io.Serializable;
import java.util.Map;

/**
 * Basic message implementation that can be serialized.
 * 
 * 
 */
public interface SerializableMessage extends Serializable {

  public String getUniqueId();

  public void setUniqueId(String uniqueId);

  public String getPayload();

  public void setPayload(String payload);

  /**
   * Returns a view of all the existing headers associated with the message.
   * <p>
   * Any changes to the returned {@link Map} are not guaranteed to be reflected in underlying map.
   * You should treat the returned Map as a read only view of the current message headers. Use
   * {@link #addMessageHeader(String, String)} or {@link #removeMessageHeader(String)} to manipulate
   * individual headers.
   * </p>
   * 
   * @return a read only view of the messages.
   */
  public Map<String, String> getMessageHeaders();

  /**
   * Overwrite all the headers.
   * <p>
   * Clear and overwrite all the headers
   * </p>
   * 
   * @param metadata
   */
  public void setMessageHeaders(Map<String, String> metadata);

  public void addMessageHeader(String key, String value);

  public void removeMessageHeader(String key);

  public String getPayloadEncoding();

  public void setPayloadEncoding(String payloadEncoding);

}
