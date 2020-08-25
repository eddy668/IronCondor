package com.nanobytes.ironcondor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nanobytes.ironcondor.model.IronCondor;
import com.nanobytes.ironcondor.model.IronCondorFactory;
import com.nanobytes.ironcondor.model.OptionsChain;
import com.nanobytes.ironcondor.model.api.OptionsChainApi;
import com.nanobytes.ironcondor.model.api.PriceHistoryApi;

import java.util.List;

public class DisplayCondors extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private String symbol;
    private Double buying_power;

    private OptionsChain chain;
    private List<IronCondor> condors;
    LineGraphSeries<DataPoint> points;
    private PriceHistoryApi price_history_api;
    private int skew = 0;
    private int slider = 0;

    private GraphView graph;
    private SeekBar selector;
    private TextView max_gains;
    private TextView percent;
    private TextView buy_put;
    private TextView sell_put;
    private TextView buy_call;
    private TextView sell_call;
    private TextView order_size;
    private Button add_skew;
    private Button reduce_skew;
    private boolean good = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_display_condors);

        this.symbol = getIntent().getStringExtra("SYMBOL");
        this.buying_power = Double.parseDouble(getIntent().getStringExtra("BUYING_POWER"));

        buy_put = findViewById(R.id.buy_put);
        sell_put = findViewById(R.id.sell_put);
        sell_call = findViewById(R.id.sell_call);
        buy_call = findViewById(R.id.buy_call);
        order_size = findViewById(R.id.order_size);
        add_skew = findViewById(R.id.add_skew);
        add_skew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                freeze_ui(false);
                skew++;
                graph.removeAllSeries();
                graph.addSeries(points);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMinX(points.getLowestValueX());
                graph.getViewport().setMaxX(points.getHighestValueX());
                graph.getViewport().scrollToEnd();
                draw_condors();
                freeze_ui(true);
            }
        });
        reduce_skew = findViewById(R.id.reduce_skew);
        reduce_skew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                freeze_ui(false);
                skew--;
                graph.removeAllSeries();
                graph.addSeries(points);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMinX(points.getLowestValueX());
                graph.getViewport().setMaxX(points.getHighestValueX());
                graph.getViewport().scrollToEnd();
                draw_condors();
                freeze_ui(true);
            }
        });

        max_gains = findViewById(R.id.max_gains);
        percent = findViewById(R.id.percent);
        selector = findViewById(R.id.selector);
        selector.setOnSeekBarChangeListener(this);

        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().computeScroll();
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);


        freeze_ui(false);
        draw_price_graph();
        draw_condors();
        freeze_ui(true);
    }

    private void draw_price_graph() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                points = new LineGraphSeries<>();
                price_history_api = new PriceHistoryApi();
                price_history_api.get_price_history(symbol, points);
                price_history_api.wait_until_done();
                graph.addSeries(points);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setMinX(points.getLowestValueX());
                graph.getViewport().setMaxX(points.getHighestValueX());
                graph.getViewport().scrollToEnd();
            }
        }).start();
    }

    private void draw_condors() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                freeze_ui(false);
                chain = new OptionsChain(symbol);
                OptionsChainApi chain_api = new OptionsChainApi();
                chain_api.get_options_chain(chain);
                chain_api.wait_until_done();
                price_history_api.wait_until_done();

                IronCondorFactory factory = new IronCondorFactory(chain);
                condors = factory.generate_condors_from_chain(skew);
                selector.setMax(condors.size()-1);
                selector.setProgress(slider);
                display_condor(slider);
                freeze_ui(true);
            }
        }).start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, final int progress,
                                  boolean fromUser) {
        freeze_ui(false);
        graph.removeAllSeries();
        graph.addSeries(points);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(points.getLowestValueX());
        graph.getViewport().setMaxX(points.getHighestValueX());
        graph.getViewport().scrollToEnd();

        slider = progress;
        display_condor(progress);
        freeze_ui(true);
    }

    public void display_condor(int index) {
        double left_size = points.getLowestValueX();
        double right_size = points.getHighestValueX();

        if(condors.size() == 0) return;
        LineGraphSeries<DataPoint> top_line = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(left_size, condors.get(index).sell_call_option.first),
                new DataPoint(right_size, condors.get(index).sell_call_option.first),
        });
        top_line.setTitle(Double.toString(condors.get(index).get_wiggle_room().first)+"%");
        top_line.setColor(Color.parseColor("#FF8BC34A"));
        graph.addSeries(top_line);

        LineGraphSeries<DataPoint> bottom_line = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(left_size, condors.get(index).sell_put_option.first),
                new DataPoint(right_size, condors.get(index).sell_put_option.first),
        });
        bottom_line.setTitle(Double.toString(condors.get(index).get_wiggle_room().second)+"%");
        bottom_line.setColor(Color.parseColor("#FFF44336"));

        graph.addSeries(bottom_line);
        points.setTitle("$"+Double.toString(Math.round(chain.price*100)/100));
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        update_text(index);
    }

    public void update_text(final int condor_index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double gains = condors.get(condor_index).max_gains(buying_power);
                Log.i("condor", Double.toString(buying_power));
                max_gains.setText("+$"+Double.toString(gains));
                percent.setText(Double.toString(condors.get(condor_index).max_roi(buying_power))+"%");

                buy_put.setText("$"+Double.toString(condors.get(condor_index).buy_put_option.first));
                sell_put.setText("$"+Double.toString(condors.get(condor_index).sell_put_option.first));
                sell_call.setText("$"+Double.toString(condors.get(condor_index).sell_call_option.first));
                buy_call.setText("$"+Double.toString(condors.get(condor_index).buy_call_option.first));
                order_size.setText("x"+Long.toString(condors.get(condor_index).largest_order_size(buying_power)));
            }
        });
    }

    public void freeze_ui(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                add_skew.setEnabled(state);
                reduce_skew.setEnabled(state);
                selector.setEnabled(state);
            }
        });
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}