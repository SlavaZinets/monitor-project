package com.yoy.monitorproject.monitor;

public class MonitorNameAlreadyExistsException extends RuntimeException {

    public MonitorNameAlreadyExistsException(String name) {
        super("Monitor with name '" + name + "' already exists");
    }
}
