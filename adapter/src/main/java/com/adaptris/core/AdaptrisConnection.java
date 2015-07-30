package com.adaptris.core;

import java.util.Set;

/**
 * <p>
 * Represents a connection> to an application or of a protocol type. E.g. JMS, database, etc.
 * </p>
 * <p>
 * It is generally the responsibility of implementations of this interface to to deliver messages to
 * registered {@link AdaptrisMessageConsumer} instances.
 * </p>
 * 
 * @since 3.0.3 extends {@link ComponentLifecycleExtension} to satisfy any underlying
 *        pre-initialisation activities.
 */
public interface AdaptrisConnection extends AdaptrisComponent, ComponentLifecycleExtension,
    StateManagedComponent, JndiBindable {

  /**
   * Get the unique-id that is associated with this connection.
   *
   * @return the unique-id
   */
  String getUniqueId();

  /**
   * Return a collection of components that need to be restarted on exception.
   *
   * @return a list of Components that need to be restarted of any exceptions.
   * @see ConnectionErrorHandler
   */
  Set<StateManagedComponent> retrieveExceptionListeners();

  /**
   * Add a component that will be notified upon exception.
   *
   * @param comp the component that will be notified.
   */
  void addExceptionListener(StateManagedComponent comp);

  /**
   * <p>
   * Adds a <code>AdaptrisMessageProducer</code> to this connection's
   * internal store of message producers.
   * </p>
   * @param producer the <code>AdaptrisMessageProducer</code> to add
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void addMessageProducer(AdaptrisMessageProducer producer)
    throws CoreException;

  /**
   * <p>
   * Returns a <code>List</code> of this connection's
   * <code>AdaptrisMessageProducer</code>s.
   * </p>
   * @return a <code>List</code> of this connection's
   * <code>AdaptrisMessageProducer</code>s
   */
  Set<AdaptrisMessageProducer> retrieveMessageProducers();

  /**
   * <p>
   * Adds a <code>AdaptrisMessageConsumer</code> to this connection's
   * internal store of message consumers.
   * </p>
   * @param consumer the <code>AdaptrisMessageConsumer</code> to add
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  void addMessageConsumer(AdaptrisMessageConsumer consumer)
    throws CoreException;

  /**
   * <p>
   * Returns a <code>List</code> of this connection's
   * <code>AdaptrisMessageConsumer</code>s.
   * </p>
   * @return a <code>List</code> of this connection's
   * <code>AdaptrisMessageConsumer</code>s
   */
  Set<AdaptrisMessageConsumer> retrieveMessageConsumers();

  /**
   * <p>
   * Sets the <code>ConnectionErrorHandler</code> to use.
   * </p>
   * @param handler the <code>ConnectionErrorHandler</code> to use
   */
  void setConnectionErrorHandler(ConnectionErrorHandler handler);

  /**
   * <p>
   * Returns the <code>ConnectionErrorHandler</code> to use.
   * </p>
   * @return the <code>ConnectionErrorHandler</code> to use
   */
  ConnectionErrorHandler getConnectionErrorHandler();

  /**
   * Return the connection as represented by this connection
   *
   * @param type the type of connection
   * @return the connection
   */
  <T> T retrieveConnection(Class<T> type);



}
