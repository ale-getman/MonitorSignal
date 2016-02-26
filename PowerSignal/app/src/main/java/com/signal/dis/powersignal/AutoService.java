package com.signal.dis.powersignal;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 20.11.2015.
 */
public class AutoService extends Service {

    final String LOG_TAG = "LOGI";

    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public String interval;
    public double dol,shi;
    public String dolstr, shistr;
    public GPSTracker gps;
    public Locale local;
    public DateFormat df;
    public Date currentDate;
    public long unixTime;
    public String time_mil;
    public PendingIntent pi;

    public TelephonyManager Tel;
    public MyPhoneStateListener MyListener;
    public static final int UNKNOW_CODE = 99;
    public int MAX_SIGNAL_DBM_VALUE = 31;
    public String weight;
    public String weight_hundred;

    public Timer mTimer;
    public MyTimerTask mMyTimerTask;
    public GsmCellLocation gsmCell;

    public String cid,sector,lac;
    public Notification notification;
    public Bitmap icon;
    public PendingIntent pendingIntent;

    public void onCreate() {
        super.onCreate();
        Log.d("LOGI", "onCreate");
        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 6);
        sdb = mDatabaseHelper.getWritableDatabase();

        gps = new GPSTracker(AutoService.this);

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);

        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //gsmCell = (GsmCellLocation)Tel.getCellLocation();

        Log.d("LOGI", "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOGI", "onStartCommand");
        pi = intent.getParcelableExtra("pend");

        Intent notificationIntent = new Intent(this, TabMenu.class);
        //notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.logo);

        /*notification = new NotificationCompat.Builder(this)
                .setContentTitle("PowerSignal")
                .setTicker("PowerSignal")
                .setContentText("Включен авто-режим.")
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        startForeground(101, notification);*/

        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 500, 10000);
        return super.onStartCommand(intent, flags, startId);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        Log.d("LOGI", "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d("LOGI", "onBind");
        return null;
    }

    public void GPSsetting(){

        if(gps.canGetLocation()) {

            shi = gps.getLongitude();
            shistr = "" + shi;

            dol = gps.getLatitude();
            dolstr = "" + dol;
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            Determine();
            int i = 1;
            try {
                // сообщаем об окончании задачи
                Intent intent = new Intent().putExtra(TabMenu.PARAM_RESULT, i);
                pi.send(AutoService.this, TabMenu.STATUS_FINISH, intent);

            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            //TIMEsetting();
            Log.d("LOGI", "time_1: " + time_mil);
        }
    }

    public void TIMEsetting(){
        currentDate = new Date();
        time_mil = df.format(currentDate);
        unixTime = System.currentTimeMillis() / 1000L;
    }

    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each time there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                float signalStrengthPercent = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
                //viewModel.setSignalStrengthString(IntegerHelper.getString(signalStrengthPercent));
                weight = "" + (int)signalStrengthPercent;
                weight_hundred = "" + (-113 + 2 * signalStrength.getGsmSignalStrength());
                TabMenu.progress = (int)signalStrengthPercent;

                gsmCell = (GsmCellLocation)Tel.getCellLocation();
                lac = "" + gsmCell.getLac();
                cid = "" + gsmCell.getCid();
                sector = cid.substring(cid.length()-1);
                cid = cid.substring(0, cid.length() - 1);

                TIMEsetting();
                notification = new NotificationCompat.Builder(AutoService.this)
                        .setContentTitle(time_mil)
                        .setTicker("PowerSignal")
                        .setContentText("CID:" + cid + "(" + sector + ") lac:" + lac + " signal:" + weight_hundred+"dbm")
                        .setSmallIcon(R.drawable.logo)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true).build();
                startForeground(123123, notification);
            }
        }

        private float calculateSignalStrengthInPercent(int signalStrength) {
            //return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE); //* 100);
            return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE* 1000);
        }
    };/* End of private Class */

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public void Determine(){
        GPSsetting();
        TIMEsetting();
        Log.d("LOGI", "time_2: " + time_mil);
        ContentValues newValues = new ContentValues();
        // Задайте значения для каждого столбца
        newValues.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues.put(DatabaseHelper.TIME_COLUMN, time_mil);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN_HUNDREND, weight_hundred);
        newValues.put(DatabaseHelper.CID, cid);
        newValues.put(DatabaseHelper.SECTOR, sector);
        newValues.put(DatabaseHelper.LAC, lac);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal", null, newValues);

        ContentValues newValues_2 = new ContentValues();
        // Задайте значения для каждого столбца
        newValues_2.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues_2.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues_2.put(DatabaseHelper.TIME_COLUMN, unixTime);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN_HUNDREND, weight_hundred);
        newValues_2.put(DatabaseHelper.CID, cid);
        newValues_2.put(DatabaseHelper.SECTOR, sector);
        newValues_2.put(DatabaseHelper.LAC, lac);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal_2", null, newValues_2);

        //CreateList();
        //TabMenu.progressBar.setProgress(TabMenu.progress);
    }
}
