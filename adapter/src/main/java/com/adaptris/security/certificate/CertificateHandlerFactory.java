/*
 * $Author: lchan $
 * $RCSfile: CertificateHandlerFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/09/26 15:39:04 $
 */
package com.adaptris.security.certificate;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Factory to build Certificate Handlers.
 * <p>
 * The only type of certificate handler supported is X509.
 * </p>
 */
public class CertificateHandlerFactory {

  private static CertificateHandlerFactory f = new CertificateHandlerFactory();

  protected CertificateHandlerFactory() {
  }

  /**
   * Get a factory for handling X509 certificates.
   * 
   * @return the instance.
   */
  public static CertificateHandlerFactory getInstance() {
    return f;
  }

  /**
   * Create a CertificateHandler instance from the specified bytes.
   * <p>
   * The byte array is expected to contain the certificate in either DER format
   * or PEM format. The contents of the byte array is expected to only contain a
   * single certificate.
   * 
   * @param bytes the bytes representing the certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public CertificateHandler generateHandler(byte[] bytes)
      throws CertificateException, IOException {
    return (new X509Handler(bytes));
  }

  /**
   * Create a CertificateHandler instance from a pre-existing Certificate.
   * 
   * @param c the Certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public CertificateHandler generateHandler(Certificate c)
      throws CertificateException, IOException {
    return (new X509Handler(c));
  }

  /**
   * Create a CertificateHandler instance from the supplied inputstream.
   * <p>
   * The inputstream is expected to contain the certificate in either DER format
   * or PEM format. The contents of the inputstream is expected to only contain
   * a single certificate.
   * 
   * @param i the inputstream containing the certificate
   * @throws CertificateException if the certificate could not be parsed
   * @throws IOException if there was an IO error
   * @return a certificate Handler
   * @see CertificateHandler
   */
  public final CertificateHandler generateHandler(InputStream i)
      throws CertificateException, IOException {
    return (new X509Handler(i));
  }
}