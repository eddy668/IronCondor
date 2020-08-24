package com.nanobytes.ironcondor.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class OptionsChain  implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public OptionsChain createFromParcel(Parcel in) {
            return new OptionsChain(in);
        }
        @Override public OptionsChain[] newArray(int size) {
            return new OptionsChain[size];
        }
    };

    public String ticker;
    public Double price;
    public List<Double> strike_prices;
    public List<Double> call_values;
    public List<Double> put_values;

    public OptionsChain(String ticker) {
        this.ticker = ticker;
        this.price = 0.0;
        this.strike_prices = new ArrayList<>();
        this.call_values = new ArrayList<>();
        this.put_values = new ArrayList<>();
    }

    public OptionsChain(Parcel in) {
        this.ticker = in.readString();
        this.price = in.readDouble();
        in.readList(this.strike_prices, Double.class.getClassLoader());
        in.readList(this.call_values, Double.class.getClassLoader());
        in.readList(this.put_values, Double.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ticker);
        dest.writeDouble(this.price);
        dest.writeList(this.strike_prices);
        dest.writeList(this.call_values);
        dest.writeList(this.put_values);
    }
}
