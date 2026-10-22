package com.yoy.monitorproject.monitor;

public class MonitorNotFoundException extends RuntimeException {

    public MonitorNotFoundException(Long id) {
        super("Monitor with id " + id + " does not exist");
    }
}
