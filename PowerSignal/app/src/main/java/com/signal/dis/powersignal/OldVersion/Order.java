package com.signal.dis.powersignal.OldVersion;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.signal.dis.powersignal.DatabaseHelper;
import com.signal.dis.powersignal.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 18.11.2015.
 */
public class Order extends Activity {

    public ProgressBar progressBar;
    public int progress;
    public Button accept;
    public EditText editAddress;
    public TextView orderData, orderWeight;

    public Locale local;
    public DateFormat df;
    public Date currentDate;
    public long unixTime;
    public String time_mil;

    public TelephonyManager Tel;
    public MyPhoneStateListener MyListener;

    public static final int UNKNOW_CODE = 99;
    public int MAX_SIGNAL_DBM_VALUE = 31;
    public String weight;
    public String address;

    public String operatorName;
    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);


        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 3);
        sdb = mDatabaseHelper.getWritableDatabase();

        progressBar = (ProgressBar) findViewById(R.id.progress);
        accept = (Button) findViewById(R.id.accept);
        editAddress = (EditText) findViewById(R.id.editAddress);
        orderData = (TextView) findViewById(R.id.orderData);
        orderWeight = (TextView) findViewById(R.id.orderWeight);

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);

        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        operatorName = Tel.getSimOperatorName();

        TIMEsetting();

        /*progressBar.setProgress(progress);
        orderData.setText(time_mil);
        orderWeight.setText(weight);*/

        accept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(operatorName.equals("PHoenix"))
                {
                    address = editAddress.getText().toString();
                    Determine();
                }
                else
                    Toast.makeText(getApplicationContext(), "Вы не абонент PHoenix.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Order.this, WorkspaceActivity.class);
                startActivity(intent);
            }
        });
    }

    public void TIMEsetting(){
        currentDate = new Date();
        time_mil = df.format(currentDate);
        unixTime = System.currentTimeMillis() / 1000L;
    }

    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);

            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                float signalStrengthPercent = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
                weight = "" + (int)signalStrengthPercent;
                progress = (int)signalStrengthPercent;
                progressBar.setProgress(progress);
                orderWeight.setText(weight);
                TIMEsetting();
                orderData.setText(time_mil);
            }
        }

        private float calculateSignalStrengthInPercent(int signalStrength) {
            return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE* 1000);
        }
    };/* End of private Class */


    public void Determine(){
        ContentValues newValues = new ContentValues();
        // Задайте значения для каждого столбца
        newValues.put(DatabaseHelper.ADDRESS_COLUMN, address);
        newValues.put(DatabaseHelper.TIME_COLUMN, time_mil);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal", null, newValues);

        ContentValues newValues_2 = new ContentValues();
        // Задайте значения для каждого столбца
        newValues_2.put(DatabaseHelper.ADDRESS_COLUMN, address);
        newValues_2.put(DatabaseHelper.TIME_COLUMN, unixTime);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal_2", null, newValues_2);
    }
}
