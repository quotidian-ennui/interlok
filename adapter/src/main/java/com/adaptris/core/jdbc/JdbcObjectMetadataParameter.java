package com.adaptris.core.jdbc;

import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.jdbc.ParameterValueType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Stored Procedure parameter implementation, can be used for all IN, INOUT and OUT Stored Procedure parameters.
 * <p>
 * If this implementation is used for an IN or an INOUT parameter, then the metadata will be pulled from the {@link AdaptrisMessage}
 * and used as the parameter value. If this implementation is used for an OUT or an INOUT parameter, then the value of the matching
 * parameter after the Stored Procedure has run, will be reapplied into the {@link AdaptrisMessage} as metadata. You will simply set
 * the metadataKey, to both retrieve a value or to set a new value as above.
 * </p>
 * <p>
 * Additionally you will set one or both of "name" and/or "order". "name" will map this parameter to a Stored Procedure parameter
 * using the Stored Procedures method signature. "order" will map this parameter according to the parameter number using the Stored
 * Procedures method signature. Note that the "order" starts from 1 and not 0, so the first parameter would be order 1. You will
 * also need to set the data type of the parameter; you may use any of the string types defined in {@link ParameterValueType}
 * </p>
 * 
 * @config jdbc-object-metadata-parameter
 * @author Aaron McGrath
 * 
 */
@XStreamAlias("jdbc-object-metadata-parameter")
public class JdbcObjectMetadataParameter extends JdbcMetadataParameter {

  @Override
  public Object applyInputParam(AdaptrisMessage msg) throws JdbcParameterException {
    super.checkMetadataKey();
    
    @SuppressWarnings("unchecked")
    Map<String, Object> objectMetadata =  (Map<String, Object>) msg.getObjectMetadata();
    
    if(!objectMetadata.containsKey(getMetadataKey()))
      throw new JdbcParameterException("Object metadata does not exist for key: " + this.getMetadataKey());

    return normalize(objectMetadata.get(this.getMetadataKey()));
  }

  @Override
  public void applyOutputParam(Object dbValue, AdaptrisMessage msg) throws JdbcParameterException {
    super.checkMetadataKey();
    
    msg.addObjectMetadata(this.getMetadataKey(), normalize(dbValue));
  }

}
