package com.binance;

import lombok.Getter;
import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@Getter
@ToString
public class Balance {
    private String asset;
    private double free;
    private double locked;
}
