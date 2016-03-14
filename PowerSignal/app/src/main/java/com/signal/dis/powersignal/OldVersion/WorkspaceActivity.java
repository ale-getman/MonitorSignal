package com.signal.dis.powersignal.OldVersion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.signal.dis.powersignal.DatabaseHelper;
import com.signal.dis.powersignal.GPSTracker;
import com.signal.dis.powersignal.R;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by User on 17.11.2015.
 */
public class WorkspaceActivity extends Activity {

    private DatabaseHelper mDatabaseHelper;
    public SQLiteDatabase sdb;

    public ListView listView;
    public Button determine,send,clear;
    public ProgressBar progressBar;
    public int progress;

    public String log;
    public double dol,shi;
    public String dolstr, shistr;
    public GPSTracker gps;
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
    public ListAdapter adapter;

    public String WEIGHT_JSON = "";
    public String response;
    public ProgressDialog dialog;
    public String FLAG = "FALSE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace);


        mDatabaseHelper = new DatabaseHelper(this, "mydatabase.db", null, 4);
        sdb = mDatabaseHelper.getWritableDatabase();

        listView = (ListView) findViewById(R.id.listView);
        determine = (Button) findViewById(R.id.determine);
        send = (Button) findViewById(R.id.send);
        clear = (Button) findViewById(R.id.clear);
        progressBar = (ProgressBar) findViewById(R.id.pb_horizontal);
        log = "test_heatmap";
        gps = new GPSTracker(WorkspaceActivity.this);

        local = new Locale("ru","RU");
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, local);

        MyListener   = new MyPhoneStateListener();
        Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        GPSsetting();
        TIMEsetting();

        CreateList();


        Log.d("LOGI", "loc_X: " + dolstr);
        Log.d("LOGI", "loc_Y: " + shistr);
        Log.d("LOGI", "time: " + time_mil);
        Log.d("LOGI", "unixTime: " + unixTime);
        Log.d("LOGI", "weight: " + weight);

        determine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*if(Tel.getSimOperatorName().equals("PHoenix"))
                    Determine();
                else
                    Toast.makeText(getApplicationContext(), "Вы не абонент PHoenix.", Toast.LENGTH_SHORT).show();*/
                Determine();
                Log.d("LOGI", "loc_X: " + dolstr);
                Log.d("LOGI", "loc_Y: " + shistr);
                Log.d("LOGI", "time: " + time_mil);
                Log.d("LOGI", "unixTime: " + unixTime);
                Log.d("LOGI", "weight: " + weight);
                /*Intent intent = new Intent(WorkspaceActivity.this, Order.class);
                startActivity(intent);*/
            }
        });

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*if(Tel.getSimOperatorName().equals("PHoenix"))
                    Send();
                else
                    Toast.makeText(getApplicationContext(), "Вы не абонент PHoenix.", Toast.LENGTH_SHORT).show();*/
                Send();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sdb.delete("WeightSignal", null, null);
                sdb.delete("WeightSignal_2", null, null);
                progressBar.setProgress(0);
                CreateList();
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
    }

    /* Called when the application resumes */
    @Override
    protected void onResume()
    {
        super.onResume();
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //CreateList();

        /*Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.ADDRESS_COLUMN,
                        DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN},
                null, null, null, null, null) ;
        cursor.moveToLast();
        progressBar.setProgress(Integer.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WEIGHT_COLUMN))));
        cursor.close();*/
    }


    public void Determine(){
        ContentValues newValues = new ContentValues();
        // Задайте значения для каждого столбца
        newValues.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues.put(DatabaseHelper.TIME_COLUMN, time_mil);
        newValues.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal", null, newValues);

        ContentValues newValues_2 = new ContentValues();
        // Задайте значения для каждого столбца
        newValues_2.put(DatabaseHelper.LOC_X_COLUMN, dolstr);
        newValues_2.put(DatabaseHelper.LOC_Y_COLUMN, shistr);
        newValues_2.put(DatabaseHelper.TIME_COLUMN, unixTime);
        newValues_2.put(DatabaseHelper.WEIGHT_COLUMN, weight);
        // Вставляем данные в таблицу
        sdb.insert("WeightSignal_2", null, newValues_2);

        GPSsetting();
        TIMEsetting();
        CreateList();
        progressBar.setProgress(progress);
    }

    public void Send(){
        Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE_2, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                        DatabaseHelper.LOC_Y_COLUMN, DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN},
                null, null, null, null, null) ;

        //cursor.moveToFirst();

        while (cursor.moveToNext()) {
            String loc_x = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_X_COLUMN));
            String loc_y = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOC_Y_COLUMN));
            //String adr = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ADDRESS_COLUMN));
            String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME_COLUMN));
            String weight = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WEIGHT_COLUMN));

            if(cursor.isFirst() && cursor.isLast())
                WEIGHT_JSON += ("[{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"}]");
            else
                if(cursor.isFirst())
                    WEIGHT_JSON += ("[{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"},");
                else
                if(cursor.isLast())
                    WEIGHT_JSON += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time+"}]");
                else
                    WEIGHT_JSON += ("{\"x\":"+loc_x+",\"y\":"+loc_y+",\"weight\":"+weight+",\"time\":"+time + "},");
        }
        // не забываем закрывать курсор
        cursor.close();
        Log.d("LOGI", "WEIGHT_JSON" + WEIGHT_JSON);

        new RequestTask().execute(getString(R.string.adress));
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

    public void TIMEsetting(){
        currentDate = new Date();
        time_mil = df.format(currentDate);
        unixTime = System.currentTimeMillis() / 1000L;
    }

    public void CreateList(){
        Cursor cursor = sdb.query(mDatabaseHelper.DATABASE_TABLE, new String[]{DatabaseHelper._ID, DatabaseHelper.LOC_X_COLUMN,
                        DatabaseHelper.LOC_Y_COLUMN,DatabaseHelper.TIME_COLUMN, DatabaseHelper.WEIGHT_COLUMN},
                null, null, null, null, DatabaseHelper._ID + " DESC") ;


        adapter = new SimpleCursorAdapter(this, // Связь.
                R.layout.list, // Определения шаблона элемента
                cursor, // Переход к курсору, который надо запомнить.
                // Массив курсоров, которые надо запомнить.
                new String[] { "Loc_X", "Loc_Y", "Time", "Weight"},
                // Массив, связывающий запомненные курсоры и шаблоны с ними связанные
                new int[] { R.id.loc_x, R.id.loc_y, R.id.time, R.id.weight });
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //(view).setBackgroundColor(Color.parseColor("#FF7d31df"));
                float buf = Float.valueOf(((TextView) view.findViewById(R.id.weight)).getText().toString());
                int buff = (int) buf;
                progressBar.setProgress(buff);
            }
        });
    }


    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            //Toast.makeText(getApplicationContext(), "Go to Firstdroid!!! GSM Cinr = "
            //        + String.valueOf(signalStrength.getGsmSignalStrength()), Toast.LENGTH_SHORT).show();
            //powerSignal = String.valueOf((int)((113+(-113 + 2 * signalStrength.getGsmSignalStrength()))*15.873));
            //powerSignal_2 = String.valueOf((-113 + 2 * signalStrength.getGsmSignalStrength()));
            //powerSignal_2 = String.valueOf(signalStrength.getCdmaDbm());

            /*powerSignal_2 = String.valueOf(signalStrength.getGsmBitErrorRate());
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmBitErrorRate() != 99)
                    powerSignal = String.valueOf((int)((113+(-113 + 2 * signalStrength.getGsmSignalStrength()))*15.873));
                else
                    powerSignal = String.valueOf((int) ((113 - (signalStrength.getGsmSignalStrength())) * 15.873));
            } else {
                powerSignal_2 = String.valueOf(signalStrength.getCdmaDbm());
            }*/

            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                float signalStrengthPercent = calculateSignalStrengthInPercent(signalStrength.getGsmSignalStrength());
                //viewModel.setSignalStrengthString(IntegerHelper.getString(signalStrengthPercent));
                weight = "" + (int)signalStrengthPercent;
                progress = (int)signalStrengthPercent;
            }
        }

        private float calculateSignalStrengthInPercent(int signalStrength) {
            //return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE); //* 100);
            return (int) ((float) signalStrength / MAX_SIGNAL_DBM_VALUE* 1000);
        }
    };/* End of private Class */

    public static String md5Custom(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                HttpPost postMethod = new HttpPost(params[0]);
                //будем передавать два параметра
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //передаем параметры из наших текстбоксов
                //логин
                nameValuePairs.add(new BasicNameValuePair("unique", md5Custom(log)));
                //пароль
                nameValuePairs.add(new BasicNameValuePair("data", WEIGHT_JSON));
                Log.d("LOGI", "WEIGHT_JSON_REQEUST" + WEIGHT_JSON);
                //собераем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                response = hc.execute(postMethod, res);

                JSONURL(response.toString());

            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.dismiss();
            if(FLAG.equals("FALSE")) {
                Toast.makeText(WorkspaceActivity.this, "Не отправлено", Toast.LENGTH_SHORT).show();
                WEIGHT_JSON = "";
            }
            else {
                WEIGHT_JSON = "";
                sdb.delete("WeightSignal_2", null, null);
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(WorkspaceActivity.this);
            dialog.setMessage("Загружаюсь...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
        }
    }

    public void JSONURL(String result) {

        try {
            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово data
            final JSONArray urls = json.getJSONArray("data");
            //проходим циклом по всем нашим параметрам
            FLAG = urls.getJSONObject(0).getString("flag").toString();
            Log.d("LOGI", "TIME: " + FLAG);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

    }
}
