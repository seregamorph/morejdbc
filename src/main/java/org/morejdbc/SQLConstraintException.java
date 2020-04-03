package org.morejdbc;

import java.sql.SQLException;

public class SQLConstraintException extends SQLException {

    public SQLConstraintException(String msg) {
        super(msg);
    }
}
