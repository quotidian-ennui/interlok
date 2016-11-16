/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.util.text.xml;

import javax.validation.Valid;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.KeyValuePairSet;

public class StaticNamespaceContextBuilder implements NamespaceContextBuilder {
  @Valid
  private KeyValuePairSet namespaceContext;

  public StaticNamespaceContextBuilder() {

  }

  public StaticNamespaceContextBuilder(KeyValuePairSet s) {
    this();
    setNamespaceContext(s);
  }

  /**
   * @return the namespaceContext
   */
  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param kvps the namespace context
   * @see SimpleNamespaceContext#create(KeyValuePairSet)
   */
  public void setNamespaceContext(KeyValuePairSet kvps) {
    this.namespaceContext = kvps;
  }

  @Override
  public NamespaceContext create(AdaptrisMessage msg, DocumentBuilderFactory f) {
    return SimpleNamespaceContext.create(getNamespaceContext(), msg);
  }
}
