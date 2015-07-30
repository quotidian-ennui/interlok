package com.adaptris.core.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.adaptris.core.http.ResourceAuthenticator.ResourceTarget;

/**
 * Responsible for authenticating against network resources when requested.
 * 
 * <p>
 * This calls {@link Authenticator#setDefault(Authenticator)} in a static block; when authentication is required, this will iterate
 * over the list of configured {@link ResourceAuthenticator} instances and return the first non-null {@link PasswordAuthentication}
 * provided.
 * </p>
 * <p>
 * Classes that wish to provide an {@link Authenticator} instance are encouraged to instead implement {@link ResourceAuthenticator}
 * and add to this instance using the {@link #addAuthenticator(ResourceAuthenticator)} and
 * {@link #removeAuthenticator(ResourceAuthenticator)} methods.
 * </p>
 * 
 * @author lchan
 * 
 */
public class AdapterResourceAuthenticator extends Authenticator {

  private static final transient AdapterResourceAuthenticator SINGLETON = new AdapterResourceAuthenticator();

  private transient Set<ResourceAuthenticator> configuredAuthenticators;

  static {
    Authenticator.setDefault(SINGLETON);
  }

  private AdapterResourceAuthenticator() {
    configuredAuthenticators = new LinkedHashSet<>();
  }

  public static final AdapterResourceAuthenticator getInstance() {
    return SINGLETON;
  }

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    PasswordAuthentication pw = null;
    ResourceTarget target = new ResourceTarget();
    target.setRequestorType(getRequestorType());
    target.setRequestingHost(getRequestingHost());
    target.setRequestingPort(getRequestingPort());
    target.setRequestingPrompt(getRequestingPrompt());
    target.setRequestingProtocol(getRequestingProtocol());
    target.setRequestingScheme(getRequestingScheme());
    target.setRequestingSite(getRequestingSite());
    target.setRequestingURL(getRequestingURL());
    for (ResourceAuthenticator a : configuredAuthenticators) {
      pw = a.authenticate(target);
      if (pw != null) {
        break;
      }
    }
    if (pw == null) {
      pw = super.getPasswordAuthentication();
    }
    return pw;
  }

  public boolean addAuthenticator(ResourceAuthenticator a) {
    return configuredAuthenticators.add(a);
  }

  public boolean removeAuthenticator(ResourceAuthenticator a) {
    return configuredAuthenticators.remove(a);
  }

  /**
   * Return a cloned list of the current set of configured authenticators.
   * 
   * @return a shallow clone of the current set of authenticators.
   */
  public Collection<ResourceAuthenticator> currentAuthenticators() {
    return new ArrayList<ResourceAuthenticator>(configuredAuthenticators);
  }
}
