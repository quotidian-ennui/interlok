package com.adaptris.core.services.system;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.IORunnable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CommandOutputCapture} that saves the output of the system command to a metadata value
 * 
 * @author gdries
 * 
 */
@XStreamAlias("system-command-add-metadata-value")
public class AddMetaDataValue implements CommandOutputCapture {

  @NotNull
  private String metadataKey;
  
  private boolean strip = true;
  
  private String encoding;
  
  @Override
  public OutputStream startCapture(final AdaptrisMessage msg) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    // Construct a new OutputStream that will set the metadata value when closed
    return new RunAfterCloseOutputStream(bos, new IORunnable() {
      @Override
      public void run() throws IOException {
        String result = getResultAsString(bos.toByteArray());
        if(isStrip()) { result = StringUtils.strip(result); }
        msg.addMetadata(metadataKey, result);
      }
      
      private String getResultAsString(byte[] result) throws IOException {
        if(StringUtils.isEmpty(encoding)) {
          return new String(result);
        } else {
          return new String(result, encoding);
        }
      }
    });
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * The metadata key under which the output of the command is to be stored. May not be empty.
   * @param metadataKey
   */
  public void setMetadataKey(String metadataKey) {
    if(StringUtils.isEmpty(metadataKey)) {
      throw new IllegalArgumentException("Metadata key may not be empty");
    }
    
    this.metadataKey = metadataKey;
  }
  
  public String getEncoding() {
    return encoding;
  }

  /**
   * Set the character encoding to interpret the output of the command. By default the JVM encoding will be used.
   * @param encoding
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isStrip() {
    return strip;
  }

  /**
   * Whether to strip whitespace like newlines, spaces and tabs from the command output. Default: true
   * @param strip
   */
  public void setStrip(boolean strip) {
    this.strip = strip;
  }

  private static class RunAfterCloseOutputStream extends FilterOutputStream {
    private final IORunnable runOnClose;
    
    public RunAfterCloseOutputStream(OutputStream out, IORunnable runOnClose) {
      super(out);
      this.runOnClose = runOnClose;
    }
    
    @Override
    public void close() throws IOException {
      super.close();
      runOnClose.run();
    }
  }

}
