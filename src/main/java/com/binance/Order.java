package com.binance;

import lombok.AllArgsConstructor;
import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@AllArgsConstructor
@ToString
public class Order {
    private double price;
    private double quantity;
}
