package com.adaptris.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that flushes out to a logger.
 * <p>
 * Note that no data is written out to the Category until the stream is flushed or closed.
 * </p>
 * <br/>
 * Example:
 * 
 * <pre>
 * {@code 
 * // make sure everything sent to System.err is logged
 * System.setErr(new PrintStream(new LoggingOutputStream(LogHandler.WARN));
 * // make sure everything sent to System.out is also logged
 * System.setOut(new PrintStream(new LoggingOutputStream(LogHandler.INFO));
 * }
 * </pre>
 * 
 */
abstract class LoggingOutputStreamImpl extends OutputStream {

  private boolean hasBeenClosed = false;
  private byte[] buf;

  public enum LogLevel {
    FATAL, ERROR, WARN, INFO, DEBUG, TRACE;
  };

  /**
   * The number of valid bytes in the buffer. This value is always in the range
   * <tt>0</tt> through <tt>buf.length</tt>; elements <tt>buf[0]</tt>
   * through <tt>buf[count-1]</tt> contain valid byte data.
   */
  private int count;
  private int bufLength;

  /**
   * The default number of bytes in the buffer. =2048
   */
  public static final int DEFAULT_BUFFER_LENGTH = 2048;

  protected transient LogLevel logLevel;

  protected LoggingOutputStreamImpl(LogLevel level) throws IllegalArgumentException {
    if (level == null) {
      throw new IllegalArgumentException("LogLevel == null");
    }
    logLevel = level;
  }

  protected abstract void log(LogLevel level, String s);

  /**
   * Closes this output stream and releases any system resources associated with
   * this stream. The general contract of <code>close</code> is that it closes
   * the output stream. A closed stream cannot perform output operations and
   * cannot be reopened.
   */
  @Override
  public void close() {
    flush();
    hasBeenClosed = true;
  }

  /**
   * Writes the specified byte to this output stream. The general contract for
   * <code>write</code> is that one byte is written to the output stream. The
   * byte to be written is the eight low-order bits of the argument
   * <code>b</code>. The 24 high-order bits of <code>b</code> are ignored.
   *
   * @param b the <code>byte</code> to write
   * @throws IOException if an I/O error occurs. In particular, an
   *           <code>IOException</code> may be thrown if the output stream has
   *           been closed.
   */
  @Override
  public void write(final int b) throws IOException {
    if (hasBeenClosed) {
      throw new IOException("The stream has been closed.");
    }
    if (count == bufLength) {
      final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
      final byte[] newBuf = new byte[newBufLength];
      System.arraycopy(buf, 0, newBuf, 0, bufLength);
      buf = newBuf;
      bufLength = newBufLength;
    }
    buf[count] = (byte) b;
    count++;
  }

  /**
   * Flushes this output stream and forces any buffered output bytes to be
   * written out. The general contract of <code>flush</code> is that calling
   * it is an indication that, if any bytes previously written have been
   * buffered by the implementation of the output stream, such bytes should
   * immediately be written to their intended destination.
   */
  @Override
  public void flush() {

    if (count == 0) {
      return;
    }

    // don't print out blank lines; flushing from PrintStream puts
    // out these
    // For linux system
    if (count == 1 && (char) buf[0] == '\n') {
      reset();
      return;
    }
    // For mac system
    if (count == 1 && (char) buf[0] == '\r') {
      reset();
      return;
    }
    // On windows system
    if (count == 2 && (char) buf[0] == '\r' && (char) buf[1] == '\n') {
      reset();
      return;
    }

    final byte[] theBytes = new byte[count];
    System.arraycopy(buf, 0, theBytes, 0, count);
    log(logLevel, new String(theBytes));
    reset();
  }

  protected void reset() {
    bufLength = DEFAULT_BUFFER_LENGTH;
    buf = new byte[DEFAULT_BUFFER_LENGTH];
    count = 0;
  }

}
