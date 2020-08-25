package com.nanobytes.ironcondor.model;

import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class IronCondorTest {
    private IronCondor condor;

    @Before
    public void setup() {
        condor = new IronCondor();
        condor.underlying_price = 83.29;
        condor.buy_call_option = new Pair<>(88.0, 0.31);
        condor.sell_call_option = new Pair<>(87.5, 0.37);
        condor.sell_put_option = new Pair<>(87.0, 4.28);
        condor.buy_put_option = new Pair<>(86.5, 3.93);
    }

    @Test
    public void can_calculate_premium() {
        assertEquals(41.0, condor.available_premium(), 0.001);
    }

    @Test
    public void can_calculate_collateral() {
        assertEquals(50, condor.required_collateral(), 0.001);
    }

    @Test
    public void can_calculate_regular_roi() {
        assertEquals(82, condor.regular_roi(), 0.001);
    }

    @Test
    public void can_calculate_largest_order_size() {
        assertEquals(0, condor.largest_order_size(8));
        assertEquals(1, condor.largest_order_size(9));
        assertEquals(5, condor.largest_order_size(50));
        assertEquals(11, condor.largest_order_size(100));
    }

    @Test
    public void can_calculate_max_gains() {
        assertEquals(451.0, condor.max_gains(100), 0.01);
    }

    @Test
    public void can_calculate_max_roi() {
        assertEquals(451.0, condor.max_roi(100), 0.01);
        assertEquals(410.0, condor.max_roi(50), 0.01);
    }
}
