package com.equinox.qikdriver.Enums;

/**
 * Created by mukht on 11/15/2016.
 */

public enum OrderStatus {

    INCOMING("order_incoming"),
    PROCESSING("order_processing"),
    COMPLETED("order_completed"),
    CANCELLED("order_cancelled"),
    PICKED_UP("picked_up"),
    ENROUTE("enroute"),
    DELIVERED("delivered");

    private String nodeName;

    OrderStatus (String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }
}
