package com.binance;

import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@ToString
public class OrderDetails {
    private String symbol;
    private String orderId;
    private String clientOrderId;
    private double price;
    private double origQty;
    private double executedQty;
    private Status status;
    private TimeInForce timeInForce;
    private Type type;
    private Side side;
    private double stopPrice;
    private double icebergQty;
    private long time;
}
