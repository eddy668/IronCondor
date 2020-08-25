package com.nanobytes.ironcondor.model;

import android.util.Pair;

public class IronCondor {
    public Double underlying_price;
    public Pair<Double, Double> buy_call_option;
    public Pair<Double, Double> sell_call_option;
    public Pair<Double, Double> sell_put_option;
    public Pair<Double, Double> buy_put_option;

    public double available_premium() {
        return -Math.round((buy_call_option.second-
               sell_call_option.second-
               sell_put_option.second+
               buy_put_option.second)*100);
    }

    public double required_collateral() {
        return Math.max(buy_call_option.first-sell_call_option.first, sell_put_option.first-buy_put_option.first)*100;
    }

    public double regular_roi() {
        return Math.round(available_premium() / required_collateral() * 100);
    }

    public long largest_order_size(double buying_power) {
        long original_contracts_available = Math.round(buying_power / required_collateral());
        double original_premium_obtained = original_contracts_available * available_premium();
        double left_over_cash = buying_power - original_contracts_available*required_collateral();

        if(original_contracts_available > 0) {
            return original_contracts_available + largest_order_size(original_premium_obtained + left_over_cash);
        } else {
            if(left_over_cash + available_premium() >= required_collateral()) {
                return 1 + largest_order_size(buying_power+available_premium()-required_collateral());
            } else {
                return  0;
            }
        }
    }

    public double max_gains(double buying_power) {
        return largest_order_size(buying_power) * available_premium();
    }

    public double max_roi(double buying_power) {
        return Math.round(max_gains(buying_power)/buying_power*100);
    }

    @Override
    public String toString() {
        return "\n\nBuy the $"+buy_call_option.first+" CALL at $"+buy_call_option.second+"\n"+
                "Sell the $"+sell_call_option.first+" CALL at $"+sell_call_option.second+"\n"+
                "Sell the $"+sell_put_option.first+" PUT at $"+sell_put_option.second+"\n"+
                "Buy the $"+buy_put_option.first+" PUT at $"+buy_put_option.second+"\n\n";
    }
}