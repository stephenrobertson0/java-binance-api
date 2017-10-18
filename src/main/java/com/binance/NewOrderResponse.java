package com.binance;

import lombok.Getter;
import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@Getter
@ToString
public class NewOrderResponse {
    private String symbol;
    private String orderId;
    private String clientOrderId;
    private long transactTime;
}
