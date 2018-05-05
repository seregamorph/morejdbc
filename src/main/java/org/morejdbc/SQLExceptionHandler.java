package org.morejdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLExceptionHandler<T> {

    T handle(SQLException e) throws SQLException;
}
