package com.blpsteam.blpslab1.jca;

import jakarta.resource.cci.Connection;



public interface YookassaConnection extends Connection {
    String createPayment(Long amount, Long orderId);

}
