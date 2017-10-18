package com.binance;

import java.util.List;

import lombok.Getter;


/**
 * Created by stephen on 10/17/17.
 */
@Getter
public class OrderBookResponse {
    private List<List<Object>> bids;
    private List<List<Object>> asks;
}
