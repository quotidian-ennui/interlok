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

package com.adaptris.core.services.metadata.xpath;

import org.w3c.dom.Document;

import com.adaptris.core.MetadataElement;
import com.adaptris.util.text.xml.XPath;

/**
 * Interface for creating string metadata from an Xpath.
 * 
 * @author lchan
 * 
 */
public interface XpathQuery extends XpathObjectQuery {

  /**
   * <p>
   * Executes an Xpath query.
   * </p>
   *
   * @param doc The XML document
   * @param xpath the {@link XPath} object to use to resolve any expressions.
   * @param expr the xpath expression
   * @return a {@link MetadataElement} with the configured key and the extracted text value
   */
  MetadataElement resolveXpath(Document doc, XPath xpath, String expr) throws Exception;

}
