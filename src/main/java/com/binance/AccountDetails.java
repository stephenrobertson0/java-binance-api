package com.binance;

import java.util.List;

import lombok.Getter;
import lombok.ToString;


/**
 * Created by stephen on 10/17/17.
 */
@Getter
@ToString
public class AccountDetails {
    private int makerCommission;
    private int takerCommission;
    private int buyerCommission;
    private int sellerCommission;
    private boolean canTrade;
    private boolean canWithdraw;
    private boolean canDeposit;
    private List<Balance> balances;
}
