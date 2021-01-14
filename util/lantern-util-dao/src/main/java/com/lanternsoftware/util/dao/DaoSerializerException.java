package com.lanternsoftware.util.dao;

public class DaoSerializerException extends RuntimeException {
    public DaoSerializerException() {
    }
    public DaoSerializerException(String message) {
        super(message);
    }
    public DaoSerializerException(String message, Throwable cause) {
        super(message, cause);
    }
    public DaoSerializerException(Throwable cause) {
        super(cause);
    }
}
