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

package com.adaptris.core.common;

import static com.adaptris.core.common.MetadataDataOutputParameter.DEFAULT_METADATA_KEY;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.io.output.WriterOutputStream;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation wraps a metadata value as an {@link OutputStream}.
 * 
 * @config metadata-output-stream-wrapper
 * @since 3.9.0
 */
@XStreamAlias("metadata-output-stream-wrapper")
@DisplayOrder(order = {"metadataKey", "contentEncoding"})
@ComponentProfile(summary = "MessageWrapper implementation wraps a metadata value as an Outputstream", since = "3.9.0")
public class MetadataOutputStreamWrapper extends MetadataStreamOutputParameter
    implements MessageWrapper<OutputStream> {
    
  public MetadataOutputStreamWrapper() {
    super();
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  @Override
  public OutputStream wrap(InterlokMessage m) throws Exception {
    return new MetadataOutputStream(m, new StringWriter());
  }

  private class MetadataOutputStream extends FilterOutputStream {

    private InterlokMessage msg;
    private StringWriter writer;

    public MetadataOutputStream(InterlokMessage msg, StringWriter writer) {
      super(new WriterOutputStream(writer, charset(getContentEncoding())));
      this.msg = msg;
      this.writer = writer;
    }

    @Override
    public void close() throws IOException {
      super.close();
      msg.addMessageHeader(getMetadataKey(), writer.toString());
    }
  }
}
