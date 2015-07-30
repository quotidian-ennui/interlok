package com.adaptris.core.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Helper methods used internally to support JDBC operations within the framework.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public final class JdbcUtil {

  private JdbcUtil() {

  }

  public static void closeQuietly(Statement p) {
    try {
      if (p != null) {
        p.close();
      }
    }
    catch (SQLException e) {
    }
  }

  public static void closeQuietly(ResultSet p) {
    try {
      if (p != null) {
        p.close();
      }
    }
    catch (SQLException e) {
    }
  }

  public static void closeQuietly(Connection c) {
    try {
      if (c != null) {
        c.close();
      }
    }
    catch (SQLException e) {
      ;
    }
  }

  /**
   * Rollback to the stored savepoint.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param svp the savepoint (if null, no rollback occurs).
   * @param sqlConnection the database connection.
   */
  public static void rollback(Savepoint svp, Connection sqlConnection) {
    if (sqlConnection == null) {
      return;
    }
    try {
      if (sqlConnection.getAutoCommit()) {
        return;
      }
      if (svp != null) {
        sqlConnection.rollback(svp);
      }
    }
    catch (Exception ignoredIntentionally) {
    }
  }

  /**
   * Rollback the connection.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param sqlConnection the database connection.
   */
  public static void rollback(Connection sqlConnection) {
    if (sqlConnection == null) {
      return;
    }
    try {
      if (sqlConnection.getAutoCommit()) {
        return;
      }
      else {
        sqlConnection.rollback();
      }
    }
    catch (Exception ignoredIntentionally) {
    }
  }

  /**
   * Commit any pending transactions on the Connection.
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation does nothing.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @throws SQLException if the commit fails.
   */
  public static void commit(Connection sqlConnection) throws SQLException {
    if (sqlConnection == null) {
      return;
    }
    if (!sqlConnection.getAutoCommit()) {
      sqlConnection.commit();
    }
  }

  /**
   * Create a Savepoint on the connection
   * <p>
   * If {@link Connection#getAutoCommit()} is true, then this operation returns null.
   * </p>
   *
   * @param sqlConnection the SQL Connection
   * @return a created Savepoint or null if the connection does not require it.
   * @throws SQLException if the operation fails.
   */
  public static Savepoint createSavepoint(Connection sqlConnection) throws SQLException {
    if (sqlConnection == null) {
      return null;
    }
    if (!sqlConnection.getAutoCommit()) {
      return sqlConnection.setSavepoint();
    }
    return null;
  }

}
