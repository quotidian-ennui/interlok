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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
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

@XStreamAlias("dynamic-namespace-context-builder")
public class DynamicNamespaceContextBuilder implements NamespaceContextBuilder {

  @Override
  public NamespaceContext create(AdaptrisMessage msg, DocumentBuilderFactory builder) throws Exception {
    return new MyNamespaceContext(createDocument(msg, builder));
  }

  private static Document createDocument(AdaptrisMessage msg, DocumentBuilderFactory builder)
      throws ParserConfigurationException, IOException, SAXException {
    Document result = null;
    try (InputStream in = msg.getInputStream()) {
      result = builder.newDocumentBuilder().parse(new InputSource(in));
    }
    return result;
  }

  private class MyNamespaceContext implements NamespaceContext {
    private static final String DEFAULT_NS = "DEFAULT";
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    public MyNamespaceContext(Document document) {
      examineNode(document.getFirstChild(), false);
    }

    private void examineNode(Node node, boolean attributesOnly) {
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
        Node attribute = attributes.item(i);
        storeAttribute((Attr) attribute);
      }

      if (!attributesOnly) {
        NodeList chields = node.getChildNodes();
        for (int i = 0; i < chields.getLength(); i++) {
          Node chield = chields.item(i);
          if (chield.getNodeType() == Node.ELEMENT_NODE) examineNode(chield, false);
        }
      }
    }

    private void storeAttribute(Attr attribute) {
      if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
        if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
          putInCache(DEFAULT_NS, attribute.getNodeValue());
        }
        else {
          putInCache(attribute.getLocalName(), attribute.getNodeValue());
        }
      }

    }

    private void putInCache(String prefix, String uri) {
      prefix2Uri.put(prefix, uri);
      uri2Prefix.put(uri, prefix);
    }

    public String getNamespaceURI(String prefix) {
      if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
        return prefix2Uri.get(DEFAULT_NS);
      }
      else {
        return prefix2Uri.get(prefix);
      }
    }

    public String getPrefix(String namespaceURI) {
      return uri2Prefix.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
      return Arrays.asList(uri2Prefix.get(namespaceURI)).iterator();
    }
  }

}
