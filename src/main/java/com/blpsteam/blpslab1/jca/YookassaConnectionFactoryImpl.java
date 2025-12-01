package com.blpsteam.blpslab1.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.io.Serializable;

public class YookassaConnectionFactoryImpl implements YookassaConnectionFactory {

    private final YookassaManagedConnectionFactory mcf;
    private final ConnectionManager cm;

    public YookassaConnectionFactoryImpl(YookassaManagedConnectionFactory mcf,ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }


    @Override
    public YookassaConnection getConnection() throws ResourceException {
        if (cm != null) {
            return (YookassaConnection) cm.allocateConnection(mcf, null);
        } else {
            var mc = (YookassaManagedConnection) mcf.createManagedConnection(null, null);
            return (YookassaConnection) mc.getConnection(null, null);
        }
    }
}
