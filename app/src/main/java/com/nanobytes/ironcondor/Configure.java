package com.nanobytes.ironcondor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Configure extends AppCompatActivity {

    private TextView symbol;
    private TextView buying_power;
    private FloatingActionButton display_condors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_configure);

        symbol = findViewById(R.id.symbol);
        buying_power = findViewById(R.id.buying_power);
        display_condors = findViewById(R.id.display_condors);
        display_condors.setOnClickListener(new FloatingActionButton.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DisplayCondors.class);
                intent.putExtra("SYMBOL", symbol.getText().toString());
                intent.putExtra("BUYING_POWER", buying_power.getText().toString());
                startActivity(intent);
            }
        });
    }
}