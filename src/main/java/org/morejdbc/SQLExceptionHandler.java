package org.morejdbc;

import org.springframework.dao.DataAccessException;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLExceptionHandler<T> {

    T handle(SQLException e) throws SQLException, DataAccessException;
}
