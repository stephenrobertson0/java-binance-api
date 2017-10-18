package com.binance;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@AllArgsConstructor
@ToString
public class OrderBook {
    private List<Order> bids;
    private List<Order> asks;
}
