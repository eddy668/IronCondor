package com.nanobytes.ironcondor.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class IronCondorFactoryTest {
    private OptionsChain chain;

    @Before
    public void Setup() {
        chain = new OptionsChain("XYZ");
        chain.price = 9.5;

        Double[] strike_prices = {6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0};
        Double[] call_prices = {8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0};
        Double[] put_prices = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        chain.strike_prices = Arrays.asList(strike_prices);
        chain.call_values = Arrays.asList(call_prices);
        chain.put_values = Arrays.asList(put_prices);
    }

    @Test
    public void WillGenerateCondorsWithValidChain() {
        IronCondorFactory factory = new IronCondorFactory(this.chain);
        List<IronCondor> condors = factory.generate_condors_from_chain();

        assertEquals(3, condors.size());
    }
}
