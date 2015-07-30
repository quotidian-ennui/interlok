package com.adaptris.core.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.LenientKnownHosts;
import com.adaptris.sftp.SftpConnectionBehaviour;
import com.adaptris.sftp.StrictKnownHosts;

public class SftpKeyAuthProducerTest extends FtpProducerCase {

  private static final String BASE_DIR_KEY = "SftpProducerExamples.baseDir";

  public SftpKeyAuthProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected SftpKeyAuthConnection createConnectionForExamples() {
    SftpKeyAuthConnection con = new SftpKeyAuthConnection();
    con.setDefaultUserName("username");
    con.setPrivateKeyFilename("/path/to/private/key/in/openssh/format");
    con.setPrivateKeyPassword("my_super_secret_password");
    con.setSocketTimeout(10000);
    con.setSftpConnectionBehaviour(new LenientKnownHosts());
    return con;
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

  private StandaloneProducer createProducerExample(SftpConnectionBehaviour behaviour) {
    SftpKeyAuthConnection con = createConnectionForExamples();
    FtpProducer producer = createProducerExample();
    try {
      con.setPrivateKeyPassword(Password.encode("my_super_secret_password", Password.PORTABLE_PASSWORD));
      con.setSftpConnectionBehaviour(behaviour);
      producer.setFilenameCreator(new FormattedFilenameCreator());
      producer.setDestination(new ConfiguredProduceDestination("sftp://sftpuser@hostname:port/path/to/directory"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneProducer(con, producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpKeyAuthConnection con = (SftpKeyAuthConnection) ((StandaloneProducer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + con.getSftpConnectionBehaviour().getClass().getSimpleName();
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneProducer[]
    {
        createProducerExample(new LenientKnownHosts("/path/to/known/hosts", false)),
        createProducerExample(new StrictKnownHosts("/path/to/known/hosts", false)),
        createProducerExample(new DefaultSftpBehaviour())
    }));
  }
}
