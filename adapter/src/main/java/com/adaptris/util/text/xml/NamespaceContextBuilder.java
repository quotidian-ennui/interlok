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

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;

public abstract class NamespaceContextBuilder {

  public NamespaceContextBuilder() {

  }

  public abstract NamespaceContext build(AdaptrisMessage msg, DocumentBuilder f) throws Exception;

  public DocumentBuilder newDocumentBuilder(DocumentBuilderFactoryBuilder f) throws ParserConfigurationException {
    DocumentBuilderFactoryBuilder factory = f == null ? DocumentBuilderFactoryBuilder.newInstance() : f;
    DocumentBuilderFactory dbf= factory.configure(DocumentBuilderFactory.newInstance());
    dbf.setNamespaceAware(true);
    return dbf.newDocumentBuilder();
  }
}
