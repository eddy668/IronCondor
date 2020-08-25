package com.nanobytes.ironcondor.model;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class IronCondorFactory {
    private OptionsChain chain;

    public IronCondorFactory(OptionsChain chain) {
        this.chain = chain;
    }

    public List<IronCondor> generate_condors_from_chain() {
        List<IronCondor> condors = new ArrayList<>();
        if(chain.strike_prices.size() % 2 != 0) {
            Log.e("IronCondorFactory", "OptionsChain did not contain the same number of elements on both sides");
            return condors;
        }

        for(int i = 0; i < (chain.strike_prices.size() / 2) - 1; i++) {
            int put_index = chain.strike_prices.size() - i - 2;

            IronCondor condor = new IronCondor();
            condor.buy_call_option = new Pair<>(chain.strike_prices.get(i), chain.call_values.get(i));
            condor.sell_call_option = new Pair<>(chain.strike_prices.get(i+1), chain.call_values.get(i+1));
            condor.sell_put_option = new Pair<>(chain.strike_prices.get(put_index), chain.call_values.get(put_index));
            condor.buy_put_option = new Pair<>(chain.strike_prices.get(put_index+1), chain.call_values.get(put_index+1));
            Log.e("IronCondorFactory", condor.toString());
        }

        return condors;
    }
}
