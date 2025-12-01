package com.blpsteam.blpslab1.jca;

import jakarta.resource.ResourceException;

public interface YookassaConnectionFactory {
    YookassaConnection getConnection() throws ResourceException;
}