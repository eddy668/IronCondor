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

    public List<IronCondor> generate_condors_from_chain(int skew) {
        List<IronCondor> condors = new ArrayList<>();
        if(chain.strike_prices.size() % 2 != 0) {
            Log.e("IronCondorFactory", "OptionsChain did not contain the same number of elements on both sides");
            return condors;
        }

        for(int i = 0; i < (chain.strike_prices.size() / 2) - 1; i++) {
            int put_index = chain.strike_prices.size() - i - 2;
            int call_index = i;

            put_index += skew;
            call_index += skew;


            try {
                IronCondor condor = new IronCondor();
                condor.underlying_price = chain.price;
                condor.buy_put_option = new Pair<>(chain.strike_prices.get(call_index), chain.put_values.get(call_index));
                condor.sell_put_option = new Pair<>(chain.strike_prices.get(call_index + 1), chain.put_values.get(call_index + 1));
                condor.sell_call_option = new Pair<>(chain.strike_prices.get(put_index), chain.call_values.get(put_index));
                condor.buy_call_option = new Pair<>(chain.strike_prices.get(put_index + 1), chain.call_values.get(put_index + 1));
                condors.add(condor);
                Log.e("IronCondorFactory", condor.toString());
            } catch (Exception e) {}
        }

        return condors;
    }
}
