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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config dynamic-namespace-context-builder
 * @author lchan
 *
 */
@XStreamAlias("dynamic-namespace-context-builder")
public class DynamicNamespaceContextBuilder extends NamespaceContextBuilder {

  @Override
  public NamespaceContext build(AdaptrisMessage msg, DocumentBuilder builder) throws Exception {
    return new MyNamespaceContext(createDocument(msg, builder));
  }

  private static Document createDocument(AdaptrisMessage msg, DocumentBuilder builder)
      throws ParserConfigurationException, IOException, SAXException {
    Document result = null;
    try (InputStream in = msg.getInputStream()) {
      result = builder.parse(new InputSource(in));
    }
    return result;
  }

  private class MyNamespaceContext implements NamespaceContext {
    private static final String DEFAULT_NS = "DEFAULT";
    private Map<String, String> prefixMap = new HashMap<>();
    private Map<String, Set<String>> nsMap = new HashMap<>();

    public MyNamespaceContext(Document document) {
      examineNode(document.getFirstChild());
    }

    private void examineNode(Node node) {
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
        Node attribute = attributes.item(i);
        storeAttribute((Attr) attribute);
      }
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE) examineNode(child);
      }
    }

    private void storeAttribute(Attr attribute) {
      if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
        if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
          store(DEFAULT_NS, attribute.getNodeValue());
        }
        else {
          store(attribute.getLocalName(), attribute.getNodeValue());
        }
      }
    }

    private void store(String prefix, String uri) {
      prefixMap.put(prefix, uri);
      Set<String> prefixes = nsMap.get(uri);
      if (prefixes == null) {
        prefixes = new HashSet<>();
      }
      prefixes.add(prefix);
      nsMap.put(uri, prefixes);
    }

    @Override
    public String getNamespaceURI(String prefix) {
      if (isNull(prefix)) {
        return XMLConstants.NULL_NS_URI;
      }
      String nsURI = prefixMap.get(prefix);
      return isNull(nsURI) ? XMLConstants.NULL_NS_URI : nsURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator();
    }

    private boolean isNull(Object value) {
      return null == value;
    }
  }

}
