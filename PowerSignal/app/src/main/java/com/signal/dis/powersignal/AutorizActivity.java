package com.signal.dis.powersignal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

/**
 * Created by User on 13.11.2015.
 */
public class AutorizActivity extends Activity {

    public EditText login,password;
    public Button input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autoriz_layout);

        login = (EditText) findViewById(R.id.log);
        password = (EditText) findViewById(R.id.pas);
        input = (Button) findViewById(R.id.input);

        input.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }
}
