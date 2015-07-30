/*
 * $Author: lchan $
 * $RCSfile: TestSftp.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/07/03 08:53:10 $
 */
package com.adaptris.sftp.test;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import org.apache.oro.io.GlobFilenameFilter;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FtpCase;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.SftpClient;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class TestSftp extends FtpCase {

  private static final String SFTP_GET_FILTER = "sftp.get.filter";
  private static final String SFTP_PUT_REMOTEDIR = "sftp.put.remotedir";
  private static final String SFTP_PUT_FILENAME = "sftp.put.filename";
  private static final String SFTP_GET_FILENAME = "sftp.get.filename";
  private static final String SFTP_GET_REMOTEDIR = "sftp.get.remotedir";
  private static final String SFTP_HOST = "sftp.host";
  private static final String SFTP_PASSWORD = "sftp.password";
  private static final String SFTP_USERNAME = "sftp.username";

  public TestSftp(String testName) {
    super(testName);
  }

  public void testListBadDirectory() throws Exception {
    if (areTestsEnabled()) {
      String oldName = Thread.currentThread().getName();
      try {
        Thread.currentThread().setName("testListBadDirectory");
        FileTransferClient client = connectClientImpl();
        try {
          Random r = new Random();
          String dir = config.getProperty(SFTP_GET_REMOTEDIR) + "/"
              + r.nextInt();
          client.dir(dir);
          fail("LS of  " + dir + " should not work");
        }
        catch (Exception e) {
          client.disconnect();
        }
      }
      finally {
        Thread.currentThread().setName(oldName);
      }
    }
  }

  @Override
  protected String getRemoteGetDirectory() throws IOException {
    return config.getProperty(SFTP_GET_REMOTEDIR);
  }

  @Override
  protected String getRemotePutDirectory() throws IOException {
    return config.getProperty(SFTP_PUT_REMOTEDIR);
  }

  @Override
  protected String getRemoteGetFilename() throws IOException {
    return config.getProperty(SFTP_GET_FILENAME);
  }

  @Override
  protected String getRemotePutFilename() throws IOException {
    return config.getProperty(SFTP_PUT_FILENAME);
  }

  @Override
  protected FilenameFilter getRemoteGetFilenameFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(SFTP_GET_FILTER));
  }

  @Override
  protected String getRemoteGetFilterString() {
    return config.getProperty(SFTP_GET_FILTER);
  }

  @Override
  protected FileFilter getRemoteGetFileFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(SFTP_GET_FILTER));
  }

  @Override
  protected FileTransferClient connectClientImpl() throws Exception {
    SftpClient client = new SftpClient(config.getProperty(SFTP_HOST));
    client.setAdditionalDebug(true);
    client.connect(config.getProperty(SFTP_USERNAME), Password.decode(config.getProperty(SFTP_PASSWORD)));
    return client;
  }

}
